package com.saree.common.health;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(name = "System", description = "System health and operational status endpoints")
public class HealthController {

    @GetMapping("/health")
    @Operation(summary = "Check backend health", description = "Returns the service health and timestamp")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Backend is healthy",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"status\":\"UP\",\"service\":\"saree-backend\",\"timestamp\":\"2026-10-26T00:00:00Z\"}")))
    })
    public Map<String, Object> health() {
        return Map.of(
                "status", "UP",
                "service", "saree-backend",
                "timestamp", Instant.now().toString()
        );
    }
}
