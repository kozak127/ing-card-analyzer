package com.github.kozak127.ingcardanalyzer;

import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.util.PDFTextStripperByArea;

import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

class DocReader {

    public Map<String, BigDecimal> parseDoc(List<PDPage> pages, Properties properties) throws IOException {
        List<Rectangle2D> areaList = createAreaList(properties);
        PDFTextStripperByArea stripper = createStripper(areaList);
        return parsePages(stripper, pages);
    }

    private List<Rectangle2D> createAreaList(Properties properties) {
        List<Rectangle2D> areaList = new ArrayList<>();
        int number = Integer.parseInt(properties.getProperty("area.number"));
        for (int i = 0; i < number; i++) {
            areaList.add(createArea(properties, i));
        }
        return areaList;
    }

    private Rectangle2D createArea(Properties properties, Integer number) {
        String queryBase = "area.";
        queryBase += number;
        int x0 = Integer.parseInt(properties.getProperty(queryBase + ".x0"));
        int x1 = Integer.parseInt(properties.getProperty(queryBase + ".x1"));
        int y0 = Integer.parseInt(properties.getProperty(queryBase + ".y0"));
        int y1 = Integer.parseInt(properties.getProperty(queryBase + ".y1"));

        return new Rectangle2D.Float(x0, y0, x1 - x0, y1 - y0);
    }

    private PDFTextStripperByArea createStripper(List<Rectangle2D> areaList) throws IOException {
        PDFTextStripperByArea stripper = new PDFTextStripperByArea();
        for (int i = 0; i < areaList.size(); i++) {
            stripper.addRegion(String.valueOf(i), areaList.get(i));
        }
        return stripper;
    }

    private Map<String, BigDecimal> parsePages(PDFTextStripperByArea stripper, List<PDPage> pages) throws IOException {
        Map<String, BigDecimal> totals = new HashMap<>();

        for (int i = 0; i < pages.size(); i++) {
            System.out.println("Parsing page " + i);
            parsePage(stripper, totals, pages.get(i));
        }
        return totals;
    }

    private void parsePage(PDFTextStripperByArea stripper, Map<String, BigDecimal> totals, PDPage page) throws IOException {
        int areaCount = stripper.getRegions().size() - 1;
        stripper.extractRegions(page);
        for (int i = 0; i < areaCount; i = i + 2) { // WARNING: THIS IS NOT YOUR USUAL LOOP -> IT ITERATES i+2
            System.out.println("Parsing regions: " + i + ";" + i + 1);
            String recipient = parseRecipient(stripper, i);
            String transactionStr = parseTransactionString(stripper, i + 1); // THIS IS WHY

            Optional<BigDecimal> transactionValue = convertTransactionValue(recipient, transactionStr);
            if (transactionValue.isEmpty()) continue;

            BigDecimal total = totals.getOrDefault(recipient, BigDecimal.ZERO);
            total = total.add(transactionValue.get());
            totals.put(recipient, total); // adding a duplicate to hashmap removes the old version
        }
    }

    private String parseRecipient(PDFTextStripperByArea stripper, int i) {
        String recipient = stripper.getTextForRegion(String.valueOf(i));
        recipient = recipient.trim();
        recipient = recipient.replace("\n", " ");
        recipient = recipient.replace("\r", "");
        return recipient;
    }

    private String parseTransactionString(PDFTextStripperByArea stripper, int i) {
        String transactionStr = stripper.getTextForRegion(String.valueOf(i));
        transactionStr = transactionStr.replace(" PLN", "");
        transactionStr = transactionStr.replaceAll("\\s+", "");
        transactionStr = transactionStr.replace(",", ".");
        return transactionStr;
    }

    private Optional<BigDecimal> convertTransactionValue(String recipient, String transactionStr) {
        if (transactionStr.isBlank()) {
            System.out.println("Null transaction: " + recipient + " -> " + transactionStr);
            return Optional.empty();
        }
        BigDecimal transactionValue = null;
        try {
            transactionValue = new BigDecimal(transactionStr);
        } catch (NumberFormatException e) {
            System.out.println("Conversion error for: " + transactionStr);
            return Optional.empty();
        }
        return Optional.of(transactionValue);
    }
}
