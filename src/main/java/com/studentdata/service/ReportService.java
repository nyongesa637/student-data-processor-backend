package com.studentdata.service;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.opencsv.CSVWriter;
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

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
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

    public byte[] exportToCsv(String search, String studentClass) throws Exception {
        List<Student> students = getFilteredStudents(search, studentClass);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             CSVWriter csvWriter = new CSVWriter(new OutputStreamWriter(out))) {

            csvWriter.writeNext(new String[]{"ID", "Student ID", "First Name", "Last Name", "DOB", "Class", "Score"});

            for (Student s : students) {
                csvWriter.writeNext(new String[]{
                        String.valueOf(s.getId()),
                        s.getStudentId(),
                        s.getFirstName(),
                        s.getLastName(),
                        s.getDob(),
                        s.getStudentClass(),
                        String.valueOf(s.getScore())
                });
            }

            csvWriter.flush();
            return out.toByteArray();
        }
    }

    public byte[] exportToPdf(String search, String studentClass) throws Exception {
        boolean hasSearch = search != null && !search.isEmpty();
        boolean hasClass = studentClass != null && !studentClass.isEmpty();

        try (ByteArrayOutputStream out = new ByteArrayOutputStream(65536)) {
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter writer = PdfWriter.getInstance(document, out);
            writer.setFullCompression();
            document.open();

            Font titleFont = new Font(Font.HELVETICA, 16, Font.BOLD);
            Paragraph title = new Paragraph("Student Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1, 2, 2, 2, 2, 1.5f, 1});
            table.setHeaderRows(1);

            Font headerFont = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);
            String[] headers = {"ID", "Student ID", "First Name", "Last Name", "DOB", "Class", "Score"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setBackgroundColor(new Color(14, 165, 233));
                cell.setPadding(5);
                table.addCell(cell);
            }

            // Process in batches for memory efficiency and incremental flushing
            Font cellFont = new Font(Font.HELVETICA, 9);
            int batchSize = 1000;
            int pageNum = 0;
            Page<Student> page;
            table.setComplete(false);

            do {
                Pageable pageable = PageRequest.of(pageNum, batchSize, Sort.by("id").ascending());

                if (hasSearch && hasClass) {
                    page = studentRepository.findByStudentIdContainingIgnoreCaseAndStudentClass(search, studentClass, pageable);
                } else if (hasSearch) {
                    page = studentRepository.findByStudentIdContainingIgnoreCase(search, pageable);
                } else if (hasClass) {
                    page = studentRepository.findByStudentClass(studentClass, pageable);
                } else {
                    page = studentRepository.findAll(pageable);
                }

                for (Student s : page.getContent()) {
                    table.addCell(new Phrase(String.valueOf(s.getId()), cellFont));
                    table.addCell(new Phrase(s.getStudentId(), cellFont));
                    table.addCell(new Phrase(s.getFirstName(), cellFont));
                    table.addCell(new Phrase(s.getLastName(), cellFont));
                    table.addCell(new Phrase(s.getDob(), cellFont));
                    table.addCell(new Phrase(s.getStudentClass(), cellFont));
                    table.addCell(new Phrase(String.valueOf(s.getScore()), cellFont));
                }

                document.add(table);
                pageNum++;
            } while (page.hasNext());

            table.setComplete(true);
            document.add(table);
            document.close();
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
