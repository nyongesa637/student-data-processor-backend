package com.studentdata.controller;

import com.studentdata.service.DataUploadService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class DataUploadController {

    private final DataUploadService dataUploadService;

    public DataUploadController(DataUploadService dataUploadService) {
        this.dataUploadService = dataUploadService;
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadCsv(@RequestParam("file") MultipartFile file) {
        try {
            int count = dataUploadService.uploadCsvToDatabase(file);
            return ResponseEntity.ok(Map.of(
                    "message", "Successfully uploaded " + count + " records to database",
                    "count", count
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "message", "Error uploading data: " + e.getMessage()
            ));
        }
    }
}
