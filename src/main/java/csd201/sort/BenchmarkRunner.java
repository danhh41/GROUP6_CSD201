package csd201.sort;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.ToLongFunction;

/**
 * Tham số: --algo heap|radix|lib  --data D1..D5  --n 1000000  --seed 1
 *          --k 1000  --variant default  --out results/benchmark.csv
 * Không truyền --algo thì chạy tất cả thuật toán đã đăng ký.
 * Khi chạy hàng loạt, mỗi tổ hợp nên là một tiến trình JVM riêng (truyền --algo).
 */
public class BenchmarkRunner {

    private static final class Algo {
        final Consumer<int[]> sorter;
        final ToLongFunction<int[]> counter; // null nếu không có

        Algo(Consumer<int[]> sorter, ToLongFunction<int[]> counter) {
            this.sorter = sorter;
            this.counter = counter;
        }
    }

    // ===== Đăng ký thuật toán: thêm quick, merge, pdq khi các bạn bàn giao =====
    private static final Map<String, Algo> REGISTRY = new LinkedHashMap<>();

    static {
        REGISTRY.put("heap", new Algo(HeapSort::sort, HeapSort::sortWithCount));
        REGISTRY.put("radix", new Algo(RadixSort::sort, RadixSort::sortWithCount));
        REGISTRY.put("lib", new Algo(Arrays::sort, null));
        // REGISTRY.put("quick", new Algo(QuickSort::sort, QuickSort::sortWithCount));
        // REGISTRY.put("merge", new Algo(MergeSort::sort, MergeSort::sortWithCount));
        // REGISTRY.put("pdq",   new Algo(PdqSort::sort,   PdqSort::sortWithCount));
    }

    public static void main(String[] args) {
        Map<String, String> o = parse(args);

        String algo = o.get("algo");
        String data = o.getOrDefault("data", "D1");
        int n = Integer.parseInt(o.getOrDefault("n", "1000000"));
        long seed = Long.parseLong(o.getOrDefault("seed", "1"));
        int k = Integer.parseInt(o.getOrDefault("k", "1000"));
        String variant = o.getOrDefault("variant", "default");
        File csvFile = new File(o.getOrDefault("out", "results/benchmark.csv"));

        try {
            CsvWriter.ensureHeader(csvFile);

            if (algo != null) {
                runOne(algo, data, n, seed, k, variant, csvFile);
            } else {
                for (String name : REGISTRY.keySet()) {
                    runOne(name, data, n, seed, k, variant, csvFile);
                }
            }

            System.out.println("Da ghi ket qua vao: " + csvFile.getAbsolutePath());
        } catch (IOException e) {
            System.out.println("Loi ghi CSV: " + e.getMessage());
        }
    }

    private static void runOne(String name, String data, int n, long seed,
                               int k, String variant, File csvFile)
            throws IOException {

        String status = "true";
        double elapsedMs = -1;
        long heapBefore = -1;
        long heapPeak = -1;
        String outputCheck = "";
        long operationCount = -1;

        try {
            Algo algo = REGISTRY.get(name);
            if (algo == null) {
                throw new IllegalArgumentException("algo chua dang ky: " + name);
            }

            int[] a = DataGenerator.generate(data, n, seed, k);

            // Ngoài vùng đo: băm chuẩn từ Arrays.sort, rồi giải phóng bản chuẩn
            int[] ref = a.clone();
            Arrays.sort(ref);
            long refHash = hash(ref);
            ref = null;

            // ---- Lượt đo thời gian (không đếm gì) ----
            System.gc();
            heapBefore = usedMemory();
            resetPeaks();

            long start = System.nanoTime();
            algo.sorter.accept(a);
            long end = System.nanoTime();

            heapPeak = Math.max(heapBefore, readPeaks());
            elapsedMs = (end - start) / 1_000_000.0;

            long h = hash(a);
            outputCheck = Long.toString(h);
            if (h != refHash || !isSorted(a)) {
                status = "false";
            }

            // ---- Lượt --count: chạy riêng, sau khi đã đo xong ----
            if (algo.counter != null) {
                a = null;
                int[] b = DataGenerator.generate(data, n, seed, k);
                operationCount = algo.counter.applyAsLong(b);
                if (hash(b) != refHash || !isSorted(b)) {
                    status = "false";
                }
            }
        } catch (OutOfMemoryError e) {
            status = "oom";
        } catch (Throwable t) {
            status = "crash";
            t.printStackTrace();
        }

        CsvWriter.appendRow(
                csvFile, name, data, n, seed, k,
                elapsedMs,
                heapBefore, heapPeak,
                -1,            // max_depth: heap/radix không đệ quy; xác nhận với nhóm
                variant,
                outputCheck,
                operationCount,
                status
        );

        System.out.println(name + " " + data + " n=" + n + ": " + status);
    }

    // h = 31*h + a[i], kiểu long, cho phép tràn
    private static long hash(int[] a) {
        long h = 0;
        for (int x : a) {
            h = 31 * h + x;
        }
        return h;
    }

    private static boolean isSorted(int[] a) {
        for (int i = 1; i < a.length; i++) {
            if (a[i - 1] > a[i]) {
                return false;
            }
        }
        return true;
    }

    private static long usedMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    private static void resetPeaks() {
        for (MemoryPoolMXBean p : ManagementFactory.getMemoryPoolMXBeans()) {
            if (p.getType() == MemoryType.HEAP) {
                p.resetPeakUsage();
            }
        }
    }

    private static long readPeaks() {
        long sum = 0;
        for (MemoryPoolMXBean p : ManagementFactory.getMemoryPoolMXBeans()) {
            if (p.getType() == MemoryType.HEAP) {
                sum += p.getPeakUsage().getUsed();
            }
        }
        return sum;
    }

    private static Map<String, String> parse(String[] args) {
        Map<String, String> m = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("--") && i + 1 < args.length) {
                m.put(args[i].substring(2), args[++i]);
            }
        }
        return m;
    }
}