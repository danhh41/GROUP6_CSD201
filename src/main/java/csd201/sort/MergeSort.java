package csd201.sort;

/**
 * Cài đặt thuật toán Merge Sort theo yêu cầu đề tài CSD201 FA26 (Mã đề 261014).
 *
 * <ul>
 *   <li>Tên trong CSV: {@code merge}</li>
 *   <li>Mô hình: <b>Top-Down</b> chia đôi đệ quy từ trên xuống.</li>
 *   <li>Cấp phát bộ nhớ phụ: Cấp phát duy nhất 1 lần mảng phụ {@code int[] aux = new int[N]}
 *       ở hàm gọi ban đầu và truyền qua các tầng đệ quy (tuyệt đối không tạo mới bên trong).</li>
 *   <li>Không tối ưu bỏ qua trộn: Luôn thực hiện bước trộn hai nửa con,
 *       không dùng điều kiện {@code if (a[mid] <= a[mid+1]) return;}.</li>
 *   <li>Thao tác trên mảng nguyên bản {@code int[]} với khóa không âm từ 0 đến 2147483647.</li>
 *   <li>Cung cấp hàm {@link #sortRange(int[], int, int)} cho đoạn nửa mở {@code [lo, hi)}.</li>
 *   <li>Hai chế độ chạy:
 *       <ul>
 *         <li><b>Benchmarking:</b> {@link #sort(int[])} / {@link #sortRange(int[], int, int)}
 *             tối ưu tốc độ, không chứa logic đếm.</li>
 *         <li><b>Chế độ đếm (--count):</b> {@link #sortWithCount(int[])} / {@link #sortRangeWithCount(int[], int, int)}
 *             đếm số phép so sánh khóa và đo độ sâu đệ quy lớn nhất ({@code max_depth}).
 *             Kết quả đóng gói 64-bit: 32-bit cao = {@code max_depth}, 32-bit thấp = {@code comparisons}.
 *             Dùng {@link #unpackDepth(long)} và {@link #unpackComparisons(long)} để đọc lại.</li>
 *       </ul>
 *   </li>
 * </ul>
 */
public final class MergeSort {

    private MergeSort() {
        // Lớp tiện ích không khởi tạo
    }

    // =========================================================================
    // Public API – Chế độ Benchmarking (không đếm, tối ưu tốc độ)
    // =========================================================================

    /**
     * Sắp xếp tăng dần toàn bộ mảng {@code int[]} bằng Merge Sort Top-Down.
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
     * Sắp xếp tăng dần đoạn nửa mở {@code [lo, hi)} trong mảng {@code int[]}.
     * Các phần tử ngoài đoạn {@code [lo, hi)} được bảo toàn nguyên vẹn.
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
        int[] aux = new int[a.length];
        mergeSort(a, aux, lo, hi - 1);
    }

    // =========================================================================
    // Public API – Chế độ đếm (--count)
    // =========================================================================

    /**
     * Sắp xếp tăng dần toàn bộ mảng và đếm số phép so sánh khóa cùng độ sâu đệ quy tối đa.
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
        return sortRangeWithCount(a, 0, a.length);
    }

    /**
     * Sắp xếp tăng dần đoạn nửa mở {@code [lo, hi)} và đếm số phép so sánh khóa cùng độ sâu đệ quy tối đa.
     *
     * @param a  mảng chứa đoạn cần sắp xếp
     * @param lo chỉ số bắt đầu (bao gồm)
     * @param hi chỉ số kết thúc (không bao gồm)
     * @return {@code long} đóng gói {@code max_depth} và {@code comparisons}
     */
    public static long sortRangeWithCount(int[] a, int lo, int hi) {
        validateRange(a, lo, hi);
        int n = hi - lo;
        if (n <= 1) {
            return 0L;
        }
        int[] aux = new int[a.length];
        long[] stats = {0L, 0L}; // stats[0] = comparisons, stats[1] = max_depth
        mergeSortWithCount(a, aux, lo, hi - 1, 0, stats);
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
     * Giải đóng gói: lấy số phép so sánh khóa từ giá trị trả về của {@link #sortWithCount}.
     *
     * @param packed giá trị trả về của {@link #sortWithCount(int[])}
     * @return số phép so sánh khóa giữa các phần tử
     */
    public static long unpackComparisons(long packed) {
        return packed & 0xFFFFFFFFL;
    }

    // =========================================================================
    // Core – Chia đôi đệ quy Top-Down và trộn (không đếm)
    // =========================================================================

    private static void mergeSort(int[] a, int[] aux, int lo, int hi) {
        if (lo >= hi) {
            return;
        }
        int mid = lo + ((hi - lo) >>> 1);
        mergeSort(a, aux, lo, mid);
        mergeSort(a, aux, mid + 1, hi);
        // Quy chuẩn bắt buộc: Bắt buộc chạy qua bước merge ở mọi trường hợp,
        // tuyệt đối KHÔNG thêm điều kiện if (a[mid] <= a[mid+1]) return;
        merge(a, aux, lo, mid, hi);
    }

    private static void merge(int[] a, int[] aux, int lo, int mid, int hi) {
        System.arraycopy(a, lo, aux, lo, hi - lo + 1);

        int i = lo;
        int j = mid + 1;
        int k = lo;

        while (i <= mid && j <= hi) {
            if (aux[j] < aux[i]) {
                a[k++] = aux[j++];
            } else {
                a[k++] = aux[i++];
            }
        }

        while (i <= mid) {
            a[k++] = aux[i++];
        }

        while (j <= hi) {
            a[k++] = aux[j++];
        }
    }

    // =========================================================================
    // Core – Chia đôi đệ quy Top-Down và trộn (có đếm comparisons + max_depth)
    // =========================================================================

    private static void mergeSortWithCount(int[] a, int[] aux, int lo, int hi,
                                           int depth, long[] stats) {
        if (depth > stats[1]) {
            stats[1] = depth;
        }
        if (lo >= hi) {
            return;
        }
        int mid = lo + ((hi - lo) >>> 1);
        mergeSortWithCount(a, aux, lo, mid, depth + 1, stats);
        mergeSortWithCount(a, aux, mid + 1, hi, depth + 1, stats);
        // Bắt buộc chạy qua merge ở mọi trường hợp
        mergeWithCount(a, aux, lo, mid, hi, stats);
    }

    private static void mergeWithCount(int[] a, int[] aux, int lo, int mid, int hi,
                                       long[] stats) {
        System.arraycopy(a, lo, aux, lo, hi - lo + 1);

        int i = lo;
        int j = mid + 1;
        int k = lo;
        long cmp = 0L;

        while (i <= mid && j <= hi) {
            cmp++;
            if (aux[j] < aux[i]) {
                a[k++] = aux[j++];
            } else {
                a[k++] = aux[i++];
            }
        }

        while (i <= mid) {
            a[k++] = aux[i++];
        }

        while (j <= hi) {
            a[k++] = aux[j++];
        }

        stats[0] += cmp;
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
