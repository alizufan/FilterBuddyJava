package com.alizufan.filterbuddy.entity.internal;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FilterInputType {
    private String id;
    private String label;
    private List<FilterOperation> operations;
}
