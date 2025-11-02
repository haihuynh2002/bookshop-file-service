package com.bookshop.file_service.file.web;

import com.bookshop.file_service.file.domain.File;
import com.bookshop.file_service.file.domain.FileRepository;
import com.bookshop.file_service.file.domain.FileService;
import com.bookshop.file_service.file.domain.ImageType;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("files")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FileController {
    @NonFinal
    static final Logger log = LoggerFactory.getLogger(FileController.class);
    FileService fileService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Flux<File> upload(
            @RequestPart("files") Flux<FilePart> fileParts,
            @RequestParam("ownerId") Long ownerId,
            @RequestParam("type") ImageType type) {
            return fileService.store(fileParts, ownerId, type);
    }

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Flux<File> update(
            @RequestPart(value = "files", required = false) Flux<FilePart> fileParts,
            @RequestParam("ownerId") Long ownerId,
            @RequestParam("type") ImageType type) {
        return fileService.update(fileParts, ownerId, type);
    }

    @GetMapping("/{filename:.+}")
    public Mono<ResponseEntity<Resource>> serveFile(@PathVariable String filename) {
        return fileService.loadAsResource(filename)
                .map(resource -> ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                        .body(resource));
    }

    @GetMapping("/{type}/{ownerId}")
    public Flux<File> getFiles(@PathVariable ImageType type,
                               @PathVariable Long ownerId) {
        return fileService.getByOwnerIdAndType(ownerId, type);
    }

//    @GetMapping("{id}")
//    public Mono<File> getFileById(@PathVariable Long id) {
//        return fileService.getById(id);
//    }

    @DeleteMapping("/{filename:.+}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteFile(@PathVariable String filename) {
        return fileService.delete(filename);
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteFileById(@PathVariable Long id) {
        return fileService.deleteById(id);
    }

    @DeleteMapping("/{type}/{ownerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteFilesByOwnerId(
            @PathVariable ImageType type,
            @PathVariable Long ownerId) {
        return fileService.deleteByOwnerIdAndType(ownerId, type);
    }
}
