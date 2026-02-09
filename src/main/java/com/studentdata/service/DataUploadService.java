package com.studentdata.service;

import com.opencsv.CSVReader;
import com.studentdata.entity.Student;
import com.studentdata.repository.StudentRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Service
public class DataUploadService {

    private final StudentRepository studentRepository;
    private final NotificationService notificationService;

    public DataUploadService(StudentRepository studentRepository, NotificationService notificationService) {
        this.studentRepository = studentRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public int uploadCsvToDatabase(MultipartFile file) throws Exception {
        int totalRecords = 0;
        List<Student> batch = new ArrayList<>();

        try (CSVReader csvReader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            // Skip header
            csvReader.readNext();

            String[] line;
            while ((line = csvReader.readNext()) != null) {
                if (line.length < 6) continue;

                int score;
                try {
                    score = Integer.parseInt(line[5].trim()) + 5;
                } catch (NumberFormatException e) {
                    continue;
                }

                Student student = new Student(
                        line[0].trim(),
                        line[1].trim(),
                        line[2].trim(),
                        line[3].trim(),
                        line[4].trim(),
                        score
                );

                batch.add(student);
                totalRecords++;

                if (batch.size() >= 1000) {
                    studentRepository.saveAll(batch);
                    studentRepository.flush();
                    batch.clear();
                }
            }

            // Save remaining records
            if (!batch.isEmpty()) {
                studentRepository.saveAll(batch);
                studentRepository.flush();
            }
        }

        notificationService.createNotification("UPLOAD", "Uploaded " + totalRecords + " records to database", "Source: " + file.getOriginalFilename());
        return totalRecords;
    }
}
