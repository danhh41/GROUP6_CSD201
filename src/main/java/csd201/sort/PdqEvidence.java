package csd201.sort;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Locale;

/**
 * Thí nghiệm bằng chứng cho phần lõi pdqsort (Người 5 – Cặp 3), dùng trong tài liệu Methods/Results.
 * KHÔNG thay thế BenchmarkRunner: số liệu chính thức của RQ3 vẫn chạy qua BenchmarkRunner.
 *
 * <pre>
 *   java -cp target/classes csd201.sort.PdqEvidence ablation   -> results/pdq_ablation.csv
 *   java -Xmx3g -cp target/classes csd201.sort.PdqEvidence count -> results/pdq_count.csv
 *   java -Xmx2g -cp target/classes csd201.sort.PdqEvidence jit   -> results/pdq_jit.csv
 * </pre>
 *
 * <ul>
 *   <li>{@code ablation}: tắt lần lượt ninther, phá mẫu, Heap Sort dự phòng (N = 10^5, seed 1)
 *       để chứng minh từng cơ chế của phần lõi có tác dụng.</li>
 *   <li>{@code count}: lượt --count của pdq lõi trên D1–D5, N = 10^6 và 10^7 (3 seed cho D1, D3, D5),
 *       kèm thời gian tham khảo so với Arrays.sort.</li>
 * </ul>
 */
public final class PdqEvidence {

    private PdqEvidence() {
    }

    public static void main(String[] args) throws IOException {
        String mode = args.length > 0 ? args[0] : "ablation";
        if ("count".equals(mode)) {
            runCount(new File(args.length > 1 ? args[1] : "results/pdq_count.csv"));
        } else if ("jit".equals(mode)) {
            runJit(new File(args.length > 1 ? args[1] : "results/pdq_jit.csv"));
        } else {
            runAblation(new File(args.length > 1 ? args[1] : "results/pdq_ablation.csv"));
        }
    }

    // =========================================================================
    // Thí nghiệm tắt cơ chế
    // =========================================================================

    private static void runAblation(File out) throws IOException {
        String[][] configs = {
            {"core", "1", "1", "1"},
            {"no-ninther", "0", "1", "1"},
            {"no-break", "1", "0", "1"},
            {"no-heap", "1", "1", "0"},
        };
        String[] datas = {"D1", "D2", "D4", "D5-2", "organ"};
        int n = 100_000;

        try (PrintWriter w = open(out)) {
            w.println("config,data,n,comparisons,max_depth,bad_partitions,heap_fallbacks,elapsed_ms,correct");
            for (int i = 0; i < 5; i++) {
                PdqSort.sort(DataGenerator.generateD1(n, 100 + i)); // làm nóng JIT
            }
            for (String[] c : configs) {
                PdqSort.ablationNinther = c[1].equals("1");
                PdqSort.ablationBreakPatterns = c[2].equals("1");
                PdqSort.ablationHeapFallback = c[3].equals("1");
                for (String d : datas) {
                    int[] base = make(d, n, 1);
                    int[] expected = base.clone();
                    Arrays.sort(expected);

                    int[] a = base.clone();
                    long t0 = System.nanoTime();
                    PdqSort.sort(a);
                    long t1 = System.nanoTime();

                    int[] b = base.clone();
                    PdqSort p = new PdqSort(true, false, false);
                    p.run(b, 0, b.length);

                    boolean ok = Arrays.equals(a, expected) && Arrays.equals(b, expected);
                    String line = String.format(Locale.ROOT, "%s,%s,%d,%d,%d,%d,%d,%.1f,%s",
                            c[0], d, n, p.getComparisons(), p.getMaxDepth(), p.getBadPartitions(),
                            p.getHeapFallbacks(), (t1 - t0) / 1e6, ok);
                    w.println(line);
                    w.flush();
                    System.out.println(line);
                }
            }
        } finally {
            PdqSort.ablationNinther = true;
            PdqSort.ablationBreakPatterns = true;
            PdqSort.ablationHeapFallback = true;
        }
    }

    // =========================================================================
    // Lượt --count trên D1–D5
    // =========================================================================

