package com.studentdata.service;

import com.studentdata.entity.Student;
import com.studentdata.repository.StudentRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private final StudentRepository studentRepository;

    public AnalyticsService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public Map<String, Object> getSummary() {
        Map<String, Object> summary = new LinkedHashMap<>();

        long totalStudents = studentRepository.count();
        summary.put("totalStudents", totalStudents);

        if (totalStudents == 0) {
            summary.put("averageScore", 0);
            summary.put("highestScore", 0);
            summary.put("lowestScore", 0);
            summary.put("classDistribution", Map.of());
            summary.put("recentRecords", List.of());
            return summary;
        }

        List<Student> allStudents = studentRepository.findAll();

        double avgScore = allStudents.stream().mapToInt(Student::getScore).average().orElse(0);
        int maxScore = allStudents.stream().mapToInt(Student::getScore).max().orElse(0);
        int minScore = allStudents.stream().mapToInt(Student::getScore).min().orElse(0);

        summary.put("averageScore", Math.round(avgScore * 100.0) / 100.0);
        summary.put("highestScore", maxScore);
        summary.put("lowestScore", minScore);

        Map<String, Long> classDistribution = allStudents.stream()
                .collect(Collectors.groupingBy(Student::getStudentClass, Collectors.counting()));
        // Top 5 classes by count
        Map<String, Long> topClasses = classDistribution.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));
        summary.put("classDistribution", topClasses);

        List<Student> recentRecords = studentRepository.findAll(
                PageRequest.of(0, 5, Sort.by("id").descending())
        ).getContent();
        summary.put("recentRecords", recentRecords);

        return summary;
    }
}
