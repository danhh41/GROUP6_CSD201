package csd201.sort;

public final class HeapSort {

    private HeapSort() {
    }

    public static void sort(int[] a) {
        if (a == null) {
            throw new IllegalArgumentException("Array must not be null");
        }
        sortRange(a, 0, a.length);
    }

    public static void sortRange(int[] a, int lo, int hi) {
        validateRange(a, lo, hi);
        int n = hi - lo;
        if (n <= 1) {
            return;
        }

        for (int i = (n >>> 1) - 1; i >= 0; i--) {
            siftDown(a, lo, i, n);
        }

        for (int end = n - 1; end > 0; end--) {
            int maxVal = a[lo];
            a[lo] = a[lo + end];
            a[lo + end] = maxVal;
            siftDown(a, lo, 0, end);
        }
    }

    public static long sortWithCount(int[] a) {
        if (a == null) {
            throw new IllegalArgumentException("Array must not be null");
        }
        return sortRangeWithCount(a, 0, a.length);
    }

    public static long sortRangeWithCount(int[] a, int lo, int hi) {
        validateRange(a, lo, hi);
        int n = hi - lo;
        if (n <= 1) {
            return 0L;
        }

        long comparisons = 0L;
        for (int i = (n >>> 1) - 1; i >= 0; i--) {
            comparisons += siftDownWithCount(a, lo, i, n);
        }

        for (int end = n - 1; end > 0; end--) {
            int maxVal = a[lo];
            a[lo] = a[lo + end];
            a[lo + end] = maxVal;
            comparisons += siftDownWithCount(a, lo, 0, end);
        }
        return comparisons;
    }

    private static void siftDown(int[] a, int lo, int root, int len) {
        int val = a[lo + root];
        int parent = root;
        int half = len >>> 1;

        while (parent < half) {
            int child = (parent << 1) + 1;
            int right = child + 1;

            if (right < len && a[lo + child] < a[lo + right]) {
                child = right;
            }

            if (val >= a[lo + child]) {
                break;
            }

            a[lo + parent] = a[lo + child];
            parent = child;
        }
        a[lo + parent] = val;
    }

    private static long siftDownWithCount(int[] a, int lo, int root, int len) {
        long cmp = 0L;
        int val = a[lo + root];
        int parent = root;
        int half = len >>> 1;

        while (parent < half) {
            int child = (parent << 1) + 1;
            int right = child + 1;

            if (right < len) {
                cmp++;
                if (a[lo + child] < a[lo + right]) {
                    child = right;
                }
            }

            cmp++;
            if (val >= a[lo + child]) {
                break;
            }

            a[lo + parent] = a[lo + child];
            parent = child;
        }
        a[lo + parent] = val;
        return cmp;
    }

    private static void validateRange(int[] a, int lo, int hi) {
        if (a == null) {
            throw new IllegalArgumentException("Array must not be null");
        }
        if (lo < 0 || hi > a.length || lo > hi) {
            throw new IndexOutOfBoundsException(
                "Invalid range: [" + lo + ", " + hi + "), length=" + a.length
            );
        }
    }
}
