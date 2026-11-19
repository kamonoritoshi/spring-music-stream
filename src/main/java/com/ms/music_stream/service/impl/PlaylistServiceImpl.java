package com.ms.music_stream.service.impl;

import com.ms.music_stream.dto.PlaylistRequest;
import com.ms.music_stream.dto.PlaylistResponse;
import com.ms.music_stream.exception.ResourceNotFoundException;
import com.ms.music_stream.mapper.EntityMapper;
import com.ms.music_stream.model.Playlist;
import com.ms.music_stream.model.Song;
import com.ms.music_stream.repository.PlaylistRepository;
import com.ms.music_stream.repository.SongRepository;
import com.ms.music_stream.service.PlaylistService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PlaylistServiceImpl implements PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final SongRepository songRepository;
    private final EntityMapper entityMapper;

    public PlaylistServiceImpl(PlaylistRepository playlistRepository,
                               SongRepository songRepository,
                               EntityMapper entityMapper) {
        this.playlistRepository = playlistRepository;
        this.songRepository = songRepository;
        this.entityMapper = entityMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlaylistResponse> getAllPlaylists() {
        List<Playlist> playlists = playlistRepository.findAll();
        return entityMapper.toPlaylistResponseList(playlists);
    }

    @Override
    @Transactional
    public PlaylistResponse createPlaylist(PlaylistRequest request) {
        Playlist playlist = new Playlist();
        playlist.setName(request.getName());
        playlist.setDescription(request.getDescription());

        Playlist saved = playlistRepository.save(playlist);
        return entityMapper.toPlaylistResponse(saved);
    }

    @Override
    @Transactional
    public PlaylistResponse addSongToPlaylist(Long playlistId, Long songId) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy playlist với id: " + playlistId));
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài hát với id: " + songId));

        playlist.getSongs().add(song);
        Playlist updated = playlistRepository.save(playlist);
        return entityMapper.toPlaylistResponse(updated);
    }

    @Override
    @Transactional
    public PlaylistResponse removeSongFromPlaylist(Long playlistId, Long songId) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy playlist với id: " + playlistId));
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài hát với id: " + songId));

        playlist.getSongs().remove(song);
        Playlist updated = playlistRepository.save(playlist);
        return entityMapper.toPlaylistResponse(updated);
    }
}
