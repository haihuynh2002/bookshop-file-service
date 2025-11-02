package com.bookshop.file_service.file.domain;

import org.springframework.core.io.Resource;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FileService {
    Flux<File> store(Flux<FilePart> fileParts, Long ownerId, ImageType type);
    Flux<File> update(Flux<FilePart> fileParts, Long ownerId, ImageType type);
    Mono<Resource> loadAsResource(String filename);
    Mono<Void> delete(String filename);
    Mono<Void> deleteById(Long id);
    Mono<Void> deleteByOwnerIdAndType(Long ownerId, ImageType type);
    Flux<File> getByOwnerIdAndType(Long ownerId, ImageType type);
    Mono<File> getById(Long id);
}