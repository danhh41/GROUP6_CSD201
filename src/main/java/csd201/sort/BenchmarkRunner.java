package csd201.sort;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.lang.management.MemoryUsage;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.ToLongFunction;

/**
 * BenchmarkRunner đáp ứng 100% yêu cầu Mục 6 và Bảng 4 của
 * "Quy định chung đồ án nhóm CSD201 FA26" (Mã đề 261014).
 *
 * Tham số dòng lệnh hỗ trợ:
 *   --algo quick|merge|heap|radix|lib|lib-Arrays.sort
 *   --data D1..D5
 *   --n 1000000
 *   --seed 1
 *   --k 1000
 *   --variant base|noA|noB
 *   --rq RQ1|RQ2|RQ3|TEST
 *   --repeat 1
 *   --xmx 2g|8g
 *   --machine_id <id>
 *   --git_commit <commit>
 *   --out results/benchmark.csv
 *   --count (chỉ chạy lượt đếm thao tác, để trống elapsed_ms)
 *   --timing-only (chỉ chạy lượt đo thời gian, để trống ops_count)
 */
public class BenchmarkRunner {

    private static final String GENERATOR_VERSION = "1.0";

    public static final class Algo {
        final Consumer<int[]> sorter;
        final ToLongFunction<int[]> counter; // null nếu không có bộ đếm

        public Algo(Consumer<int[]> sorter, ToLongFunction<int[]> counter) {
            this.sorter = sorter;
            this.counter = counter;
        }
    }

    // ===== Đăng ký thuật toán =====
    public static final Map<String, Algo> REGISTRY = new LinkedHashMap<>();

    static {
        REGISTRY.put("quick", new Algo(QuickSort::sort, QuickSort::sortWithCount));
        REGISTRY.put("merge", new Algo(MergeSort::sort, MergeSort::sortWithCount));
        REGISTRY.put("heap", new Algo(HeapSort::sort, HeapSort::sortWithCount));
        REGISTRY.put("radix", new Algo(RadixSort::sort, RadixSort::sortWithCount));
        REGISTRY.put("lib-Arrays.sort", new Algo(Arrays::sort, null));
        REGISTRY.put("lib", REGISTRY.get("lib-Arrays.sort")); // alias
    }

