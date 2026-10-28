package csd201.sort;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.SplittableRandom;

/**
 * Bộ sinh dữ liệu chuẩn dùng chung cho toàn bộ dự án CSD201 FA26 (Mã đề 261014).
 * <p>
 * Sinh mảng thuần {@code int[]} với các phần tử là số nguyên không âm trong miền
 * [0, 2^31 - 1] (từ {@code 0} đến {@code Integer.MAX_VALUE}).
 * </p>
 *
 * <h3>Các họ dữ liệu hỗ trợ (Bảng 4 trong đề tài):</h3>
 * <ul>
 *   <li><b>D1 (Ngẫu nhiên - Random):</b> Sinh $N$ phần tử ngẫu nhiên phân bố đều
 *       dùng {@link SplittableRandom} với seed xác định. Sử dụng phép ép bit dương
 *       {@code random.nextInt() & 0x7FFFFFFF}.</li>
 *   <li><b>D2 (Tăng dần - Sorted):</b> Tạo bản sao từ D1, sau đó gọi {@link Arrays#sort(int[])}
 *       để sắp tăng dần (giữ nguyên tập giá trị và phân bố của D1).</li>
 *   <li><b>D3 (Gần tăng dần - Nearly Sorted):</b> Lấy bản sao từ D2, chọn ngẫu nhiên
 *       và hoán đổi đúng $N / 1.000$ cặp vị trí bằng {@link SplittableRandom}.</li>
 *   <li><b>D4 (Giảm dần - Reversed):</b> Lấy bản sao từ D2 rồi đảo ngược thứ tự các phần tử.</li>
 *   <li><b>D5 (Nhiều khóa trùng - Many Duplicates):</b> Sinh trước $K$ giá trị nguyên phân biệt
 *       (dùng seed), sau đó mỗi phần tử chọn ngẫu nhiên đồng đều trong $K$ giá trị đó.
 *       Hỗ trợ 3 mức chuẩn: $K = 1.000$, $K = 10$, và $K = 2$.</li>
 * </ul>
 *
 * <p>Phụ trách: <b>Người 2 – Cặp 1</b>.</p>
 */
public final class DataGenerator {

    /** Mức K = 1.000 khóa trùng (nhiều khóa trùng mức thấp / 0.1% N tại N=10^6). */
    public static final int K_1000 = 1000;

    /** Mức K = 10 khóa trùng (nhiều khóa trùng mức vừa). */
    public static final int K_10 = 10;

    /** Mức K = 2 khóa trùng (nhị phân / cực nhiều khóa trùng). */
    public static final int K_2 = 2;

    /** Mặt nạ bit loại bỏ bit dấu để đưa số nguyên về miền không âm [0, 2^31 - 1]. */
    private static final int POSITIVE_MASK = 0x7FFFFFFF;

    private DataGenerator() {
        // Lớp tiện ích không khởi tạo
    }

    // =========================================================================
    // D1: Ngẫu nhiên (Random)
    // =========================================================================

    /**
     * D1 (Ngẫu nhiên): Sinh mảng $N$ phần tử ngẫu nhiên phân bố đều trong miền $[0, 2^{31} - 1]$.
     *
     * @param n    số lượng phần tử ($N \ge 0$)
     * @param seed hạt giống ngẫu nhiên
     * @return mảng {@code int[]} ngẫu nhiên
     * @throws IllegalArgumentException nếu $n < 0$
     */
    public static int[] generateD1(int n, long seed) {
        validateSize(n);
        SplittableRandom r = new SplittableRandom(seed);
        int[] a = new int[n];
        for (int i = 0; i < n; i++) {
            a[i] = r.nextInt() & POSITIVE_MASK;
        }
        return a;
    }

    // =========================================================================
    // D2: Tăng dần (Sorted)
    // =========================================================================

    /**
     * D2 (Tăng dần): Tạo bản sao từ D1 (cùng seed) rồi sắp xếp tăng dần.
     * Giữ nguyên toàn bộ tập giá trị của D1.
     *
     * @param n    số lượng phần tử ($N \ge 0$)
     * @param seed hạt giống ngẫu nhiên
     * @return mảng {@code int[]} đã sắp xếp tăng dần
     * @throws IllegalArgumentException nếu $n < 0$
     */
    public static int[] generateD2(int n, long seed) {
        int[] a = generateD1(n, seed);
        Arrays.sort(a);
        return a;
    }

