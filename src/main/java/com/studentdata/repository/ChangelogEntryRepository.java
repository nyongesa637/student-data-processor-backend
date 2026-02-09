package com.studentdata.repository;

import com.studentdata.entity.ChangelogEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChangelogEntryRepository extends JpaRepository<ChangelogEntry, Long> {

    List<ChangelogEntry> findAllByOrderByReleaseDateDesc();

    List<ChangelogEntry> findByComponentOrderByReleaseDateDesc(String component);

    boolean existsByVersionAndComponent(String version, String component);
}