    public static void main(String[] args) {
        Map<String, String> o = parse(args);

        String algo = o.get("algo");
        String data = o.getOrDefault("data", "D1");
        int n = Integer.parseInt(o.getOrDefault("n", "1000000"));
        long seed = Long.parseLong(o.getOrDefault("seed", "1"));
        int k = Integer.parseInt(o.getOrDefault("k", "0"));
        String variant = o.getOrDefault("variant", "base");
        String rq = o.getOrDefault("rq", "RQ2");
        int repeat = Integer.parseInt(o.getOrDefault("repeat", "1"));
        String xmx = o.getOrDefault("xmx", detectXmx());
        String machineId = o.getOrDefault("machine_id", detectMachineId());
        String gitCommit = o.getOrDefault("git_commit", "git");
        File csvFile = new File(o.getOrDefault("out", "results/benchmark.csv"));

        boolean isCountOnly = o.containsKey("count");
        boolean isTimingOnly = o.containsKey("timing-only");

        try {
            CsvWriter.ensureHeader(csvFile);

            if (algo != null) {
                runBenchmark(algo, rq, data, n, seed, k, variant, repeat, xmx, machineId, gitCommit,
                        isCountOnly, isTimingOnly, csvFile);
            } else {
                for (String name : REGISTRY.keySet()) {
                    if ("lib".equals(name)) continue; // tránh chạy trùng alias
                    runBenchmark(name, rq, data, n, seed, k, variant, repeat, xmx, machineId, gitCommit,
                            isCountOnly, isTimingOnly, csvFile);
                }
            }

            System.out.println("Ghi ket qua thanh cong vao: " + csvFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Loi ghi file CSV: " + e.getMessage());
        }
    }

    private static void runBenchmark(
            String rawAlgoName,
            String rq,
            String data,
            int n,
            long seed,
            int k,
            String variant,
            int repeat,
            String xmx,
            String machineId,
            String gitCommit,
            boolean isCountOnly,
            boolean isTimingOnly,
            File csvFile) throws IOException {

        // Chuẩn hóa tên thuật toán theo Bảng 2 & Bảng 4 (lib-Arrays.sort)
        String algoName = "lib".equals(rawAlgoName) ? "lib-Arrays.sort" : rawAlgoName;
        Algo algo = REGISTRY.get(rawAlgoName);
        if (algo == null) {
            throw new IllegalArgumentException("Thuat toan chua duoc dang ky: " + rawAlgoName);
        }

        String params = ("D5".equals(data) || k > 0) ? ("k=" + k) : "k=0";

        // 1. Sinh dữ liệu chính thức
        int[] original = DataGenerator.generate(data, n, seed, k);
        String inputSha256 = computeSha256(original);

        // 2. Tạo bản oracle chuẩn bằng Arrays.sort (ngoài vùng đo)
        int[] ref = original.clone();
        Arrays.sort(ref);
        long refHash = hash(ref);
        ref = null; // Giải phóng mảng ref

        // =========================================================================
        // LƯỢT 1: ĐO THỜI GIAN VÀ BỘ NHỚ (NẾU KHÔNG BẬT --count)
        // =========================================================================
        if (!isCountOnly) {
            String runIdTime = UUID.randomUUID().toString().substring(0, 8);
            String status = "ok";
            String elapsedMsStr = "";
            String heapBeforeStr = "";
            String heapPeakStr = "";
            String outputCheck = "";

            try {
                // Mục 6.2: Làm nóng JIT 3 lần trên dữ liệu nhỏ (50.000 phần tử)
                warmup(algo);

                int[] a = original.clone();

                // Mục 6.4: Thu gom rác và đặt lại đỉnh heap trước lượt đo
                System.gc();
                try {
                    Thread.sleep(30); // Cho GC ổn định
                } catch (InterruptedException ignored) {}

                resetHeapPeaks();
                long heapBefore = heapBytes(false);

                // Mục 6.3: Đo thời gian nanoTime bao quanh phần xử lý
                long start = System.nanoTime();
                algo.sorter.accept(a);
                long end = System.nanoTime();

                long heapPeak = Math.max(heapBefore, heapBytes(true));
                double elapsedMs = (end - start) / 1_000_000.0;
                elapsedMsStr = String.format(java.util.Locale.US, "%.4f", elapsedMs);
                heapBeforeStr = Long.toString(heapBefore);
                heapPeakStr = Long.toString(heapPeak);

                // Mục 6.3 & Bảng 4: Tính mã băm h = 31 * h + a[i]
                long h = hash(a);
                outputCheck = Long.toString(h);

                // Kiểm tra tính đúng đắn với Oracle
                if (h != refHash || !isSorted(a)) {
                    status = "wrong";
                }
                a = null;
            } catch (OutOfMemoryError e) {
                status = "oom";
            } catch (Throwable t) {
                status = "crash";
                t.printStackTrace();
            }

            // Ghi dòng đo thời gian: ops_count và max_depth ĐỂ TRỐNG (theo đúng Bảng 4)
            CsvWriter.appendRow(
                    csvFile, runIdTime, rq, algoName, variant, data, n, params, seed,
                    inputSha256, GENERATOR_VERSION, repeat,
                    elapsedMsStr, heapBeforeStr, heapPeakStr,
                    "", "", // ops_count và max_depth để trống
                    outputCheck, status, xmx, machineId, gitCommit
            );
            System.out.println("[TIMING] " + algoName + " " + data + " n=" + n + " status=" + status
                    + " elapsed=" + elapsedMsStr + " ms");
        }

        // =========================================================================
        // LƯỢT 2: ĐO SỐ THAO TÁC --count (NẾU CÓ BỘ ĐẾM VÀ KHÔNG BẬT --timing-only)
        // =========================================================================
        if (!isTimingOnly && algo.counter != null) {
            String runIdCount = UUID.randomUUID().toString().substring(0, 8);
            String status = "ok";
            String opsCountStr = "";
            String maxDepthStr = "";
            String outputCheck = "";

            try {
                int[] b = original.clone();
                long rawCount = algo.counter.applyAsLong(b);

                if ("quick".equals(rawAlgoName)) {
                    int depth = QuickSort.unpackDepth(rawCount);
                    long ops = QuickSort.unpackComparisons(rawCount);
                    maxDepthStr = Integer.toString(depth);
                    opsCountStr = Long.toString(ops);
                } else if ("merge".equals(rawAlgoName)) {
                    int depth = MergeSort.unpackDepth(rawCount);
                    long ops = MergeSort.unpackComparisons(rawCount);
                    maxDepthStr = Integer.toString(depth);
                    opsCountStr = Long.toString(ops);
                } else {
                    opsCountStr = Long.toString(rawCount);
                }

                long h = hash(b);
                outputCheck = Long.toString(h);
                if (h != refHash || !isSorted(b)) {
                    status = "wrong";
                }
                b = null;
            } catch (OutOfMemoryError e) {
                status = "oom";
            } catch (Throwable t) {
                status = "crash";
                t.printStackTrace();
            }

            // Ghi dòng --count: elapsed_ms ĐỂ TRỐNG (theo đúng Bảng 4)
            CsvWriter.appendRow(
                    csvFile, runIdCount, rq, algoName, variant, data, n, params, seed,
                    inputSha256, GENERATOR_VERSION, repeat,
                    "", "", "", // elapsed_ms, heapBefore, heapPeak để trống
                    opsCountStr, maxDepthStr,
                    outputCheck, status, xmx, machineId, gitCommit
            );
            System.out.println("[COUNT]  " + algoName + " " + data + " n=" + n + " status=" + status
                    + " ops=" + opsCountStr + (maxDepthStr.isEmpty() ? "" : (" depth=" + maxDepthStr)));
        }
    }

    /**
     * Mục 6.2: Chạy làm nóng JIT 3 lần trên mảng nhỏ 50.000 phần tử để JIT biên dịch.
     */
    private static void warmup(Algo algo) {
        int warmupN = 50_000;
        int[] warmupData = DataGenerator.generateD1(warmupN, 999999L);
        for (int i = 0; i < 3; i++) {
            int[] temp = warmupData.clone();
            algo.sorter.accept(temp);
        }
    }

    /**
     * Mục 6.8: Tính SHA-256 trên dãy byte biểu diễn mảng int[] theo thứ tự Big-Endian.
     */
    public static String computeSha256(int[] a) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] buf = new byte[8192];
            int idx = 0;
            for (int val : a) {
                buf[idx++] = (byte) (val >>> 24);
                buf[idx++] = (byte) (val >>> 16);
                buf[idx++] = (byte) (val >>> 8);
                buf[idx++] = (byte) val;
                if (idx == buf.length) {
                    md.update(buf, 0, idx);
                    idx = 0;
                }
            }
            if (idx > 0) {
                md.update(buf, 0, idx);
            }
            byte[] digest = md.digest();
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 khong kha dung", e);
        }
    }

    /**
     * Tổng kiểm tra đầu ra: h = 31 * h + a[i], kiểu long, cho phép tràn.
     */
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

    /**
     * Mục 6.4: Đo lượng heap đang sử dụng hoặc đỉnh heap qua MemoryPoolMXBean (Java 8+).
     */
    private static long heapBytes(boolean peak) {
        long sum = 0;
        for (MemoryPoolMXBean p : ManagementFactory.getMemoryPoolMXBeans()) {
            if (p.getType() != MemoryType.HEAP) continue;
            MemoryUsage u = peak ? p.getPeakUsage() : p.getUsage();
            if (u != null) sum += u.getUsed();
        }
        return sum;
    }

    /**
     * Mục 6.4: Đặt lại giá trị đỉnh của mọi vùng heap.
     */
    private static void resetHeapPeaks() {
        for (MemoryPoolMXBean p : ManagementFactory.getMemoryPoolMXBeans()) {
            if (p.getType() == MemoryType.HEAP) {
                p.resetPeakUsage();
            }
        }
    }

    private static String detectXmx() {
        long maxBytes = Runtime.getRuntime().maxMemory();
        long maxGb = Math.round(maxBytes / (1024.0 * 1024.0 * 1024.0));
        return (maxGb > 0 ? maxGb : 2) + "g";
    }

    private static String detectMachineId() {
        String name = System.getenv("COMPUTERNAME");
        if (name == null || name.isEmpty()) {
            name = System.getenv("HOSTNAME");
        }
        return (name != null && !name.isEmpty()) ? name : "PC-DEFAULT";
    }

    private static Map<String, String> parse(String[] args) {
        Map<String, String> m = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("--")) {
                String key = args[i].substring(2);
                if (i + 1 < args.length && !args[i + 1].startsWith("--")) {
                    m.put(key, args[++i]);
                } else {
                    m.put(key, "true");
                }
            }
        }
        return m;
    }
}