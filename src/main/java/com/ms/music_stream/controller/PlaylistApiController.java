package com.ms.music_stream.controller;

import com.ms.music_stream.dto.PlaylistRequest;
import com.ms.music_stream.dto.PlaylistResponse;
import com.ms.music_stream.service.PlaylistService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/playlists")
public class PlaylistApiController {

    private final PlaylistService playlistService;

    public PlaylistApiController(PlaylistService playlistService) {
        this.playlistService = playlistService;
    }

    @GetMapping
    public ResponseEntity<List<PlaylistResponse>> getAllPlaylists() {
        return ResponseEntity.ok(playlistService.getAllPlaylists());
    }

    @PostMapping
    public ResponseEntity<PlaylistResponse> createPlaylist(@Valid @RequestBody PlaylistRequest request) {
        PlaylistResponse saved = playlistService.createPlaylist(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PostMapping("/{playlistId}/songs/{songId}")
    public ResponseEntity<PlaylistResponse> addSongToPlaylist(@PathVariable Long playlistId, @PathVariable Long songId) {
        PlaylistResponse updated = playlistService.addSongToPlaylist(playlistId, songId);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{playlistId}/songs/{songId}")
    public ResponseEntity<PlaylistResponse> removeSongFromPlaylist(@PathVariable Long playlistId, @PathVariable Long songId) {
        PlaylistResponse updated = playlistService.removeSongFromPlaylist(playlistId, songId);
        return ResponseEntity.ok(updated);
    }
}
