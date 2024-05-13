package com.alizufan.filterbuddy.entity.internal;

import com.alizufan.filterbuddy.entity.input.FilterAttribute;

import lombok.Data;

@Data
public class FilterFinalData {
    private FilterAttribute input;
    private FilterInputCondition condition;
    private FilterRule rule;
}
