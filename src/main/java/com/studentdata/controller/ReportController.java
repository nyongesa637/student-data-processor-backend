package com.studentdata.controller;

import com.studentdata.entity.Student;
import com.studentdata.service.ReportService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping
    public ResponseEntity<Page<Student>> getStudents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String studentClass) {
        return ResponseEntity.ok(reportService.getStudents(page, size, search, studentClass));
    }

    @GetMapping("/classes")
    public ResponseEntity<List<String>> getClasses() {
        return ResponseEntity.ok(reportService.getDistinctClasses());
    }
}
