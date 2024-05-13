package com.alizufan.filterbuddy.entity.input;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FilterAttribute {
    private String field;
    private String inputType;
    private String operation;
    private List<String> values;
}
