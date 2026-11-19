package com.ms.music_stream.controller;

import com.ms.music_stream.dto.SongResponse;
import com.ms.music_stream.dto.SongUploadRequest;
import com.ms.music_stream.service.SongService;
import jakarta.validation.Valid;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/songs")
public class SongApiController {

    private final SongService songService;

    public SongApiController(SongService songService) {
        this.songService = songService;
    }

    @GetMapping
    public ResponseEntity<List<SongResponse>> getAllSongs(@RequestParam(required = false) String q) {
        List<SongResponse> songs = songService.getAllSongs(q);
        return ResponseEntity.ok(songs);
    }

    @PostMapping("/upload")
    public ResponseEntity<SongResponse> uploadSong(
            @Valid @ModelAttribute SongUploadRequest request,
            @RequestParam(value = "audioFile", required = false) MultipartFile audioFile,
            @RequestParam(value = "coverFile", required = false) MultipartFile coverFile) {

        SongResponse createdSong = songService.uploadSong(request, audioFile, coverFile);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdSong);
    }

    @GetMapping("/stream/{id}")
    public ResponseEntity<ResourceRegion> streamAudio(
            @PathVariable Long id,
            @RequestHeader HttpHeaders headers) {

        SongService.AudioStreamData streamData = songService.getAudioStream(id, headers);

        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                .contentType(streamData.mediaType())
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .body(streamData.region());
    }
}
