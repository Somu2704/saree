package com.saree.media.controller;

import com.saree.media.dto.MediaRequest;
import com.saree.media.dto.MediaResponse;
import com.saree.media.service.MediaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Media", description = "Media metadata and signed upload flow")
public class MediaController {

    private final MediaService mediaService;

    public MediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    @PostMapping("/admin/media/upload-url")
    @Operation(summary = "Generate signed upload URL for a product asset")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Upload metadata created"),
            @ApiResponse(responseCode = "400", description = "Invalid media request")
    })
    public ResponseEntity<MediaResponse> requestUpload(@Valid @RequestBody MediaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mediaService.requestUpload(request));
    }

    @GetMapping("/media/products/{productId}")
    @Operation(summary = "List all media for a product")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Media list returned")
    })
    public ResponseEntity<List<MediaResponse>> listForProduct(@PathVariable UUID productId) {
        return ResponseEntity.ok(mediaService.listForProduct(productId));
    }

    @GetMapping("/media/{id}")
    @Operation(summary = "Fetch a media metadata record by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Media record found"),
            @ApiResponse(responseCode = "400", description = "Media not found")
    })
    public ResponseEntity<MediaResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(mediaService.getById(id));
    }

    @DeleteMapping("/admin/media/{id}")
    @Operation(summary = "Delete media metadata record")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Media deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Media not found")
    })
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        mediaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
