package com.studentdata.service;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Service
public class DataGenerationService {

    @Value("${app.output.directory}")
    private String outputDirectory;

    private static final String[] FIRST_NAMES = {
            "James", "Mary", "Robert", "Patricia", "John", "Jennifer", "Michael", "Linda",
            "David", "Elizabeth", "William", "Barbara", "Richard", "Susan", "Joseph", "Jessica",
            "Thomas", "Sarah", "Christopher", "Karen", "Charles", "Lisa", "Daniel", "Nancy",
            "Matthew", "Betty", "Anthony", "Margaret", "Mark", "Sandra", "Donald", "Ashley",
            "Steven", "Kimberly", "Paul", "Emily", "Andrew", "Donna", "Joshua", "Michelle"
    };

    private static final String[] LAST_NAMES = {
            "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis",
            "Rodriguez", "Martinez", "Hernandez", "Lopez", "Gonzalez", "Wilson", "Anderson",
            "Thomas", "Taylor", "Moore", "Jackson", "Martin", "Lee", "Perez", "Thompson",
            "White", "Harris", "Sanchez", "Clark", "Ramirez", "Lewis", "Robinson"
    };

    private static final String[] CLASSES = {
            "Class A", "Class B", "Class C", "Class D", "Class E",
            "Class F", "Class G", "Class H", "Class I", "Class J"
    };

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public String generateExcel(int count) throws IOException {
        Path dirPath = Paths.get(outputDirectory);
        Files.createDirectories(dirPath);

        String filename = "students_" + System.currentTimeMillis() + ".xlsx";
        Path filePath = dirPath.resolve(filename);

        Random random = new Random();

        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100)) {
            Sheet sheet = workbook.createSheet("Students");

            // Header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Student ID", "First Name", "Last Name", "DOB", "Class", "Score"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            // Data rows
            for (int i = 1; i <= count; i++) {
                Row row = sheet.createRow(i);
                row.createCell(0).setCellValue("STU" + String.format("%07d", i));
                row.createCell(1).setCellValue(FIRST_NAMES[random.nextInt(FIRST_NAMES.length)]);
                row.createCell(2).setCellValue(LAST_NAMES[random.nextInt(LAST_NAMES.length)]);
                row.createCell(3).setCellValue(generateRandomDob(random));
                row.createCell(4).setCellValue(CLASSES[random.nextInt(CLASSES.length)]);
                row.createCell(5).setCellValue(random.nextInt(101)); // Score 0-100
            }

            try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
                workbook.write(fos);
            }

            workbook.dispose();
        }

        return filename;
    }

    private String generateRandomDob(Random random) {
        LocalDate start = LocalDate.of(2000, 1, 1);
        LocalDate end = LocalDate.of(2010, 12, 31);
        long startEpochDay = start.toEpochDay();
        long endEpochDay = end.toEpochDay();
        long randomDay = startEpochDay + random.nextLong(endEpochDay - startEpochDay + 1);
        return LocalDate.ofEpochDay(randomDay).format(DATE_FORMAT);
    }
}
