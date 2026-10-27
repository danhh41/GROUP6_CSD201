package csd201.sort;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.SplittableRandom;

import static org.junit.jupiter.api.Assertions.*;

public class RadixSortTest {

    @Test
    @DisplayName("5 ca kiểm thử cố định theo đề bài")
    public void testFixedCases() {
        assertSortCorrect(new int[]{7});
        assertSortCorrect(new int[]{2, 1});
        assertSortCorrect(new int[]{3, 3, 3});
        assertSortCorrect(new int[]{0, Integer.MAX_VALUE, 0});
        assertSortCorrect(new int[]{5, 1, 4, 1, 5, 9, 2, 6, 5, 3, 5});
    }

    @Test
    @DisplayName("Kiểm thử mảng rỗng, 1 phần tử, 2 phần tử và các giá trị biên đặc biệt")
    public void testEdgeValuesAndSizes() {
        assertSortCorrect(new int[]{});
        assertSortCorrect(new int[]{42});
        assertSortCorrect(new int[]{100, 50});
        assertSortCorrect(new int[]{50, 100});
        assertSortCorrect(new int[]{7, 7, 7, 7});

        // Kiểm tra các giá trị chuyển tiếp byte cơ số 256
        int[] boundaryValues = new int[]{0, 255, 256, 65535, 65536, 16777215, 16777216, Integer.MAX_VALUE, 1, 1024};
        assertSortCorrect(boundaryValues);
    }

    @Test
    @DisplayName("1.000 mảng ngẫu nhiên kích thước 0-100 thuộc họ D1-D5")
    public void test1000RandomArraysD1ToD5() {
        SplittableRandom rng = new SplittableRandom(20261021L);

        for (int t = 0; t < 1000; t++) {
            int n = rng.nextInt(101); // 0 đến 100
            int[] a = new int[n];
            int type = t % 5;

            switch (type) {
                case 0: // D1: Ngẫu nhiên (0 đến 2^31 - 1)
                    for (int i = 0; i < n; i++) {
                        a[i] = rng.nextInt(Integer.MAX_VALUE);
                    }
                    break;
                case 1: // D2: Tăng dần
                    for (int i = 0; i < n; i++) {
                        a[i] = rng.nextInt(Integer.MAX_VALUE);
                    }
                    Arrays.sort(a);
                    break;
                case 2: // D3: Gần tăng dần
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
                case 3: // D4: Giảm dần
                    for (int i = 0; i < n; i++) {
                        a[i] = rng.nextInt(Integer.MAX_VALUE);
                    }
                    Arrays.sort(a);
                    for (int i = 0; i < n / 2; i++) {
                        int tmp = a[i]; a[i] = a[n - 1 - i]; a[n - 1 - i] = tmp;
                    }
                    break;
                case 4: // D5: Nhiều khóa trùng (K = 2 hoặc 10)
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

    @Test
    @DisplayName("Kiểm thử 100.000 phần tử: bằng nhau, tăng dần, giảm dần")
    public void testLarge100KArrays() {
        int n = 100_000;

        // 1. Toàn bộ bằng nhau
        int[] equalArr = new int[n];
        Arrays.fill(equalArr, 987654321);
        assertSortCorrect(equalArr);

        // 2. Tăng dần
        int[] sortedArr = new int[n];
        for (int i = 0; i < n; i++) {
            sortedArr[i] = i * 2;
        }
        assertSortCorrect(sortedArr);

        // 3. Giảm dần
        int[] reverseArr = new int[n];
        for (int i = 0; i < n; i++) {
            reverseArr[i] = (n - i) * 3;
        }
        assertSortCorrect(reverseArr);
    }

    @Test
    @DisplayName("Kiểm thử hàm sortWithCount và công thức đếm đọc/ghi")
    public void testCountFunctionality() {
        int[] a = new int[]{5, 1, 4, 1, 5, 9, 2, 6, 5, 3, 5};
        int n = a.length;
        int[] expected = a.clone();
        Arrays.sort(expected);

        long ops = RadixSort.sortWithCount(a);
        assertArrayEquals(expected, a);

        // Đúng 4 pass * 3N thao tác đọc/ghi trên mảng dữ liệu = 12 * N
        assertEquals(12L * n, ops, "Số thao tác đọc/ghi mảng dữ liệu phải bằng chính xác 12 * N");

        // Kiểm tra mảng rỗng và mảng 1 phần tử
        assertEquals(0L, RadixSort.sortWithCount(new int[]{}));
        assertEquals(0L, RadixSort.sortWithCount(new int[]{123}));
    }

    private void assertSortCorrect(int[] original) {
        int[] expected = original.clone();
        Arrays.sort(expected);

        int[] actual = original.clone();
        RadixSort.sort(actual);

        assertArrayEquals(expected, actual, "Kết quả RadixSort không khớp với Arrays.sort!");
    }
}
