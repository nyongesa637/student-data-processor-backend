package com.studentdata.controller;

import com.studentdata.entity.FeatureRequest;
import com.studentdata.repository.FeatureRequestRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/feature-requests")
public class FeatureRequestController {

    private final FeatureRequestRepository featureRequestRepository;

    public FeatureRequestController(FeatureRequestRepository featureRequestRepository) {
        this.featureRequestRepository = featureRequestRepository;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> submitFeatureRequest(@RequestBody FeatureRequest request) {
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Title is required"));
        }
        if (request.getDescription() == null || request.getDescription().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Description is required"));
        }
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
        }
        featureRequestRepository.save(request);
        return ResponseEntity.ok(Map.of("message", "Feature request submitted successfully"));
    }
}
