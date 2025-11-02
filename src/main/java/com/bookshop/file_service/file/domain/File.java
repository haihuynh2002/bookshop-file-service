package com.bookshop.file_service.file.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@Table("file")
public class File {
    @Id
    Long id;

    @NotNull(message = "Book ID is required")
    @Positive(message = "Book ID must be positive")
    Long ownerId;

    @NotBlank(message = "Filename is required")
    String filename;

    @NotBlank(message = "Original filename is required")
    String originalFilename;

    String contentType;

    @NotBlank(message = "File path is required")
    String filePath;

    @NotNull(message = "File size is required")
    @Positive(message = "File size must be positive")
    Long size;

    @NotNull(message = "File type is required")
    ImageType type;

    @CreatedDate
    Instant createdDate;

    @LastModifiedDate
    Instant lastModifiedDate;
}