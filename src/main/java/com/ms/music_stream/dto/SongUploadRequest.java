package com.ms.music_stream.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SongUploadRequest {

    @NotBlank(message = "Tiêu đề không được để trống")
    private String title;

    @NotBlank(message = "Tên nghệ sĩ không được để trống")
    private String artist;

    private String album;

    private String genre;

    private MultipartFile audioFile;

    private MultipartFile coverFile;
}
