package com.studentdata.service;

import com.opencsv.CSVReader;
import org.postgresql.PGConnection;
import org.postgresql.copy.PGCopyOutputStream;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;

@Service
public class DataUploadService {

    private final JdbcTemplate jdbcTemplate;
    private final NotificationService notificationService;

    public DataUploadService(JdbcTemplate jdbcTemplate, NotificationService notificationService) {
        this.jdbcTemplate = jdbcTemplate;
        this.notificationService = notificationService;
    }

    public int uploadCsvToDatabase(MultipartFile file) throws Exception {
        int totalRecords = 0;

        Connection conn = jdbcTemplate.getDataSource().getConnection();
        try {
            conn.setAutoCommit(false);
            PGConnection pgConn = conn.unwrap(PGConnection.class);
            String copySql = "COPY students (student_id, first_name, last_name, dob, student_class, score) FROM STDIN WITH (FORMAT text)";

            try (OutputStream copyOut = new PGCopyOutputStream(pgConn, copySql);
                 CSVReader csvReader = new CSVReader(new InputStreamReader(file.getInputStream()))) {

                // Skip header
                csvReader.readNext();

                StringBuilder sb = new StringBuilder(128);

                String[] line;
                while ((line = csvReader.readNext()) != null) {
                    if (line.length < 6) continue;

                    int score;
                    try {
                        score = Integer.parseInt(line[5].trim()) + 5;
                    } catch (NumberFormatException e) {
                        continue;
                    }

                    sb.setLength(0);
                    sb.append(escapeCopyValue(line[0].trim())).append('\t');
                    sb.append(escapeCopyValue(line[1].trim())).append('\t');
                    sb.append(escapeCopyValue(line[2].trim())).append('\t');
                    sb.append(escapeCopyValue(line[3].trim())).append('\t');
                    sb.append(escapeCopyValue(line[4].trim())).append('\t');
                    sb.append(score).append('\n');

                    copyOut.write(sb.toString().getBytes(StandardCharsets.UTF_8));
                    totalRecords++;
                }
            }

            conn.commit();
        } catch (Exception e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
            conn.close();
        }

        notificationService.createNotification("UPLOAD", "Uploaded " + totalRecords + " records to database", "Source: " + file.getOriginalFilename());
        return totalRecords;
    }

    private static String escapeCopyValue(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        if (value.indexOf('\\') >= 0 || value.indexOf('\t') >= 0 || value.indexOf('\n') >= 0 || value.indexOf('\r') >= 0) {
            return value.replace("\\", "\\\\")
                        .replace("\t", "\\t")
                        .replace("\n", "\\n")
                        .replace("\r", "\\r");
        }
        return value;
    }
}
