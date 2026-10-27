package csd201.sort;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Ghi kết quả thực nghiệm ra file CSV tuân thủ tuyệt đối Bảng 4 của
 * "Quy định chung đồ án nhóm CSD201 FA26" (Mã đề 261014).
 *
 * Các cột chuẩn:
 * run_id, rq, algorithm, variant, dataset, n, params, seed, input_sha256,
 * generator_version, repeat, elapsed_ms, heap_before_bytes, heap_peak_bytes,
 * ops_count, max_depth, output_check, status, xmx, machine_id, git_commit
 */
public final class CsvWriter {

    private CsvWriter() {
    }

    public static final String HEADER =
            "run_id,rq,algorithm,variant,dataset,n,params,seed,input_sha256,"
            + "generator_version,repeat,elapsed_ms,heap_before_bytes,heap_peak_bytes,"
            + "ops_count,max_depth,output_check,status,xmx,machine_id,git_commit";

    /** Chỉ ghi header khi file chưa có hoặc đang rỗng, để chạy nhiều lần vẫn ghi nối tiếp. */
    public static void ensureHeader(File file) throws IOException {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        if (file.exists() && file.length() > 0) {
            return;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, false))) {
            writer.write(HEADER);
            writer.newLine();
        }
    }

    public static void appendRow(
            File file,
            String runId,
            String rq,
            String algorithm,
            String variant,
            String dataset,
            int n,
            String params,
            long seed,
            String inputSha256,
            String generatorVersion,
            int repeat,
            String elapsedMs,
            String heapBeforeBytes,
            String heapPeakBytes,
            String opsCount,
            String maxDepth,
            String outputCheck,
            String status,
            String xmx,
            String machineId,
            String gitCommit) throws IOException {

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            writer.write(
                    escapeCsv(runId) + ","
                    + escapeCsv(rq) + ","
                    + escapeCsv(algorithm) + ","
                    + escapeCsv(variant) + ","
                    + escapeCsv(dataset) + ","
                    + n + ","
                    + escapeCsv(params) + ","
                    + seed + ","
                    + escapeCsv(inputSha256) + ","
                    + escapeCsv(generatorVersion) + ","
                    + repeat + ","
                    + escapeCsv(elapsedMs) + ","
                    + escapeCsv(heapBeforeBytes) + ","
                    + escapeCsv(heapPeakBytes) + ","
                    + escapeCsv(opsCount) + ","
                    + escapeCsv(maxDepth) + ","
                    + escapeCsv(outputCheck) + ","
                    + escapeCsv(status) + ","
                    + escapeCsv(xmx) + ","
                    + escapeCsv(machineId) + ","
                    + escapeCsv(gitCommit)
            );
            writer.newLine();
        }
    }

    private static String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}