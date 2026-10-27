package csd201.sort;

/**
 * Cài đặt pdqsort (Pattern-defeating Quicksort) theo yêu cầu đề tài CSD201 FA26 (Mã đề 261014).
 * Theo O. R. L. Peters, "Pattern-defeating quicksort", arXiv:2106.05123, 2021.
 *
 * <ul>
 *   <li>Tên trong CSV: {@code pdq}</li>
 *   <li>Phần lõi (Người 5 – Cặp 3):
 *     <ol>
 *       <li>Insertion Sort cho đoạn dưới 24 phần tử.</li>
 *       <li>Chọn pivot: trung vị của 3; đoạn trên 128 phần tử dùng ninther của Tukey.</li>
 *       <li>Phân hoạch phải (partition right), trả về cờ "đã phân hoạch sẵn".</li>
 *       <li>Chống trường hợp xấu: đếm phân hoạch lệch, hoán đổi vài phần tử để phá mẫu;
 *           quá log₂(N) lần thì chuyển đoạn đó sang {@link HeapSort#sortRange(int[], int, int)}
 *           (hàm của Người 3).</li>
 *     </ol>
 *   </li>
 *   <li>Cơ chế A (phát hiện đoạn đã sắp) và B (gom khóa trùng) là phần của Người 6, được
 *       chừa sẵn ở hai vị trí đánh dấu {@code [NGƯỜI 6]}, bật/tắt bằng cờ {@code useA}, {@code useB}.</li>
 *   <li>Trong lượt {@code --count}: {@link #sortWithCount(int[])} trả {@code long} đóng gói giống
 *       {@link QuickSort} và {@link MergeSort}: 32-bit cao = {@code max_depth},
 *       32-bit thấp = số phép so sánh. Dùng {@link #unpackDepth(long)} / {@link #unpackComparisons(long)}.</li>
 * </ul>
 */
public final class PdqSort {

    // =========================================================================
    // Hằng số theo đề bài
    // =========================================================================

    /** Đoạn có kích thước nhỏ hơn ngưỡng này thì dùng Insertion Sort. */
    static final int INSERTION_SORT_THRESHOLD = 24;
    /** Đoạn có kích thước lớn hơn ngưỡng này thì chọn pivot bằng ninther. */
    static final int NINTHER_THRESHOLD = 128;
    /** Giới hạn số lần dịch của partial insertion sort (dùng cho cơ chế A – Người 6). */
    static final int PARTIAL_INSERTION_SORT_LIMIT = 8;

    // =========================================================================
    // Cờ CHỈ dùng cho thí nghiệm tắt cơ chế của phần lõi (PdqEvidence).
    // Mặc định đều bật; benchmark chính thức không đụng tới các cờ này.
    // =========================================================================
    static boolean ablationNinther = true;
    static boolean ablationBreakPatterns = true;
    static boolean ablationHeapFallback = true;

    // =========================================================================
    // Cấu hình và thống kê của một lần chạy
    // =========================================================================

    private final boolean counting;   // true = lượt --count: đếm so sánh và độ sâu
    private final boolean useA;       // cơ chế A (Người 6)
    private final boolean useB;       // cơ chế B (Người 6)

    private long comparisons;         // số phép so sánh khóa
    private int depth;                // độ sâu đệ quy hiện tại
    private int maxDepth;             // độ sâu đệ quy lớn nhất
    private int badPartitions;        // số lần phân hoạch lệch
    private int heapFallbacks;        // số lần chuyển sang Heap Sort

    private int[] a;                  // mảng đang sắp
    private boolean alreadyPartitioned; // cờ trả về từ partitionRight

    /**
     * @param counting true nếu là lượt --count
     * @param useA     bật cơ chế A (phát hiện đoạn đã sắp) – Người 6
     * @param useB     bật cơ chế B (gom khóa trùng) – Người 6
     */
    public PdqSort(boolean counting, boolean useA, boolean useB) {
        this.counting = counting;
        this.useA = useA;
        this.useB = useB;
    }

    // =========================================================================
    // Public API – dùng cho BenchmarkRunner
    // =========================================================================

    /**
     * Sắp xếp tăng dần toàn bộ mảng (lượt đo thời gian, không đếm).
     * Bản lõi: chưa bật cơ chế A, B.
     *
     * @param a mảng cần sắp xếp (không được {@code null})
     */
    public static void sort(int[] a) {
        if (a == null) {
            throw new IllegalArgumentException("Mảng không được null");
        }
        new PdqSort(false, false, false).run(a, 0, a.length);
    }

    public static void sort(int[] a, String variant) {
        if (a == null) {
            throw new IllegalArgumentException("Mảng không được null");
        }
        boolean useA = !"noA".equalsIgnoreCase(variant) && !"core".equalsIgnoreCase(variant);
        boolean useB = !"noB".equalsIgnoreCase(variant) && !"core".equalsIgnoreCase(variant);
        new PdqSort(false, useA, useB).run(a, 0, a.length);
    }

