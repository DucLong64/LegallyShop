package com.legallyshop.legallyshop.category.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class AttributeTemplateRequest {

    @NotBlank(message = "Tên thuộc tính không được trống")
    private String name;

    @NotNull(message = "Kiểu nhập không được trống")
    @Pattern(
            regexp = "text|select|number|boolean",
            message = "inputType phải là: text, select, number, hoặc boolean"
    )
    private String inputType;

    private Boolean isRequired = false;

    private Integer sortOrder = 0;
}