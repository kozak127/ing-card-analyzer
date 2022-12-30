package com.github.kozak127.ingcardanalyzer;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class App {
    public static void main(String[] args) {
        try {
            // load properties
            Properties prop = new Properties();
            FileInputStream xmlFile = new FileInputStream(args[0]);
            prop.loadFromXML(xmlFile);

            // load input file
            File input = new File(args[1]);
            PDDocument document = PDDocument.load(input);
            List<PDPage> pages = document.getDocumentCatalog().getAllPages();

            // parse file
            DocReader docReader = new DocReader();
            Map<String, BigDecimal> totals = docReader.parseDoc(pages, prop);

            // print results
            System.out.println("TOTAL:");
            totals.entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByValue())
                    .forEach(entry -> System.out.println(entry.getKey() + ";" + entry.getValue()));

            // close document
            if (document != null) {
                document.close();
            }
        } catch (Exception e) { // XD
            e.printStackTrace();
        }
    }
}
