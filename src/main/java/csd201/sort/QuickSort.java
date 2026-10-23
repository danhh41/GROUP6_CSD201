package csd201.sort;

/**
 * Cài đặt thuật toán Quick Sort theo yêu cầu đề tài CSD201 FA26 (Mã đề 261014).
 *
 * <ul>
 *   <li>Tên trong CSV: {@code quick}</li>
 *   <li>Phân hoạch: <b>Hoare</b> (hai con trỏ hội tụ vào giữa).</li>
 *   <li>Pivot: <b>trung vị của 3</b> phần tử tại vị trí đầu, giữa và cuối đoạn.</li>
 *   <li>Chiến lược: đệ quy <b>hai phía</b> – không chuyển sang Insertion Sort,
 *       không dùng sơ đồ Lomuto.</li>
 *   <li>Hỗ trợ {@link #sortRange(int[], int, int)} dạng {@code [lo, hi)} để
 *       pdqsort (Người 5) gọi làm fallback.</li>
 *   <li>Trong lượt {@code --count}: {@link #sortWithCount(int[])} trả {@code long}
 *       đóng gói: 32-bit cao = {@code max_depth}, 32-bit thấp = số phép so sánh.
 *       Dùng {@link #unpackDepth(long)} / {@link #unpackComparisons(long)} để giải.</li>
 * </ul>
 */
public final class QuickSort {

    private QuickSort() {
        // Lớp tiện ích không khởi tạo
    }

    // =========================================================================
    // Public API – sắp xếp (không đếm)
    // =========================================================================

    /**
     * Sắp xếp tăng dần toàn bộ mảng {@code int[]} tại chỗ.
     *
     * @param a mảng cần sắp xếp (không được {@code null})
     */
    public static void sort(int[] a) {
        if (a == null) {
            throw new IllegalArgumentException("Mảng không được null");
        }
        sortRange(a, 0, a.length);
    }

    /**
     * Sắp xếp tăng dần đoạn nửa mở {@code [lo, hi)} trong mảng {@code int[]} tại chỗ.
     * Các phần tử ngoài đoạn được bảo toàn nguyên vẹn.
     *
     * @param a  mảng chứa đoạn cần sắp xếp
     * @param lo chỉ số bắt đầu (bao gồm)
     * @param hi chỉ số kết thúc (không bao gồm)
     */
    public static void sortRange(int[] a, int lo, int hi) {
        validateRange(a, lo, hi);
        quickSort(a, lo, hi - 1);
    }

    // =========================================================================
    // Public API – sắp xếp + đếm (lượt --count)
    // =========================================================================

    /**
     * Sắp xếp tăng dần toàn bộ mảng và thu thập số phép so sánh cùng độ sâu đệ quy tối đa.
     *
     * <p>Giá trị trả về đóng gói hai số liệu vào một {@code long}:
     * <pre>
     *   bits [63..32] = max_depth   (số nguyên 32-bit không âm)
     *   bits [31.. 0] = comparisons (số nguyên 32-bit không âm)
     * </pre>
     * Dùng {@link #unpackDepth(long)} và {@link #unpackComparisons(long)} để đọc lại.
     *
     * @param a mảng cần sắp xếp (không được {@code null})
     * @return {@code long} đóng gói {@code max_depth} và {@code comparisons}
     */
    public static long sortWithCount(int[] a) {
        if (a == null) {
            throw new IllegalArgumentException("Mảng không được null");
        }
        if (a.length <= 1) {
            return 0L;
        }
        // stats[0] = tổng số phép so sánh, stats[1] = max_depth quan sát được
        long[] stats = {0L, 0L};
        quickSortWithCount(a, 0, a.length - 1, 0, stats);
        return (stats[1] << 32) | (stats[0] & 0xFFFFFFFFL);
    }

    /**
     * Giải đóng gói: lấy {@code max_depth} từ giá trị trả về của {@link #sortWithCount}.
     *
     * @param packed giá trị trả về của {@link #sortWithCount(int[])}
     * @return độ sâu đệ quy tối đa
     */
    public static int unpackDepth(long packed) {
        return (int) (packed >>> 32);
    }

    /**
     * Giải đóng gói: lấy số phép so sánh từ giá trị trả về của {@link #sortWithCount}.
     *
     * @param packed giá trị trả về của {@link #sortWithCount(int[])}
     * @return số phép so sánh khóa giữa các phần tử
     */
    public static long unpackComparisons(long packed) {
        return packed & 0xFFFFFFFFL;
    }

    // =========================================================================
    // Core – phân hoạch Hoare + đệ quy (không đếm)
    // =========================================================================