    /**
     * Sắp xếp tăng dần đoạn nửa mở {@code [lo, hi)}.
     *
     * @param a  mảng chứa đoạn cần sắp xếp
     * @param lo chỉ số bắt đầu (bao gồm)
     * @param hi chỉ số kết thúc (không bao gồm)
     */
    public static void sortRange(int[] a, int lo, int hi) {
        validateRange(a, lo, hi);
        new PdqSort(false, false, false).run(a, lo, hi);
    }

    /**
     * Sắp xếp toàn bộ mảng ở lượt --count.
     *
     * @param a mảng cần sắp xếp (không được {@code null})
     * @return {@code long} đóng gói: bits [63..32] = max_depth, bits [31..0] = comparisons
     */
    public static long sortWithCount(int[] a) {
        if (a == null) {
            throw new IllegalArgumentException("Mảng không được null");
        }
        PdqSort p = new PdqSort(true, false, false);
        p.run(a, 0, a.length);
        return pack(p.maxDepth, p.comparisons);
    }

    public static long sortWithCount(int[] a, String variant) {
        if (a == null) {
            throw new IllegalArgumentException("Mảng không được null");
        }
        boolean useA = !"noA".equalsIgnoreCase(variant) && !"core".equalsIgnoreCase(variant);
        boolean useB = !"noB".equalsIgnoreCase(variant) && !"core".equalsIgnoreCase(variant);
        PdqSort p = new PdqSort(true, useA, useB);
        p.run(a, 0, a.length);
        return pack(p.maxDepth, p.comparisons);
    }

    /** Lấy {@code max_depth} từ giá trị trả về của {@link #sortWithCount(int[])}. */
    public static int unpackDepth(long packed) {
        return (int) (packed >>> 32);
    }

    /** Lấy số phép so sánh từ giá trị trả về của {@link #sortWithCount(int[])}. */
    public static long unpackComparisons(long packed) {
        return packed & 0xFFFFFFFFL;
    }

    /**
     * Sắp đoạn {@code [lo, hi)} của mảng {@code a} với cấu hình của đối tượng này.
     * Sau khi chạy, đọc số liệu qua các getter.
     */
    public void run(int[] a, int lo, int hi) {
        validateRange(a, lo, hi);
        this.a = a;
        comparisons = 0;
        depth = 0;
        maxDepth = 0;
        badPartitions = 0;
        heapFallbacks = 0;
        int size = hi - lo;
        if (size >= 2) {
            // Số lần được phép phân hoạch lệch = floor(log2(N))
            pdqLoop(lo, hi, log2(size), true);
        }
        this.a = null;
    }

