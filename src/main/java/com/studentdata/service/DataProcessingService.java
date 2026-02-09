package com.studentdata.service;

import com.opencsv.CSVWriter;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class DataProcessingService {

    @Value("${app.output.directory}")
    private String outputDirectory;

    private final NotificationService notificationService;

    public DataProcessingService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public String processExcelToCsv(MultipartFile file) throws Exception {
        Path dirPath = Paths.get(outputDirectory);
        Files.createDirectories(dirPath);

        String csvFilename = file.getOriginalFilename().replace(".xlsx", "") + "_processed.csv";
        Path csvPath = dirPath.resolve(csvFilename);

        // Save uploaded file temporarily
        Path tempFile = Files.createTempFile("upload_", ".xlsx");
        file.transferTo(tempFile.toFile());

        try (OPCPackage pkg = OPCPackage.open(tempFile.toFile());
             CSVWriter csvWriter = new CSVWriter(new BufferedWriter(new FileWriter(csvPath.toFile()), 65536))) {

            ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(pkg);
            XSSFReader reader = new XSSFReader(pkg);
            StylesTable styles = reader.getStylesTable();

            // Write CSV header
            csvWriter.writeNext(new String[]{"Student ID", "First Name", "Last Name", "DOB", "Class", "Score"});

            // Process using SAX
            SheetHandler sheetHandler = new SheetHandler(csvWriter);
            XMLReader xmlReader = createXmlReader(styles, strings, sheetHandler);

            InputStream sheetStream = reader.getSheetsData().next();
            xmlReader.parse(new InputSource(sheetStream));
            sheetStream.close();
        } finally {
            Files.deleteIfExists(tempFile);
        }

        notificationService.createNotification("PROCESSING", "Processed Excel to CSV", "File: " + csvFilename);
        return csvFilename;
    }

    private XMLReader createXmlReader(StylesTable styles, ReadOnlySharedStringsTable strings,
                                       SheetHandler handler) throws Exception {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        SAXParser saxParser = factory.newSAXParser();
        XMLReader xmlReader = saxParser.getXMLReader();
        xmlReader.setContentHandler(new XSSFSheetXMLHandler(styles, strings, handler, false));
        return xmlReader;
    }

    private static class SheetHandler implements XSSFSheetXMLHandler.SheetContentsHandler {
        private final CSVWriter csvWriter;
        private final String[] currentRow = new String[6];
        private boolean isHeaderRow = true;
        private boolean hasData = false;

        SheetHandler(CSVWriter csvWriter) {
            this.csvWriter = csvWriter;
        }

        @Override
        public void startRow(int rowNum) {
            isHeaderRow = (rowNum == 0);
            hasData = false;
            for (int i = 0; i < 6; i++) {
                currentRow[i] = "";
            }
        }

        @Override
        public void endRow(int rowNum) {
            if (isHeaderRow || !hasData) {
                return;
            }

            // Add 10 to score (column index 5)
            try {
                double score = Double.parseDouble(currentRow[5]);
                currentRow[5] = String.valueOf((int) (score + 10));
            } catch (NumberFormatException e) {
                // Keep original value if not a number
            }

            csvWriter.writeNext(currentRow);
        }

        @Override
        public void cell(String cellReference, String formattedValue, XSSFComment comment) {
            int colIdx = 0;
            for (int i = 0; i < cellReference.length(); i++) {
                char c = cellReference.charAt(i);
                if (Character.isLetter(c)) {
                    colIdx = colIdx * 26 + (c - 'A' + 1);
                } else {
                    break;
                }
            }
            colIdx--;

            if (colIdx >= 0 && colIdx < 6) {
                currentRow[colIdx] = formattedValue;
                hasData = true;
            }
        }
    }
}
