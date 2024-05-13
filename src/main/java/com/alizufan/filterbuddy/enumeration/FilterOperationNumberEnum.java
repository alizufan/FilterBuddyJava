package com.alizufan.filterbuddy.enumeration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.alizufan.filterbuddy.entity.internal.FilterOperation;

import lombok.Getter;

@Getter
public enum FilterOperationNumberEnum {
    IsEqual("isEqual", "="),
    IsNotEqual("isNotEqual", "≠"),
    IsGreaterThan("isGreaterThan", ">"),
    IsLessThan("isLessThan", "<"),
    IsGreaterThanOrEqual("isGreaterThanOrEqual", "≥"),
    IsLessThanOrEqual("isLessThanOrEqual", "≤"),
    IsEmpty("isEmpty", "is empty"),
    IsNotEmpty("isNotEmpty", "is not empty");

    private String id;
    private String label;

    FilterOperationNumberEnum(String id, String label) {
        this.id = id;
        this.label = label;
    }

    public static FilterOperation findOperation(String id) {
        FilterOperationNumberEnum d = FilterOperationNumberEnum.find(id);
        if (d == null) return null;
        return new FilterOperation(d.id, d.label);
    }

    public static FilterOperationNumberEnum find(String id) {
        Optional<FilterOperationNumberEnum> value = Arrays
            .stream(values())
            .filter(v -> {
                if (v.id == null) return false;
                return v.id.equals(id);
            })
            .findFirst();
        return value.isPresent() ? value.get() : null;
    }

    public static List<FilterOperation> toList() {
        List<FilterOperation> values = Arrays
            .stream(values())
            .map(v -> new FilterOperation(v.id, v.label))
            .collect(Collectors.toList());
        return values != null ? values : new ArrayList<>();
    }

}
