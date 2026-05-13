package com.legallyshop.legallyshop.product.dto.request;

import com.legallyshop.legallyshop.product.entity.ProductStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ProductUpdateRequest {

    @NotBlank(message = "Tên sản phẩm không được trống")
    private String name;

    private Long categoryId;
    private String description;
    private ProductStatus status;

    private List<ProductCreateRequest.AttributeValueDto> attributes = new ArrayList<>();
    private List<ProductCreateRequest.ImageDto> images = new ArrayList<>();
}