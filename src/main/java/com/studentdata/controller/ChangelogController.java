package com.studentdata.controller;

import com.studentdata.entity.ChangelogEntry;
import com.studentdata.service.ChangelogService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/changelog")
public class ChangelogController {

    private final ChangelogService changelogService;

    public ChangelogController(ChangelogService changelogService) {
        this.changelogService = changelogService;
    }

    @GetMapping
    public ResponseEntity<List<ChangelogEntry>> getChangelog(
            @RequestParam(required = false) String component) {
        if (component != null && !component.isBlank()) {
            return ResponseEntity.ok(changelogService.getEntriesByComponent(component));
        }
        return ResponseEntity.ok(changelogService.getAllEntries());
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        return changelogService.subscribe();
    }

    @PostMapping
    public ResponseEntity<ChangelogEntry> createEntry(@RequestBody ChangelogEntry entry) {
        return ResponseEntity.ok(changelogService.createEntry(entry));
    }
}
