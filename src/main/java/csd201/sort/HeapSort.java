package csd201.sort;

/**
 * Cài đặt thuật toán Heap Sort theo yêu cầu đề tài CSD201 FA26 (Mã đề 261014).
 *
 * - Tên trong CSV: heap
 * - Thuật toán: Max-heap tại chỗ (in-place), dựng heap theo phương pháp bottom-up.
 * - Sắp xếp tăng dần trên mảng int[] với khóa không âm từ 0 đến 2147483647.
 * - Cung cấp hàm sortRange(int[] a, int lo, int hi) cho đoạn nửa mở [lo, hi)
 *   để pdqsort (Người 5) gọi làm fallback khi phân hoạch lệch.
 * - Hỗ trợ các hàm *WithCount để đếm số phép so sánh khóa cho lượt --count.
 */
public final class HeapSort {

    private HeapSort() {
        // Lớp tiện ích không khởi tạo
    }

    /**
     * Sắp xếp tăng dần toàn bộ mảng int[] tại chỗ.
     *
     * @param a mảng cần sắp xếp
     */
    public static void sort(int[] a) {
        if (a == null) {
            throw new IllegalArgumentException("Mảng không được null");
        }
        sortRange(a, 0, a.length);
    }

    /**
     * Sắp xếp tăng dần đoạn nửa mở [lo, hi) trong mảng int[] tại chỗ.
     * Các phần tử ngoài đoạn [lo, hi) được bảo toàn nguyên vẹn.
     *
     * @param a  mảng chứa đoạn cần sắp xếp
     * @param lo chỉ số bắt đầu (bao gồm)
     * @param hi chỉ số kết thúc (không bao gồm)
     */
    public static void sortRange(int[] a, int lo, int hi) {
        validateRange(a, lo, hi);
        int n = hi - lo;
        if (n <= 1) {
            return;
        }

        // Bước 1: Dựng max-heap theo phương pháp bottom-up
        for (int i = (n >>> 1) - 1; i >= 0; i--) {
            siftDown(a, lo, i, n);
        }

        // Bước 2: Lần lượt đưa phần tử lớn nhất về cuối heap và thu hẹp heap
        for (int end = n - 1; end > 0; end--) {
            int maxVal = a[lo];
            a[lo] = a[lo + end];
            a[lo + end] = maxVal;
            siftDown(a, lo, 0, end);
        }
    }

    /**
     * Sắp xếp tăng dần toàn bộ mảng và đếm số phép so sánh khóa.
     *
     * @param a mảng cần sắp xếp
     * @return số phép so sánh khóa giữa các phần tử
     */
    public static long sortWithCount(int[] a) {
        if (a == null) {
            throw new IllegalArgumentException("Mảng không được null");
        }
        return sortRangeWithCount(a, 0, a.length);
    }

    /**
     * Sắp xếp tăng dần đoạn nửa mở [lo, hi) và đếm số phép so sánh khóa.
     *
     * @param a  mảng chứa đoạn cần sắp xếp
     * @param lo chỉ số bắt đầu (bao gồm)
     * @param hi chỉ số kết thúc (không bao gồm)
     * @return số phép so sánh khóa giữa các phần tử
     */
    public static long sortRangeWithCount(int[] a, int lo, int hi) {
        validateRange(a, lo, hi);
        int n = hi - lo;
        if (n <= 1) {
            return 0L;
        }

        long comparisons = 0L;

        // Bước 1: Dựng max-heap bottom-up kèm đếm so sánh
        for (int i = (n >>> 1) - 1; i >= 0; i--) {
            comparisons += siftDownWithCount(a, lo, i, n);
        }

        // Bước 2: Trích xuất phần tử lớn nhất và heapify lại kèm đếm so sánh
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
            int child = (parent << 1) + 1; // Con trái
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
                cmp++; // So sánh giữa hai nút con: a[lo + child] < a[lo + right]
                if (a[lo + child] < a[lo + right]) {
                    child = right;
                }
            }

            cmp++; // So sánh giữa giá trị đang hạ (val) và nút con lớn hơn
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
            throw new IllegalArgumentException("Mảng không được null");
        }
        if (lo < 0 || hi > a.length || lo > hi) {
            throw new IndexOutOfBoundsException(
                "Đoạn không hợp lệ: [lo=" + lo + ", hi=" + hi + "), độ dài mảng=" + a.length
            );
        }
    }
}
