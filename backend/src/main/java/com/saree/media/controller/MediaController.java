package com.saree.media.controller;

import com.saree.media.dto.MediaRequest;
import com.saree.media.dto.MediaResponse;
import com.saree.media.service.MediaService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class MediaController {

    private final MediaService mediaService;

    public MediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    @PostMapping("/admin/media/upload-url")
    public ResponseEntity<MediaResponse> requestUpload(@Valid @RequestBody MediaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mediaService.requestUpload(request));
    }

    @GetMapping("/media/products/{productId}")
    public ResponseEntity<List<MediaResponse>> listForProduct(@PathVariable UUID productId) {
        return ResponseEntity.ok(mediaService.listForProduct(productId));
    }

    @GetMapping("/media/{id}")
    public ResponseEntity<MediaResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(mediaService.getById(id));
    }

    @DeleteMapping("/admin/media/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        mediaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
