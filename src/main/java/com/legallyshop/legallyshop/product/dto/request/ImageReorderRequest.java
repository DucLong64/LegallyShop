package com.legallyshop.legallyshop.product.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class ImageReorderRequest {

    /**
     * Danh sách image ID theo thứ tự mới.
     * Backend gán sortOrder = index của từng id trong list.
     * ID đầu tiên sẽ được set isPrimary = true tự động.
     */
    @NotEmpty(message = "Danh sách id không được trống")
    private List<Long> imageIds;
}