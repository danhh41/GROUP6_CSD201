package csd201.sort;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Kiểm thử toàn diện cho {@link DataGenerator} (Người 2 - Cặp 1).
 */
public class DataGeneratorTest {

    private static final long SEED = 20261025L;

    @Test
    @DisplayName("D1: Kiểm tra miền giá trị không âm [0, 2^31 - 1], tính tái lập và kích thước")
    public void testD1Properties() {
        int n = 10_000;
        int[] a1 = DataGenerator.generateD1(n, SEED);
        int[] a2 = DataGenerator.generateD1(n, SEED);
        int[] a3 = DataGenerator.generateD1(n, SEED + 1);

        assertEquals(n, a1.length);
        // Tái lập hoàn toàn với cùng seed
        assertArrayEquals(a1, a2);
        // Seed khác phải sinh mảng khác
        assertFalse(Arrays.equals(a1, a3));

        // Kiểm tra mọi phần tử đều là số nguyên không âm [0, 2^31 - 1]
        for (int x : a1) {
            assertTrue(x >= 0, "Phần tử phải không âm: " + x);
            assertTrue(x <= Integer.MAX_VALUE, "Phần tử không vượt quá Integer.MAX_VALUE");
        }
    }

    @Test
    @DisplayName("D2: Kiểm tra tính tăng dần và bảo toàn tập giá trị của D1")
    public void testD2Properties() {
        int n = 5_000;
        int[] d1 = DataGenerator.generateD1(n, SEED);
        int[] d2 = DataGenerator.generateD2(n, SEED);

        assertEquals(n, d2.length);

        // Kiểm tra tăng dần
        for (int i = 1; i < n; i++) {
            assertTrue(d2[i - 1] <= d2[i], "D2 phải sắp tăng dần tại vị trí " + i);
        }

        // Kiểm tra giữ nguyên tập giá trị của D1
        int[] d1Sorted = d1.clone();
        Arrays.sort(d1Sorted);
        assertArrayEquals(d1Sorted, d2, "D2 phải là bản sắp xếp của D1 cùng seed");

        // Kiểm tra phương thức generateD2From
        int[] fromD1 = DataGenerator.generateD2From(d1);
        assertArrayEquals(d2, fromD1);
    }

    @Test
    @DisplayName("D3: Kiểm tra gần tăng dần, bảo toàn tập giá trị và đúng số cặp hoán đổi")
    public void testD3Properties() {
        int n = 10_000;
        int[] d2 = DataGenerator.generateD2(n, SEED);
        int[] d3 = DataGenerator.generateD3(n, SEED);

        assertEquals(n, d3.length);

        // Sau khi sắp xếp lại d3, phải khớp hoàn toàn với d2 (cùng tập giá trị)
        int[] d3Sorted = d3.clone();
        Arrays.sort(d3Sorted);
        assertArrayEquals(d2, d3Sorted, "D3 sau khi sắp lại phải có cùng tập giá trị với D2");

        // Với N = 10.000, số cặp hoán đổi là 10.000 / 1.000 = 10 cặp (tối đa 20 vị trí
        // thay đổi)
        int diffCount = 0;
        for (int i = 0; i < n; i++) {
            if (d2[i] != d3[i]) {
                diffCount++;
            }
        }
        // Số vị trí bị lệch không vượt quá 2 * (N / 1000)
        assertTrue(diffCount <= 20, "Số vị trí khác biệt giữa D2 và D3 không quá 20: " + diffCount);

        // Kiểm tra với N < 1000: N / 1000 = 0 -> không hoán đổi cặp nào, D3 phải giống
        // D2
        int smallN = 500;
        int[] smallD2 = DataGenerator.generateD2(smallN, SEED);
        int[] smallD3 = DataGenerator.generateD3(smallN, SEED);
        assertArrayEquals(smallD2, smallD3, "Khi N < 1000, D3 phải bằng D2 do số cặp hoán đổi là 0");

        // Kiểm tra generateD3FromSorted
        int[] fromD2 = DataGenerator.generateD3FromSorted(d2, SEED);
        assertArrayEquals(d3, fromD2);
    }

    @Test
    @DisplayName("D4: Kiểm tra giảm dần và bảo toàn tập giá trị")
    public void testD4Properties() {
        int n = 5_000;
        int[] d2 = DataGenerator.generateD2(n, SEED);
        int[] d4 = DataGenerator.generateD4(n, SEED);

        assertEquals(n, d4.length);

        // Kiểm tra giảm dần
        for (int i = 1; i < n; i++) {
            assertTrue(d4[i - 1] >= d4[i], "D4 phải sắp giảm dần tại vị trí " + i);
        }

        // Đảo ngược D4 phải thu được D2
        for (int i = 0; i < n; i++) {
            assertEquals(d2[i], d4[n - 1 - i], "D4 phải là D2 đảo ngược");
        }

        // Kiểm tra generateD4FromSorted
        int[] fromD2 = DataGenerator.generateD4FromSorted(d2);
        assertArrayEquals(d4, fromD2);
    }

