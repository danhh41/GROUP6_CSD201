package csd201.sort;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.SplittableRandom;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Kiểm thử phần lõi pdqsort (Người 5 – Cặp 3).
 * Bộ kiểm thử chính thức cho mọi thuật toán và biến thể noA/noB là phần của Người 6.
 */
public class PdqSortTest {

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
    @DisplayName("Mảng rỗng và mảng null")
    public void testEmptyAndNull() {
        assertSortCorrect(new int[]{});
        assertThrows(IllegalArgumentException.class, () -> PdqSort.sort(null));
        assertThrows(IllegalArgumentException.class, () -> PdqSort.sortWithCount(null));
    }

    @Test
    @DisplayName("1.000 mảng ngẫu nhiên kích thước 0..100 thuộc D1–D5")
    public void testRandomSmallArrays() {
        runRandomArrays(1000, 100, 2026);
    }

    @Test
    @DisplayName("1.000 mảng kích thước 0..5.000 (đi qua ninther, phá mẫu và Heap Sort dự phòng)")
    public void testRandomMediumArrays() {
        runRandomArrays(1000, 5000, 7);
    }

    @Test
    @DisplayName("Mảng 10^5 phần tử: toàn bằng nhau, tăng dần, giảm dần")
    public void testLargeSpecialArrays() {
        int n = 100_000;
        int[] eq = new int[n];
        Arrays.fill(eq, 7);
        assertSortCorrect(eq);

        int[] asc = new int[n];
        int[] desc = new int[n];
        for (int i = 0; i < n; i++) {
            asc[i] = i;
            desc[i] = n - i;
        }
        assertSortCorrect(asc);
        assertSortCorrect(desc);
    }

    @Test
    @DisplayName("Ống đàn organ và răng cưa (mẫu làm phân hoạch lệch)")
    public void testPatternArrays() {
        int n = 100_000;
        int[] organ = new int[n];
        int[] saw = new int[n];
        for (int i = 0; i < n; i++) {
            organ[i] = (i < n / 2) ? i : n - i;
            saw[i] = i % 97;
        }
        assertSortCorrect(organ);
        assertSortCorrect(saw);
    }

    @Test
    @DisplayName("sortRange chỉ sắp đoạn [lo, hi), phần tử ngoài đoạn giữ nguyên")
    public void testSortRangeIntegrity() {
        int[] original = DataGenerator.generateD1(1000, 3);
        int lo = 100;
        int hi = 900;
        int[] a = original.clone();
        PdqSort.sortRange(a, lo, hi);

        for (int i = 0; i < lo; i++) {
            assertEquals(original[i], a[i]);
        }
        for (int i = hi; i < a.length; i++) {
            assertEquals(original[i], a[i]);
        }
        int[] expected = Arrays.copyOfRange(original, lo, hi);
        Arrays.sort(expected);
        assertArrayEquals(expected, Arrays.copyOfRange(a, lo, hi));
        assertThrows(IndexOutOfBoundsException.class, () -> PdqSort.sortRange(a, 5, 2));
    }

    @Test
    @DisplayName("Lượt --count: max_depth bậc log2(N), so sánh bậc N log N")
    public void testCountBounds() {
        int n = 1_000_000;
        int[] a = DataGenerator.generateD1(n, 1);
        long packed = PdqSort.sortWithCount(a);
        assertTrue(isSorted(a));

        int depth = PdqSort.unpackDepth(packed);
        long cmp = PdqSort.unpackComparisons(packed);
        double log2n = Math.log(n) / Math.log(2);
        assertTrue(depth > 0 && depth <= 2 * Math.ceil(log2n), "max_depth = " + depth);
        assertTrue(cmp > n && cmp < 2 * n * log2n, "comparisons = " + cmp);
    }

    @Test
    @DisplayName("Khóa trùng không có cơ chế B: chuyển sang HeapSort.sortRange và vẫn đúng")
    public void testHeapFallbackOnDuplicates() {
        int[] a = DataGenerator.generateD5(200_000, 1, DataGenerator.K_2);
        int[] expected = a.clone();
        Arrays.sort(expected);

        PdqSort p = new PdqSort(true, false, false);
        p.run(a, 0, a.length);
        assertArrayEquals(expected, a);
        assertTrue(p.getHeapFallbacks() > 0, "Phải có ít nhất một lần chuyển sang Heap Sort");
    }

    // -------------------------------------------------------------------------

    private static void runRandomArrays(int count, int maxSize, long seed) {
        SplittableRandom r = new SplittableRandom(seed);
        String[] kinds = {"D1", "D2", "D3", "D4", "D5"};
        int[] ks = {DataGenerator.K_1000, DataGenerator.K_10, DataGenerator.K_2};
        for (int t = 0; t < count; t++) {
            int n = r.nextInt(maxSize + 1);
            String kind = kinds[t % kinds.length];
            int k = ks[(t / kinds.length) % ks.length];
            int[] a = DataGenerator.generate(kind, n, r.nextLong(), k);
            assertSortCorrect(a);
        }
    }

    /** Kiểm tra cả lượt đo thời gian (sort) và lượt --count (sortWithCount). */
    private static void assertSortCorrect(int[] input) {
        int[] expected = input.clone();
        Arrays.sort(expected);

        int[] x = input.clone();
        PdqSort.sort(x);
        assertArrayEquals(expected, x, "sort() sai với n = " + input.length);

        int[] y = input.clone();
        PdqSort.sortWithCount(y);
        assertArrayEquals(expected, y, "sortWithCount() sai với n = " + input.length);
    }

    private static boolean isSorted(int[] a) {
        for (int i = 1; i < a.length; i++) {
            if (a[i - 1] > a[i]) {
                return false;
            }
        }
        return true;
    }
}
