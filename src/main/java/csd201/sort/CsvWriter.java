package csd201.sort;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public final class CsvWriter {

    private CsvWriter() {
    }

    private static final String HEADER =
            "algorithm,data,n,seed,k,elapsed_ms,heap_before_bytes,"
            + "heap_peak_bytes,max_depth,variant,"
            + "output_check,operation_count,status";

    /** Chỉ ghi header khi file chưa có hoặc đang rỗng, để chạy nhiều lần vẫn ghi nối tiếp. */
    public static void ensureHeader(File file) throws IOException {
        File parent = file.getParentFile();

        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        if (file.exists() && file.length() > 0) {
            return;
        }

        try (BufferedWriter writer =
                new BufferedWriter(new FileWriter(file, false))) {
            writer.write(HEADER);
            writer.newLine();
        }
    }

    public static void appendRow(
            File file,
            String algorithm,
            String data,
            int n,
            long seed,
            int k,
            double elapsedMs,
            long heapBeforeBytes,
            long heapPeakBytes,
            int maxDepth,
            String variant,
            String outputCheck,
            long operationCount,
            String status) throws IOException {

        try (BufferedWriter writer =
                new BufferedWriter(new FileWriter(file, true))) {

            writer.write(escapeCsv(algorithm) + ","
                    + escapeCsv(data) + ","
                    + n + ","
                    + seed + ","
                    + k + ","
                    + elapsedMs + ","
                    + heapBeforeBytes + ","
                    + heapPeakBytes + ","
                    + maxDepth + ","
                    + escapeCsv(variant) + ","
                    + escapeCsv(outputCheck) + ","
                    + operationCount + ","
                    + escapeCsv(status));

            writer.newLine();
        }
    }

    private static String escapeCsv(String value) {
        if (value == null) {
            return "";
        }

        if (value.contains(",")
                || value.contains("\"")
                || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }

        return value;
    }
}