    private static void runCount(File out) throws IOException {
        String[] datas = {"D1", "D2", "D3", "D4", "D5-1000", "D5-10", "D5-2"};
        int[] sizes = {1_000_000, 10_000_000};

        try (PrintWriter w = open(out)) {
            w.println("algorithm,variant,data,n,seed,elapsed_ms,lib_ms,comparisons,max_depth,"
                    + "bad_partitions,heap_fallbacks,output_check,correct");
            for (int i = 0; i < 5; i++) {
                PdqSort.sort(DataGenerator.generateD1(1_000_000, 100 + i)); // làm nóng JIT
                Arrays.sort(DataGenerator.generateD1(1_000_000, 200 + i));
            }
            for (int n : sizes) {
                for (String d : datas) {
                    boolean multiSeed = d.equals("D1") || d.equals("D3") || d.startsWith("D5");
                    int seeds = multiSeed ? 3 : 1;
                    for (int seed = 1; seed <= seeds; seed++) {
                        int[] base = make(d, n, seed);
                        int[] expected = base.clone();
                        Arrays.sort(expected);

                        long[] tPdq = new long[3];
                        long[] tLib = new long[3];
                        boolean ok = true;
                        long h = 0;
                        for (int rep = 0; rep < 3; rep++) {
                            int[] a = base.clone();
                            System.gc();
                            long t0 = System.nanoTime();
                            PdqSort.sort(a);
                            tPdq[rep] = System.nanoTime() - t0;
                            ok &= Arrays.equals(a, expected);
                            h = hash(a);

                            int[] b = base.clone();
                            System.gc();
                            long t1 = System.nanoTime();
                            Arrays.sort(b);
                            tLib[rep] = System.nanoTime() - t1;
                        }
                        Arrays.sort(tPdq);
                        Arrays.sort(tLib);

                        int[] c = base.clone();
                        PdqSort p = new PdqSort(true, false, false);
                        p.run(c, 0, c.length);
                        ok &= Arrays.equals(c, expected);

                        String line = String.format(Locale.ROOT,
                                "pdq,core,%s,%d,%d,%.1f,%.1f,%d,%d,%d,%d,%d,%s",
                                d, n, seed, tPdq[1] / 1e6, tLib[1] / 1e6, p.getComparisons(),
                                p.getMaxDepth(), p.getBadPartitions(), p.getHeapFallbacks(), h, ok);
                        w.println(line);
                        w.flush();
                        System.out.println(line);
                    }
                }
            }
        }
    }

    // =========================================================================
    // Thí nghiệm JIT: cùng một JVM, gọi lặp 8 lần trên cùng dữ liệu
    // =========================================================================

    /*
     * Lượt đo của BenchmarkRunner chỉ làm nóng trên D1 (50.000 phần tử). Trên D1, cơ chế A
     * (partialInsertionSort) hầu như không được gọi nên chưa được JIT biên dịch khi đo D2/D3.
     * Thí nghiệm này gọi pdq 8 lần liên tiếp trong cùng JVM để thấy thời gian khi đường A đã nóng.
     * Kết quả: results/pdq_jit.csv (variant,dataset,n,seed,call,elapsed_ms,correct).
     */
    private static void runJit(File out) throws IOException {
        String[] variants = {"base", "noA", "noB"};
        String[] datas = {"D2", "D3", "D5-2"};
        int n = 1_000_000;
        int calls = 8;
        try (PrintWriter w = open(out)) {
            w.println("variant,dataset,n,seed,call,elapsed_ms,correct");
            for (String d : datas) {
                int[] base = make(d, n, 1);
                int[] expected = base.clone();
                Arrays.sort(expected);
                for (String v : variants) {
                    for (int call = 1; call <= calls; call++) {
                        int[] a = base.clone();
                        long t0 = System.nanoTime();
                        PdqSort.sort(a, v);
                        long t1 = System.nanoTime();
                        String line = String.format(Locale.ROOT, "%s,%s,%d,%d,%d,%.4f,%s",
                                v, d, n, 1, call, (t1 - t0) / 1e6, Arrays.equals(a, expected));
                        w.println(line);
                        System.out.println(line);
                    }
                }
            }
        }
    }

    // =========================================================================

    /** Dữ liệu của DataGenerator, thêm "organ" (ống đàn organ: tăng rồi giảm) để thử phá mẫu. */
    private static int[] make(String d, int n, long seed) {
        if ("organ".equals(d)) {
            int[] a = new int[n];
            for (int i = 0; i < n; i++) {
                a[i] = (i < n / 2) ? i : n - i;
            }
            return a;
        }
        return DataGenerator.generate(d, n, seed, 0);
    }

    private static long hash(int[] a) {
        long h = 0;
        for (int x : a) {
            h = 31 * h + x;
        }
        return h;
    }

    private static PrintWriter open(File f) throws IOException {
        File parent = f.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        return new PrintWriter(new FileWriter(f, false));
    }
}
