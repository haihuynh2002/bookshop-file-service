package com.bookshop.file_service.file.domain;

import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LocalStorageService implements FileService {

    static final Logger log = LoggerFactory.getLogger(LocalStorageService.class);
    final FileRepository fileRepository;

    @Value("${file.upload-dir:uploads}")
    String uploadDir;

    Path rootLocation;

    @PostConstruct
    public void init() {
        try {
            this.rootLocation = Paths.get(uploadDir);
            Files.createDirectories(rootLocation);
            log.info("File storage initialized at: {}", rootLocation.toAbsolutePath());
        } catch (Exception e) {
            throw new RuntimeException("Could not initialize storage", e);
        }
    }

    @Override
    public Flux<File> store(Flux<FilePart> fileParts, Long ownerId, ImageType type) {
        return fileParts.flatMap(filePart -> {
            String filename = UUID.randomUUID() + "_" + filePart.filename();
            Path destination = rootLocation.resolve(filename);

            return filePart.transferTo(destination)
                    .then(Mono.fromCallable(() -> {
                        File file = File.builder()
                                .ownerId(ownerId)
                                .filename(filename)
                                .originalFilename(filePart.filename())
                                .contentType(filePart.headers().getContentType() != null ?
                                        filePart.headers().getContentType().toString() :
                                        "application/octet-stream")
                                .filePath(destination.toString())
                                .size(Files.size(destination))
                                .type(type)
                                .build();
                        return file;
                    }))
//                    .flatMap(this::checkForDuplicates)
                    .flatMap(fileRepository::save)
                    .doOnSuccess(file -> log.info("File stored successfully: {}", file.getFilename()))
                    .doOnError(error -> {
                        try {
                            Files.deleteIfExists(destination);
                        } catch (Exception e) {
                            log.warn("Failed to delete file after error: {}", destination, e);
                        }
                    });
            });
    }

    public Flux<File> update(Flux<FilePart> fileParts, Long ownerId, ImageType type) {
        return deleteByOwnerIdAndType(ownerId, type)
                .doOnError(error -> log.info(error.getMessage()))
                .thenMany(store(fileParts, ownerId, type));
    }

    @Override
    public Mono<Resource> loadAsResource(String filename) {
        return Mono.fromCallable(() -> {
            Path file = rootLocation.resolve(filename);
            Resource resource = new UrlResource(file.toUri());

            if (!resource.exists()) {
                throw new java.io.FileNotFoundException("File not found: " + filename);
            }

            if (!resource.isReadable()) {
                throw new FileServiceException("File is not readable: " + filename);
            }

            return resource;
        });
    }

    @Override
    public Mono<Void> delete(String filename) {
        return fileRepository.findByFilename(filename)
                .switchIfEmpty(Mono.error(new FileNotFoundException("File not found: " + filename)))
                .flatMap(file -> Mono.fromRunnable(() -> {
                    try {
                        Files.deleteIfExists(rootLocation.resolve(filename));
                    } catch (Exception e) {
                        throw new FileServiceException("Failed to delete file: " + filename, e);
                    }
                }))
                .then(fileRepository.deleteByFilename(filename))
                .doOnSuccess(v -> log.info("File deleted successfully: {}", filename));
    }

    @Override
    public Mono<Void> deleteById(Long id) {
        return fileRepository.findById(id)
                .switchIfEmpty(Mono.error(new FileNotFoundException("File not found with ID: " + id)))
                .flatMap(file -> delete(file.getFilename()));
    }

    @Override
    public Mono<Void> deleteByOwnerIdAndType(Long ownerId, ImageType type) {
        return fileRepository.findByOwnerIdAndType(ownerId, type)
                .flatMap(file -> delete(file.getFilename()))
                .then()
                .doOnSuccess(v -> log.info("All files deleted for owner ID: {}", ownerId));
    }

    @Override
    public Flux<File> getByOwnerIdAndType(Long ownerId, ImageType type) {
        return fileRepository.findByOwnerIdAndType(ownerId, type);
    }

    @Override
    public Mono<File> getById(Long id) {
        return fileRepository.findById(id)
                .switchIfEmpty(Mono.error(new FileNotFoundException("File not found with ID: " + id)));
    }

    private Mono<File> checkForDuplicates(File file) {
        return fileRepository.findByOwnerIdAndOriginalFilename(file.getOwnerId(), file.getOriginalFilename())
                .hasElements()
                .flatMap(hasDuplicate -> {
                    if (hasDuplicate) {
                        return Mono.error(new DuplicateFileException(
                                "File with name '" + file.getOriginalFilename() +
                                        "' already exists for book ID: " + file.getOwnerId()));
                    }
                    return Mono.just(file);
                });
    }
}