# BẢNG KẾT QUẢ RQ2 RÚT GỌN (DÀNH RIÊNG CHO NGƯỜI 3)
*Kích thước mảng: N = 1.000.000 phần tử (1 triệu số nguyên). Đơn vị thời gian: mili-giây (ms).*

---

## 1. BẢNG HEAPSORT (THUẬT TOÁN 1 CỦA BẠN)

| Dữ liệu (data) | Ý nghĩa dữ liệu | Số giá trị khác nhau (k) | Thời gian chạy (ms) | Số phép so sánh (operations) | Dung lượng RAM (Peak) |
| :---: | :--- | :---: | :---: | :---: | :---: |
| **D1** | Ngẫu nhiên hoàn toàn | 0 (tự do) | **113.1 ms** | **36.793.562** (~36.8 triệu) | 4.89 MB (In-place) |
| **D2** | Đã tăng dần sẵn | 0 | **57.9 ms** | **37.704.797** (~37.7 triệu) | 4.89 MB |
| **D3** | Giảm dần hoàn toàn | 0 | **58.8 ms** | **37.695.480** (~37.7 triệu) | 4.89 MB |
| **D4** | Gần như tăng dần | 0 | **67.3 ms** | **36.001.237** (~36.0 triệu) | 4.89 MB |
| **D5** | Trùng lặp vừa | 1000 | **96.5 ms** | **36.774.961** (~36.8 triệu) | 4.89 MB |
| **D5** | Trùng lặp nhiều | 10 | **57.2 ms** | **33.483.471** (~33.5 triệu) | 4.89 MB |
| **D5** | **Trùng lặp tối đa (chỉ 2 số 0 và 1)** | **2** | **34.6 ms** ⚡ *(Giảm 3 lần!)* | **18.350.859** ⚡ *(Giảm một nửa!)* | 4.89 MB |

> 💡 **Điểm mấu chốt để trả lời thầy:**
> - Trên dữ liệu bình thường (D1), HeapSort chạy khoảng **113 ms** và tốn gần **37 triệu lần so sánh**.
> - Nhưng khi vào **D5 (k = 2)** chỉ có 2 giá trị trùng lặp, thao tác `siftDown` dừng lại cực sớm, số lần so sánh **giảm một nửa xuống 18.3 triệu lần**, thời gian giảm xuống còn **34.6 ms**!
> - Cột RAM luôn giữ nguyên **4.89 MB** chứng minh HeapSort chạy **In-place O(1)** không tốn thêm byte RAM nào.

---

## 2. BẢNG RADIXSORT (THUẬT TOÁN 2 CỦA BẠN)

| Dữ liệu (data) | Ý nghĩa dữ liệu | Số giá trị khác nhau (k) | Thời gian chạy (ms) | Số thao tác đọc/ghi (operations) | Dung lượng RAM (Peak) |
| :---: | :--- | :---: | :---: | :---: | :---: |
| **D1** | Ngẫu nhiên hoàn toàn | 0 (tự do) | **20.0 ms** 🚀 | **12.000.000** (Đúng 12N) | 9.00 MB (Tốn O(N) RAM) |
| **D2** | Đã tăng dần sẵn | 0 | **24.6 ms** 🚀 | **12.000.000** (Đúng 12N) | 9.00 MB |
| **D3** | Giảm dần hoàn toàn | 0 | **16.4 ms** 🚀 | **12.000.000** (Đúng 12N) | 9.00 MB |
| **D4** | Gần như tăng dần | 0 | **16.6 ms** 🚀 | **12.000.000** (Đúng 12N) | 9.00 MB |
| **D5** | Trùng lặp vừa (k = 1000) | 1000 | **17.8 ms** 🚀 | **12.000.000** (Đúng 12N) | 9.00 MB |
| **D5** | Trùng lặp nhiều (k = 10) | 10 | **19.4 ms** 🚀 | **12.000.000** (Đúng 12N) | 9.00 MB |
| **D5** | Trùng lặp tối đa (k = 2) | 2 | **19.4 ms** 🚀 | **12.000.000** (Đúng 12N) | 9.00 MB |

> 💡 **Điểm mấu chốt để trả lời thầy:**
> - RadixSort chạy **siêu nhanh và cực kỳ ổn định (khoảng 16 - 24 ms trên mọi loại dữ liệu)** vì nó không thèm so sánh các con số mà chỉ ném vào 256 cái xô.
> - Cột thao tác luôn là con số chuẩn toán học: **12.000.000 (đúng bằng 12 × N)** cho 4 lượt duyệt.
> - Cột RAM tăng từ **4.89 MB lên 9.00 MB** (gấp đôi) chứng minh nó tốn thêm 1 mảng phụ kích thước $O(N)$.