    /**
     * Tạo dữ liệu D2 từ một mảng D1 đã có sẵn.
     *
     * @param d1 mảng D1 nguồn (không bị thay đổi)
     * @return bản sao mới đã được sắp xếp tăng dần
     * @throws IllegalArgumentException nếu {@code d1 == null}
     */
    public static int[] generateD2From(int[] d1) {
        if (d1 == null) {
            throw new IllegalArgumentException("Mảng D1 nguồn không được null");
        }
        int[] copy = d1.clone();
        Arrays.sort(copy);
        return copy;
    }

    // =========================================================================
    // D3: Gần tăng dần (Nearly Sorted)
    // =========================================================================

    /**
     * D3 (Gần tăng dần): Lấy bản sao từ D2, chọn ngẫu nhiên và hoán đổi đúng $N / 1.000$ cặp vị trí.
     * Dùng {@link SplittableRandom} với seed xác định.
     *
     * @param n    số lượng phần tử ($N \ge 0$)
     * @param seed hạt giống ngẫu nhiên
     * @return mảng {@code int[]} gần tăng dần
     * @throws IllegalArgumentException nếu $n < 0$
     */
    public static int[] generateD3(int n, long seed) {
        int[] a = generateD2(n, seed);
        return applyD3Perturbation(a, seed);
    }

    /**
     * Tạo dữ liệu D3 từ một mảng đã sắp xếp tăng dần có sẵn.
     *
     * @param sortedArray mảng đã sắp xếp (không bị thay đổi)
     * @param seed        hạt giống cho các lượt hoán đổi
     * @return bản sao mới sau khi hoán đổi đúng $N / 1.000$ cặp vị trí
     * @throws IllegalArgumentException nếu {@code sortedArray == null}
     */
    public static int[] generateD3FromSorted(int[] sortedArray, long seed) {
        if (sortedArray == null) {
            throw new IllegalArgumentException("Mảng nguồn không được null");
        }
        int[] copy = sortedArray.clone();
        return applyD3Perturbation(copy, seed);
    }

    private static int[] applyD3Perturbation(int[] a, long seed) {
        int n = a.length;
        if (n < 2) {
            return a;
        }
        SplittableRandom r = new SplittableRandom(seed);
        int swaps = n / 1000;
        for (int i = 0; i < swaps; i++) {
            int x = r.nextInt(n);
            int y = r.nextInt(n);
            int temp = a[x];
            a[x] = a[y];
            a[y] = temp;
        }
        return a;
    }

    // =========================================================================
    // D4: Giảm dần (Reversed)
    // =========================================================================

    /**
     * D4 (Giảm dần): Lấy bản sao từ D2 rồi đảo ngược thứ tự các phần tử.
     *
     * @param n    số lượng phần tử ($N \ge 0$)
     * @param seed hạt giống ngẫu nhiên
     * @return mảng {@code int[]} sắp xếp giảm dần
     * @throws IllegalArgumentException nếu $n < 0$
     */
    public static int[] generateD4(int n, long seed) {
        int[] a = generateD2(n, seed);
        reverse(a);
        return a;
    }

    /**
     * Tạo dữ liệu D4 từ một mảng đã sắp xếp tăng dần có sẵn.
     *
     * @param sortedArray mảng đã sắp xếp (không bị thay đổi)
     * @return bản sao mới sau khi đảo ngược thứ tự
     * @throws IllegalArgumentException nếu {@code sortedArray == null}
     */
    public static int[] generateD4FromSorted(int[] sortedArray) {
        if (sortedArray == null) {
            throw new IllegalArgumentException("Mảng nguồn không được null");
        }
        int[] copy = sortedArray.clone();
        reverse(copy);
        return copy;
    }

    private static void reverse(int[] a) {
        for (int i = 0, j = a.length - 1; i < j; i++, j--) {
            int temp = a[i];
            a[i] = a[j];
            a[j] = temp;
        }
    }

    // =========================================================================
    // D5: Nhiều khóa trùng (Many Duplicates)
    // =========================================================================