    public long getComparisons() {
        return comparisons;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public int getBadPartitions() {
        return badPartitions;
    }

    public int getHeapFallbacks() {
        return heapFallbacks;
    }

    // =========================================================================
    // Vòng lặp chính
    // =========================================================================

    /** Gọi đệ quy cho đoạn [begin, end), có cập nhật độ sâu. */
    private void pdqLoop(int begin, int end, int badAllowed, boolean leftmost) {
        if (counting) {
            depth++;
            if (depth > maxDepth) {
                maxDepth = depth;
            }
        }
        loop(begin, end, badAllowed, leftmost);
        if (counting) {
            depth--;
        }
    }

    /*
     * Thân của pdqsort. Đệ quy cho nửa trái, còn nửa phải xử lý bằng vòng lặp
     * (khử đệ quy đuôi) giống bản gốc của Peters.
     * leftmost = true nếu đoạn nằm sát mép trái của mảng (không có phần tử đứng
     * trước làm lính canh).
     */
    private void loop(int begin, int end, int badAllowed, boolean leftmost) {
        while (true) {
            int size = end - begin;

            // (1) Đoạn nhỏ: Insertion Sort
            if (size < INSERTION_SORT_THRESHOLD) {
                if (leftmost) {
                    insertionSort(begin, end);
                } else {
                    unguardedInsertionSort(begin, end);
                }
                return;
            }

            // (2) Chọn pivot và đưa pivot về vị trí begin
            int s2 = size >>> 1;
            if (ablationNinther && size > NINTHER_THRESHOLD) {
                // Ninther của Tukey: trung vị của 3 trung vị
                sort3(begin, begin + s2, end - 1);
                sort3(begin + 1, begin + (s2 - 1), end - 2);
                sort3(begin + 2, begin + (s2 + 1), end - 3);
                sort3(begin + (s2 - 1), begin + s2, begin + (s2 + 1));
                swap(begin, begin + s2);
            } else {
                // Trung vị của 3: sau lệnh này a[begin] là trung vị, a[end-1] lớn nhất
                sort3(begin + s2, begin, end - 1);
            }

            // [NGƯỜI 6] Cơ chế B: nếu pivot bằng phần tử đứng ngay trước đoạn
            // (a[begin-1] >= pivot, mà a[begin-1] <= mọi phần tử của đoạn) thì
            // phân hoạch trái để gom mọi khóa bằng pivot rồi bỏ qua phần đó.
            if (useB && !leftmost && !less(a[begin - 1], a[begin])) {
                begin = partitionLeft(begin, end) + 1;
                continue;
            }

            // (3) Phân hoạch phải
            int pivotPos = partitionRight(begin, end);
            boolean alreadyPart = alreadyPartitioned;

            int lSize = pivotPos - begin;
            int rSize = end - (pivotPos + 1);
            boolean highlyUnbalanced = lSize < size / 8 || rSize < size / 8;

            if (highlyUnbalanced) {
                // (4) Chống trường hợp xấu
                badPartitions++;
                if (--badAllowed == 0 && ablationHeapFallback) {
                    // Quá log2(N) lần lệch: chuyển đoạn này sang Heap Sort của Người 3
                    heapFallbacks++;
                    if (counting) {
                        comparisons += HeapSort.sortRangeWithCount(a, begin, end);
                    } else {
                        HeapSort.sortRange(a, begin, end);
                    }
                    return;
                }
                if (ablationBreakPatterns) {
                    breakPatterns(begin, pivotPos, end, lSize, rSize);
                }
            } else if (useA && alreadyPart
                    && partialInsertionSort(begin, pivotPos)
                    && partialInsertionSort(pivotPos + 1, end)) {
                // [NGƯỜI 6] Cơ chế A: phân hoạch cân bằng và không phải hoán đổi ->
                // thử partial insertion sort trên 2 nửa; cả hai thành công thì xong đoạn.
                return;
            }

            // Đệ quy nửa trái, lặp tiếp với nửa phải
            pdqLoop(begin, pivotPos, badAllowed, leftmost);
            begin = pivotPos + 1;
            leftmost = false;
        }
    }

    // =========================================================================
    // Phân hoạch phải
    // =========================================================================

    /*
     * Phân hoạch đoạn [begin, end) với pivot = a[begin]:
     * phần tử < pivot sang trái, phần tử >= pivot sang phải.
     * Trả về vị trí cuối cùng của pivot; gán alreadyPartitioned = true nếu
     * không phải hoán đổi lần nào (đoạn đã phân hoạch sẵn).
     *
     * Điều kiện trước: trong (begin, end) có phần tử >= pivot (bảo đảm bởi bước
     * chọn pivot), nên vòng quét từ trái không vượt mảng.
     */
    private int partitionRight(int begin, int end) {
        int pivot = a[begin];
        int first = begin;
        int last = end;

        // Tìm phần tử đầu tiên >= pivot từ bên trái
        while (less(a[++first], pivot)) {
        }

        // Tìm phần tử đầu tiên < pivot từ bên phải. Nếu first chưa đi được bước
        // nào thì chưa có lính canh bên trái, phải kiểm tra first < last.
        if (first - 1 == begin) {
            while (first < last && !less(a[--last], pivot)) {
            }
        } else {
            while (!less(a[--last], pivot)) {
            }
        }

        // Hai con trỏ đã gặp nhau mà chưa hoán đổi -> đã phân hoạch sẵn
        alreadyPartitioned = first >= last;

        // Hoán đổi các cặp sai vị trí. Sau mỗi lần hoán đổi, a[first] < pivot và
        // a[last] >= pivot làm lính canh cho hai vòng quét tiếp theo.
        while (first < last) {
            swap(first, last);
            while (less(a[++first], pivot)) {
            }
            while (!less(a[--last], pivot)) {
            }
        }

        // Đưa pivot về vị trí đúng
        int pivotPos = first - 1;
        a[begin] = a[pivotPos];
        a[pivotPos] = pivot;
        return pivotPos;
    }

    // =========================================================================
    // Phá mẫu khi phân hoạch lệch
    // =========================================================================

    /** Hoán đổi vài phần tử ở hai nửa để phá các mẫu dữ liệu gây phân hoạch lệch. */
    private void breakPatterns(int begin, int pivotPos, int end, int lSize, int rSize) {
        if (lSize >= INSERTION_SORT_THRESHOLD) {
            swap(begin, begin + lSize / 4);
            swap(pivotPos - 1, pivotPos - lSize / 4);
            if (lSize > NINTHER_THRESHOLD) {
                swap(begin + 1, begin + (lSize / 4 + 1));
                swap(begin + 2, begin + (lSize / 4 + 2));
                swap(pivotPos - 2, pivotPos - (lSize / 4 + 1));
                swap(pivotPos - 3, pivotPos - (lSize / 4 + 2));
            }
        }
        if (rSize >= INSERTION_SORT_THRESHOLD) {
            swap(pivotPos + 1, pivotPos + (1 + rSize / 4));
            swap(end - 1, end - rSize / 4);
            if (rSize > NINTHER_THRESHOLD) {
                swap(pivotPos + 2, pivotPos + (2 + rSize / 4));
                swap(pivotPos + 3, pivotPos + (3 + rSize / 4));
                swap(end - 2, end - (1 + rSize / 4));
                swap(end - 3, end - (2 + rSize / 4));
            }
        }
    }

    // =========================================================================
    // Insertion Sort
    // =========================================================================

    /** Insertion Sort có kiểm tra biên, dùng cho đoạn sát mép trái. */
    private void insertionSort(int begin, int end) {
        for (int cur = begin + 1; cur < end; cur++) {
            int tmp = a[cur];
            int sift = cur;
            while (sift > begin && less(tmp, a[sift - 1])) {
                a[sift] = a[sift - 1];
                sift--;
            }
            a[sift] = tmp;
        }
    }

    /*
     * Insertion Sort không kiểm tra biên, dùng cho đoạn không sát mép trái.
     * a[begin-1] là pivot cũ, <= mọi phần tử trong đoạn, nên vòng lặp tự dừng.
     */
    private void unguardedInsertionSort(int begin, int end) {
        for (int cur = begin + 1; cur < end; cur++) {
            int tmp = a[cur];
            int sift = cur;
            while (less(tmp, a[sift - 1])) {
                a[sift] = a[sift - 1];
                sift--;
            }
            a[sift] = tmp;
        }
    }

    // =========================================================================
    // Chỗ dành cho Người 6
    // =========================================================================

    /*
     * [NGƯỜI 6] Cơ chế B – phân hoạch trái: pivot = a[begin]; đưa mọi phần tử
     * <= pivot sang trái, > pivot sang phải; trả về vị trí cuối cùng của pivot.
     */
    private int partitionLeft(int begin, int end) {
        int pivot = a[begin];
        int first = begin;
        int last = end;

        while (less(pivot, a[--last])) {
        }

        if (last == end - 1) {
            while (first < last && !less(pivot, a[++first])) {
            }
        } else {
            while (!less(pivot, a[++first])) {
            }
        }

        while (first < last) {
            swap(first, last);
            while (less(pivot, a[--last])) {
            }
            while (!less(pivot, a[++first])) {
            }
        }

        int pivotPos = first - 1;
        a[begin] = a[pivotPos];
        a[pivotPos] = pivot;
        return pivotPos;
    }

    /*
     * [NGƯỜI 6] Cơ chế A – partial insertion sort trên [begin, end): chạy Insertion
     * Sort nhưng dừng (trả về false) nếu tổng số lần dịch vượt
     * PARTIAL_INSERTION_SORT_LIMIT; trả về true nếu sắp xong đoạn.
     */
    private boolean partialInsertionSort(int begin, int end) {
        if (begin == end) {
            return true;
        }
        int limit = 0;
        for (int cur = begin + 1; cur < end; cur++) {
            int tmp = a[cur];
            int sift = cur;
            while (sift > begin && less(tmp, a[sift - 1])) {
                a[sift] = a[sift - 1];
                sift--;
                if (++limit > PARTIAL_INSERTION_SORT_LIMIT) {
                    return false;
                }
            }
            a[sift] = tmp;
        }
        return true;
    }

    // =========================================================================
    // Hàm phụ
    // =========================================================================

    /** So sánh khóa; mọi phép so sánh khóa của phần lõi đều đi qua đây. */
    private boolean less(int x, int y) {
        if (counting) {
            comparisons++;
        }
        return x < y;
    }

    private void swap(int i, int j) {
        int t = a[i];
        a[i] = a[j];
        a[j] = t;
    }

    /** Sắp a[i] <= a[j]. */
    private void sort2(int i, int j) {
        if (less(a[j], a[i])) {
            swap(i, j);
        }
    }

    /** Sắp a[i] <= a[j] <= a[k]; sau đó a[j] là trung vị của 3. */
    private void sort3(int i, int j, int k) {
        sort2(i, j);
        sort2(j, k);
        sort2(i, j);
    }

    /** floor(log2(n)) với n >= 1. */
    static int log2(int n) {
        return 31 - Integer.numberOfLeadingZeros(n);
    }

    private static long pack(int depth, long comparisons) {
        if (comparisons > 0xFFFFFFFFL) {
            throw new ArithmeticException("Số phép so sánh vượt 32 bit, không đóng gói được: " + comparisons);
        }
        return ((long) depth << 32) | comparisons;
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
