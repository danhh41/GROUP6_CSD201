package csd201.sort;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.SplittableRandom;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Bộ kiểm thử toàn diện cho thuật toán Quick Sort (Người 1 - Cặp 1).
 * <p>
 * Đáp ứng đầy đủ các yêu cầu tại Mục 4, Mục 5 và Mục 6 của đề tài:
 * <ul>
 *   <li>5 ca kiểm thử cố định theo đề bài.</li>
 *   <li>Kiểm thử các mảng biên, mảng rỗng, 1 phần tử và ngoại lệ null.</li>
 *   <li>Bảo toàn tính toàn vẹn khi gọi {@link QuickSort#sortRange(int[], int, int)}.</li>
 *   <li>1.000 mảng ngẫu nhiên kích thước 0..100 thuộc đủ các họ D1–D5 so với oracle Arrays.sort.</li>
 *   <li>Mảng lớn 10^5 phần tử: toàn bằng nhau, tăng dần, giảm dần.</li>
 *   <li>Lượt --count: giải mã max_depth và số phép so sánh.</li>
 *   <li>Ca kiểm thử đối kháng (Killer Adversary theo Musser 1997) đánh giá suy giảm O(N^2) và so sánh đối chứng với pdqsort.</li>
 * </ul>
 * </p>
 */
public class QuickSortTest {

    // =========================================================================
    // 1. 5 ca kiểm thử cố định theo Mục 5 đề bài
    // =========================================================================

    @Test
    @DisplayName("5 ca kiểm thử cố định theo Mục 5 đề bài")
    public void testFixedCases() {
        assertSortCorrect(new int[]{7});
        assertSortCorrect(new int[]{2, 1});
        assertSortCorrect(new int[]{3, 3, 3});
        assertSortCorrect(new int[]{0, Integer.MAX_VALUE, 0});
        assertSortCorrect(new int[]{5, 1, 4, 1, 5, 9, 2, 6, 5, 3, 5});
    }

    // =========================================================================
    // 2. Mảng rỗng, 1 phần tử, 2 phần tử và các giá trị biên
    // =========================================================================

    @Test
    @DisplayName("Kiểm thử mảng rỗng, 1 phần tử, 2 phần tử và ngoại lệ null")
    public void testEdgeValuesAndSizes() {
        assertSortCorrect(new int[]{});
        assertSortCorrect(new int[]{42});
        assertSortCorrect(new int[]{100, 50});
        assertSortCorrect(new int[]{50, 100});
        assertSortCorrect(new int[]{7, 7, 7, 7});

        int[] boundaryValues = new int[]{0, 255, 256, 65535, Integer.MAX_VALUE, 1, 1024};
        assertSortCorrect(boundaryValues);

        // Kiểm tra null ném ngoại lệ
        assertThrows(IllegalArgumentException.class, () -> QuickSort.sort(null));
        assertThrows(IllegalArgumentException.class, () -> QuickSort.sortWithCount(null));
        assertThrows(IllegalArgumentException.class, () -> QuickSort.sortRange(null, 0, 1));
    }

    // =========================================================================
    // 3. sortRange trên đoạn nửa mở [lo, hi)
    // =========================================================================

    @Test
    @DisplayName("sortRange chỉ sắp đoạn [lo, hi), phần tử ngoài đoạn giữ nguyên tuyệt đối")
    public void testSortRangeIntegrity() {
        int[] original = new int[]{99, 88, 77, 15, 3, 9, 2, 8, 44, 33, 22};
        int lo = 3;
        int hi = 8; // đoạn con [15, 3, 9, 2, 8]

        int[] a = original.clone();
        QuickSort.sortRange(a, lo, hi);

        // Kiểm tra phần tử trước lo không đổi
        for (int i = 0; i < lo; i++) {
            assertEquals(original[i], a[i], "Phần tử tại index " + i + " bên ngoài đoạn đã bị thay đổi!");
        }

        // Kiểm tra phần tử từ hi trở đi không đổi
        for (int i = hi; i < original.length; i++) {
            assertEquals(original[i], a[i], "Phần tử tại index " + i + " bên ngoài đoạn đã bị thay đổi!");
        }

        // Kiểm tra đoạn con [lo, hi) đã được sắp xếp tăng dần đúng
        int[] subArrayExpected = Arrays.copyOfRange(original, lo, hi);
        Arrays.sort(subArrayExpected);
        for (int i = 0; i < subArrayExpected.length; i++) {
            assertEquals(subArrayExpected[i], a[lo + i], "Đoạn con chưa sắp xếp đúng tại index " + (lo + i));
        }

        // Kiểm tra đoạn rỗng và đoạn 1 phần tử
        int[] b = original.clone();
        QuickSort.sortRange(b, 4, 4);
        assertArrayEquals(original, b);

        QuickSort.sortRange(b, 4, 5);
        assertArrayEquals(original, b);

        // Kiểm tra ngoại lệ biên
        assertThrows(IndexOutOfBoundsException.class, () -> QuickSort.sortRange(b, -1, 5));
        assertThrows(IndexOutOfBoundsException.class, () -> QuickSort.sortRange(b, 2, b.length + 1));
        assertThrows(IndexOutOfBoundsException.class, () -> QuickSort.sortRange(b, 5, 2));
    }

    // =========================================================================
    // 4. 1.000 mảng ngẫu nhiên kích thước 0..100 thuộc họ D1–D5
    // =========================================================================

    @Test
    @DisplayName("1.000 mảng ngẫu nhiên kích thước 0-100 thuộc họ D1-D5")
    public void test1000RandomArraysD1ToD5() {
        SplittableRandom rng = new SplittableRandom(20261028L);
        String[] kinds = {"D1", "D2", "D3", "D4", "D5"};
        int[] ks = {DataGenerator.K_1000, DataGenerator.K_10, DataGenerator.K_2};

        for (int t = 0; t < 1000; t++) {
            int n = rng.nextInt(101); // 0 đến 100
            String kind = kinds[t % kinds.length];
            int k = ks[(t / kinds.length) % ks.length];
            int[] a = DataGenerator.generate(kind, n, rng.nextLong(), k);
            assertSortCorrect(a);
        }
    }

    // =========================================================================
    // 5. Mảng lớn 10^5 phần tử: toàn bằng nhau, tăng dần, giảm dần
    // =========================================================================

    @Test
    @DisplayName("Mảng 10^5 phần tử: toàn bằng nhau, tăng dần, giảm dần")
    public void testLargeSpecialArrays() {
        int n = 100_000;

        // Mảng toàn bằng nhau
        int[] eq = new int[n];
        Arrays.fill(eq, 777);
        assertSortCorrect(eq);

        // Mảng tăng dần
        int[] asc = new int[n];
        for (int i = 0; i < n; i++) {
            asc[i] = i;
        }
        assertSortCorrect(asc);

        // Mảng giảm dần
        int[] desc = new int[n];
        for (int i = 0; i < n; i++) {
            desc[i] = n - i;
        }
        assertSortCorrect(desc);
    }

    // =========================================================================
    // 6. Lượt --count: Kiểm tra độ sâu đệ quy và số phép so sánh
    // =========================================================================

    @Test
    @DisplayName("Lượt --count: max_depth bậc O(log N) và comparisons bậc O(N log N) trên dữ liệu ngẫu nhiên D1")
    public void testCountBoundsOnRandomData() {
        int n = 100_000;
        int[] a = DataGenerator.generateD1(n, 42L);
        long packed = QuickSort.sortWithCount(a);

        assertTrue(isSorted(a), "Mảng chưa được sắp xếp đúng ở lượt --count");

        int depth = QuickSort.unpackDepth(packed);
        long comparisons = QuickSort.unpackComparisons(packed);

        double log2n = Math.log(n) / Math.log(2);

        // Trên mảng ngẫu nhiên phân hoạch Hoare median-of-3 cân bằng tốt:
        // Độ sâu thường trong khoảng 1.5 đến 2.5 * log2(N)
        assertTrue(depth > 0 && depth < 3.0 * log2n,
                "Độ sâu đệ quy bất thường: depth=" + depth + ", log2(N)=" + log2n);

        // Số phép so sánh nằm trong khoảng O(N log N)
        assertTrue(comparisons > n && comparisons < 3.0 * n * log2n,
                "Số phép so sánh bất thường: comparisons=" + comparisons);
    }

    // =========================================================================
    // 7. Ca kiểm thử đối kháng (Killer Adversary - Musser 1997)
    // =========================================================================

    @Test
    @DisplayName("Ca kiểm thử đối kháng: Hoán vị Musser ép QuickSort rơi vào O(N^2) và so sánh đối chứng với pdqsort")
    public void testMusserKillerAdversary() {
        // Kiểm tra ở kích thước N = 2.000 để đánh giá rõ thoái hóa O(N^2)
        int n = 2000;
        int[] killerQuick = DataGenerator.generateKillerAdversary(n);
        int[] killerPdq = killerQuick.clone();
        int[] expected = killerQuick.clone();
        Arrays.sort(expected);

        // 1. Chạy Quick Sort với dữ liệu Killer Adversary
        long packedQuick = QuickSort.sortWithCount(killerQuick);
        assertArrayEquals(expected, killerQuick, "Quick Sort sắp xếp sai trên dữ liệu Killer Adversary");

        int depthQuick = QuickSort.unpackDepth(packedQuick);
        long cmpQuick = QuickSort.unpackComparisons(packedQuick);

        // 2. Chạy pdqsort đối chứng trên cùng dữ liệu Killer Adversary
        long packedPdq = PdqSort.sortWithCount(killerPdq);
        assertArrayEquals(expected, killerPdq, "pdqsort sắp xếp sai trên dữ liệu Killer Adversary");

        int depthPdq = PdqSort.unpackDepth(packedPdq);
        long cmpPdq = PdqSort.unpackComparisons(packedPdq);

        // ĐÁNH GIÁ ĐỐI KHÁNG:
        // Quick Sort: do median-of-three bị đánh lừa, mỗi lượt chỉ cắt 1-2 phần tử:
        // Số phép so sánh Quick Sort tăng vọt ~ N^2 / 4 (với N=2000 là xấp xỉ ~1.000.000 so sánh)
        // Độ sâu đệ quy tăng vọt tới ~ N / 2 = 1000
        assertTrue(depthQuick >= 500, "Độ sâu đệ quy của Quick Sort phải thoái hóa O(N), thực tế: " + depthQuick);
        assertTrue(cmpQuick >= 500_000L, "Số so sánh của Quick Sort phải vọt lên bậc O(N^2), thực tế: " + cmpQuick);

        // pdqsort: Nhờ cơ chế chống trường hợp xấu (phá mẫu hoặc chuyển sang Heapsort):
        // Số phép so sánh được bảo toàn ở mức O(N log N) và độ sâu rất nhỏ
        double log2n = Math.log(n) / Math.log(2);
        assertTrue(cmpPdq < cmpQuick / 10, "pdqsort phải có số phép so sánh ít hơn vượt bậc so với Quick Sort bị phá");
        assertTrue(depthPdq < 3 * log2n, "pdqsort phải bảo đảm độ sâu O(log N)");

        System.out.printf("[KILLER ADVERSARY N=%d] QuickSort: cmp=%d, depth=%d | pdqsort: cmp=%d, depth=%d%n",
                n, cmpQuick, depthQuick, cmpPdq, depthPdq);
    }

    @Test
    @DisplayName("Kiểm tra tính đúng đắn của dữ liệu Killer Adversary ở các kích thước N = 10, 100, 1.000, 5.000")
    public void testMusserKillerCorrectnessVariousSizes() {
        int[] sizes = {10, 100, 1_000, 5_000};
        for (int n : sizes) {
            int[] killer = DataGenerator.generateKillerAdversary(n);
            int[] expected = killer.clone();
            Arrays.sort(expected);

            QuickSort.sort(killer);
            assertArrayEquals(expected, killer, "QuickSort không sắp xếp đúng với killer sequence N=" + n);
        }
    }

    // =========================================================================
    // Tiện ích kiểm tra
    // =========================================================================

    private static void assertSortCorrect(int[] input) {
        int[] expected = input.clone();
        Arrays.sort(expected);

        // Kiểm tra lượt sort() thông thường
        int[] a = input.clone();
        QuickSort.sort(a);
        assertArrayEquals(expected, a, "sort() sai với mảng kích thước " + input.length);

        // Kiểm tra lượt sortWithCount()
        int[] b = input.clone();
        QuickSort.sortWithCount(b);
        assertArrayEquals(expected, b, "sortWithCount() sai với mảng kích thước " + input.length);
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
