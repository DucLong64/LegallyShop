-- V5__add_comments.sql
-- Thêm comment cho tất cả table và column để dễ maintain sau này

-- ============================================================
-- TABLE: category
-- ============================================================
COMMENT ON TABLE category IS 'Danh mục sản phẩm — hỗ trợ cấu trúc cha-con đệ quy (self-referencing)';

COMMENT ON COLUMN category.id         IS 'Khóa chính tự tăng';
COMMENT ON COLUMN category.parent_id  IS 'ID danh mục cha — NULL nếu là danh mục gốc (vd: Điện thoại, Laptop)';
COMMENT ON COLUMN category.name       IS 'Tên hiển thị của danh mục (vd: Điện thoại, Phụ kiện)';
COMMENT ON COLUMN category.slug       IS 'Định danh URL thân thiện, unique (vd: dien-thoai). Dùng cho SEO';
COMMENT ON COLUMN category.sort_order IS 'Thứ tự hiển thị trong menu — số nhỏ hơn hiện trước';
COMMENT ON COLUMN category.created_at IS 'Thời điểm tạo bản ghi';

-- ============================================================
-- TABLE: attribute_template
-- ============================================================
COMMENT ON TABLE attribute_template IS 'Template thuộc tính gắn với danh mục — mỗi danh mục tự định nghĩa bộ thuộc tính riêng (vd: Điện thoại có RAM/ROM/Pin, Áo thể thao có Size/Chất liệu)';

COMMENT ON COLUMN attribute_template.id          IS 'Khóa chính tự tăng';
COMMENT ON COLUMN attribute_template.category_id IS 'Danh mục sở hữu template này — FK tới category.id';
COMMENT ON COLUMN attribute_template.name        IS 'Tên thuộc tính (vd: RAM, Dung lượng pin, Hệ điều hành)';
COMMENT ON COLUMN attribute_template.input_type  IS 'Kiểu nhập liệu: text | select | number | boolean. Dùng để render form ở Admin';
COMMENT ON COLUMN attribute_template.is_required IS 'TRUE nếu bắt buộc điền khi tạo sản phẩm thuộc danh mục này';
COMMENT ON COLUMN attribute_template.sort_order  IS 'Thứ tự hiển thị thuộc tính trong form và trang chi tiết sản phẩm';

-- ============================================================
-- TABLE: product
-- ============================================================
COMMENT ON TABLE product IS 'Sản phẩm chính — lưu thông tin chung. Giá và tồn kho không lưu ở đây mà ở bảng sku';

COMMENT ON COLUMN product.id          IS 'Khóa chính tự tăng';
COMMENT ON COLUMN product.category_id IS 'Danh mục của sản phẩm — FK tới category.id';
COMMENT ON COLUMN product.name        IS 'Tên đầy đủ sản phẩm (vd: iPhone 15 Pro Max)';
COMMENT ON COLUMN product.slug        IS 'Định danh URL duy nhất (vd: iphone-15-pro-max). Dùng cho SEO và routing';
COMMENT ON COLUMN product.description IS 'Mô tả chi tiết dạng HTML/Markdown. Hiển thị ở trang chi tiết sản phẩm';
COMMENT ON COLUMN product.status      IS 'Trạng thái xuất bản: DRAFT (nháp) | ACTIVE (đang bán) | INACTIVE (ngừng bán)';
COMMENT ON COLUMN product.is_active   IS 'Cờ soft-delete — FALSE để ẩn sản phẩm mà không xóa dữ liệu';
COMMENT ON COLUMN product.created_at  IS 'Thời điểm tạo sản phẩm — tự điền bởi JPA Auditing';
COMMENT ON COLUMN product.updated_at  IS 'Thời điểm cập nhật gần nhất — tự điền bởi JPA Auditing';

-- ============================================================
-- TABLE: product_attribute
-- ============================================================
COMMENT ON TABLE product_attribute IS 'Giá trị thuộc tính cụ thể của từng sản phẩm — liên kết product với attribute_template';

COMMENT ON COLUMN product_attribute.id          IS 'Khóa chính tự tăng';
COMMENT ON COLUMN product_attribute.product_id  IS 'Sản phẩm sở hữu thuộc tính này — FK tới product.id, cascade delete';
COMMENT ON COLUMN product_attribute.template_id IS 'Template thuộc tính tương ứng — FK tới attribute_template.id';
COMMENT ON COLUMN product_attribute.value       IS 'Giá trị thuộc tính (vd: "8GB", "5000mAh", "iOS 17"). Lưu dạng TEXT để linh hoạt';

-- ============================================================
-- TABLE: sku
-- ============================================================
COMMENT ON TABLE sku IS 'Stock Keeping Unit — biến thể của sản phẩm. Một sản phẩm có nhiều SKU khác nhau về màu/size/dung lượng, mỗi SKU có giá và tồn kho độc lập';

