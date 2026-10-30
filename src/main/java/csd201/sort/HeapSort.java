package csd201.sort;

public final class HeapSort {

    // Khoa khong cho tao doi tuong (Utility Class)
    private HeapSort() {
    }

    // Sap xep toan bo mang (chay toc do cao, khong dem)
    public static void sort(int[] a) {
        if (a == null) {
            throw new IllegalArgumentException("Array must not be null");
        }
        sortRange(a, 0, a.length);
    }

    // Sap xep doan con [lo, hi) - Ham phao cuu sinh fallback cho pdqsort
    public static void sortRange(int[] a, int lo, int hi) {
        validateRange(a, lo, hi);
        int n = hi - lo;
        if (n <= 1) {
            return; // Base case: 0 hoac 1 phan tu thi da co thu tu san
        }

        // Giai doan 1: Xay Max-Heap (cho cac nut cha chim xuong de so to len dinh)
        for (int i = (n >>> 1) - 1; i >= 0; i--) {
            siftDown(a, lo, i, n);
        }

        // Giai doan 2: Doi dinh to nhat ve cuoi, khoa lai, cho dinh moi chim xuong
        for (int end = n - 1; end > 0; end--) {
            int maxVal = a[lo];
            a[lo] = a[lo + end];
            a[lo + end] = maxVal;
            siftDown(a, lo, 0, end);
        }
    }

    // Sap xep toan bo mang va tra ve tong so phep so sanh cho RQ2
    public static long sortWithCount(int[] a) {
        if (a == null) {
            throw new IllegalArgumentException("Array must not be null");
        }
        return sortRangeWithCount(a, 0, a.length);
    }

    // Sap xep doan con [lo, hi) va dem tong so phep so sanh
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

    // Trai tim thuat toan: Cho so nho chim xuong, so to noi len dinh
    private static void siftDown(int[] a, int lo, int root, int len) {
        int val = a[lo + root];
        int parent = root;
        int half = len >>> 1;

        while (parent < half) {
            int child = (parent << 1) + 1;
            int right = child + 1;

            // Chon dua con lon hon giua con trai va con phai
            if (right < len && a[lo + child] < a[lo + right]) {
                child = right;
            }

            // Neu cha da lon hon hoac bang con -> Dung lai ngay (giup D5 chay sieu nhanh)
            if (val >= a[lo + child]) {
                break;
            }

            a[lo + parent] = a[lo + child];
            parent = child;
        }
        a[lo + parent] = val;
    }

    // Cho phan tu chim xuong giong siftDown nhung co gan bo dem cmp++
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

    // Kiem tra an toan chi so [lo, hi) de tranh loi ArrayIndexOutOfBounds
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
