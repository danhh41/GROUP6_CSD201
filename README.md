# GROUP 6 — Đề tài 1: Sắp xếp khóa số nguyên quy mô lớn và pdqsort

[![Java](https://img.shields.io/badge/Java-17%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Maven](https://img.shields.io/badge/Apache%20Maven-3.8%2B-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![JUnit 5](https://img.shields.io/badge/JUnit-5.10.2-25A162?style=for-the-badge&logo=junit5&logoColor=white)](https://junit.org/junit5/)
[![Tests](https://img.shields.io/badge/Tests-42%2F42%20Passing-brightgreen?style=for-the-badge)](https://github.com/)
[![Paper](https://img.shields.io/badge/Paper-IEEE%20Format-00629B?style=for-the-badge&logo=ieee&logoColor=white)](paper/main.tex)

Dự án nghiên cứu và thực nghiệm môn **CSD201 — Data Structures and Algorithms** (Học kỳ: Fall 2026, Lớp: SE20C, Mã đề tài: `261014`, Đại học FPT).

---

## 📌 Mục lục

- [1. Giới thiệu & Bối cảnh đề tài](#1-giới-thiệu--bối-cảnh-đề-tài)
- [2. Ba câu hỏi nghiên cứu (Research Questions)](#2-ba-câu-hỏi-nghiên-cứu-research-questions)
- [3. Cấu trúc thư mục dự án](#3-cấu-trúc-thư-mục-dự-án)
- [4. Đặc tả kỹ thuật các thuật toán](#4-đặc-tả-kỹ-thuật-các-thuật-toán)
- [5. Họ dữ liệu thực nghiệm (D1–D5 & Killer Adversary)](#5-họ-dữ-liệu-thực-nghiệm-d1d5--killer-adversary)
- [6. Yêu cầu môi trường & Cài đặt](#6-yêu-cầu-môi-trường--cài-đặt)
- [7. Hướng dẫn chạy kiểm thử (Unit Testing)](#7-hướng-dẫn-chạy-kiểm-thử-unit-testing)
- [8. Hướng dẫn chạy thực nghiệm (Benchmarking & Runners)](#8-hướng-dẫn-chạy-thực-nghiệm-benchmarking--runners)
- [9. Tái hiện nhanh các cấu hình trong < 60s](#9-tái-hiện-nhanh-các-cấu-hình-trong--60s)
- [10. Trực quan hóa dữ liệu (Biểu đồ)](#10-trực-quan-hóa-dữ-liệu-biểu-đồ)
- [11. Quy chuẩn CSV & Chỉ số đo lường](#11-quy-chuẩn-csv--chỉ-số-đo-lường)
- [12. Phân công nhiệm vụ nhóm (Theo Mục 3.4)](#12-phân-công-nhiệm-vụ-nhóm-theo-mục-34)
- [13. Tài liệu tham khảo](#13-tài-liệu-tham-khảo)

---

## 1. Giới thiệu & Bối cảnh đề tài

Trong các hệ thống xử lý giao dịch tài chính quy mô lớn, việc đối soát cuối ngày yêu cầu sắp xếp hàng chục triệu mã giao dịch số nguyên 32-bit ($0 \le x \le 2^{31} - 1$). Trong thực tế, dữ liệu hiếm khi hoàn toàn ngẫu nhiên: nhiều lô dữ liệu gần như đã có thứ tự theo mốc thời gian, hoặc chứa mật độ phần tử trùng lặp rất lớn.

Trong khi thư viện chuẩn Java sử dụng Dual-Pivot Quicksort, các ngôn ngữ hiện đại như Go (từ 1.19) và Rust (trước 1.81) đã chuẩn hóa sang **pdqsort (Pattern-Defeating Quicksort)** — thuật toán kết hợp tốc độ trung bình vượt trội của Quick Sort với khả năng tự nhận diện dữ liệu có thứ tự, gom khóa trùng và cơ chế Introsort fallback để bảo đảm nghiêm ngặt cận trên $O(N \log N)$ trong trường hợp xấu nhất.

Đồ án này tiến hành khảo sát thực nghiệm có kiểm soát chặt chẽ giữa 4 thuật toán kinh điển trong giáo trình (**Quick Sort, Merge Sort, Heap Sort, Radix Sort**), thuật toán lai thích ứng **pdqsort** và mốc tham chiếu thư viện **`Arrays.sort`**.

---

## 2. Ba câu hỏi nghiên cứu (Research Questions)

| RQ | Câu hỏi trọng tâm | Biến số thay đổi | Yếu tố giữ cố định |
| :--- | :--- | :--- | :--- |
| **RQ1** | Quick Sort, Merge Sort, Heap Sort và Radix Sort khác nhau thế nào về thời gian, bộ nhớ phụ và số thao tác khi $N$ tăng quy mô lớn? | $N = 10^5, 10^6, 5\times 10^6, 10^7, 2\times 10^7$ | Dữ liệu ngẫu nhiên D1, 3 seed (1, 2, 3) |
| **RQ2** | Mức độ có thứ tự (D1–D4) và mức độ trùng khóa (D5) làm thay đổi thứ hạng của 4 thuật toán như thế nào? | - Thí nghiệm A: D1, D2, D3, D4.<br>- Thí nghiệm B: D1 và D5 với $K = 1.000; 10; 2$. | $N = 10^6$ và $N = 10^7$, 3 seed |
| **RQ3** | pdqsort cải thiện bao nhiêu so với Quick Sort giáo trình trên các dạng dữ liệu của RQ2, và mỗi cơ chế (Cơ chế A: nhận diện đoạn đã sắp, Cơ chế B: gom khóa trùng) đóng góp bao nhiêu? | - Quick Sort giáo trình<br>- pdqsort bản đầy đủ<br>- Biến thể `noA` (tắt cơ chế A)<br>- Biến thể `noB` (tắt cơ chế B)<br>- Mốc thư viện `lib-Arrays.sort` | Các phân phối D1–D5 tại $N = 10^6$ và $N = 10^7$ |

---

## 3. Cấu trúc thư mục dự án

```text
GROUP6_CSD201/
├── pom.xml                                 # Cấu hình Maven (Java 17, JUnit 5.10.2)
├── README.md                               # Tài liệu hướng dẫn chuẩn của dự án
├── PHAN_CONG.md                            # Bảng phân công chi tiết 6 thành viên (Mục 3.4)
├── plot_figures.py                         # Script Python vẽ toàn bộ biểu đồ báo cáo
├── run_rq1.ps1                             # Script PowerShell tự động hóa thí nghiệm RQ1
├── run_rq2.ps1                             # Script PowerShell tự động hóa thí nghiệm RQ2
├── run_rq2_count.ps1                       # Script đo đạc lượt đếm thao tác cho RQ2
├── summarize_rq1.ps1                       # Script tổng hợp bảng số liệu RQ1
├── summarize_rq2.ps1                       # Script tổng hợp bảng số liệu RQ2
├── paper/                                  # Bài báo khoa học chuẩn IEEE (LaTeX)
│   └── main.tex                            # Mã nguồn LaTeX bài báo (2 cột IEEE, trang bìa độc lập)
├── docs/                                   # Báo cáo nội bộ và tài liệu phân tích
│   └── Cap3_pdqsort_Methods_Results.docx
├── results/                                # Kết quả thực nghiệm CSV và biểu đồ đã sinh
│   ├── rq1.csv                             # Dữ liệu thô thực nghiệm RQ1
│   ├── rq2_v2.csv                          # Dữ liệu thô thực nghiệm RQ2
│   ├── pdq_ablation.csv                    # Thí nghiệm triệt tiêu từng cơ chế pdqsort
│   ├── pdq_count.csv                       # Số thao tác & độ sâu đệ quy pdqsort
│   └── figures/                            # Thư mục biểu đồ vector phân giải cao (Fig 1 - 4)
│       ├── fig1_rq1_scaling.png
│       ├── fig2_rq2_runtime_by_distribution.png
│       ├── fig3_rq2_ops_count.png
│       └── fig4_rq3_pdq_ablation.png
└── src/
    ├── main/java/csd201/sort/              # Mã nguồn các thuật toán & tiện ích đo lường
    │   ├── QuickSort.java                  # Hoare partition, median-of-3, count & max_depth
    │   ├── MergeSort.java                  # Top-down, 1 mảng phụ N, không bỏ qua trộn
    │   ├── HeapSort.java                   # In-place max-heap, bottom-up, sortRange cho pdqsort
    │   ├── RadixSort.java                  # LSD Radix Sort 4 lượt cơ số 256, đếm 12N đọc/ghi
    │   ├── PdqSort.java                    # pdqsort đầy đủ (Tukey ninther, Cơ chế A, Cơ chế B, Fallback)
    │   ├── DataGenerator.java              # Bộ sinh dữ liệu chuẩn D1–D5 & Killer Adversary
    │   ├── BenchmarkRunner.java            # Bộ runner đo đạc chuẩn (nanoTime, MXBean heap, hash check)
    │   ├── CsvWriter.java                  # Trình ghi CSV 21 cột chuẩn IEEE
    │   ├── PdqEvidence.java                # Lớp thực nghiệm bóc tách chứng cứ pdqsort
    │   └── TempGenerators.java             # Lớp chuyển tiếp tương thích ngược
    └── test/java/csd201/sort/              # Bộ kiểm thử đơn vị & kiểm thử đối kháng (JUnit 5)
        ├── QuickSortTest.java              # 8 bài test QuickSort & Ca đối kháng Musser/McIlroy
        ├── MergeSortTest.java              # 6 bài test MergeSort (sortRange, count, edge cases)
        ├── HeapSortTest.java               # 6 bài test HeapSort (sortRange fallback, max-heap)
        ├── RadixSortTest.java              # 5 bài test RadixSort (LSD 4 pass, công thức 12N)
        ├── PdqSortTest.java                # 9 bài test pdqsort (ablation, patterns, fallback)
        └── DataGeneratorTest.java          # 8 bài test bộ sinh D1–D5 & tính tái lập
```

---

## 4. Đặc tả kỹ thuật các thuật toán

Mọi thuật toán thực thi trực tiếp trên mảng nguyên thủy `int[]` với khóa không âm trong miền $[0, 2^{31} - 1]$ (`Integer.MAX_VALUE`). Tuyệt đối không dùng `Integer[]`, `ArrayList` hay `Comparator`.

| Thuật toán | Tên trong CSV | Đặc tả quy ước cài đặt | Lượt đo số thao tác (`--count`) |
| :--- | :--- | :--- | :--- |
| **Quick Sort** | `quick` | - Phân hoạch Hoare hai con trỏ hội tụ, dừng khi gặp khóa bằng pivot.<br>- Pivot: median-of-three tại các vị trí `lo`, `mid`, `hi`.<br>- Đệ quy 2 phía, không chuyển sang Insertion Sort, không dùng Lomuto. | Đếm số phép so sánh khóa và theo dõi `max_depth`. |
| **Merge Sort** | `merge` | - Top-Down chia đôi từ trên xuống.<br>- Cấp phát duy nhất 1 lần mảng phụ `aux` kích thước $N$ ở hàm gốc và tái sử dụng.<br>- Không dùng tối ưu bỏ qua trộn `if (a[mid] <= a[mid+1]) return;`. | Đếm số phép so sánh khi trộn (`aux[j] < aux[i]`) và theo dõi `max_depth`. |
| **Heap Sort** | `heap` | - Max-heap tại chỗ (in-place), dựng heap theo phương pháp bottom-up.<br>- Hỗ trợ hàm `sortRange(int[] a, int lo, int hi)` cho đoạn nửa mở $[lo, hi)$ làm fallback cho pdqsort. | Đếm số phép so sánh khóa khi sàng phần tử (`siftDown`). Bộ nhớ phụ nghiêm ngặt $O(1)$. |
| **Radix Sort** | `radix` | - LSD Radix Sort cơ số 256, thực hiện đúng 4 lượt cho khóa 32-bit.<br>- Cấp phát duy nhất 1 mảng phụ $N$ phần tử, đảo vai trò `src`/`dst` xen kẽ qua 4 lượt. | Đếm số thao tác đọc/ghi trên mảng dữ liệu chính và mảng phụ. Công thức chuẩn: $12N$ thao tác. |
| **pdqsort** | `pdq` | - Theo Peters (arXiv:2106.05123):<br>  + Đoạn $< 24$ phần tử: Insertion Sort (bản không kiểm tra biên cho đoạn trong).<br>  + Pivot: median-of-3; đoạn $> 128$ phần tử dùng Tukey's ninther.<br>  + Phân hoạch phải, trả về cờ `alreadyPartitioned`.<br>  + **Cơ chế A (Phát hiện đoạn đã sắp):** Partial insertion sort trên 2 nửa, giới hạn 8 lần dịch.<br>  + **Cơ chế B (Gom khóa trùng):** Phân hoạch trái khi pivot bằng phần tử liền trước.<br>  + **Chống suy thoái:** Phá mẫu khi lệch; quá $\lfloor\log_2 N\rfloor$ lần lệch thì chuyển sang `HeapSort.sortRange`. | Đếm toàn bộ phép so sánh khóa qua hàm `less()` và cộng dồn từ Heap fallback; đo `max_depth`. |
| **Mốc thư viện** | `lib-Arrays.sort` | `java.util.Arrays.sort(int[])` (Dual-Pivot Quicksort của JDK 8). | Dùng làm mốc đối chứng oracle kiểm tra tính đúng đắn. |

---

## 5. Họ dữ liệu thực nghiệm (D1–D5 & Killer Adversary)

| Mã | Tên gọi | Quy cách sinh dữ liệu | Ghi chú kỹ thuật |
| :---: | :--- | :--- | :--- |
| **D1** | Ngẫu nhiên (Random) | $N$ giá trị phân bố đều trong $[0, 2^{31} - 1]$ sinh bằng `java.util.SplittableRandom(seed)`. | Đảm bảo tính độc lập và tái lập 100% |
| **D2** | Tăng dần (Sorted) | Bản sao của D1 được sắp tăng dần bằng `Arrays.sort()`. | Giữ nguyên tập giá trị chính xác của D1 |
| **D3** | Gần tăng dần (Nearly Sorted) | Bản sao D2, hoán đổi ngẫu nhiên đúng $N / 1.000$ cặp vị trí bằng `SplittableRandom(seed)`. | Mô phỏng dữ liệu phát sinh theo thời gian |
| **D4** | Giảm dần (Reversed) | Bản sao của D2 rồi đảo ngược thứ tự các phần tử. | Thử thách phân hoạch và chọn pivot |
| **D5** | Nhiều khóa trùng (Duplicates) | Sinh trước $K$ giá trị nguyên phân biệt bằng seed; mỗi phần tử chọn ngẫu nhiên đều trong $K$ giá trị.<br>Gồm 3 mức chuẩn: $K = 1.000$, $K = 10$, và $K = 2$. | Đánh giá năng lực xử lý khóa trùng của phân hoạch Hoare vs. Cơ chế B pdqsort |
| **KILLER** | Mảng đối kháng (Killer Adversary) | Sinh hoán vị sát thủ theo phương pháp của M. D. McIlroy (1999) & Musser (1997), đánh lừa median-of-three và phân hoạch Hoare. | Ép Quick Sort thoái hóa về $O(N^2)$ so sánh và độ sâu $N/2$. |

---

## 6. Yêu cầu môi trường & Cài đặt

### Yêu cầu hệ thống
* **Java Development Kit (JDK):** JDK 17+ (hoặc JDK 8+ cho production/benchmark).
* **Quản lý dự án:** Apache Maven 3.8+ (hoặc chạy trực tiếp qua `javac`/`java` tích hợp sẵn).
* **Python (tùy chọn cho vẽ đồ thị):** Python 3.8+ kèm `matplotlib`, `pandas`, `seaborn`.

### Cài đặt thư viện Python vẽ hình (nếu cần)
```bash
pip install matplotlib pandas seaborn
```

---

## 7. Hướng dẫn chạy kiểm thử (Unit Testing)

Toàn bộ dự án được bảo vệ bởi **42 ca kiểm thử JUnit 5**, bao phủ tính đúng đắn, các ca biên, tính toàn vẹn `sortRange`, 1.000 mảng ngẫu nhiên và các ca đối kháng.

### Cách 1: Chạy qua Maven (chuẩn)
```bash
mvn clean test
```

### Cách 2: Chạy trực tiếp qua JUnit Platform Console Standalone
Dành cho môi trường Terminal không cấu hình sẵn lệnh Maven:
```powershell
# 1. Biên dịch source và test với mã hóa UTF-8
& "C:\Program Files\Java\jdk-17\bin\javac.exe" -encoding UTF-8 -d target/classes -cp "target/classes" (Get-ChildItem -Recurse -Filter "*.java" src/main/java | ForEach-Object { $_.FullName })
& "C:\Program Files\Java\jdk-17\bin\javac.exe" -encoding UTF-8 -d target/test-classes -cp "target/classes;C:\Users\FOTZS41\.m2\repository\org\junit\jupiter\junit-jupiter-api\5.10.2\junit-jupiter-api-5.10.2.jar;C:\Users\FOTZS41\.m2\repository\org\junit\platform\junit-platform-commons\1.10.2\junit-platform-commons-1.10.2.jar" (Get-ChildItem -Recurse -Filter "*.java" src/test/java | ForEach-Object { $_.FullName })

# 2. Thực thi kiểm thử toàn bộ test suite
& "C:\Program Files\Java\jdk-17\bin\java.exe" -jar "C:\Users\FOTZS41\.m2\repository\org\junit\platform\junit-platform-console-standalone\1.13.0-M3\junit-platform-console-standalone-1.13.0-M3.jar" --class-path "target\classes;target\test-classes" --scan-class-path
```

### Danh mục test suites trong dự án
* **`QuickSortTest` (8 tests):** 5 ca cố định theo đề, mảng rỗng/1/2 phần tử, mảng $10^5$ phần tử, `sortRange` bảo toàn vùng ngoài, 1.000 mảng ngẫu nhiên D1–D5, lượt `--count`, và **Ca kiểm thử đối kháng Killer Adversary**.
* **`MergeSortTest` (6 tests):** 5 ca cố định, kiểm tra cấp phát 1 mảng phụ duy nhất, không bỏ qua trộn, đếm so sánh & `max_depth`.
* **`HeapSortTest` (6 tests):** Max-heap bottom-up, `sortRange` phục vụ pdqsort fallback, bộ nhớ $O(1)$.
* **`RadixSortTest` (5 tests):** LSD 4 pass cơ số 256, kiểm chứng công thức $12N$ thao tác đọc/ghi.
* **`PdqSortTest` (9 tests):** Ninther Tukey, phá mẫu răng cưa/organ pipe, kiểm tra fallback Heapsort khi nhiều khóa trùng.
* **`DataGeneratorTest` (8 tests):** Miền giá trị dương, tính tái lập seed, kiểm tra đúng số cặp hoán vị D3, đúng $K$ khóa phân biệt D5.

---

## 8. Hướng dẫn chạy thực nghiệm (Benchmarking & Runners)

### Cú pháp tổng quát của `BenchmarkRunner`
```bash
java -Xmx8g -cp target/classes csd201.sort.BenchmarkRunner \
  --algo <quick|merge|heap|radix|pdq|lib-Arrays.sort> \
  --data <D1|D2|D3|D4|D5|KILLER> \
  --n <kích thước mảng> \
  --seed <hạt giống> \
  --k <số khóa trùng nếu là D5> \
  --variant <base|noA|noB> \
  --rq <RQ1|RQ2|RQ3> \
  --out <đường dẫn file CSV>
```

Các cờ bổ trợ quan trọng:
* `--timing-only`: Chỉ chạy đo thời gian (`elapsed_ms`) và đỉnh RAM (`heap_peak_bytes`).
* `--count`: Chỉ chạy lượt đếm số thao tác (`ops_count`) và độ sâu đệ quy (`max_depth`).
* `--timeout <giây>`: Đặt giới hạn thời gian chờ tối đa cho 1 lượt đo (mặc định 60 giây theo Mục 6.1).

### Thực thi tự động hóa toàn bộ RQ qua PowerShell
```powershell
# Chạy toàn bộ cấu hình cho RQ1
.\run_rq1.ps1 -Xmx 8g -Out "results/rq1.csv"

# Chạy toàn bộ cấu hình thời gian cho RQ2
.\run_rq2.ps1 -Xmx 8g -Out "results/rq2_v2.csv"

# Chạy đo số thao tác cho RQ2
.\run_rq2_count.ps1

# Tổng hợp kết quả thống kê
.\summarize_rq1.ps1
.\summarize_rq2.ps1
```

---

## 9. Tái hiện nhanh các cấu hình trong < 60s

Theo yêu cầu Mục 8 của Quy định chung đồ án, các lệnh sau đây cho phép hội đồng thẩm định tái hiện độc lập kết quả trong thời gian dưới 60 giây trên Terminal ngoài IDE:

### A. Tái hiện RQ1 (Heap Sort, dữ liệu ngẫu nhiên D1, $N = 1.000.000$)
```bash
java -cp target/classes csd201.sort.BenchmarkRunner --algo heap --data D1 --n 1000000 --seed 1 --rq RQ1 --out results/recheck_rq1.csv
```

### B. Tái hiện RQ2 (Radix Sort, dữ liệu gần tăng dần D3, $N = 1.000.000$)
```bash
java -cp target/classes csd201.sort.BenchmarkRunner --algo radix --data D3 --n 1000000 --seed 1 --rq RQ2 --out results/recheck_rq2.csv
```

### C. Tái hiện RQ3 (pdqsort, dữ liệu nhị phân D5 $K=2$, $N = 1.000.000$)
```bash
java -cp target/classes csd201.sort.BenchmarkRunner --algo pdq --data D5 --k 2 --n 1000000 --seed 1 --rq RQ3 --out results/recheck_rq3.csv
```

### D. Tái hiện ca đối kháng Killer Adversary ($N = 2.000$)
Chứng minh Quick Sort thoái hóa về $O(N^2)$ so sánh và $N/2$ độ sâu, trong khi pdqsort bảo toàn $O(N \log N)$:
```bash
java -cp target/classes csd201.sort.BenchmarkRunner --algo quick --data KILLER --n 2000 --seed 1 --count --out results/recheck_killer.csv
java -cp target/classes csd201.sort.BenchmarkRunner --algo pdq --data KILLER --n 2000 --seed 1 --count --out results/recheck_killer.csv
```

---

## 10. Trực quan hóa dữ liệu (Biểu đồ)

Tất cả dữ liệu thực nghiệm được tổng hợp và biểu diễn qua 4 hình đồ thị vector chuẩn IEEE xuất bản trong thư mục `results/figures/`:

```bash
python plot_figures.py
```

* **Hình 1 (`fig1_rq1_scaling.png`):** Thông lượng và quy luật mở rộng thời gian theo $N$ ($10^5 \to 2\times 10^7$) trên thang Log-Log cho RQ1.
* **Hình 2 (`fig2_rq2_runtime_by_distribution.png`):** Biểu đồ cột nhóm so sánh thời gian thực thi của 4 thuật toán trên các phân phối D1–D5 cho RQ2.
* **Hình 3 (`fig3_rq2_ops_count.png`):** So sánh số thao tác cơ bản và độ sâu đệ quy trên từng dạng phân phối dữ liệu.
* **Hình 4 (`fig4_rq3_pdq_ablation.png`):** Thí nghiệm triệt tiêu bóc tách đóng góp của Cơ chế A (`noA`) và Cơ chế B (`noB`) trong pdqsort.

---

## 11. Quy chuẩn CSV & Chỉ số đo lường

File CSV xuất xưởng tuân thủ tuyệt đối cấu trúc **21 cột chuẩn** theo Bảng 4 của Quy định chung đồ án:

```csv
run_id,rq,algorithm,variant,dataset,n,params,seed,input_sha256,generator_version,repeat,elapsed_ms,heap_before_bytes,heap_peak_bytes,ops_count,max_depth,output_check,status,xmx,machine_id,git_commit
```

### Ý nghĩa và giao thức đo:
1. **`elapsed_ms`:** Đo bằng `System.nanoTime()` bao bọc duy nhất lời gọi hàm sắp xếp (ngoài vùng sinh dữ liệu và kiểm tra). Lượt đo thời gian để trống `ops_count` và `max_depth`.
2. **`heap_peak_bytes - heap_before_bytes`:** Đo chênh lệch bộ nhớ heap đỉnh sử dụng `java.lang.management.MemoryPoolMXBean`. Có bước gọi `System.gc()` và đặt lại đỉnh trước mỗi lượt.
3. **`ops_count`:** Đo độc lập ở lượt `--count`: số phép so sánh khóa đối với Quick, Merge, Heap, pdqsort; số lần đọc/ghi mảng dữ liệu ($12N$) đối với Radix Sort.
4. **`max_depth`:** Độ sâu đệ quy lớn nhất ghi nhận được ở lượt `--count` (áp dụng cho Quick, Merge, pdqsort).
5. **`output_check`:** Mã băm kiểm tra tính đúng đắn: $h = 31 \times h + a[i]$ (kiểu `long`, cho phép tràn bit). Đối chiếu từng phần tử với oracle `Arrays.sort`.
6. **`status`:** Ghi nhận trạng thái thực thi: `ok` (thành công), `timeout` (> 60s), `oom` (tràn bộ nhớ), `crash` (lỗi StackOverflowError hoặc Exception), `wrong` (sai thứ tự sắp xếp).

---

## 12. Phân công nhiệm vụ nhóm (Theo Mục 3.4)

Tuân thủ nghiêm ngặt quy định không khai báo tỷ lệ phần trăm đóng góp:

| STT | Họ và tên | MSSV | Cặp | Module & Test đã thực hiện | RQ tham gia phân tích | Phần Paper đã viết / Review |
|:---:|:---|:---:|:---:|:---|:---|:---|
| 1 | Nguyễn Thành Danh | QE200138 | Cặp 1 | Cài đặt `QuickSort.java`, ca kiểm thử đối kháng `QuickSortTest` | Tham gia phân tích RQ1 | Viết phần Introduction & Background |
| 2 | Võ Xuân Long | QE200140 | Cặp 1 | Cài đặt `MergeSort.java`, bộ sinh dữ liệu `DataGenerator.java`, `DataGeneratorTest` | Tham gia phân tích RQ1 | Viết phần Experimental Setup |
| 3 | Phan Văn Bảo | QE200113 | Cặp 2 | Cài đặt `HeapSort.java` (hàm `sortRange` dự phòng), `RadixSort.java`, `HeapSortTest`, `RadixSortTest` | **Chủ trì phân tích RQ2** (Ảnh hưởng phân phối D1–D5) | **Tổng hợp bài báo IEEE (LaTeX/Word)**, viết Methods & RQ2 |
| 4 | Nguyễn Nhật Tường Vy | QE200093 | Cặp 2 | Xây dựng bộ đo `BenchmarkRunner.java`, đo RAM `MemoryPoolMXBean`, `CsvWriter.java` | Tham gia phân tích RQ2 | Viết Threats to Validity & Review bài |
| 5 | Trần Hoàng Thịnh | QE200056 | Cặp 3 | Cài đặt phần lõi `PdqSort.java` (Ninther, hoán đổi phá mẫu, Heap fallback), `PdqSortTest` | Tham gia phân tích RQ3 | Viết phần pdqsort trong Methods & Results |
| 6 | Nguyễn Đồng Tấn Phước | QE200014 | Cặp 3 | Biến thể `noA`, `noB`, thí nghiệm triệt tiêu `PdqEvidence.java`, script vẽ hình `plot_figures.py` | **Chủ trì phân tích RQ3** (Đóng góp pdqsort & biến thể) | Viết Discussion, Conclusion & Review bài |

---

## 13. Tài liệu tham khảo

1. **C. A. R. Hoare**, "Quicksort," *The Computer Journal*, vol. 5, no. 1, pp. 10–16, 1962.
2. **J. W. J. Williams**, "Algorithm 232: Heapsort," *Communications of the ACM*, vol. 7, no. 6, pp. 347–348, 1964.
3. **J. L. Bentley and M. D. McIlroy**, "Engineering a sort function," *Software: Practice and Experience*, vol. 23, no. 11, pp. 1249–1265, 1993.
4. **O. R. L. Peters**, "Pattern-defeating quicksort," *arXiv preprint arXiv:2106.05123*, 2021.
5. **D. R. Musser**, "Introspective sorting and selection algorithms," *Software: Practice and Experience*, vol. 27, no. 8, pp. 983–993, 1997.
6. **M. D. McIlroy**, "A killer adversary for quicksort," *Software: Practice and Experience*, vol. 29, no. 4, pp. 341–344, 1999.
7. **R. Sedgewick and K. Wayne**, *Algorithms*, 4th ed. Boston, MA, USA: Addison-Wesley, 2011, §2.2–2.4, §5.1.
