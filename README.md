# GROUP6_CSD201 - Đề tài 1: Sorting & pdqsort

Dự án môn **CSD201 FA26** (Mã đề: `261014`) — Nghiên cứu & thực nghiệm thuật toán sắp xếp khóa số nguyên quy mô lớn (`int[]`).

---

## 1. Trạng thái module: Người 3 – Cặp 2 (Heap Sort & Radix Sort)

Đã hoàn thành cài đặt và kiểm thử (11/11 tests pass, Java 17, Maven).

### A. Heap Sort (`csd201.sort.HeapSort`)
- **Thuật toán:** Max-heap tại chỗ (in-place), dựng heap theo phương pháp bottom-up.
- **Tên trong CSV:** `heap`
- **Chữ ký hàm:**
  ```java
  // Sắp xếp tăng dần toàn bộ mảng
  public static void sort(int[] a)

  // Sắp xếp đoạn nửa mở [lo, hi) - NGƯỜI 5 (pdqsort) gọi làm fallback
  public static void sortRange(int[] a, int lo, int hi)

  // Đếm số phép so sánh khóa cho lượt --count của NGƯỜI 4
  public static long sortWithCount(int[] a)
  public static long sortRangeWithCount(int[] a, int lo, int hi)
  ```
- **Quy ước đếm:** Chỉ đếm các phép so sánh giá trị khóa (`a[lo + child] < a[lo + right]`, `val >= a[lo + child]`), không tính so sánh chỉ số vòng lặp.

---

### B. Radix Sort (`csd201.sort.RadixSort`)
- **Thuật toán:** LSD Radix Sort cơ số 256, đúng 4 lượt cho khóa `int` 32-bit không âm ($0 \le x \le 2^{31}-1$).
- **Bộ nhớ phụ:** Cấp phát duy nhất 1 mảng phụ $N$ phần tử và mảng đếm 256 phần tử. Chuyển đổi xen kẽ `src`/`dst` qua 4 lượt chẵn.
- **Tên trong CSV:** `radix`
- **Chữ ký hàm:**
  ```java
  // Sắp xếp tăng dần toàn bộ mảng
  public static void sort(int[] a)

  // Đếm số lần đọc/ghi mảng dữ liệu cho lượt --count của NGƯỜI 4
  public static long sortWithCount(int[] a)
  ```
- **Quy ước đếm:** Đếm thao tác đọc/ghi trên mảng dữ liệu chính và mảng phụ. Mỗi lượt gồm $N$ đọc (tính tần suất) $+ N$ đọc $+ N$ ghi (phân phối) = $3N$. Qua 4 lượt, tổng cộng là **$12N$** (với $N \le 1$ trả về $0$).

---

## 2. Trạng thái module: Người 2 – Cặp 1 (Bộ sinh dữ liệu Data Generator D1 – D5)

Đã hoàn thành cài đặt chuẩn hóa dùng chung cho toàn bộ nhóm và kiểm thử (8/8 tests pass, tổng 19/19 tests pass toàn dự án).

### A. Bộ sinh dữ liệu (`csd201.sort.DataGenerator`)
- **Miền giá trị:** Mảng thuần `int[]` với khóa nguyên không âm trong miền $[0, 2^{31} - 1]$ (từ `0` đến `Integer.MAX_VALUE`).
- **Các họ dữ liệu hỗ trợ (Bảng 4 trong đề tài):**
  - **D1 (Ngẫu nhiên - Random):** Sinh ngẫu nhiên đều bằng `java.util.SplittableRandom(seed)`, ép bit dương `random.nextInt() & 0x7FFFFFFF`.
  - **D2 (Tăng dần - Sorted):** Bản sao của D1 được sắp tăng dần bằng `Arrays.sort()`, giữ nguyên tập giá trị của D1.
  - **D3 (Gần tăng dần - Nearly Sorted):** Bản sao của D2, hoán đổi ngẫu nhiên đúng $N / 1.000$ cặp vị trí bằng `SplittableRandom(seed)`.
  - **D4 (Giảm dần - Reversed):** Bản sao của D2 rồi đảo ngược thứ tự các phần tử.
  - **D5 (Nhiều khóa trùng - Many Duplicates):** Sinh trước $K$ giá trị nguyên phân biệt (bằng seed), sau đó mỗi phần tử chọn ngẫu nhiên đồng đều trong $K$ giá trị. Hỗ trợ 3 mức chuẩn:
    - $K = 1.000$ (`DataGenerator.K_1000`)
    - $K = 10$ (`DataGenerator.K_10`)
    - $K = 2$ (`DataGenerator.K_2`)
- **Chữ ký hàm chính:**
  ```java
  // Sinh từng họ dữ liệu riêng biệt
  public static int[] generateD1(int n, long seed)
  public static int[] generateD2(int n, long seed)
  public static int[] generateD3(int n, long seed)
  public static int[] generateD4(int n, long seed)
  public static int[] generateD5(int n, long seed, int k)

  // Tiện ích cho D5 theo 3 mức chuẩn
  public static int[] generateD5_1000(int n, long seed)
  public static int[] generateD5_10(int n, long seed)
  public static int[] generateD5_2(int n, long seed)

  // Sinh K khóa phân biệt cho D5
  public static int[] generateDistinctKeys(long seed, int k)

  // Điều phối tổng quát (dùng trong BenchmarkRunner và kiểm thử)
  public static int[] generate(String data, int n, long seed, int k)
  ```
- **Tương thích ngược:** Lớp `TempGenerators` đã được chuyển hướng để gọi sang `DataGenerator`, đảm bảo không làm gián đoạn mã nguồn của Người 1, Cặp 2 và Cặp 3.

---

## 3. Hướng dẫn chạy kiểm thử

Yêu cầu: JDK 17+ và Maven.

```bash
mvn clean test
```