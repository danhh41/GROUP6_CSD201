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

## 2. Trạng thái module: Người 2 – Cặp 1 (Data Generator D1 – D5 & Merge Sort)

Đã hoàn thành cài đặt chuẩn hóa bộ sinh dữ liệu và thuật toán Merge Sort (8/8 tests DataGenerator, 6/6 tests MergeSort, tổng 25/25 tests pass toàn dự án).

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

### B. Merge Sort (`csd201.sort.MergeSort`)
- **Thuật toán:** Top-Down Merge Sort chia đôi đệ quy từ trên xuống.
- **Tên trong CSV:** `merge`
- **Bộ nhớ phụ:** Cấp phát duy nhất 1 lần mảng phụ `int[] aux = new int[N]` ở hàm gọi ban đầu và truyền qua các tầng đệ quy (tuyệt đối không `new` mảng con bên trong).
- **Quy tắc trộn:** Không áp dụng tối ưu bỏ qua trộn `if (a[mid] <= a[mid+1]) return;`, luôn thực hiện bước trộn ở mọi trường hợp.
- **Chữ ký hàm:**
  ```java
  // Sắp xếp tăng dần toàn bộ mảng (Benchmarking)
  public static void sort(int[] a)

  // Sắp xếp đoạn nửa mở [lo, hi)
  public static void sortRange(int[] a, int lo, int hi)

  // Đếm số phép so sánh khóa và đo max_depth cho lượt --count của NGƯỜI 4
  public static long sortWithCount(int[] a)
  public static long sortRangeWithCount(int[] a, int lo, int hi)

  // Giải đóng gói kết quả: 32-bit cao = max_depth, 32-bit thấp = comparisons
  public static int unpackDepth(long packed)
  public static long unpackComparisons(long packed)
  ```
- **Quy ước đếm:**
  - `comparisons`: Đếm số phép so sánh khóa khi trộn hai mảng con (`aux[j] < aux[i]`).
  - `max_depth`: Tăng biến đếm độ sâu khi gọi đệ quy và cập nhật giá trị cực đại quan sát được.

---

## 3. Trạng thái module: Người 1 – Cặp 1 (Quick Sort)

Đã hoàn thành cài đặt thuật toán Quick Sort.

### A. Quick Sort (`csd201.sort.QuickSort`)
- **Thuật toán:** Phân hoạch Hoare hai đầu, pivot là trung vị của 3 (median-of-three: `lo`, `mid`, `hi`), đệ quy hai phía.
- **Tên trong CSV:** `quick`
- **Chữ ký hàm:**
  ```java
  // Sắp xếp tăng dần toàn bộ mảng
  public static void sort(int[] a)

  // Sắp xếp đoạn nửa mở [lo, hi)
  public static void sortRange(int[] a, int lo, int hi)

  // Đếm số phép so sánh khóa và đo max_depth cho lượt --count
  public static long sortWithCount(int[] a)

  // Giải đóng gói kết quả: 32-bit cao = max_depth, 32-bit thấp = comparisons
  public static int unpackDepth(long packed)
  public static long unpackComparisons(long packed)
  ```
- **Quy ước đếm:**
  - `comparisons`: Đếm phép so sánh khi chọn pivot median-of-three và so sánh trong phân hoạch Hoare.
  - `max_depth`: Đo độ sâu đệ quy lớn nhất.

---

## 4. Hướng dẫn chạy kiểm thử

Yêu cầu: JDK 17+ và Maven.

```bash
mvn clean test
```
Người 5 – Cặp 3: pdqsort lõi

- PdqSort: Insertion Sort < 24, trung vị của 3 / ninther > 128, phân hoạch phải
  kèm cờ "đã phân hoạch sẵn", chống trường hợp xấu (phá mẫu, quá log2 N lần
  thì chuyển sang HeapSort.sortRange của Người 3)
- Chừa sẵn partitionLeft / partialInsertionSort cho Người 6 (cờ useA, useB)
- Đăng ký "pdq" trong BenchmarkRunner (@Người 4 xem giúp 2 chỗ sửa)
- PdqSortTest: 9 test, mvn test 34/34 pass
- PdqEvidence + results/pdq_*.csv: số liệu cho tài liệu
- docs/Cap3_pdqsort_Methods_Results.docx: Methods/Results cặp 3