    /**
     * Sinh trước đúng $K$ giá trị nguyên phân biệt không âm trong miền $[0, 2^{31} - 1]$
     * sử dụng {@link SplittableRandom(seed)}.
     *
     * @param seed hạt giống ngẫu nhiên
     * @param k    số lượng giá trị phân biệt ($K > 0$)
     * @return mảng chứa đúng $K$ giá trị nguyên phân biệt
     * @throws IllegalArgumentException nếu $k \le 0$
     */
    public static int[] generateDistinctKeys(long seed, int k) {
        if (k <= 0) {
            throw new IllegalArgumentException("K phải là số nguyên dương, nhận: " + k);
        }
        SplittableRandom r = new SplittableRandom(seed);
        Set<Integer> set = new HashSet<>(Math.max(16, (int) (k / 0.75f) + 1));
        int[] distinct = new int[k];
        int count = 0;
        while (count < k) {
            int val = r.nextInt() & POSITIVE_MASK;
            if (set.add(val)) {
                distinct[count++] = val;
            }
        }
        return distinct;
    }

    /**
     * D5 (Nhiều khóa trùng): Sinh trước $K$ giá trị nguyên phân biệt (bằng seed),
     * sau đó khởi tạo mảng $N$ phần tử mà mỗi phần tử được chọn ngẫu nhiên đồng đều
     * từ $K$ giá trị trên.
     *
     * @param n    số lượng phần tử ($N \ge 0$)
     * @param seed hạt giống ngẫu nhiên
     * @param k    số khóa phân biệt ($K > 0$, ví dụ 1000, 10, hoặc 2)
     * @return mảng {@code int[]} chứa nhiều khóa trùng
     * @throws IllegalArgumentException nếu $n < 0$ hoặc $k \le 0$
     */
    public static int[] generateD5(int n, long seed, int k) {
        validateSize(n);
        if (k <= 0) {
            throw new IllegalArgumentException("K phải là số nguyên dương, nhận: " + k);
        }
        if (n == 0) {
            return new int[0];
        }

        SplittableRandom r = new SplittableRandom(seed);

        // Bước 1: Sinh đúng K giá trị phân biệt
        Set<Integer> set = new HashSet<>(Math.max(16, (int) (k / 0.75f) + 1));
        int[] distinct = new int[k];
        int count = 0;
        while (count < k) {
            int val = r.nextInt() & POSITIVE_MASK;
            if (set.add(val)) {
                distinct[count++] = val;
            }
        }

        // Bước 2: Chọn ngẫu nhiên đều từ K giá trị
        int[] a = new int[n];
        for (int i = 0; i < n; i++) {
            a[i] = distinct[r.nextInt(k)];
        }
        return a;
    }

    /** D5 mức $K = 1.000$. */
    public static int[] generateD5_1000(int n, long seed) {
        return generateD5(n, seed, K_1000);
    }

    /** D5 mức $K = 10$. */
    public static int[] generateD5_10(int n, long seed) {
        return generateD5(n, seed, K_10);
    }

    /** D5 mức $K = 2$. */
    public static int[] generateD5_2(int n, long seed) {
        return generateD5(n, seed, K_2);
    }

    // =========================================================================
    // Phương thức điều phối tổng quát (Universal Dispatcher)
    // =========================================================================

    /**
     * Phương thức điều phối tổng quát sinh dữ liệu theo mã họ dữ liệu ("D1", "D2", "D3", "D4", "D5").
     * Tương thích hoàn toàn với tham số của {@code BenchmarkRunner}.
     *
     * @param data mã dạng dữ liệu: "D1", "D2", "D3", "D4", "D5" (không phân biệt hoa thường)
     * @param n    kích thước mảng ($N \ge 0$)
     * @param seed hạt giống ngẫu nhiên
     * @param k    số lượng khóa phân biệt cho D5 ($K > 0$; nếu $\le 0$ sẽ mặc định là $1.000$)
     * @return mảng {@code int[]} được sinh theo cấu hình
     * @throws IllegalArgumentException nếu tham số không hợp lệ
     */
    public static int[] generate(String data, int n, long seed, int k) {
        if (data == null) {
            throw new IllegalArgumentException("data không được null");
        }
        String normalized = data.trim().toUpperCase();
        switch (normalized) {
            case "D1":
                return generateD1(n, seed);
            case "D2":
                return generateD2(n, seed);
            case "D3":
                return generateD3(n, seed);
            case "D4":
                return generateD4(n, seed);
            case "D5":
                return generateD5(n, seed, k > 0 ? k : K_1000);
            case "D5-1000":
            case "D5_1000":
            case "D5-1K":
            case "D5_1K":
                return generateD5(n, seed, K_1000);
            case "D5-10":
            case "D5_10":
                return generateD5(n, seed, K_10);
            case "D5-2":
            case "D5_2":
                return generateD5(n, seed, K_2);
            case "KILLER":
            case "MCILROY":
                return generateKillerAdversary(n);
            case "MUSSER":
                return generateMusserKiller(n);
            default:
                throw new IllegalArgumentException("Họ dữ liệu không hợp lệ: '" + data + 
                        "'. Hỗ trợ: D1, D2, D3, D4, D5 (hoặc D5-1000, D5-10, D5-2), KILLER");
        }
    }

