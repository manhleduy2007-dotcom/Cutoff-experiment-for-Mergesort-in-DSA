# Thí nghiệm tìm giá trị CUTOFF tối ưu cho Mergesort

Mergesort top-down có một cải tiến quen thuộc: khi đoạn con có độ dài không quá `CUTOFF`, chuyển sang insertion sort thay vì tiếp tục đệ quy. Giáo trình nhắc đến hai giá trị CUTOFF là 7 (cũ) và 10 (mới). Thí nghiệm này đo thời gian chạy với 26 giá trị CUTOFF khác nhau để tìm giá trị phù hợp nhất trên một cấu hình máy cụ thể.

Báo cáo đầy đủ: [`Report.md`](Report.md).

## Cấu trúc thư mục

```
.
├── MergeSortCutOff.java   Cài đặt mergesort + insertion sort (tự viết từ slide bài giảng)
├── Data.java              Sinh bộ dữ liệu thí nghiệm
├── Benchmark.java         Đo thời gian và thống kê
├── plot.py                Vẽ đồ thị từ kết quả đo
├── compare.py             So sánh độ dao động giữa thí nghiệm chính và thí nghiệm đối chứng
├── brokendemo/            Thí nghiệm minh họa: bản lỗi cố ý và chương trình so sánh
├── data/                  20 file dữ liệu (5 kiểu × 4 kích thước)
├── results/               Kết quả thí nghiệm chính (CSV), cấu hình máy và đồ thị
├── results_single/        Kết quả thí nghiệm đối chứng (không gộp bản sao)
└── Report.md              Báo cáo thí nghiệm
```

## Yêu cầu

- JDK 17 trở lên (thí nghiệm gốc chạy trên Java 25.0.2)
- Python 3 và thư viện `matplotlib` (cho bước vẽ đồ thị)

## Cách chạy lại thí nghiệm chính

Tất cả các lệnh chạy ở thư mục gốc của project.

```bash
# 1. Biên dịch
javac *.java

# 2. Sinh dữ liệu vào data/ (seed cố định nên luôn ra cùng một bộ dữ liệu)
java Data

# 3. Đo thời gian: 25 lần đo, 5 vòng warm-up, ghi vào results/ (khoảng 20–30 phút)
java -Xms2g -Xmx2g Benchmark 25 5 data results

#    Bản chạy thử nhanh: 3 lần đo, 2 vòng warm-up, ghi vào thư mục riêng
java Benchmark 3 2 data results_test

# 4. Vẽ đồ thị vào results/
pip3 install matplotlib
python3 plot.py
```

Tham số của `Benchmark`: `[soLanDo] [soVongWarmup] [thuMucData] [thuMucKetQua]`, mặc định `25 5 data results`.

> **Lưu ý:** chương trình ghi đè `raw.csv`, `summary.csv` và `environment.txt` trong thư mục kết quả. Mọi lần chạy thử hoặc chạy đối chứng phải dùng thư mục kết quả khác `results`, nếu không số liệu của thí nghiệm chính sẽ bị mất.

Khi đo chính thức nên cắm sạc, đóng các ứng dụng khác và không dùng máy trong lúc chạy.

## Cách chạy lại thí nghiệm đối chứng

Thí nghiệm đối chứng kiểm tra tác dụng của việc gộp nhiều bản sao trong một lần bấm giờ (mục 4.3d và Phụ lục A của báo cáo), bằng cách đo lại khi mỗi lần bấm giờ chỉ sắp xếp một mảng.

1. Tạm sửa hai hằng số trong `Benchmark.java`:
   ```java
   static final int[] SIZES = {1_000, 10_000};
   static final int MIN_ELEMENTS_PER_MEASUREMENT = 1;
   ```
2. Biên dịch và chạy vào thư mục riêng:
   ```bash
   javac *.java
   java Benchmark 25 5 data results_single
   ```
3. Sửa hai hằng số về giá trị ban đầu (`{1_000, 10_000, 100_000, 1_000_000}` và `200_000`), rồi `javac *.java` lại.
4. So sánh độ dao động (độ lệch chuẩn chia trung vị) giữa hai thí nghiệm:
   ```bash
   python3 compare.py
   ```

## Cách chạy thí nghiệm minh họa

Thí nghiệm minh họa cho thấy một cài đặt có lỗi logic vẫn chạy và còn trông nhanh hơn cài đặt đúng (mục 4.3g và Phụ lục B của báo cáo). `brokendemo/BrokenSort.java` là bản sao của `MergeSortCutOff.java` với `insertionSort` bị làm hỏng có chủ đích; `brokendemo/BrokenDemo.java` đo và so sánh hai bản.

```bash
javac -d brokendemo/out MergeSortCutOff.java brokendemo/*.java
java -cp brokendemo/out BrokenDemo
```

## Thiết kế thí nghiệm

