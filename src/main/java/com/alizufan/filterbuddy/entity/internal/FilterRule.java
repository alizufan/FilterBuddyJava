package com.alizufan.filterbuddy.entity.internal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FilterRule {

    private String key;

    private String field;

    private FilterCasty casty;

    private String inputType;

    private boolean active;

}
