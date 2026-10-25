package com.saree.media;

import static org.assertj.core.api.Assertions.assertThat;

import com.saree.catalog.dto.ProductRequest;
import com.saree.catalog.dto.ProductResponse;
import com.saree.catalog.service.ProductService;
import com.saree.media.dto.MediaRequest;
import com.saree.media.service.MediaService;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MediaIntegrationTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private MediaService mediaService;

    @Test
    void requestUpload_shouldCreateMediaRecordAndListByProduct() {
        ProductResponse product = productService.create(new ProductRequest(
                "COT-2001",
                "Handloom Cotton Saree",
                "handloom-cotton-saree",
                "Premium cotton saree for festive occasions.",
                "Cotton",
                "Mustard",
                "Festive",
                new BigDecimal("1999.00"),
                new BigDecimal("1699.00"),
                20,
                "ACTIVE",
                null,
                null));

        var media = mediaService.requestUpload(new MediaRequest(
                product.id(),
                "front-view.jpg",
                "image/jpeg",
                "IMAGE",
                4_200_000L,
                1600,
                1200,
                null,
                true,
                1));

        assertThat(media.uploadUrl()).contains("upload.saree.local");
        assertThat(media.originalFilename()).isEqualTo("front-view.jpg");

        var mediaList = mediaService.listForProduct(product.id());
        assertThat(mediaList).hasSize(1);
    }
}
