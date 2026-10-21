package csd201.sort;

/**
 * Cài đặt thuật toán Radix Sort theo yêu cầu đề tài CSD201 FA26 (Mã đề 261014).
 *
 * - Tên trong CSV: radix
 * - Thuật toán: LSD (Least Significant Digit) Radix Sort cơ số 256 (BITS_PER_BYTE = 8).
 * - Đúng 4 lượt cho khóa int 32-bit không âm (miền giá trị 0 đến 2^31 - 1).
 * - Mỗi lượt là một counting sort ổn định.
 * - Cấp phát duy nhất một mảng phụ aux kích thước N và mảng đếm 256 phần tử.
 * - Tận dụng 4 lượt (số chẵn) chuyển đổi qua lại giữa mảng nguồn và mảng phụ
 *   (pass 0: a -> aux, pass 1: aux -> a, pass 2: a -> aux, pass 3: aux -> a)
 *   để kết quả cuối cùng nằm tại mảng a mà không cần thêm bất kỳ mảng phụ nào.
 * - Trong lượt --count: đếm số lần đọc và ghi mảng dữ liệu (không tính mảng đếm cố định 256).
 */
public final class RadixSort {

    private static final int BITS_PER_BYTE = 8;
    private static final int RADIX = 256;      // 1 << 8
    private static final int MASK = 0xFF;      // 255
    private static final int TOTAL_PASSES = 4; // 32 bit / 8 bit

    private RadixSort() {
        // Lớp tiện ích không khởi tạo
    }

    /**
     * Sắp xếp tăng dần toàn bộ mảng int[] bằng LSD Radix Sort 4 lượt cơ số 256.
     *
     * @param a mảng chứa các số nguyên không âm cần sắp xếp
     */
    public static void sort(int[] a) {
        if (a == null) {
            throw new IllegalArgumentException("Mảng không được null");
        }
        int n = a.length;
        if (n <= 1) {
            return;
        }

        // Cấp phát duy nhất 1 mảng phụ kích thước N và 1 mảng đếm 256 phần tử
        int[] aux = new int[n];
        int[] count = new int[RADIX + 1];

        int[] src = a;
        int[] dst = aux;

        for (int pass = 0; pass < TOTAL_PASSES; pass++) {
            int shift = pass * BITS_PER_BYTE;

            // Xóa mảng đếm
            for (int r = 0; r <= RADIX; r++) {
                count[r] = 0;
            }

            // Bước 1: Đếm tần suất xuất hiện của byte thứ 'pass'
            for (int i = 0; i < n; i++) {
                int digit = (src[i] >>> shift) & MASK;
                count[digit + 1]++;
            }

            // Bước 2: Tính tổng tích lũy để xác định vị trí bắt đầu
            for (int r = 0; r < RADIX; r++) {
                count[r + 1] += count[r];
            }

            // Bước 3: Phân phối phần tử ổn định sang mảng đích dst
            for (int i = 0; i < n; i++) {
                int digit = (src[i] >>> shift) & MASK;
                dst[count[digit]++] = src[i];
            }

            // Đổi vai trò giữa mảng nguồn và mảng đích cho pass kế tiếp
            int[] tmp = src;
            src = dst;
            dst = tmp;
        }
        // Vì số pass là 4 (chẵn), dữ liệu sau 4 lượt luôn nằm chính xác ở mảng a.
    }

    /**
     * Sắp xếp tăng dần toàn bộ mảng và đếm số lần đọc/ghi trên mảng dữ liệu.
     *
     * @param a mảng chứa các số nguyên không âm cần sắp xếp
     * @return tổng số thao tác đọc và ghi trên mảng dữ liệu (a và aux)
     */
    public static long sortWithCount(int[] a) {
        if (a == null) {
            throw new IllegalArgumentException("Mảng không được null");
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

            // Bước 1: Đọc mảng src để đếm tần suất (n lần đọc)
            for (int i = 0; i < n; i++) {
                int digit = (src[i] >>> shift) & MASK;
                count[digit + 1]++;
                operations++; // 1 lần đọc src[i]
            }

            // Bước 2: Tích lũy trên mảng count cố định 256 (không tính vào mảng dữ liệu)
            for (int r = 0; r < RADIX; r++) {
                count[r + 1] += count[r];
            }

            // Bước 3: Đọc src[i] và ghi vào dst (n lần đọc + n lần ghi)
            for (int i = 0; i < n; i++) {
                int digit = (src[i] >>> shift) & MASK;
                dst[count[digit]++] = src[i];
                operations += 2L; // 1 lần đọc src[i] + 1 lần ghi dst[...]
            }

            int[] tmp = src;
            src = dst;
            dst = tmp;
        }

        return operations;
    }
}
