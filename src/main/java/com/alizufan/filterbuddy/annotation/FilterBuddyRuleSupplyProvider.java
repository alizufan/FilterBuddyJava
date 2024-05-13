package com.alizufan.filterbuddy.annotation;

import java.util.List;

import com.alizufan.filterbuddy.entity.internal.FilterRule;
import com.alizufan.filterbuddy.exception.FailedFetchFilterRules;

public interface FilterBuddyRuleSupplyProvider {
    List<FilterRule> fetchFilterRules() throws FailedFetchFilterRules;
}