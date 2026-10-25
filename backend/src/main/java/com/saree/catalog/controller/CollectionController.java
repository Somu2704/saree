package com.saree.catalog.controller;

import com.saree.catalog.dto.CollectionRequest;
import com.saree.catalog.dto.CollectionResponse;
import com.saree.catalog.service.CollectionService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class CollectionController {

    private final CollectionService collectionService;

    public CollectionController(CollectionService collectionService) {
        this.collectionService = collectionService;
    }

    @GetMapping("/collections")
    public ResponseEntity<List<CollectionResponse>> list() {
        return ResponseEntity.ok(collectionService.list());
    }

    @GetMapping("/collections/{slug}")
    public ResponseEntity<CollectionResponse> getBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(collectionService.getBySlug(slug));
    }

    @PostMapping("/admin/collections")
    public ResponseEntity<CollectionResponse> create(@Valid @RequestBody CollectionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(collectionService.create(request));
    }

    @PutMapping("/admin/collections/{id}")
    public ResponseEntity<CollectionResponse> update(@PathVariable UUID id, @Valid @RequestBody CollectionRequest request) {
        return ResponseEntity.ok(collectionService.update(id, request));
    }
}
