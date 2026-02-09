package com.studentdata.controller;

import com.studentdata.entity.ChangelogEntry;
import com.studentdata.repository.ChangelogEntryRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/changelog")
public class ChangelogController {

    private final ChangelogEntryRepository changelogEntryRepository;

    public ChangelogController(ChangelogEntryRepository changelogEntryRepository) {
        this.changelogEntryRepository = changelogEntryRepository;
    }

    @GetMapping
    public ResponseEntity<List<ChangelogEntry>> getChangelog() {
        return ResponseEntity.ok(changelogEntryRepository.findAllByOrderByReleaseDateDesc());
    }
}
