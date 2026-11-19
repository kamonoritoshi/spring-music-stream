package com.ms.music_stream.mapper;

import com.ms.music_stream.dto.PlaylistResponse;
import com.ms.music_stream.dto.SongResponse;
import com.ms.music_stream.model.Playlist;
import com.ms.music_stream.model.Song;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class EntityMapper {

    public SongResponse toSongResponse(Song song) {
        if (song == null) {
            return null;
        }
        return SongResponse.builder()
                .id(song.getId())
                .title(song.getTitle())
                .artist(song.getArtist())
                .album(song.getAlbum())
                .genre(song.getGenre())
                .audioUrl(song.getAudioPath())
                .coverUrl(song.getCoverPath())
                .playCount(song.getPlayCount() != null ? song.getPlayCount() : 0L)
                .uploadedAt(song.getUploadedAt())
                .build();
    }

    public List<SongResponse> toSongResponseList(List<Song> songs) {
        if (songs == null) {
            return Collections.emptyList();
        }
        return songs.stream()
                .map(this::toSongResponse)
                .collect(Collectors.toList());
    }

    public PlaylistResponse toPlaylistResponse(Playlist playlist) {
        if (playlist == null) {
            return null;
        }

        List<SongResponse> songResponses = new ArrayList<>();
        if (playlist.getSongs() != null) {
            songResponses = playlist.getSongs().stream()
                    .map(this::toSongResponse)
                    .collect(Collectors.toList());
        }

        return PlaylistResponse.builder()
                .id(playlist.getId())
                .name(playlist.getName())
                .description(playlist.getDescription())
                .createdAt(playlist.getCreatedAt())
                .songCount(songResponses.size())
                .songs(songResponses)
                .build();
    }

    public List<PlaylistResponse> toPlaylistResponseList(List<Playlist> playlists) {
        if (playlists == null) {
            return Collections.emptyList();
        }
        return playlists.stream()
                .map(this::toPlaylistResponse)
                .collect(Collectors.toList());
    }
}
