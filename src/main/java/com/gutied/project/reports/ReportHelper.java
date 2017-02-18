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
}