| Biến | Giá trị |
|---|---|
| CUTOFF | 1–20, 24, 28, 32, 40, 48, 64 (CUTOFF = 1 tương đương mergesort thuần, dùng làm mốc so sánh) |
| Kích thước N | 1.000; 10.000; 100.000; 1.000.000 |
| Kiểu dữ liệu | `random`, `sorted`, `reversed`, `nearly_sorted` (đổi chỗ ngẫu nhiên 1% phần tử), `few_unique` (chỉ 10 giá trị khác nhau) |
| Số lần đo | 25 lần cho mỗi tổ hợp, sau 5 vòng warm-up |
| Seed dữ liệu | `20261116` |

Các biện pháp để kết quả đo đáng tin:

- **Warm-up**: chạy bỏ 5 vòng đầu để JIT của Java biên dịch xong mã trước khi đo.
- **Bấm giờ bằng `System.nanoTime()` và tách thời gian copy mảng**: mỗi lần đo copy mảng gốc sang mảng làm việc trước khi bấm giờ, nên mọi CUTOFF được đo trên cùng dữ liệu và thời gian copy không bị tính.
- **Kéo dài khoảng đo cho mảng nhỏ**: mỗi lần bấm giờ sắp xếp nhiều bản sao (tổng cộng ít nhất 200.000 phần tử) rồi chia trung bình, để sai số của đồng hồ không đáng kể. Thí nghiệm đối chứng cho thấy với N = 1.000, độ dao động giảm từ 70–85% xuống 2–3%.
- **Xáo thứ tự CUTOFF và gọi `System.gc()`** ở đầu mỗi vòng đo, để nhiễu thay đổi theo thời gian (nhiệt độ CPU, dọn bộ nhớ) không dồn về một giá trị CUTOFF.
- **Lặp 25 lần và dùng trung vị** (median) làm số liệu chính, vì ít bị ảnh hưởng bởi các lần đo bất thường.
- **Kiểm tra tính đúng**: so kết quả với `Arrays.sort` ở vòng warm-up đầu và kiểm tra mảng đã sắp xếp sau mỗi lần đo (ngoài thời gian đo).

## Định dạng kết quả

**`raw.csv`**: mỗi dòng là một lần đo.

| Cột | Ý nghĩa |
|---|---|
| `data_type` | Kiểu dữ liệu |
| `n` | Kích thước mảng |
| `cutoff` | Giá trị CUTOFF |
| `run` | Thứ tự lần đo (1–25) |
| `time_ns` | Thời gian một lần sắp xếp, tính bằng nano giây |

**`summary.csv`**: mỗi dòng là một tổ hợp (kiểu dữ liệu, N, CUTOFF).

| Cột | Ý nghĩa |
|---|---|
| `median_ms`, `mean_ms`, `std_ms`, `min_ms` | Trung vị, trung bình, độ lệch chuẩn, nhỏ nhất (mili giây) |
| `ratio_vs_baseline` | Trung vị chia cho trung vị tại CUTOFF = 1. Nhỏ hơn 1 nghĩa là nhanh hơn mergesort thuần |

**`environment.txt`**: hệ điều hành, phiên bản Java, số nhân CPU, tham số thí nghiệm.

Cả `results/` và `results_single/` đều có ba file trên. Riêng `results/` có thêm đồ thị: `cutoff_<kiểu>.png` (5 file, một file cho mỗi kiểu dữ liệu: thời gian tương đối theo CUTOFF, mỗi N một đường) và `runtime_vs_n.png` (thời gian theo N, thang log-log).

## Kết quả tóm tắt

Trên máy thí nghiệm (MacBook Pro M1 Pro, 10 nhân, 32 GB RAM, macOS 26.7.3, Java 25.0.2):

- Với dữ liệu ngẫu nhiên, CUTOFF = 10 nhanh hơn hoặc bằng CUTOFF = 7 ở mọi kích thước có số đo đủ tin cậy (N ≤ 100.000).
- Vùng tối ưu cho dữ liệu ngẫu nhiên khoảng 12–20, nhưng chỉ nhanh hơn CUTOFF = 10 khoảng 0–3%.
- Không có CUTOFF tốt nhất cho mọi kiểu dữ liệu: dữ liệu đảo ngược cần CUTOFF nhỏ, dữ liệu đã sắp xếp và gần sắp xếp hưởng lợi từ CUTOFF lớn.
- Đề xuất: dùng CUTOFF = 10 cho trường hợp chung trên cấu hình máy này.

Chi tiết và phân tích xem trong [`Report.md`](Report.md).

## Ghi chú về việc sử dụng AI

Đề bài cho phép dùng AI để thảo luận, viết script và xây dựng bộ dữ liệu. `Data.java`, `Benchmark.java`, `plot.py`, `compare.py`, `brokendemo/BrokenSort.java`, `brokendemo/BrokenDemo.java` và README này được viết với sự hỗ trợ của AI. `MergeSortCutOff.java` được tự cài đặt từ nội dung bài giảng. Chi tiết về phần AI hỗ trợ xem Phụ lục D của `Report.md`.