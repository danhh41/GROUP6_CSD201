package csd201.sort;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.SplittableRandom;

import static org.junit.jupiter.api.Assertions.*;

public class HeapSortTest {

    // TEST 1: Kiem tra 5 ca co dinh bat buoc theo barem de bai
    @Test
    @DisplayName("5 ca kiểm thử cố định theo đề bài")
    public void testFixedCases() {
        assertSortCorrect(new int[]{7}); // 1 phan tu
        assertSortCorrect(new int[]{2, 1}); // 2 phan tu bi nguoc
        assertSortCorrect(new int[]{3, 3, 3}); // toan so bang nhau
        assertSortCorrect(new int[]{0, Integer.MAX_VALUE, 0}); // so cuc dai chong tran bit
        assertSortCorrect(new int[]{5, 1, 4, 1, 5, 9, 2, 6, 5, 3, 5}); // mang phuc tap chu so Pi
    }

    // TEST 2: Kiem tra cac truong hop bien dac biet va moc chuyen giao kien truc bit
    @Test
    @DisplayName("Kiểm thử mảng rỗng, 1 phần tử, 2 phần tử và các giá trị biên")
    public void testEdgeValuesAndSizes() {
        assertSortCorrect(new int[]{}); // mang rong n=0
        assertSortCorrect(new int[]{42}); // mang 1 phan tu
        assertSortCorrect(new int[]{100, 50}); // 2 phan tu nguoc
        assertSortCorrect(new int[]{50, 100}); // 2 phan tu xuoi
        assertSortCorrect(new int[]{7, 7, 7, 7}); // toan so trung lap

        // Ranh gioi 1 byte (255, 256), 2 byte (65535) va 4 byte (MAX_VALUE)
        int[] boundaryValues = new int[]{0, 255, 256, 65535, Integer.MAX_VALUE, 1, 1024};
        assertSortCorrect(boundaryValues);
    }

    // TEST 3: Kiem tra ham sortRange sap xep doan con [lo, hi) de cuu nguy cho pdqsort
    @Test
    @DisplayName("Kiểm thử sortRange trên đoạn [lo, hi) và đảm bảo phần tử ngoài đoạn không đổi")
    public void testSortRangeIntegrity() {
        int[] original = new int[]{99, 88, 77, 15, 3, 9, 2, 8, 44, 33, 22};
        int lo = 3;
        int hi = 8; // doan con gom [15, 3, 9, 2, 8]

        int[] a = original.clone();
        HeapSort.sortRange(a, lo, hi);

        // Kiem tra cac phan tu truoc lo khong bi thay doi
        for (int i = 0; i < lo; i++) {
            assertEquals(original[i], a[i], "Phần tử tại index " + i + " bên ngoài đoạn đã bị thay đổi!");
        }

        // Kiem tra cac phan tu sau hi khong bi thay doi
        for (int i = hi; i < original.length; i++) {
            assertEquals(original[i], a[i], "Phần tử tại index " + i + " bên ngoài đoạn đã bị thay đổi!");
        }

        // Kiem tra doan con [lo, hi) da duoc sap xep tang dan dung
        int[] subArrayExpected = Arrays.copyOfRange(original, lo, hi);
        Arrays.sort(subArrayExpected);
        for (int i = 0; i < subArrayExpected.length; i++) {
            assertEquals(subArrayExpected[i], a[lo + i], "Đoạn con chưa được sắp xếp đúng tại index " + (lo + i));
        }

        // Kiem tra doan rong va doan 1 phan tu
        int[] b = original.clone();
        HeapSort.sortRange(b, 4, 4); // rong
        assertArrayEquals(original, b);

        HeapSort.sortRange(b, 4, 5); // 1 phan tu
        assertArrayEquals(original, b);

        // Kiem tra bat ngoai le khi truyen chi so bay ba
        assertThrows(IndexOutOfBoundsException.class, () -> HeapSort.sortRange(b, -1, 5));
        assertThrows(IndexOutOfBoundsException.class, () -> HeapSort.sortRange(b, 2, b.length + 1));
        assertThrows(IndexOutOfBoundsException.class, () -> HeapSort.sortRange(b, 5, 2));
    }

