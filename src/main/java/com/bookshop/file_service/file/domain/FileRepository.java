package com.bookshop.file_service.file.domain;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FileRepository extends ReactiveCrudRepository<File, Long> {
    Flux<File> findByOwnerIdAndType(Long ownerId, ImageType type);
    Mono<File> findByFilename(String filename);
    Flux<File> findByOwnerIdAndOriginalFilename(Long ownerId, String originalFilename);
    Mono<Void> deleteByFilename(String filename);
    Mono<Void> deleteByOwnerIdAndType(Long ownerId, ImageType type);
}