package csd201.sort;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.SplittableRandom;

import static org.junit.jupiter.api.Assertions.*;

public class MergeSortTest {

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
    @DisplayName("Kiểm thử mảng rỗng, 1 phần tử, 2 phần tử và các giá trị biên")
    public void testEdgeValuesAndSizes() {
        assertSortCorrect(new int[]{});
        assertSortCorrect(new int[]{42});
        assertSortCorrect(new int[]{100, 50});
        assertSortCorrect(new int[]{50, 100});
        assertSortCorrect(new int[]{7, 7, 7, 7});

        int[] boundaryValues = new int[]{0, 255, 256, 65535, Integer.MAX_VALUE, 1, 1024};
        assertSortCorrect(boundaryValues);

        // Kiểm tra null ném ngoại lệ
        assertThrows(IllegalArgumentException.class, () -> MergeSort.sort(null));
        assertThrows(IllegalArgumentException.class, () -> MergeSort.sortWithCount(null));
    }

    @Test
    @DisplayName("Kiểm thử sortRange trên đoạn [lo, hi) và đảm bảo phần tử ngoài đoạn không đổi")
    public void testSortRangeIntegrity() {
        int[] original = new int[]{99, 88, 77, 15, 3, 9, 2, 8, 44, 33, 22};
        int lo = 3;
        int hi = 8; // đoạn con gồm [15, 3, 9, 2, 8]

        int[] a = original.clone();
        MergeSort.sortRange(a, lo, hi);

        // Kiểm tra các phần tử trước lo không thay đổi
        for (int i = 0; i < lo; i++) {
            assertEquals(original[i], a[i], "Phần tử tại index " + i + " bên ngoài đoạn đã bị thay đổi!");
        }

        // Kiểm tra các phần tử sau hi không thay đổi
        for (int i = hi; i < original.length; i++) {
            assertEquals(original[i], a[i], "Phần tử tại index " + i + " bên ngoài đoạn đã bị thay đổi!");
        }

        // Kiểm tra đoạn con [lo, hi) đã được sắp xếp tăng dần đúng
        int[] subArrayExpected = Arrays.copyOfRange(original, lo, hi);
        Arrays.sort(subArrayExpected);
        for (int i = 0; i < subArrayExpected.length; i++) {
            assertEquals(subArrayExpected[i], a[lo + i], "Đoạn con chưa được sắp xếp đúng tại index " + (lo + i));
        }

        // Kiểm tra đoạn rỗng và đoạn 1 phần tử
        int[] b = original.clone();
        MergeSort.sortRange(b, 4, 4); // rỗng
        assertArrayEquals(original, b);

        MergeSort.sortRange(b, 4, 5); // 1 phần tử
        assertArrayEquals(original, b);

        // Kiểm tra ngoại lệ biên
        assertThrows(IndexOutOfBoundsException.class, () -> MergeSort.sortRange(b, -1, 5));
        assertThrows(IndexOutOfBoundsException.class, () -> MergeSort.sortRange(b, 2, b.length + 1));
        assertThrows(IndexOutOfBoundsException.class, () -> MergeSort.sortRange(b, 5, 2));
    }

