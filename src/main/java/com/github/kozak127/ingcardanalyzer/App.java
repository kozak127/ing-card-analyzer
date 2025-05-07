package com.github.kozak127.ingcardanalyzer;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

import java.io.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class App {
    public static void main(String[] args) {
        try {
            // load properties
            Properties summaryProperties = new Properties();
            FileInputStream xmlSummaryProperties = new FileInputStream(args[0]);
            summaryProperties.loadFromXML(xmlSummaryProperties);

            Properties groupProperties = new Properties();
            FileInputStream xmlGroupProperties = new FileInputStream("group.xml");
            groupProperties.loadFromXML(xmlGroupProperties);

            // load input file
            File input = new File(args[1]);
            PDDocument document = PDDocument.load(input);
            List<PDPage> pages = document.getDocumentCatalog().getAllPages();

            // parse file
            DocReader docReader = new DocReader();
            Map<String, BigDecimal> totals = docReader.parseDoc(pages, summaryProperties, groupProperties);

            // create output
            Writer fileWriter = new FileWriter("summary.csv", false);

            // sort & print results
            System.out.println("TOTAL:");
            totals.entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByValue())
                    .peek(entry -> System.out.println(entry.getKey() + ";" + entry.getValue()))
                    .forEach(entry -> writeLine(fileWriter, entry));

            // close documents
            document.close();
            fileWriter.close();

        } catch (Exception e) { // XD
            e.printStackTrace();
        }
    }

    private static void writeLine(Writer fileWriter, Map.Entry<String, BigDecimal> entry) {
        try {
            fileWriter.write(entry.getKey() + ";" + entry.getValue() + System.lineSeparator());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
