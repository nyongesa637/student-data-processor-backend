package com.studentdata.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "changelog_entries")
public class ChangelogEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String version;

    @Column(name = "release_date", nullable = false)
    private LocalDate releaseDate;

    @Column(nullable = false, length = 2000)
    private String changes;

    @Column(columnDefinition = "varchar(255) default 'GENERAL'")
    private String component;

    public ChangelogEntry() {}

    public ChangelogEntry(String version, LocalDate releaseDate, String changes) {
        this.version = version;
        this.releaseDate = releaseDate;
        this.changes = changes;
        this.component = "GENERAL";
    }

    public ChangelogEntry(String version, LocalDate releaseDate, String changes, String component) {
        this.version = version;
        this.releaseDate = releaseDate;
        this.changes = changes;
        this.component = component;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public LocalDate getReleaseDate() { return releaseDate; }
    public void setReleaseDate(LocalDate releaseDate) { this.releaseDate = releaseDate; }

    public String getChanges() { return changes; }
    public void setChanges(String changes) { this.changes = changes; }

    public String getComponent() { return component; }
    public void setComponent(String component) { this.component = component; }
}
