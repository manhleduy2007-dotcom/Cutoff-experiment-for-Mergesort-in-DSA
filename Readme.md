- Merge Sort và Quick Sort đều là thuật toán chia để trị : chia mảng thành các mảng con nhỏ hơn => sau đó sử dụng đệ quy xuống còn 1 phần tử: 
Vấn đề mà cả 2 đều gặp phải => Việc sử dụng đệ quy ở các mảng nhỏ rất là lãng phí. 
Ví dụ: Với mảng chỉ khoảng 5 phần tử 


Bàn về độ phức tạp: Về lý thuyết, Insertion Sort sao lại nhanh hơn mảng nhỏ: 
- Merge Sort và Quick Sort sẽ sử dụng độ phức tạp O (n log n) và Insertion Sort sử dụng độ phức tạp là O (n^2). 

- Các con số CUTOFF => đây đều khong phải con số toán học chính xác mà là giá trị thực nghiệm => Đơn giản là người ta thử nhiều giá trị rồi chọn gía trị cho kết quả nhanh nhất mà thôi. 

- Các con số Cutoff như 7 và 10 chỉ đại diện cho thế hệ trước và thế hệ sau này khi có máy tính hiện đại hơn (có cache lớn hơn + dự đonas rẽ nhánh và các vòng lặp đơn giản như Insertion Sort chạy hiệu quả hơn65l,





**MERGESORT** 
- Phân tích hàm Merge trong Merge Sort: 
1. Mục đích: 
Hàm này trộn hai mảng con đã sắp xếp thành một mảng con đã sắp xếp 
Tức là 2 cái mảng con kia đã được sắp xếp => sau đấy sẽ trộn 2 mảng kia lại với nhau 
a[lo...mid] đã sắp xếp và a[mid+1...hi] đã sắp xếp 
=> việc sắp xếp từng nửa là do đệ quy của sort() làm trước đó. 


**DATA** 
- File Data sẽ tạo 20 file: 5 kiểu dữ liệu (ngẫu nhiê, đã sắp xếp, đảo ngược, gần sắp xếp, nhiều số trùng)