COMMENT ON COLUMN sku.id             IS 'Khóa chính tự tăng';
COMMENT ON COLUMN sku.product_id     IS 'Sản phẩm cha — FK tới product.id, cascade delete';
COMMENT ON COLUMN sku.sku_code       IS 'Mã SKU duy nhất trong hệ thống (vd: IPHONE15-BLACK-256). Dùng cho quản lý kho';
COMMENT ON COLUMN sku.price          IS 'Giá bán hiện tại (VNĐ). Đây là giá khách hàng thanh toán';
COMMENT ON COLUMN sku.original_price IS 'Giá gốc trước khi giảm (VNĐ). Dùng để tính và hiển thị % giảm giá. NULL nếu không có giảm giá';
COMMENT ON COLUMN sku.stock_qty      IS 'Số lượng tồn kho hiện tại. Bị trừ khi đặt hàng, cộng lại khi huỷ đơn. Luôn >= 0';
COMMENT ON COLUMN sku.is_active      IS 'FALSE để ẩn biến thể này khỏi trang sản phẩm (vd: hết hàng vĩnh viễn)';

-- ============================================================
-- TABLE: sku_option
-- ============================================================
COMMENT ON TABLE sku_option IS 'Tùy chọn xác định biến thể của SKU — mỗi SKU có thể có nhiều tùy chọn (vd: Màu sắc + Dung lượng)';

COMMENT ON COLUMN sku_option.id           IS 'Khóa chính tự tăng';
COMMENT ON COLUMN sku_option.sku_id       IS 'SKU sở hữu tùy chọn này — FK tới sku.id, cascade delete';
COMMENT ON COLUMN sku_option.option_name  IS 'Tên nhóm tùy chọn (vd: Màu sắc, Dung lượng, Size)';
COMMENT ON COLUMN sku_option.option_value IS 'Giá trị tùy chọn (vd: Đen titan, 256GB, XL). Hiển thị trên nút chọn biến thể';

-- ============================================================
-- TABLE: product_image
-- ============================================================
COMMENT ON TABLE product_image IS 'Ảnh của sản phẩm — có thể là ảnh chung (sku_id NULL) hoặc ảnh riêng cho từng biến thể SKU';

COMMENT ON COLUMN product_image.id         IS 'Khóa chính tự tăng';
COMMENT ON COLUMN product_image.product_id IS 'Sản phẩm sở hữu ảnh — FK tới product.id, cascade delete';
COMMENT ON COLUMN product_image.sku_id     IS 'SKU tương ứng — NULL nếu là ảnh chung của product. Có giá trị khi ảnh đặc trưng cho 1 màu/biến thể';
COMMENT ON COLUMN product_image.url        IS 'URL ảnh đã upload lên Cloudinary hoặc S3 (vd: https://res.cloudinary.com/...)';
COMMENT ON COLUMN product_image.is_primary IS 'TRUE nếu đây là ảnh đại diện — hiển thị ở danh sách và thumbnail. Mỗi product chỉ nên có 1 ảnh primary';
COMMENT ON COLUMN product_image.sort_order IS 'Thứ tự hiển thị trong gallery — số nhỏ hơn hiện trước';

-- ============================================================
-- TABLE: orders
-- ============================================================
COMMENT ON TABLE orders IS 'Đơn hàng — lưu thông tin đặt hàng của khách. Dùng tên "orders" thay vì "order" vì ORDER là từ khóa SQL';

COMMENT ON COLUMN orders.id              IS 'Khóa chính tự tăng';
COMMENT ON COLUMN orders.order_code      IS 'Mã đơn hàng hiển thị cho khách (vd: ORD-20240507-0023). Unique, thân thiện hơn ID số';
COMMENT ON COLUMN orders.user_id         IS 'Khách hàng đặt đơn — FK tới users.id. NULL nếu cho phép đặt hàng không cần đăng nhập';
COMMENT ON COLUMN orders.status          IS 'Trạng thái đơn: PENDING (chờ xác nhận) | CONFIRMED | SHIPPING | DELIVERED | CANCELLED';
COMMENT ON COLUMN orders.total_amount    IS 'Tổng tiền hàng sau khi trừ giảm giá (VNĐ), chưa tính phí ship';
COMMENT ON COLUMN orders.shipping_fee    IS 'Phí vận chuyển (VNĐ). Lưu tại thời điểm đặt để tránh thay đổi sau';
COMMENT ON COLUMN orders.discount_amount IS 'Số tiền được giảm từ voucher/khuyến mãi (VNĐ)';
COMMENT ON COLUMN orders.receiver_name   IS 'Tên người nhận hàng — snapshot tại thời điểm đặt';
COMMENT ON COLUMN orders.receiver_phone  IS 'Số điện thoại người nhận — snapshot tại thời điểm đặt';
COMMENT ON COLUMN orders.shipping_address IS 'Địa chỉ giao hàng đầy đủ — snapshot tại thời điểm đặt (tên đường, phường, quận, tỉnh)';
COMMENT ON COLUMN orders.payment_method  IS 'Phương thức thanh toán: COD | VNPAY | MOMO | BANK_TRANSFER';
COMMENT ON COLUMN orders.payment_status  IS 'Trạng thái thanh toán: UNPAID | PAID | REFUNDED. Tách biệt với order status';
COMMENT ON COLUMN orders.note            IS 'Ghi chú của khách khi đặt hàng (vd: "Giao giờ hành chính", "Gọi trước khi giao")';
COMMENT ON COLUMN orders.created_at      IS 'Thời điểm đặt hàng — tự điền bởi JPA Auditing';
COMMENT ON COLUMN orders.updated_at      IS 'Thời điểm cập nhật gần nhất (vd: khi đổi trạng thái) — tự điền bởi JPA Auditing';