    /**
     * Đệ quy Quick Sort trên đoạn đóng {@code [lo, hi]}.
     * Kết thúc khi đoạn có 0 hoặc 1 phần tử.
     */
    private static void quickSort(int[] a, int lo, int hi) {
        if (lo >= hi) {
            return;
        }
        int pivot = medianOfThree(a, lo, hi);
        int p = hoarePartition(a, lo, hi, pivot);
        // Hoare: a[lo..p] đều <= pivot, a[p+1..hi] đều >= pivot
        quickSort(a, lo, p);
        quickSort(a, p + 1, hi);
    }

    /**
     * Phân hoạch Hoare trên đoạn đóng {@code [lo, hi]}.
     *
     * <p>Hai con trỏ {@code i} (từ trái) và {@code j} (từ phải) hội tụ vào giữa.
     * Kết quả: mọi phần tử trong {@code [lo, p]} ≤ {@code pivot}
     *          và mọi phần tử trong {@code [p+1, hi]} ≥ {@code pivot}.
     * Chỉ số {@code p} trả về luôn nằm trong {@code [lo, hi-1]}.
     *
     * @param pivot giá trị pivot (không phải chỉ số)
     * @return chỉ số phân hoạch {@code p}
     */
    private static int hoarePartition(int[] a, int lo, int hi, int pivot) {
        int i = lo - 1;
        int j = hi + 1;

        while (true) {
            do { i++; } while (a[i] < pivot);
            do { j--; } while (a[j] > pivot);

            if (i >= j) {
                return j;
            }

            int tmp = a[i];
            a[i] = a[j];
            a[j] = tmp;
        }
    }

    /**
     * Tính trung vị của 3 phần tử tại {@code lo}, {@code mid} và {@code hi}.
     * Đặt chúng theo thứ tự a[lo] &le; a[mid] &le; a[hi] (in-place).
     *
     * @return giá trị trung vị dùng làm pivot
     */
    private static int medianOfThree(int[] a, int lo, int hi) {
        int mid = lo + ((hi - lo) >>> 1);
        if (a[lo] > a[mid]) { swap(a, lo, mid); }
        if (a[lo] > a[hi])  { swap(a, lo, hi);  }
        if (a[mid] > a[hi]) { swap(a, mid, hi); }
        return a[mid];
    }

    private static void swap(int[] a, int i, int j) {
        int tmp = a[i];
        a[i]   = a[j];
        a[j]   = tmp;
    }

    // =========================================================================
    // Core – phân hoạch Hoare + đệ quy (có đếm so sánh + max_depth)
    // =========================================================================

    /**
     * Đệ quy có đếm so sánh và theo dõi độ sâu đệ quy.
     *
     * @param a     mảng
     * @param lo    chỉ số trái (đóng)
     * @param hi    chỉ số phải (đóng)
     * @param depth độ sâu hiện tại (bắt đầu từ 0)
     * @param stats {@code stats[0]} = tổng số phép so sánh tích lũy,
     *              {@code stats[1]} = max_depth tích lũy
     */
    private static void quickSortWithCount(int[] a, int lo, int hi,
                                           int depth, long[] stats) {
        if (depth > stats[1]) {
            stats[1] = depth;
        }
        if (lo >= hi) {
            return;
        }

        // Median-of-three: tối đa 3 phép so sánh
        int mid = lo + ((hi - lo) >>> 1);
        long cmp = 0L;

        cmp++;
        if (a[lo] > a[mid]) { swap(a, lo, mid); }
        cmp++;
        if (a[lo] > a[hi])  { swap(a, lo, hi);  }
        cmp++;
        if (a[mid] > a[hi]) { swap(a, mid, hi); }

        int pivot = a[mid];
        stats[0] += cmp;

        // Hoare partition với đếm
        int i = lo - 1;
        int j = hi + 1;
        long pcmp = 0L;

        while (true) {
            do { i++; pcmp++; } while (a[i] < pivot);
            do { j--; pcmp++; } while (a[j] > pivot);

            if (i >= j) {
                break;
            }
            swap(a, i, j);
        }
        stats[0] += pcmp;

        int p = j;
        quickSortWithCount(a, lo, p,     depth + 1, stats);
        quickSortWithCount(a, p + 1, hi, depth + 1, stats);
    }

    // =========================================================================
    // Kiểm tra đầu vào
    // =========================================================================

    private static void validateRange(int[] a, int lo, int hi) {
        if (a == null) {
            throw new IllegalArgumentException("Mảng không được null");
        }
        if (lo < 0 || hi > a.length || lo > hi) {
            throw new IndexOutOfBoundsException(
                "Đoạn không hợp lệ: [lo=" + lo + ", hi=" + hi
                + "), độ dài mảng=" + a.length
            );
        }
    }
}
