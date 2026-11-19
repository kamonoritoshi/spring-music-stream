package com.ms.music_stream.service;

import com.ms.music_stream.dto.PlaylistRequest;
import com.ms.music_stream.dto.PlaylistResponse;

import java.util.List;

public interface PlaylistService {
    List<PlaylistResponse> getAllPlaylists();
    PlaylistResponse createPlaylist(PlaylistRequest request);
    PlaylistResponse addSongToPlaylist(Long playlistId, Long songId);
    PlaylistResponse removeSongFromPlaylist(Long playlistId, Long songId);
}
