package csd201.sort;

public final class RadixSort {

    private static final int BITS_PER_BYTE = 8;
    private static final int RADIX = 256;
    private static final int MASK = 0xFF;
    private static final int TOTAL_PASSES = 4;

    private RadixSort() {
    }

    public static void sort(int[] a) {
        if (a == null) {
            throw new IllegalArgumentException("Array must not be null");
        }
        int n = a.length;
        if (n <= 1) {
            return;
        }

        int[] aux = new int[n];
        int[] count = new int[RADIX + 1];

        int[] src = a;
        int[] dst = aux;

        for (int pass = 0; pass < TOTAL_PASSES; pass++) {
            int shift = pass * BITS_PER_BYTE;

            for (int r = 0; r <= RADIX; r++) {
                count[r] = 0;
            }

            for (int i = 0; i < n; i++) {
                int digit = (src[i] >>> shift) & MASK;
                count[digit + 1]++;
            }

            for (int r = 0; r < RADIX; r++) {
                count[r + 1] += count[r];
            }

            for (int i = 0; i < n; i++) {
                int digit = (src[i] >>> shift) & MASK;
                dst[count[digit]++] = src[i];
            }

            int[] tmp = src;
            src = dst;
            dst = tmp;
        }
    }

    public static long sortWithCount(int[] a) {
        if (a == null) {
            throw new IllegalArgumentException("Array must not be null");
        }
        int n = a.length;
        if (n <= 1) {
            return 0L;
        }

        long operations = 0L;
        int[] aux = new int[n];
        int[] count = new int[RADIX + 1];

        int[] src = a;
        int[] dst = aux;

        for (int pass = 0; pass < TOTAL_PASSES; pass++) {
            int shift = pass * BITS_PER_BYTE;

            for (int r = 0; r <= RADIX; r++) {
                count[r] = 0;
            }

            for (int i = 0; i < n; i++) {
                int digit = (src[i] >>> shift) & MASK;
                count[digit + 1]++;
                operations++;
            }

            for (int r = 0; r < RADIX; r++) {
                count[r + 1] += count[r];
            }

            for (int i = 0; i < n; i++) {
                int digit = (src[i] >>> shift) & MASK;
                dst[count[digit]++] = src[i];
                operations += 2L;
            }

            int[] tmp = src;
            src = dst;
            dst = tmp;
        }

        return operations;
    }
}
