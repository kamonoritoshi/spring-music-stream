package com.ms.music_stream.controller;

import com.ms.music_stream.model.Song;
import com.ms.music_stream.repository.SongRepository;
import com.ms.music_stream.service.FileStorageService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/api/songs")
public class SongApiController {

    private final SongRepository songRepository;
    private final FileStorageService fileStorageService;

    private static final long CHUNK_SIZE = 1024 * 1024; // 1MB

    public SongApiController(SongRepository songRepository, FileStorageService fileStorageService) {
        this.songRepository = songRepository;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping
    public ResponseEntity<List<Song>> getAllSongs(@RequestParam(required = false) String q) {
        if (q != null && !q.trim().isEmpty()) {
            return ResponseEntity.ok(songRepository.findByTitleContainingIgnoreCaseOrArtistContainingIgnoreCase(q.trim(), q.trim()));
        }
        return ResponseEntity.ok(songRepository.findAllByOrderByUploadedAtDesc());
    }

    @PostMapping("/upload")
    public ResponseEntity<Song> uploadSong(
            @RequestParam("title") String title,
            @RequestParam("artist") String artist,
            @RequestParam(value = "album", required = false) String album,
            @RequestParam(value = "genre", required = false) String genre,
            @RequestParam("audioFile") MultipartFile audioFile,
            @RequestParam(value = "coverFile", required = false) MultipartFile coverFile) {

        if (audioFile == null || audioFile.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Audio file is required");
        }

        try {
            String audioPath = fileStorageService.saveFile(audioFile, "audio");
            String coverPath = null;
            if (coverFile != null && !coverFile.isEmpty()) {
                coverPath = fileStorageService.saveFile(coverFile, "covers");
            }

            Song song = new Song();
            song.setTitle(title);
            song.setArtist(artist);
            song.setAlbum(album);
            song.setGenre(genre);
            song.setAudioPath(audioPath);
            song.setCoverPath(coverPath);
            song.setPlayCount(0L);

            Song savedSong = songRepository.save(song);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedSong);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to store files: " + e.getMessage(), e);
        }
    }

    @GetMapping("/stream/{id}")
    public ResponseEntity<ResourceRegion> streamAudio(
            @PathVariable Long id,
            @RequestHeader HttpHeaders headers) {

        Song song = songRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Song not found with id: " + id));

        Path audioFilePath = fileStorageService.getFilePathFromRelativeWebPath(song.getAudioPath());

        try {
            Resource audioResource = new UrlResource(audioFilePath.toUri());
            if (!audioResource.exists() || !audioResource.isReadable()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Audio file not found on disk");
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

            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .contentType(mediaType)
                    .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                    .body(region);

        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error reading audio file", e);
        }
    }
}
