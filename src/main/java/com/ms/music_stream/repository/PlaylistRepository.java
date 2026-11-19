package com.ms.music_stream.repository;

import com.ms.music_stream.model.Playlist;
import com.ms.music_stream.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, Long> {
    List<Playlist> findByUserOrderByCreatedAtDesc(User user);
    List<Playlist> findByUserIdOrderByCreatedAtDesc(Long userId);
}
