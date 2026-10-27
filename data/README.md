# Dữ liệu thực nghiệm và Bộ sinh dữ liệu (Data Generator)

Tài liệu này đáp ứng yêu cầu Mục 6.8 và Bảng 6 của "Quy định chung đồ án nhóm CSD201 FA26".

---

## 1. Phiên bản và Thông tin chung
- **Tên lớp sinh dữ liệu:** `csd201.sort.DataGenerator`
- **Phiên bản:** `1.0`
- **Kiểu dữ liệu:** Mảng số nguyên nguyên thủy `int[]` với khóa không âm: $0 \le x \le 2^{31} - 1$ (từ `0` đến `Integer.MAX_VALUE`).
- **Bộ sinh số ngẫu nhiên:** Sử dụng `java.util.SplittableRandom` của Java 8+ để đảm bảo tính ngẫu nhiên đều, độc lập và có thể tái lập 100% dựa trên `seed`.

---

## 2. Quy ước các họ dữ liệu (D1 – D5)
Theo quy định của Đề tài 1:

1. **D1 (Ngẫu nhiên - Uniform Random):**
   - Sinh bằng `random.nextInt() & 0x7FFFFFFF`.
   - Các phần tử phân phối đều độc lập trên toàn bộ dải khóa.

2. **D2 (Tăng dần - Sorted):**
   - Tạo ra từ bản sao của D1, sau đó gọi `Arrays.sort()` để sắp xếp tăng dần.
   - Giữ nguyên tập giá trị và tần suất của D1.

3. **D3 (Gần tăng dần - Nearly Sorted):**
   - Tạo từ bản sao của D2, sau đó hoán đổi ngẫu nhiên chính xác $\lfloor N / 1000 \rfloor$ cặp vị trí bằng `SplittableRandom(seed)`.

4. **D4 (Giảm dần - Reversed):**
   - Tạo từ bản sao của D2 rồi đảo ngược hoàn toàn thứ tự các phần tử.

5. **D5 (Nhiều khóa trùng lặp - Many Duplicates):**
   - Sinh trước $K$ giá trị nguyên phân biệt (bằng `seed`), sau đó mỗi phần tử trong mảng $N$ được chọn ngẫu nhiên đồng đều trong $K$ giá trị này.
   - Ba mức quy định:
     - $K = 1000$ (Trùng mức vừa)
     - $K = 10$ (Trùng nhiều)
     - $K = 2$ (Dữ liệu nhị phân / cực kỳ nhiều trùng lặp)

---

## 3. Quy ước tính mã kiểm tra SHA-256 đầu vào
Theo Mục 6.8:
- Dãy byte của mảng `int[]` được biểu diễn theo thứ tự **Big-Endian** (mỗi số `int` chiếm 4 byte, ghi byte cao trước).
- Hàm băm: `MessageDigest.getInstance("SHA-256")`.
- Mã SHA-256 được ghi vào cột `input_sha256` của file CSV để chứng minh tính tái lập của dữ liệu trước khi đưa vào thuật toán sắp xếp.

---

## 4. Các Seed quy chuẩn dùng trong thực nghiệm
- Các thí nghiệm chính thức dùng 3 hạt giống: `seed = 1`, `seed = 2`, `seed = 3`.
- Mọi mảng dữ liệu được sinh trực tiếp trong bộ nhớ (in-memory) và giải phóng sau mỗi lượt đo để không vượt quá giới hạn RAM và không yêu cầu lưu trữ các file nhị phân lớn trên repository.
