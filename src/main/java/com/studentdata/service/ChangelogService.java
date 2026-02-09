package com.studentdata.service;

import com.studentdata.entity.ChangelogEntry;
import com.studentdata.repository.ChangelogEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class ChangelogService {

    private final ChangelogEntryRepository repository;
    private final List<SseEmitter> subscribers = new CopyOnWriteArrayList<>();

    public ChangelogService(ChangelogEntryRepository repository) {
        this.repository = repository;
    }

    public List<ChangelogEntry> getAllEntries() {
        return repository.findAllByOrderByReleaseDateDesc();
    }

    public List<ChangelogEntry> getEntriesByComponent(String component) {
        return repository.findByComponentOrderByReleaseDateDesc(component);
    }

    public ChangelogEntry createEntry(ChangelogEntry entry) {
        ChangelogEntry saved = repository.save(entry);
        notifySubscribers(saved);
        return saved;
    }

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        subscribers.add(emitter);
        emitter.onCompletion(() -> subscribers.remove(emitter));
        emitter.onTimeout(() -> subscribers.remove(emitter));
        emitter.onError(e -> subscribers.remove(emitter));
        return emitter;
    }

    private void notifySubscribers(ChangelogEntry entry) {
        List<SseEmitter> deadEmitters = new java.util.ArrayList<>();
        for (SseEmitter emitter : subscribers) {
            try {
                emitter.send(SseEmitter.event()
                        .name("changelog-update")
                        .data(entry));
            } catch (IOException e) {
                deadEmitters.add(emitter);
            }
        }
        subscribers.removeAll(deadEmitters);
    }
}
