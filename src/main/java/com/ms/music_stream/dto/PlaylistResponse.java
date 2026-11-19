package com.ms.music_stream.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlaylistResponse {
    private Long id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private int songCount;
    @Builder.Default
    private List<SongResponse> songs = new ArrayList<>();
}