    // TEST 4: Kiem tra do ben voi 1.000 mang ngau nhien du loai D1-D5
    @Test
    @DisplayName("1.000 mảng ngẫu nhiên kích thước 0-100 thuộc họ D1-D5")
    public void test1000RandomArraysD1ToD5() {
        SplittableRandom rng = new SplittableRandom(20261021L);

        for (int t = 0; t < 1000; t++) {
            int n = rng.nextInt(101); // 0 den 100
            int[] a = new int[n];
            int type = t % 5;

            switch (type) {
                case 0: // D1: Ngau nhien
                    for (int i = 0; i < n; i++) {
                        a[i] = rng.nextInt(Integer.MAX_VALUE);
                    }
                    break;
                case 1: // D2: Tang dan
                    for (int i = 0; i < n; i++) {
                        a[i] = rng.nextInt(Integer.MAX_VALUE);
                    }
                    Arrays.sort(a);
                    break;
                case 2: // D3: Gan tang dan
                    for (int i = 0; i < n; i++) {
                        a[i] = rng.nextInt(Integer.MAX_VALUE);
                    }
                    Arrays.sort(a);
                    if (n > 1) {
                        int i1 = rng.nextInt(n);
                        int i2 = rng.nextInt(n);
                        int tmp = a[i1]; a[i1] = a[i2]; a[i2] = tmp;
                    }
                    break;
                case 3: // D4: Giam dan
                    for (int i = 0; i < n; i++) {
                        a[i] = rng.nextInt(Integer.MAX_VALUE);
                    }
                    Arrays.sort(a);
                    for (int i = 0; i < n / 2; i++) {
                        int tmp = a[i]; a[i] = a[n - 1 - i]; a[n - 1 - i] = tmp;
                    }
                    break;
                case 4: // D5: Nhieu khoa trung (K = 2 hoac 10)
                    int k = (rng.nextBoolean()) ? 2 : 10;
                    int[] keys = new int[k];
                    for (int i = 0; i < k; i++) {
                        keys[i] = rng.nextInt(Integer.MAX_VALUE);
                    }
                    for (int i = 0; i < n; i++) {
                        a[i] = keys[rng.nextInt(k)];
                    }
                    break;
                default:
                    break;
            }

            assertSortCorrect(a);
        }
    }

    // TEST 5: Kiem tra kha nang chiu tai tren mang cuc lon 100.000 phan tu
    @Test
    @DisplayName("Kiểm thử 100.000 phần tử: bằng nhau, tăng dần, giảm dần")
    public void testLarge100KArrays() {
        int n = 100_000;

        // 1. Toan bo bang nhau
        int[] equalArr = new int[n];
        Arrays.fill(equalArr, 123456789);
        assertSortCorrect(equalArr);

        // 2. Tang dan
        int[] sortedArr = new int[n];
        for (int i = 0; i < n; i++) {
            sortedArr[i] = i * 2;
        }
        assertSortCorrect(sortedArr);

        // 3. Giam dan
        int[] reverseArr = new int[n];
        for (int i = 0; i < n; i++) {
            reverseArr[i] = (n - i) * 3;
        }
        assertSortCorrect(reverseArr);
    }

    // TEST 6: Kiem tra bo dem so phep so sanh cho RQ2
    @Test
    @DisplayName("Kiểm thử hàm sortWithCount và sortRangeWithCount")
    public void testCountFunctionality() {
        int[] a = new int[]{5, 1, 4, 1, 5, 9, 2, 6, 5, 3, 5};
        int[] expected = a.clone();
        Arrays.sort(expected);

        long cmpCount = HeapSort.sortWithCount(a);
        assertArrayEquals(expected, a);
        assertTrue(cmpCount > 0, "Số phép so sánh khóa phải lớn hơn 0");

        // Kiem tra voi mang 1 phan tu va mang rong
        assertEquals(0L, HeapSort.sortWithCount(new int[]{}));
        assertEquals(0L, HeapSort.sortWithCount(new int[]{42}));

        // Kiem tra sortRangeWithCount
        int[] b = new int[]{9, 8, 7, 6, 5, 4, 3, 2, 1};
        long rangeCount = HeapSort.sortRangeWithCount(b, 2, 7);
        assertTrue(rangeCount > 0);
    }

    // Ham tro giup: Chay HeapSort va doi chieu 100% voi Arrays.sort cua Java
    private void assertSortCorrect(int[] a) {
        int[] expected = a.clone();
        Arrays.sort(expected);
        HeapSort.sort(a);
        assertArrayEquals(expected, a, "Mảng sau khi sắp xếp không khớp với Arrays.sort()");
    }
}
