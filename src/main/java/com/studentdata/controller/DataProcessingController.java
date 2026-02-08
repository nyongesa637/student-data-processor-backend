package com.studentdata.controller;

import com.studentdata.service.DataProcessingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class DataProcessingController {

    private final DataProcessingService dataProcessingService;

    public DataProcessingController(DataProcessingService dataProcessingService) {
        this.dataProcessingService = dataProcessingService;
    }

    @PostMapping("/process")
    public ResponseEntity<Map<String, String>> processFile(@RequestParam("file") MultipartFile file) {
        try {
            String csvFilename = dataProcessingService.processExcelToCsv(file);
            return ResponseEntity.ok(Map.of(
                    "message", "Successfully processed Excel to CSV",
                    "filename", csvFilename
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "message", "Error processing file: " + e.getMessage()
            ));
        }
    }
}
