package com.ms.music_stream.service.impl;

import com.ms.music_stream.dto.SongResponse;
import com.ms.music_stream.dto.SongUploadRequest;
import com.ms.music_stream.exception.InvalidFileException;
import com.ms.music_stream.exception.ResourceNotFoundException;
import com.ms.music_stream.mapper.EntityMapper;
import com.ms.music_stream.model.Song;
import com.ms.music_stream.repository.SongRepository;
import com.ms.music_stream.service.FileStorageService;
import com.ms.music_stream.service.SongService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@Service
public class SongServiceImpl implements SongService {

    private static final long CHUNK_SIZE = 1024 * 1024; // 1MB

    private final SongRepository songRepository;
    private final FileStorageService fileStorageService;
    private final EntityMapper entityMapper;

    public SongServiceImpl(SongRepository songRepository,
                           FileStorageService fileStorageService,
                           EntityMapper entityMapper) {
        this.songRepository = songRepository;
        this.fileStorageService = fileStorageService;
        this.entityMapper = entityMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SongResponse> getAllSongs(String query) {
        List<Song> songs;
        if (StringUtils.hasText(query)) {
            String trimmed = query.trim();
            songs = songRepository.findByTitleContainingIgnoreCaseOrArtistContainingIgnoreCase(trimmed, trimmed);
        } else {
            songs = songRepository.findAllByOrderByUploadedAtDesc();
        }
        return entityMapper.toSongResponseList(songs);
    }

    @Override
    @Transactional
    public SongResponse uploadSong(SongUploadRequest request, MultipartFile audioFile, MultipartFile coverFile) {
        // Fallback check if audioFile came via request DTO or parameter
        MultipartFile effectiveAudioFile = audioFile != null ? audioFile : request.getAudioFile();
        MultipartFile effectiveCoverFile = coverFile != null ? coverFile : request.getCoverFile();

        if (effectiveAudioFile == null || effectiveAudioFile.isEmpty()) {
            throw new InvalidFileException("Tệp âm thanh không được để trống");
        }

        String originalFilename = effectiveAudioFile.getOriginalFilename();
        if (originalFilename != null) {
            String lower = originalFilename.toLowerCase();
            if (!lower.endsWith(".mp3") && !lower.endsWith(".wav") && !lower.endsWith(".ogg") && !lower.endsWith(".m4a")) {
                throw new InvalidFileException("Định dạng tệp âm thanh không hợp lệ (hỗ trợ .mp3, .wav, .ogg, .m4a)");
            }
        }

        try {
            String audioPath = fileStorageService.saveFile(effectiveAudioFile, "audio");
            String coverPath = null;
            if (effectiveCoverFile != null && !effectiveCoverFile.isEmpty()) {
                coverPath = fileStorageService.saveFile(effectiveCoverFile, "covers");
            }

            Song song = new Song();
            song.setTitle(request.getTitle());
            song.setArtist(request.getArtist());
            song.setAlbum(request.getAlbum());
            song.setGenre(request.getGenre());
            song.setAudioPath(audioPath);
            song.setCoverPath(coverPath);
            song.setPlayCount(0L);

            Song savedSong = songRepository.save(song);
            return entityMapper.toSongResponse(savedSong);
        } catch (IOException e) {
            throw new InvalidFileException("Lỗi lưu trữ tệp tin: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public AudioStreamData getAudioStream(Long songId, HttpHeaders headers) {
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài hát với id: " + songId));

        Path audioFilePath = fileStorageService.getFilePathFromRelativeWebPath(song.getAudioPath());

        try {
            Resource audioResource = new UrlResource(audioFilePath.toUri());
            if (!audioResource.exists() || !audioResource.isReadable()) {
                throw new ResourceNotFoundException("Tệp âm thanh không tồn tại trên hệ thống lưu trữ");
            }

            long contentLength = audioResource.contentLength();
            List<HttpRange> ranges = headers.getRange();

            ResourceRegion region;
            if (!ranges.isEmpty()) {
                HttpRange range = ranges.get(0);
                long start = range.getRangeStart(contentLength);
                long end = range.getRangeEnd(contentLength);
                long rangeLength = Math.min(CHUNK_SIZE, end - start + 1);

                if (start == 0) {
                    song.setPlayCount(song.getPlayCount() == null ? 1L : song.getPlayCount() + 1L);
                    songRepository.save(song);
                }

                region = new ResourceRegion(audioResource, start, rangeLength);
            } else {
                long rangeLength = Math.min(CHUNK_SIZE, contentLength);
                song.setPlayCount(song.getPlayCount() == null ? 1L : song.getPlayCount() + 1L);
                songRepository.save(song);
                region = new ResourceRegion(audioResource, 0, rangeLength);
            }

            MediaType mediaType = MediaTypeFactory.getMediaType(audioResource)
                    .orElse(MediaType.parseMediaType("audio/mpeg"));

            return new AudioStreamData(region, mediaType, audioResource);
        } catch (IOException e) {
            throw new InvalidFileException("Lỗi đọc tệp tin âm thanh", e);
        }
    }
}
