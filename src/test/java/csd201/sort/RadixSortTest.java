package csd201.sort;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.SplittableRandom;

import static org.junit.jupiter.api.Assertions.*;

public class RadixSortTest {

    // TEST 1: Kiem tra 5 ca co dinh bat buoc theo barem de bai
    @Test
    @DisplayName("5 ca kiểm thử cố định theo đề bài")
    public void testFixedCases() {
        assertSortCorrect(new int[]{7}); // 1 phan tu
        assertSortCorrect(new int[]{2, 1}); // 2 phan tu bi nguoc
        assertSortCorrect(new int[]{3, 3, 3}); // toan so bang nhau
        assertSortCorrect(new int[]{0, Integer.MAX_VALUE, 0}); // so cuc dai chong tran bit
        assertSortCorrect(new int[]{5, 1, 4, 1, 5, 9, 2, 6, 5, 3, 5}); // mang chu so Pi
    }

    // TEST 2: Kiem tra cac truong hop bien va ranh gioi byte (255, 256)
    @Test
    @DisplayName("Kiểm thử mảng rỗng, 1 phần tử, 2 phần tử và các giá trị biên")
    public void testEdgeValuesAndSizes() {
        assertSortCorrect(new int[]{}); // mang rong n=0
        assertSortCorrect(new int[]{42}); // mang 1 phan tu
        assertSortCorrect(new int[]{100, 50}); // 2 phan tu nguoc
        assertSortCorrect(new int[]{50, 100}); // 2 phan tu xuoi
        assertSortCorrect(new int[]{7, 7, 7, 7}); // toan so trung lap

        // Ranh gioi chuyen giao Byte 0 sang Byte 1 (255 sang 256) va Byte 2 sang 3 (65535)
        int[] boundaryValues = new int[]{0, 255, 256, 65535, Integer.MAX_VALUE, 1, 1024};
        assertSortCorrect(boundaryValues);
    }

    // TEST 3: Kiem tra do ben voi 1.000 mang ngau nhien du loai D1-D5
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

    // TEST 4: Kiem tra kha nang chiu tai tren mang lon 100.000 phan tu
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

    // TEST 5: Kiem tra bo dem thao tac doc/ghi phai dung bang 12N cho RQ2
    @Test
    @DisplayName("Kiểm thử hàm sortWithCount trả về đúng 12N thao tác")
    public void testCountFunctionality() {
        int[] a = new int[]{5, 1, 4, 1, 5, 9, 2, 6, 5, 3, 5}; // n = 11
        int[] expected = a.clone();
        Arrays.sort(expected);

        long ops = RadixSort.sortWithCount(a);
        assertArrayEquals(expected, a);
        // Voi n = 11, 4 luot x 3N = dung 132 thao tac doc/ghi
        assertEquals(11L * 12L, ops, "Số thao tác đọc/ghi phải đúng bằng 12N");

        // Kiem tra mang rong va mang 1 phan tu
        assertEquals(0L, RadixSort.sortWithCount(new int[]{}));
        assertEquals(0L, RadixSort.sortWithCount(new int[]{42}));
    }

    // Ham tro giup: Chay RadixSort va doi chieu 100% voi Arrays.sort cua Java
    private void assertSortCorrect(int[] a) {
        int[] expected = a.clone();
        Arrays.sort(expected);
        RadixSort.sort(a);
        assertArrayEquals(expected, a, "Mảng sau khi sắp xếp không khớp với Arrays.sort()");
    }
}