-- ============================================================
-- TABLE: order_item
-- ============================================================
COMMENT ON TABLE order_item IS 'Chi tiết từng sản phẩm trong đơn hàng — lưu snapshot thông tin tại thời điểm đặt để đề phòng sản phẩm bị sửa/xóa sau';

COMMENT ON COLUMN order_item.id           IS 'Khóa chính tự tăng';
COMMENT ON COLUMN order_item.order_id     IS 'Đơn hàng chứa item này — FK tới orders.id';
COMMENT ON COLUMN order_item.sku_id       IS 'SKU được mua — FK tới sku.id. Giữ để tra cứu, nhưng không dùng làm nguồn sự thật về giá';
COMMENT ON COLUMN order_item.product_name IS 'Snapshot tên sản phẩm tại thời điểm đặt hàng — không bị ảnh hưởng nếu product đổi tên sau';
COMMENT ON COLUMN order_item.sku_options  IS 'Snapshot biến thể dạng chuỗi (vd: "Đen titan / 256GB") — hiển thị trong lịch sử đơn hàng';
COMMENT ON COLUMN order_item.quantity     IS 'Số lượng mua. Luôn > 0';
COMMENT ON COLUMN order_item.unit_price   IS 'Giá mỗi đơn vị tại thời điểm đặt (VNĐ) — snapshot, không đổi dù product thay giá sau';
COMMENT ON COLUMN order_item.subtotal     IS 'Thành tiền = unit_price * quantity (VNĐ). Lưu sẵn để tránh tính lại';

-- ============================================================
-- TABLE: users
-- ============================================================
COMMENT ON TABLE users IS 'Tài khoản người dùng — khách hàng và admin. Dùng tên "users" thay vì "user" vì USER là từ khóa SQL';

COMMENT ON COLUMN users.id        IS 'Khóa chính tự tăng';
COMMENT ON COLUMN users.email     IS 'Email đăng nhập — unique, không phân biệt hoa thường';
COMMENT ON COLUMN users.password  IS 'Mật khẩu đã hash bằng BCrypt. KHÔNG lưu mật khẩu plaintext';
COMMENT ON COLUMN users.full_name IS 'Họ và tên đầy đủ';
COMMENT ON COLUMN users.phone     IS 'Số điện thoại liên hệ';
COMMENT ON COLUMN users.role      IS 'Phân quyền: CUSTOMER (khách hàng thông thường) | ADMIN (quản trị viên)';
COMMENT ON COLUMN users.is_active IS 'FALSE để khóa tài khoản mà không xóa dữ liệu lịch sử đơn hàng';
COMMENT ON COLUMN users.created_at IS 'Thời điểm đăng ký tài khoản';

-- ============================================================
-- TABLE: user_address
-- ============================================================
COMMENT ON TABLE user_address IS 'Sổ địa chỉ giao hàng của người dùng — mỗi user có thể lưu nhiều địa chỉ';

COMMENT ON COLUMN user_address.id         IS 'Khóa chính tự tăng';
COMMENT ON COLUMN user_address.user_id    IS 'Chủ sở hữu địa chỉ — FK tới users.id, cascade delete';
COMMENT ON COLUMN user_address.full_name  IS 'Tên người nhận tại địa chỉ này (có thể khác tên tài khoản)';
COMMENT ON COLUMN user_address.phone      IS 'Số điện thoại liên hệ tại địa chỉ này';
COMMENT ON COLUMN user_address.province   IS 'Tỉnh/Thành phố (vd: Hà Nội, TP. Hồ Chí Minh)';
COMMENT ON COLUMN user_address.district   IS 'Quận/Huyện';
COMMENT ON COLUMN user_address.ward       IS 'Phường/Xã';
COMMENT ON COLUMN user_address.detail     IS 'Địa chỉ chi tiết: số nhà, tên đường, tòa nhà... (vd: 123 Nguyễn Trãi, P. Thượng Đình)';
COMMENT ON COLUMN user_address.is_default IS 'TRUE nếu đây là địa chỉ mặc định — tự động điền khi checkout. Mỗi user chỉ có 1 địa chỉ default';