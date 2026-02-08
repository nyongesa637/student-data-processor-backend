package com.studentdata.controller;

import com.studentdata.service.DataGenerationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class DataGenerationController {

    private final DataGenerationService dataGenerationService;

    public DataGenerationController(DataGenerationService dataGenerationService) {
        this.dataGenerationService = dataGenerationService;
    }

    @PostMapping("/generate")
    public ResponseEntity<Map<String, String>> generateData(@RequestParam int count) {
        try {
            String filename = dataGenerationService.generateExcel(count);
            return ResponseEntity.ok(Map.of(
                    "message", "Successfully generated " + count + " student records",
                    "filename", filename
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "message", "Error generating data: " + e.getMessage()
            ));
        }
    }
}
