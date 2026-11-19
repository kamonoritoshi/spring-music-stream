package com.ms.music_stream.config;

import com.ms.music_stream.model.Playlist;
import com.ms.music_stream.model.Role;
import com.ms.music_stream.model.User;
import com.ms.music_stream.repository.PlaylistRepository;
import com.ms.music_stream.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PlaylistRepository playlistRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository,
                      PlaylistRepository playlistRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.playlistRepository = playlistRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        User adminUser = userRepository.findByUsername("admin").orElse(null);
        if (adminUser == null) {
            adminUser = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .fullName("Quản Trị Viên")
                    .role(Role.ROLE_ADMIN)
                    .build();
            adminUser = userRepository.save(adminUser);
        }

        if (!userRepository.existsByUsername("user")) {
            User normalUser = User.builder()
                    .username("user")
                    .password(passwordEncoder.encode("user123"))
                    .fullName("Người Dùng Mẫu")
                    .role(Role.ROLE_USER)
                    .build();
            userRepository.save(normalUser);
        }

        // Migrate existing playlists that may have null user
        List<Playlist> orphanPlaylists = playlistRepository.findAll();
        for (Playlist playlist : orphanPlaylists) {
            if (playlist.getUser() == null) {
                playlist.setUser(adminUser);
                playlistRepository.save(playlist);
            }
        }
    }
}