    @Test
    @DisplayName("D5: Kiểm tra sinh đúng K giá trị phân biệt và mảng phân bố đều trên K giá trị đó")
    public void testD5Properties() {
        int[] kValues = { DataGenerator.K_1000, DataGenerator.K_10, DataGenerator.K_2 };

        for (int k : kValues) {
            // Kiểm tra K khóa phân biệt
            int[] keys = DataGenerator.generateDistinctKeys(SEED, k);
            assertEquals(k, keys.length, "Phải sinh đúng K khóa");

            Set<Integer> keySet = new HashSet<>();
            for (int key : keys) {
                assertTrue(key >= 0, "Khóa phải không âm");
                keySet.add(key);
            }
            assertEquals(k, keySet.size(), "Tất cả K khóa phải tuyệt đối phân biệt");

            // Kiểm tra mảng D5 được tạo thành từ đúng tập K khóa này
            int n = 20_000;
            int[] d5 = DataGenerator.generateD5(n, SEED, k);
            assertEquals(n, d5.length);

            Set<Integer> seenInArray = new HashSet<>();
            for (int val : d5) {
                assertTrue(keySet.contains(val), "Mọi phần tử trong D5 phải thuộc tập K khóa đã sinh");
                seenInArray.add(val);
            }

            // Với N = 20.000 và K nhỏ (10 hoặc 2), tất cả K khóa gần như chắc chắn đều xuất
            // hiện
            if (k <= 10) {
                assertEquals(k, seenInArray.size(), "Với K=" + k + " và N=" + n + ", mọi khóa đều phải xuất hiện");
            }
        }

        // Kiểm tra các helper tiện ích D5_1000, D5_10, D5_2
        int n = 1_000;
        assertArrayEquals(DataGenerator.generateD5(n, SEED, DataGenerator.K_1000),
                DataGenerator.generateD5_1000(n, SEED));
        assertArrayEquals(DataGenerator.generateD5(n, SEED, DataGenerator.K_10), DataGenerator.generateD5_10(n, SEED));
        assertArrayEquals(DataGenerator.generateD5(n, SEED, DataGenerator.K_2), DataGenerator.generateD5_2(n, SEED));
    }

    @Test
    @DisplayName("Kiểm tra điều phối tổng quát generate(...) với các mã họ dữ liệu")
    public void testUniversalDispatcher() {
        int n = 1_000;
        assertArrayEquals(DataGenerator.generateD1(n, SEED), DataGenerator.generate("D1", n, SEED, 0));
        assertArrayEquals(DataGenerator.generateD2(n, SEED), DataGenerator.generate("d2", n, SEED, 0));
        assertArrayEquals(DataGenerator.generateD3(n, SEED), DataGenerator.generate("D3", n, SEED, 0));
        assertArrayEquals(DataGenerator.generateD4(n, SEED), DataGenerator.generate("d4", n, SEED, 0));
        assertArrayEquals(DataGenerator.generateD5(n, SEED, 1000), DataGenerator.generate("D5", n, SEED, 1000));
        assertArrayEquals(DataGenerator.generateD5_10(n, SEED), DataGenerator.generate("D5-10", n, SEED, 0));
        assertArrayEquals(DataGenerator.generateD5_2(n, SEED), DataGenerator.generate("D5_2", n, SEED, 0));

        // Kiểm tra ngoại lệ với mã không hợp lệ
        assertThrows(IllegalArgumentException.class, () -> DataGenerator.generate("UNKNOWN", n, SEED, 0));
        assertThrows(IllegalArgumentException.class, () -> DataGenerator.generate(null, n, SEED, 0));
    }

    @Test
    @DisplayName("Kiểm tra biên: Mảng rỗng N=0, N=1 và kiểm tra tham số không hợp lệ")
    public void testEdgeCasesAndValidation() {
        // N = 0
        assertEquals(0, DataGenerator.generateD1(0, SEED).length);
        assertEquals(0, DataGenerator.generateD2(0, SEED).length);
        assertEquals(0, DataGenerator.generateD3(0, SEED).length);
        assertEquals(0, DataGenerator.generateD4(0, SEED).length);
        assertEquals(0, DataGenerator.generateD5(0, SEED, 10).length);

        // N = 1
        assertEquals(1, DataGenerator.generateD1(1, SEED).length);
        assertEquals(1, DataGenerator.generateD2(1, SEED).length);
        assertEquals(1, DataGenerator.generateD3(1, SEED).length);
        assertEquals(1, DataGenerator.generateD4(1, SEED).length);
        assertEquals(1, DataGenerator.generateD5(1, SEED, 10).length);

        // N < 0
        assertThrows(IllegalArgumentException.class, () -> DataGenerator.generateD1(-1, SEED));
        assertThrows(IllegalArgumentException.class, () -> DataGenerator.generateD2(-1, SEED));
        assertThrows(IllegalArgumentException.class, () -> DataGenerator.generateD3(-1, SEED));
        assertThrows(IllegalArgumentException.class, () -> DataGenerator.generateD4(-1, SEED));
        assertThrows(IllegalArgumentException.class, () -> DataGenerator.generateD5(-1, SEED, 10));

        // K <= 0 cho D5
        assertThrows(IllegalArgumentException.class, () -> DataGenerator.generateD5(100, SEED, 0));
        assertThrows(IllegalArgumentException.class, () -> DataGenerator.generateD5(100, SEED, -5));
        assertThrows(IllegalArgumentException.class, () -> DataGenerator.generateDistinctKeys(SEED, 0));
    }

    @Test
    @DisplayName("Kiểm tra tính tương thích ngược của TempGenerators")
    @SuppressWarnings("deprecation")
    public void testTempGeneratorsBackwardsCompatibility() {
        int n = 500;
        assertArrayEquals(
                DataGenerator.generate("D1", n, SEED, 0),
                TempGenerators.generate("D1", n, SEED, 0));
        assertArrayEquals(
                DataGenerator.generate("D5", n, SEED, 10),
                TempGenerators.generate("D5", n, SEED, 10));
    }
}
