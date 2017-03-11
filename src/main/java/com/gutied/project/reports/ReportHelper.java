package com.gutied.project.reports;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.SortedMap;

public class ReportHelper {

    private static final Logger LOG = LoggerFactory.getLogger(ReportHelper.class);

    private static void writeEntities(SortedMap<String, Long> entitiesHistogram, BufferedWriter writer, String type) {
        entitiesHistogram.keySet().iterator().forEachRemaining(key -> {
            try {
                writer.write(key + ", " + entitiesHistogram.get(key) + ", " + type + "\n");
                LOG.info("Writing {} with {} {}", key, entitiesHistogram.get(key), type);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public static void writeResultsToFile(String filename, SortedMap<String, Long> positiveEntitiesHistogram,
                                          SortedMap<String, Long> negativeEntitiesHistogram) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(filename), StandardCharsets.UTF_16)) {
            writer.write("Entity, Occurrences, Quote sentiment\n");
            writeEntities(positiveEntitiesHistogram, writer, "positive");
            writeEntities(negativeEntitiesHistogram, writer, "negative");
        }
    }


    public static void writeResultsToFileOcurrences(String filename, SortedMap<String, Long> positiveEntitiesHistogram,
                                                    SortedMap<String, Long> negativeEntitiesHistogram) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(filename), StandardCharsets.UTF_16)) {
            writer.write("Entity, positive, negative, difference\n");
            positiveEntitiesHistogram.keySet().iterator().forEachRemaining(key -> {
                Long positive = positiveEntitiesHistogram.get(key) == null ? 0l : positiveEntitiesHistogram.get(key);
                Long negative = negativeEntitiesHistogram.get(key) == null ? 0l : negativeEntitiesHistogram.get(key);
                long diff = positive - negative;
                negativeEntitiesHistogram.remove(key);
                try {
                    writer.write(key + ", " + positive + ", " + negative + ", " + diff + "\n");
                    LOG.info("Writing {} with {} positives,  {} negatives and a difference of {}", key, positive, negative, diff);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            negativeEntitiesHistogram.keySet().iterator().forEachRemaining(key -> {
                Long positive = positiveEntitiesHistogram.get(key) == null ? 0l : positiveEntitiesHistogram.get(key);
                Long negative = negativeEntitiesHistogram.get(key) == null ? 0l : negativeEntitiesHistogram.get(key);
                long diff = Math.abs(negative - positive);
                try {
                    writer.write(key + ", " + positive + ", " + negative + ", " + diff + "\n");
                    LOG.info("Writing {} with {} positives,  {} negatives and a difference of {}", key, positive, negative, diff);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }


    static public String[] normalizeEntities(String string) {
        string = string.toLowerCase().trim();
        string = string.replaceAll(",", " ");
        string = string.replaceAll("\\.", " ");
        string = string.replaceAll("-", " ");
        string = string.replaceAll("/", " ");
        string = string.replaceAll("!", " ");
        string = string.replaceAll("\\+", " ");
        string = string.replaceAll("\\(", " ");
        string = string.replaceAll("\\)", " ");
        string = string.replaceAll("\\)", " ");
        string = string.replaceAll("\\*", " ");
        string = string.replaceAll("&", " ");
        string = string.replaceAll("£", " ");
        string = string.replaceAll("$", " ");
        string = string.replaceAll("‘", " ");
        string = string.replaceAll("'", " ");
        return string.split(" ");
    }
}
