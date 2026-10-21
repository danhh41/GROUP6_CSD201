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

## 2. Hướng dẫn chạy kiểm thử

Yêu cầu: JDK 17+ và Maven.

```bash
mvn clean test
```