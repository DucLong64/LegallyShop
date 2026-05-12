package com.legallyshop.legallyshop.category.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ReorderRequest {
    /**
     * Danh sách id theo thứ tự mới.
     * Backend sẽ gán sortOrder = index của từng id trong list.
     * VD: [3, 1, 2] → id=3 có sortOrder=0, id=1 có sortOrder=1, id=2 có sortOrder=2
     */
    @NotEmpty(message = "Danh sách id không được trống")
    private List<Long> ids;
}

