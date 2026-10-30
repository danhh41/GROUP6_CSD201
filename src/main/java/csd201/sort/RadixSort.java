package csd201.sort;

public final class RadixSort {

    private static final int BITS_PER_BYTE = 8;
    private static final int RADIX = 256; // 256 cai xo tuong ung 1 byte (2^8)
    private static final int MASK = 0xFF; // Mat na bit 255 trich xuat 1 byte
    private static final int TOTAL_PASSES = 4; // So int co 32 bit = 4 byte -> chay 4 luot

    // Khoa khong cho tao doi tuong (Utility Class)
    private RadixSort() {
    }

    // Sap xep toan bo mang bang LSD Radix Sort toc do cao (khong dem)
    public static void sort(int[] a) {
        if (a == null) {
            throw new IllegalArgumentException("Array must not be null");
        }
        int n = a.length;
        if (n <= 1) {
            return; // 0 hoac 1 phan tu thi da co thu tu san
        }

        int[] aux = new int[n]; // Mang phu chua cac xo (ton O(N) RAM)
        int[] count = new int[RADIX + 1];

        int[] src = a;
        int[] dst = aux;

        // Chay dung 4 luot tu Byte 0 (thap nhat) den Byte 3 (cao nhat)
        for (int pass = 0; pass < TOTAL_PASSES; pass++) {
            int shift = pass * BITS_PER_BYTE;

            // Reset dem ve 0
            for (int r = 0; r <= RADIX; r++) {
                count[r] = 0;
            }

            // 1. Dem tan suat cac phan tu vao xo
            for (int i = 0; i < n; i++) {
                int digit = (src[i] >>> shift) & MASK;
                count[digit + 1]++;
            }

            // 2. Cong don (Prefix Sum) de xac dinh vi tri dau cua moi xo
            for (int r = 0; r < RADIX; r++) {
                count[r + 1] += count[r];
            }

            // 3. Chuyen phan tu tu mang cu vao dung xo o mang moi
            for (int i = 0; i < n; i++) {
                int digit = (src[i] >>> shift) & MASK;
                dst[count[digit]++] = src[i];
            }

            // 4. Doi vai tro mang phu thanh mang chinh cho luot sau
            int[] tmp = src;
            src = dst;
            dst = tmp;
        }
    }

    // Sap xep va dem tong so thao tac doc/ghi bo nho (chuan 12N) cho RQ2
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

            // 1 thao tac doc moi phan tu khi dem tan suat (1N)
            for (int i = 0; i < n; i++) {
                int digit = (src[i] >>> shift) & MASK;
                count[digit + 1]++;
                operations++;
            }

            for (int r = 0; r < RADIX; r++) {
                count[r + 1] += count[r];
            }

            // 1 thao tac doc va 1 thao tac ghi khi chuyen xo (2N)
            for (int i = 0; i < n; i++) {
                int digit = (src[i] >>> shift) & MASK;
                dst[count[digit]++] = src[i];
                operations += 2L;
            }

            int[] tmp = src;
            src = dst;
            dst = tmp;
        }

        return operations; // Tong cong 4 luot x 3N = dung 12N thao tac
    }
}
