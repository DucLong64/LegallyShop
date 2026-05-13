package com.legallyshop.legallyshop.product.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.legallyshop.legallyshop.common.exception.AppException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryService {

    private final Cloudinary cloudinary;

    @Value("${cloudinary.folder:yourshop}")
    private String folder;

    /**
     * Upload ảnh lên Cloudinary.
     *
     * @param file      file ảnh từ multipart request
     * @param subfolder subfolder trong Cloudinary (vd: "products", "categories")
     * @return URL công khai của ảnh đã upload
     */
    public String upload(MultipartFile file, String subfolder) {
        validateImageFile(file);

        try {
            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", folder + "/" + subfolder,
                            // Tự resize về tối đa 1200px chiều rộng, giữ tỉ lệ
                            "transformation", "w_1200,c_limit,q_auto,f_auto",
                            // Tên file = timestamp để tránh cache cũ
                            "use_filename", false,
                            "unique_filename", true
                    )
            );
            return (String) result.get("secure_url");

        } catch (IOException e) {
            log.error("Cloudinary upload failed: {}", e.getMessage());
            throw AppException.badRequest("Upload ảnh thất bại: " + e.getMessage());
        }
    }

    /**
     * Upload ảnh thumbnail — resize nhỏ hơn, dùng cho list view.
     */
    public String uploadThumbnail(MultipartFile file, String subfolder) {
        validateImageFile(file);

        try {
            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", folder + "/" + subfolder + "/thumb",
                            "transformation", "w_400,h_400,c_fill,g_auto,q_auto,f_auto",
                            "unique_filename", true
                    )
            );
            return (String) result.get("secure_url");

        } catch (IOException e) {
            throw AppException.badRequest("Upload thumbnail thất bại: " + e.getMessage());
        }
    }

    /**
     * Xóa ảnh khỏi Cloudinary theo public_id.
     * public_id lấy từ URL: https://res.cloudinary.com/{cloud}/{version}/{public_id}.{ext}
     */
    public void delete(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) return;

        try {
            String publicId = extractPublicId(imageUrl);
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            log.info("Cloudinary deleted: {}", publicId);
        } catch (Exception e) {
            // Log lỗi nhưng không throw — ảnh xấu không nên block nghiệp vụ chính
            log.warn("Failed to delete image from Cloudinary: {}", e.getMessage());
        }
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw AppException.badRequest("File ảnh không được trống");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw AppException.badRequest("File phải là ảnh (jpg, png, webp...)");
        }

        // Giới hạn 5MB
        if (file.getSize() > 5 * 1024 * 1024) {
            throw AppException.badRequest("Kích thước ảnh không được vượt quá 5MB");
        }
    }

    /**
     * Trích xuất public_id từ Cloudinary URL.
     * VD: https://res.cloudinary.com/demo/image/upload/v123/yourshop/products/abc.jpg
     * → yourshop/products/abc
     */
    private String extractPublicId(String url) {
        // Lấy phần sau "/upload/" và bỏ version + extension
        String[] parts = url.split("/upload/");
        if (parts.length < 2) return url;

        String path = parts[1];
        // Bỏ version (v12345/) nếu có
        if (path.matches("v\\d+/.*")) {
            path = path.substring(path.indexOf('/') + 1);
        }
        // Bỏ extension
        int dotIdx = path.lastIndexOf('.');
        return dotIdx > 0 ? path.substring(0, dotIdx) : path;
    }
}
