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
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.ToLongFunction;

/**
 * Tham số: --algo heap|radix|quick|merge|lib --data D1..D5 --n 1000000 --seed 1
 * --k 1000 --variant default --timeout 600 --count --out results/benchmark.csv
 * Không truyền --algo thì chạy tất cả thuật toán đã đăng ký. Khi chạy hàng
 * loạt, mỗi tổ hợp nên là một tiến trình JVM riêng (truyền --algo).
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

    // ===== Đăng ký thuật toán =====
    private static final Map<String, Algo> REGISTRY = new LinkedHashMap<>();

    static {
        REGISTRY.put("heap", new Algo(HeapSort::sort, HeapSort::sortWithCount));
        REGISTRY.put("radix", new Algo(RadixSort::sort, RadixSort::sortWithCount));
        REGISTRY.put("lib", new Algo(Arrays::sort, null));
        REGISTRY.put("quick", new Algo(QuickSort::sort, QuickSort::sortWithCount));
        REGISTRY.put("merge", new Algo(MergeSort::sort, MergeSort::sortWithCount));
        // REGISTRY.put("pdq",   new Algo(PdqSort::sort,   PdqSort::sortWithCount));
    }

    // Giới hạn thời gian cho mỗi lượt chạy (mili giây). Đổi bằng --timeout (giây).
    private static long timeoutMs = 600_000;

    public static void main(String[] args) {
        Map<String, String> o = parse(args);

        String algo = o.get("algo");
        String data = o.getOrDefault("data", "D1");
        int n = Integer.parseInt(o.getOrDefault("n", "1000000"));
        long seed = Long.parseLong(o.getOrDefault("seed", "1"));
        int k = Integer.parseInt(o.getOrDefault("k", "1000"));
        String variant = o.getOrDefault("variant", "default");
        File csvFile = new File(o.getOrDefault("out", "results/benchmark.csv"));
        boolean countOnly = Boolean.parseBoolean(
                o.getOrDefault("count", "false")
        );
        timeoutMs = Long.parseLong(o.getOrDefault("timeout", "600")) * 1000;

        try {
            CsvWriter.ensureHeader(csvFile);

            if (algo != null) {
                runOne(algo, data, n, seed, k, variant, csvFile, countOnly);
            } else {
                for (String name : REGISTRY.keySet()) {
                    runOne(name, data, n, seed, k, variant, csvFile, countOnly);
                }
            }

            System.out.println("Da ghi ket qua vao: " + csvFile.getAbsolutePath());
        } catch (IOException e) {
            System.out.println("Loi ghi CSV: " + e.getMessage());
        }
    }

    /**
     * Chạy task trong luồng riêng. Trả về true nếu xong kịp, false nếu quá hạn.
     */
    private static boolean runWithTimeout(Runnable task, long limitMs,
            AtomicReference<Throwable> error) {
        Thread worker = new Thread(() -> {
            try {
                task.run();
            } catch (Throwable ex) {
                error.set(ex);
            }
        }, "sort-worker");
        worker.setDaemon(true);
        worker.start();
        try {
            worker.join(limitMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return !worker.isAlive();
    }

    /**
     * Ném lại lỗi bắt được từ luồng phụ để catch ở runOne xử lý (oom/crash).
     */
    private static void rethrow(Throwable ex) {
        if (ex instanceof Error) {
            throw (Error) ex;
        }
        if (ex instanceof RuntimeException) {
            throw (RuntimeException) ex;
        }
        throw new RuntimeException(ex);
    }

    private static void runOne(String name, String data, int n, long seed,
            int k, String variant, File csvFile,
            boolean countOnly) throws IOException {

        String status = "true";
        double elapsedMs = -1;
        long heapBefore = -1;
        long heapPeak = -1;
        String outputCheck = "";
        long operationCount = -1;
        int maxDepth = -1;
        boolean timedOut = false;

        try {
            Algo algo = REGISTRY.get(name);
            if (algo == null) {
                throw new IllegalArgumentException(
                        "algo chua dang ky: " + name);
            }

            int[] a = DataGenerator.generate(data, n, seed, k);
            AtomicReference<Throwable> err = new AtomicReference<>();

            if (countOnly) {
                // Chi dem thao tac, khong do thoi gian
                if (algo.counter == null) {
                    status = "unsupported_count";
                } else {
                    // Tao ket qua chuan TRUOC khi dem, vi bo dem sap xep a tai cho
                    int[] expected = a.clone();
                    Arrays.sort(expected);

                    long[] raw = new long[1];
                    boolean done = runWithTimeout(() -> {
                        raw[0] = algo.counter.applyAsLong(a);
                    }, timeoutMs, err);

                    if (!done) {
                        status = "timeout";
                        timedOut = true;
                    } else {
                        if (err.get() != null) {
                            rethrow(err.get());
                        }
                        long rawCount = raw[0];

                        if ("quick".equals(name)) {
                            maxDepth = QuickSort.unpackDepth(rawCount);
                            operationCount = QuickSort.unpackComparisons(rawCount);
                        } else if ("merge".equals(name)) {
                            maxDepth = MergeSort.unpackDepth(rawCount);
                            operationCount = MergeSort.unpackComparisons(rawCount);
                        } else {
                            operationCount = rawCount;
                        }

                        outputCheck = Long.toString(hash(a));

                        if (!Arrays.equals(a, expected)) {
                            status = "false";
                        }
                    }
                }
            } else {
                // Chi do thoi gian, khong chay bo dem thao tac
                System.gc();
                heapBefore = usedMemory();
                resetPeaks();

                long[] elapsedNs = new long[1];
                boolean done = runWithTimeout(() -> {
                    long t0 = System.nanoTime();
                    algo.sorter.accept(a);
                    elapsedNs[0] = System.nanoTime() - t0;
                }, timeoutMs, err);

                if (!done) {
                    status = "timeout";
                    timedOut = true;
                    heapBefore = -1;      // so do bo nho khong con y nghia
                } else {
                    if (err.get() != null) {
                        rethrow(err.get());
                    }
                    elapsedMs = elapsedNs[0] / 1_000_000.0;
                    heapPeak = Math.max(heapBefore, readPeaks());

                    // Kiem tra ket qua ngoai vung do thoi gian
                    int[] expected = DataGenerator.generate(data, n, seed, k);
                    Arrays.sort(expected);

                    outputCheck = Long.toString(hash(a));

                    if (!Arrays.equals(a, expected)) {
                        status = "false";
                    }
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
                elapsedMs, heapBefore, heapPeak,
                maxDepth, variant, outputCheck,
                operationCount, status
        );

        System.out.println(
                name + " " + data + " n=" + n
                + (countOnly ? " [COUNT]" : " [TIMING]")
                + ": " + status);

        if (timedOut) {
            System.out.println("Qua han " + (timeoutMs / 1000)
                    + " giay, thoat JVM de dung luong sap xep.");
            Runtime.getRuntime().halt(0);
        }
    }

    // h = 31*h + a[i], kiểu long, cho phép tràn
    private static long hash(int[] a) {
        long h = 0;
        for (int x : a) {
            h = 31 * h + x;
        }
        return h;
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
            if (args[i].equals("--count")) {
                m.put("count", "true");          // cờ, không có giá trị đi kèm
            } else if (args[i].startsWith("--") && i + 1 < args.length) {
                m.put(args[i].substring(2), args[++i]);
            }
        }
        return m;
    }
}
