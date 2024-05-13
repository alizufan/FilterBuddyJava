package com.alizufan.filterbuddy.enumeration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.alizufan.filterbuddy.entity.internal.FilterInputType;
import com.alizufan.filterbuddy.entity.internal.FilterOperation;

import lombok.Getter;

@Getter
public enum FilterInputTypeEnum {

    TEXT("text", "Text", FilterOperationTextEnum::toList, FilterOperationTextEnum::findOperation),
    NUMBER("number", "Number", FilterOperationNumberEnum::toList, FilterOperationNumberEnum::findOperation),
    SELECT("select", "Select", FilterOperationSelectEnum::toList, FilterOperationSelectEnum::findOperation),
    STATUS("status", "Status", FilterOperationStatusEnum::toList, FilterOperationStatusEnum::findOperation),
    DATE("date", "Date", FilterOperationDateEnum::toList, FilterOperationDateEnum::findOperation),
    DATETIME("datetime", "DateTime", FilterOperationDateTimeEnum::toList, FilterOperationDateTimeEnum::findOperation);

    private String id;
    private String label;
    private Supplier<List<FilterOperation>> operations;
    private Function<String, FilterOperation> operation;

    FilterInputTypeEnum(String id, String label, Supplier<List<FilterOperation>> operations, Function<String, FilterOperation> operation) {
        this.id = id;
        this.label = label;
        this.operations = operations;
        this.operation = operation;
    }

    public static FilterInputTypeEnum find(String id) {
        Optional<FilterInputTypeEnum> value = Arrays
            .stream(values())
            .filter(v -> v.id.equals(id))
            .findFirst();
        return value.isPresent() ? value.get() : null;
    }

    public static List<FilterInputType> toList(String... id) {
        List<FilterInputType> values = Arrays
            .stream(values())
            .filter(v -> {
                if (id == null) return true;
                if (id.length > 0) return Arrays.asList(id).contains(v.id);
                return true;
            })
            .map(v -> new FilterInputType(v.id, v.label, v.operations.get()))
            .collect(Collectors.toList());
        return values != null ? values : new ArrayList<>();
    }

}