    /**
     * Dữ liệu đối kháng phá Quick Sort theo phương pháp "killer adversary" của M. D. McIlroy (1999).
     * <p>
     * Trực tiếp đánh lừa phân hoạch Hoare và pivot median-of-three, ép Quick Sort rơi vào
     * trường hợp xấu nhất: độ sâu đệ quy tăng vọt tới N/2 và số phép so sánh vọt lên ~ N^2 / 4.
     * </p>
     *
     * @param n kích thước mảng (N >= 0)
     * @return mảng int[] chứa hoán vị sát thủ McIlroy
     * @throws IllegalArgumentException nếu n < 0
     */
    public static int[] generateKillerAdversary(int n) {
        validateSize(n);
        if (n <= 1) {
            return new int[n];
        }
        int gas = n - 1;
        int[] val = new int[n];
        Arrays.fill(val, gas);
        int[] state = {0, 0}; // state[0] = nsolid, state[1] = candidate

        int[] a = new int[n];
        for (int i = 0; i < n; i++) {
            a[i] = i;
        }

        simulateHoareQuickSort(a, 0, n - 1, val, gas, state);

        for (int i = 0; i < n; i++) {
            if (val[i] == gas) {
                val[i] = state[0]++;
            }
        }
        return val;
    }

    private static boolean adversaryLess(int x, int y, int[] val, int gas, int[] state) {
        if (val[x] == gas && val[y] == gas) {
            if (x == state[1]) {
                val[x] = state[0]++;
            } else {
                val[y] = state[0]++;
            }
        }
        if (val[x] == gas) {
            state[1] = x;
        } else if (val[y] == gas) {
            state[1] = y;
        }
        return val[x] < val[y];
    }

    private static void simulateHoareQuickSort(int[] a, int lo, int hi, int[] val, int gas, int[] state) {
        if (lo >= hi) {
            return;
        }
        int mid = lo + ((hi - lo) >>> 1);
        if (adversaryLess(a[mid], a[lo], val, gas, state)) {
            int t = a[lo]; a[lo] = a[mid]; a[mid] = t;
        }
        if (adversaryLess(a[hi], a[lo], val, gas, state)) {
            int t = a[lo]; a[lo] = a[hi]; a[hi] = t;
        }
        if (adversaryLess(a[hi], a[mid], val, gas, state)) {
            int t = a[mid]; a[mid] = a[hi]; a[hi] = t;
        }

        int pivot = a[mid];
        int i = lo - 1;
        int j = hi + 1;

        while (true) {
            do { i++; } while (adversaryLess(a[i], pivot, val, gas, state));
            do { j--; } while (adversaryLess(pivot, a[j], val, gas, state));

            if (i >= j) {
                break;
            }
            int t = a[i]; a[i] = a[j]; a[j] = t;
        }

        simulateHoareQuickSort(a, lo, j, val, gas, state);
        simulateHoareQuickSort(a, j + 1, hi, val, gas, state);
    }

    /**
     * Dữ liệu đối kháng phá Quick Sort tĩnh theo Musser (1997).
     *
     * @param n kích thước mảng (N >= 0)
     * @return mảng int[] chứa dãy hoán vị đối kháng Musser
     * @throws IllegalArgumentException nếu n < 0
     */
    public static int[] generateMusserKiller(int n) {
        validateSize(n);
        if (n <= 1) {
            return new int[n];
        }
        int[] a = new int[n];
        int k = n / 2;
        for (int i = 1; i <= k; i++) {
            if ((i & 1) != 0) {
                a[i - 1] = i;
            } else {
                a[i - 1] = k + i - 1;
            }
        }
        int idx = k;
        for (int i = 1; i <= n; i++) {
            if ((i & 1) == 0 && idx < n) {
                a[idx++] = i;
            }
        }
        while (idx < n) {
            a[idx] = n;
            idx++;
        }
        return a;
    }

    private static void validateSize(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("Kích thước mảng N không được âm: " + n);
        }
    }
}
