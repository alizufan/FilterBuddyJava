package com.alizufan.filterbuddy.entity.internal;

import com.alizufan.filterbuddy.enumeration.FilterErrorEnum;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FilterInputCondition {
    private FilterErrorEnum error;
    private boolean active;
}
