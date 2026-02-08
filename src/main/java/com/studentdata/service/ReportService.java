package com.studentdata.service;

import com.studentdata.entity.Student;
import com.studentdata.repository.StudentRepository;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class ReportService {

    private final StudentRepository studentRepository;

    public ReportService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public Page<Student> getStudents(int page, int size, String search, String studentClass) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());

        boolean hasSearch = search != null && !search.isEmpty();
        boolean hasClass = studentClass != null && !studentClass.isEmpty();

        if (hasSearch && hasClass) {
            return studentRepository.findByStudentIdContainingIgnoreCaseAndStudentClass(search, studentClass, pageable);
        } else if (hasSearch) {
            return studentRepository.findByStudentIdContainingIgnoreCase(search, pageable);
        } else if (hasClass) {
            return studentRepository.findByStudentClass(studentClass, pageable);
        }

        return studentRepository.findAll(pageable);
    }

    public List<String> getDistinctClasses() {
        return studentRepository.findDistinctClasses();
    }

    public byte[] exportToExcel(String search, String studentClass) throws Exception {
        List<Student> students = getFilteredStudents(search, studentClass);

        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Students");

            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "Student ID", "First Name", "Last Name", "DOB", "Class", "Score"};
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            int rowNum = 1;
            for (Student s : students) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(s.getId());
                row.createCell(1).setCellValue(s.getStudentId());
                row.createCell(2).setCellValue(s.getFirstName());
                row.createCell(3).setCellValue(s.getLastName());
                row.createCell(4).setCellValue(s.getDob());
                row.createCell(5).setCellValue(s.getStudentClass());
                row.createCell(6).setCellValue(s.getScore());
            }

            workbook.write(out);
            workbook.dispose();
            return out.toByteArray();
        }
    }

    private List<Student> getFilteredStudents(String search, String studentClass) {
        boolean hasSearch = search != null && !search.isEmpty();
        boolean hasClass = studentClass != null && !studentClass.isEmpty();

        if (hasSearch && hasClass) {
            return studentRepository.findByStudentIdContainingIgnoreCaseAndStudentClass(search, studentClass);
        } else if (hasSearch) {
            return studentRepository.findByStudentIdContainingIgnoreCase(search);
        } else if (hasClass) {
            return studentRepository.findByStudentClass(studentClass);
        }

        return studentRepository.findAll();
    }
}
