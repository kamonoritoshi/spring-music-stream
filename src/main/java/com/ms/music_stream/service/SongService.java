package com.ms.music_stream.service;

import com.ms.music_stream.dto.SongResponse;
import com.ms.music_stream.dto.SongUploadRequest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface SongService {
    List<SongResponse> getAllSongs(String query);
    SongResponse uploadSong(SongUploadRequest request, MultipartFile audioFile, MultipartFile coverFile);
    AudioStreamData getAudioStream(Long songId, HttpHeaders headers);

    record AudioStreamData(ResourceRegion region, MediaType mediaType, Resource resource) {}
}