    @Test
    @DisplayName("1.000 mảng ngẫu nhiên kích thước 0-100 thuộc họ D1-D5")
    public void test1000RandomArraysD1ToD5() {
        SplittableRandom rng = new SplittableRandom(20261025L);

        for (int t = 0; t < 1000; t++) {
            int n = rng.nextInt(101); // 0 đến 100
            int[] a = new int[n];
            int type = t % 5;

            switch (type) {
                case 0 -> { // D1: Ngẫu nhiên
                    for (int i = 0; i < n; i++) {
                        a[i] = rng.nextInt(Integer.MAX_VALUE);
                    }
                }
                case 1 -> { // D2: Tăng dần
                    for (int i = 0; i < n; i++) {
                        a[i] = rng.nextInt(Integer.MAX_VALUE);
                    }
                    Arrays.sort(a);
                }
                case 2 -> { // D3: Gần tăng dần
                    for (int i = 0; i < n; i++) {
                        a[i] = rng.nextInt(Integer.MAX_VALUE);
                    }
                    Arrays.sort(a);
                    if (n > 1) {
                        int i1 = rng.nextInt(n);
                        int i2 = rng.nextInt(n);
                        int tmp = a[i1]; a[i1] = a[i2]; a[i2] = tmp;
                    }
                }
                case 3 -> { // D4: Giảm dần
                    for (int i = 0; i < n; i++) {
                        a[i] = rng.nextInt(Integer.MAX_VALUE);
                    }
                    Arrays.sort(a);
                    for (int i = 0; i < n / 2; i++) {
                        int tmp = a[i]; a[i] = a[n - 1 - i]; a[n - 1 - i] = tmp;
                    }
                }
                case 4 -> { // D5: Nhiều khóa trùng (K = 2 hoặc 10)
                    int k = (rng.nextBoolean()) ? 2 : 10;
                    int[] keys = new int[k];
                    for (int i = 0; i < k; i++) {
                        keys[i] = rng.nextInt(Integer.MAX_VALUE);
                    }
                    for (int i = 0; i < n; i++) {
                        a[i] = keys[rng.nextInt(k)];
                    }
                }
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
        Arrays.fill(equalArr, 123456789);
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
    @DisplayName("Kiểm thử chế độ đếm: comparisons, max_depth và không bỏ qua trộn khi đã có thứ tự")
    public void testCountModeComparisonsAndDepth() {
        // Mảng rỗng và mảng 1 phần tử
        long packed0 = MergeSort.sortWithCount(new int[]{});
        assertEquals(0, MergeSort.unpackDepth(packed0));
        assertEquals(0L, MergeSort.unpackComparisons(packed0));

        long packed1 = MergeSort.sortWithCount(new int[]{42});
        assertEquals(0, MergeSort.unpackDepth(packed1));
        assertEquals(0L, MergeSort.unpackComparisons(packed1));

        // Mảng 2 phần tử ngược thứ tự
        int[] twoRev = new int[]{2, 1};
        long packed2Rev = MergeSort.sortWithCount(twoRev);
        assertArrayEquals(new int[]{1, 2}, twoRev);
        assertEquals(1, MergeSort.unpackDepth(packed2Rev));
        assertEquals(1L, MergeSort.unpackComparisons(packed2Rev));

        // Mảng 2 phần tử ĐÃ CÓ THỨ TỰ: bắt buộc không được bỏ qua trộn (vẫn phải có 1 so sánh)
        int[] twoSorted = new int[]{1, 2};
        long packed2Sorted = MergeSort.sortWithCount(twoSorted);
        assertArrayEquals(new int[]{1, 2}, twoSorted);
        assertEquals(1, MergeSort.unpackDepth(packed2Sorted));
        assertEquals(1L, MergeSort.unpackComparisons(packed2Sorted),
                "Bắt buộc không được thêm tối ưu bỏ qua trộn if (a[mid] <= a[mid+1])");

        // Đo độ sâu đệ quy lớn nhất trên các lũy thừa của 2: N = 2^k -> max_depth = k
        for (int k = 1; k <= 10; k++) {
            int n = 1 << k; // 2, 4, 8, ..., 1024
            int[] arr = new int[n];
            for (int i = 0; i < n; i++) {
                arr[i] = n - i;
            }
            long packed = MergeSort.sortWithCount(arr);
            int depth = MergeSort.unpackDepth(packed);
            long cmp = MergeSort.unpackComparisons(packed);

            assertEquals(k, depth, "Độ sâu đệ quy với N=" + n + " (2^" + k + ") phải bằng " + k);
            assertTrue(cmp > 0, "Số phép so sánh khóa phải > 0");
        }

        // Kiểm tra sortRangeWithCount
        int[] b = new int[]{9, 8, 7, 6, 5, 4, 3, 2, 1};
        long rangePacked = MergeSort.sortRangeWithCount(b, 2, 6); // 4 phần tử [7, 6, 5, 4] -> max_depth = 2
        assertEquals(2, MergeSort.unpackDepth(rangePacked));
        assertTrue(MergeSort.unpackComparisons(rangePacked) > 0);
    }

    private void assertSortCorrect(int[] original) {
        int[] expected = original.clone();
        Arrays.sort(expected);

        int[] actual = original.clone();
        MergeSort.sort(actual);

        assertArrayEquals(expected, actual, "Kết quả MergeSort không khớp với Arrays.sort!");
    }
}
