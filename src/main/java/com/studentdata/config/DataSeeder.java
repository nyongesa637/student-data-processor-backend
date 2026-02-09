package com.studentdata.config;

import com.studentdata.entity.ChangelogEntry;
import com.studentdata.repository.ChangelogEntryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DataSeeder implements CommandLineRunner {

    private final ChangelogEntryRepository repository;

    public DataSeeder(ChangelogEntryRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        seedEntry("1.0.0", LocalDate.of(2026, 1, 1), "GENERAL",
                "Initial release: Excel generation, CSV processing, database upload, student report with pagination/search/filtering, export to Excel/CSV/PDF.");

        seedEntry("2.0.0", LocalDate.of(2026, 2, 9), "GENERAL",
                "Major UI overhaul with sidenav navigation, home dashboard, analytics, notifications, and changelog system.");
        seedEntry("2.0.0", LocalDate.of(2026, 2, 9), "FRONTEND",
                "Sidenav layout, breadcrumb nav, global search, notification bell, chat panel, Material Icons, router-based architecture.");
        seedEntry("2.0.0", LocalDate.of(2026, 2, 9), "BACKEND",
                "Analytics endpoint, notification persistence, feature request API, database indexes on student_class and student_id.");

        seedEntry("3.0.0", LocalDate.of(2026, 2, 9), "GENERAL",
                "Component-aware changelog with real-time SSE updates and filter support.");
        seedEntry("3.0.0", LocalDate.of(2026, 2, 9), "FRONTEND",
                "ChangelogService with SSE EventSource, component filter buttons, component badges, fixed chat panel field bindings.");
        seedEntry("3.0.0", LocalDate.of(2026, 2, 9), "BACKEND",
                "ChangelogService with SSE streaming, POST endpoint, component column, DataSeeder, new repository query methods.");
    }

    private void seedEntry(String version, LocalDate date, String component, String changes) {
        if (!repository.existsByVersionAndComponent(version, component)) {
            repository.save(new ChangelogEntry(version, date, changes, component));
        }
    }
}
