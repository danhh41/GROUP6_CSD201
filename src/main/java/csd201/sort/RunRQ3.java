package csd201.sort;

import java.io.*;
import java.util.*;

/**
 * Standalone RQ3 data collection program.
 * Runs pdq (full/noA/noB) + quick + lib-Arrays.sort on all RQ2 datasets at N=10^6 and 10^7.
 * Outputs CSV to results/rq3_data.csv
 */
public class RunRQ3 {
    static final int WARMUP = 3;
    static final int REPS = 3;

    public static void main(String[] args) throws Exception {
        int[] sizes = {1_000_000, 10_000_000};
        String[][] datasets = {
            {"D1", "0"}, {"D2", "0"}, {"D3", "0"}, {"D4", "0"},
            {"D5", "1000"}, {"D5", "10"}, {"D5", "2"}
        };
        String[] variants = {"base", "noA", "noB"};

        File outFile = new File("results/rq3_data.csv");
        try (PrintWriter w = new PrintWriter(new FileWriter(outFile, false))) {
            w.println("algorithm,variant,dataset,n,k,seed,elapsed_ms,comparisons,max_depth,status");

            // Warmup JIT
            System.out.println("Warming up JIT...");
            for (int i = 0; i < 5; i++) {
                int[] tmp = DataGenerator.generateD1(50000, 100 + i);
                PdqSort.sort(tmp);
                QuickSort.sort(tmp.clone());
                Arrays.sort(tmp.clone());
            }
            System.out.println("Warmup complete.");

            for (int n : sizes) {
                for (String[] ds : datasets) {
                    String dataName = ds[0];
                    int k = Integer.parseInt(ds[1]);
                    String dsLabel = k > 0 ? dataName + "(K=" + k + ")" : dataName;

                    System.out.println("=== N=" + n + " " + dsLabel + " ===");

                    // Use 3 seeds for D1, D3, D5; 1 seed for D2, D4
                    boolean multiSeed = dataName.equals("D1") || dataName.equals("D3") || dataName.equals("D5");
                    int seedCount = multiSeed ? 3 : 1;

                    for (int seed = 1; seed <= seedCount; seed++) {
                        int[] base = DataGenerator.generate(dataName, n, seed, k);

                        // Quick Sort
                        runAndWrite(w, "quick", "base", dataName, n, k, seed, base);

                        // pdq variants
                        for (String variant : variants) {
                            runAndWrite(w, "pdq", variant, dataName, n, k, seed, base);
                        }

                        // lib-Arrays.sort
                        runAndWriteLib(w, dataName, n, k, seed, base);
                    }
                }
            }
        }
        System.out.println("Done! Output: " + outFile.getAbsolutePath());
    }

    static void runAndWrite(PrintWriter w, String algo, String variant, String data,
                            int n, int k, int seed, int[] base) {
        // Timing runs
        long[] times = new long[REPS];
        String status = "ok";
        for (int rep = 0; rep < REPS; rep++) {
            int[] a = base.clone();
            System.gc();
            try {
                long t0 = System.nanoTime();
                if ("quick".equals(algo)) {
                    QuickSort.sort(a);
                } else {
                    PdqSort.sort(a, variant);
                }
                times[rep] = System.nanoTime() - t0;
                if (!isSorted(a)) status = "wrong";
            } catch (StackOverflowError e) {
                status = "crash";
                times[rep] = Long.MAX_VALUE;
            } catch (Throwable t) {
                status = "crash";
                times[rep] = Long.MAX_VALUE;
            }
        }
        Arrays.sort(times);
        double medianMs = times[1] / 1e6;

        // Count run
        long cmp = 0;
        int depth = 0;
        try {
            int[] c = base.clone();
            long packed;
            if ("quick".equals(algo)) {
                packed = QuickSort.sortWithCount(c);
                cmp = QuickSort.unpackComparisons(packed);
                depth = QuickSort.unpackDepth(packed);
            } else {
                packed = PdqSort.sortWithCount(c, variant);
                cmp = PdqSort.unpackComparisons(packed);
                depth = PdqSort.unpackDepth(packed);
            }
        } catch (Throwable ignored) {}

        String line = String.format(Locale.ROOT, "%s,%s,%s,%d,%d,%d,%.4f,%d,%d,%s",
                algo, variant, data, n, k, seed, medianMs, cmp, depth, status);
        w.println(line);
        w.flush();
        System.out.println(line);
    }

    static void runAndWriteLib(PrintWriter w, String data, int n, int k, int seed, int[] base) {
        long[] times = new long[REPS];
        for (int rep = 0; rep < REPS; rep++) {
            int[] a = base.clone();
            System.gc();
            long t0 = System.nanoTime();
            Arrays.sort(a);
            times[rep] = System.nanoTime() - t0;
        }
        Arrays.sort(times);
        double medianMs = times[1] / 1e6;
        String line = String.format(Locale.ROOT, "%s,%s,%s,%d,%d,%d,%.4f,%d,%d,%s",
                "lib", "base", data, n, k, seed, medianMs, 0, 0, "ok");
        w.println(line);
        w.flush();
        System.out.println(line);
    }

    static boolean isSorted(int[] a) {
        for (int i = 1; i < a.length; i++) {
            if (a[i - 1] > a[i]) return false;
        }
        return true;
    }
}
