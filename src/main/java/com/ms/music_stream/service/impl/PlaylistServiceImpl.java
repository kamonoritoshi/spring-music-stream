package com.ms.music_stream.service.impl;

import com.ms.music_stream.dto.PlaylistRequest;
import com.ms.music_stream.dto.PlaylistResponse;
import com.ms.music_stream.exception.ResourceNotFoundException;
import com.ms.music_stream.mapper.EntityMapper;
import com.ms.music_stream.model.Playlist;
import com.ms.music_stream.model.Song;
import com.ms.music_stream.model.User;
import com.ms.music_stream.repository.PlaylistRepository;
import com.ms.music_stream.repository.SongRepository;
import com.ms.music_stream.repository.UserRepository;
import com.ms.music_stream.service.PlaylistService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PlaylistServiceImpl implements PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final SongRepository songRepository;
    private final UserRepository userRepository;
    private final EntityMapper entityMapper;

    public PlaylistServiceImpl(PlaylistRepository playlistRepository,
                               SongRepository songRepository,
                               UserRepository userRepository,
                               EntityMapper entityMapper) {
        this.playlistRepository = playlistRepository;
        this.songRepository = songRepository;
        this.userRepository = userRepository;
        this.entityMapper = entityMapper;
    }

    private User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new AccessDeniedException("Yêu cầu đăng nhập để thực hiện hành động này");
        }

        if (authentication.getPrincipal() instanceof User user) {
            return user;
        }

        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AccessDeniedException("Không tìm thấy thông tin người dùng: " + username));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlaylistResponse> getAllPlaylists() {
        // Return current user's playlists
        return getMyPlaylists();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlaylistResponse> getMyPlaylists() {
        User currentUser = getCurrentAuthenticatedUser();
        List<Playlist> playlists = playlistRepository.findByUserOrderByCreatedAtDesc(currentUser);
        return entityMapper.toPlaylistResponseList(playlists);
    }

    @Override
    @Transactional
    public PlaylistResponse createPlaylist(PlaylistRequest request) {
        User currentUser = getCurrentAuthenticatedUser();

        Playlist playlist = new Playlist();
        playlist.setName(request.getName());
        playlist.setDescription(request.getDescription());
        playlist.setUser(currentUser);

        Playlist saved = playlistRepository.save(playlist);
        return entityMapper.toPlaylistResponse(saved);
    }

    @Override
    @Transactional
    public PlaylistResponse addSongToPlaylist(Long playlistId, Long songId) {
        User currentUser = getCurrentAuthenticatedUser();

        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy playlist với id: " + playlistId));

        if (playlist.getUser() == null || !playlist.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Bạn không có quyền chỉnh sửa playlist này");
        }

        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài hát với id: " + songId));

        playlist.getSongs().add(song);
        Playlist updated = playlistRepository.save(playlist);
        return entityMapper.toPlaylistResponse(updated);
    }

    @Override
    @Transactional
    public PlaylistResponse removeSongFromPlaylist(Long playlistId, Long songId) {
        User currentUser = getCurrentAuthenticatedUser();

        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy playlist với id: " + playlistId));

        if (playlist.getUser() == null || !playlist.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Bạn không có quyền chỉnh sửa playlist này");
        }

        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài hát với id: " + songId));

        playlist.getSongs().remove(song);
        Playlist updated = playlistRepository.save(playlist);
        return entityMapper.toPlaylistResponse(updated);
    }
}
