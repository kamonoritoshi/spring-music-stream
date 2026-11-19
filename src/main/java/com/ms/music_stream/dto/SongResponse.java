package com.ms.music_stream.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SongResponse {
    private Long id;
    private String title;
    private String artist;
    private String album;
    private String genre;
    private String audioUrl;
    private String coverUrl;
    private Long playCount;
    private LocalDateTime uploadedAt;

    // Backward-compatibility getters for frontend index.html
    public String getAudioPath() {
        return audioUrl;
    }

    public String getCoverPath() {
        return coverUrl;
    }
}
