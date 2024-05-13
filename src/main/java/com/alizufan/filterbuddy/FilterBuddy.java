package com.alizufan.filterbuddy;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import com.alizufan.filterbuddy.annotation.FilterBuddyRuleSupplier;
import com.alizufan.filterbuddy.annotation.FilterBuddyRuleSupplyProvider;
import com.alizufan.filterbuddy.entity.input.FilterAttribute;
import com.alizufan.filterbuddy.entity.internal.FilterFinalData;
import com.alizufan.filterbuddy.entity.internal.FilterInputCondition;
import com.alizufan.filterbuddy.entity.internal.FilterOperation;
import com.alizufan.filterbuddy.entity.internal.FilterRule;
import com.alizufan.filterbuddy.enumeration.FilterErrorEnum;
import com.alizufan.filterbuddy.enumeration.FilterInputTypeEnum;
import com.alizufan.filterbuddy.exception.FailedFetchFilterRules;
import com.alizufan.filterbuddy.exception.FailedProcessFilterRules;

import java.lang.reflect.Field;

@Data
@NoArgsConstructor
public class FilterBuddy {

    protected Map<String, FilterFinalData> data = new LinkedHashMap<>();
    protected Map<String, FilterAttribute> input = new LinkedHashMap<>();
    protected Map<String, FilterRule> rules = new LinkedHashMap<>();

    public static FilterBuddy start(Map<String, FilterAttribute> input) {
        return new FilterBuddy(input);
    }

    public static FilterBuddy start(Supplier<Map<String, FilterAttribute>> inputFn) {
        return new FilterBuddy(inputFn);
    }

    public FilterBuddy(Map<String, FilterAttribute> input) {
        if (input == null) return;
        this.input = input;
    }

    public FilterBuddy(Supplier<Map<String, FilterAttribute>> inputFn) {
        if (inputFn == null) return;
        this.input = inputFn.get();
    }

    /**
     * Load Filter Rules From Supplier Annotation
     * 
     * @return  FilterBuddy
     * 
     * @throws  FailedFetchFilterRules
     *          If failed fetch filter rules data from your source.
     * @throws  FailedProcessFilterRules
     *          If failed load filter object.
     */
    public FilterBuddy load(Class<?> obj) throws FailedFetchFilterRules, FailedProcessFilterRules {
        Field[] fields;

        try {
            fields = obj.getDeclaredFields();
        } catch (Throwable e) {
            throw new FailedProcessFilterRules("failed get declared filter object fields", e);
        }

        for (Field field : fields) {
            if (!field.isAnnotationPresent(FilterBuddyRuleSupplier.class)) continue;
            try {
                this.fetchRules(field);
            } catch (Throwable e) {
                throw e;
            }
        }

        return this;
    }


    /**
     * Validate Filter Input By Filter Rules <p>
     * 
     * Note: 
     * <p> Filter input initated from FilterBuddy constructor.
     * 
     * @return  FilterBuddy
     * 
     */
    public FilterBuddy validate() {
        return this.validate(this.input);
    }

    /**
     * Validate Filter Input By Filter Rules
     * 
     * @param   Supplier<Map<String, FilterAttribute>> inputFn
     * 
     * @return  FilterBuddy
     * 
     */
    public FilterBuddy validate(Supplier<Map<String, FilterAttribute>> inputFn) {
        return this.validate(inputFn == null ? new LinkedHashMap<>() : inputFn.get());
    }

    /**
     * Validate Filter Input By Filter Rules
     * 
     * @param   Map<String, FilterAttribute> input
     * 
     * @return  FilterBuddy
     * 
     */
    public FilterBuddy validate(Map<String, FilterAttribute> input) {
        if (rules == null || rules.isEmpty()) {
            return this;
        }

        if (input == null) input = new LinkedHashMap<>();

        for (String key : this.rules.keySet()) {
            // init new
            FilterFinalData d = new FilterFinalData();
            FilterInputCondition condition = new FilterInputCondition();

            // source data
            FilterAttribute in = input.get(key);
            FilterRule rule = this.rules.get(key);

            String ruleInputType = rule.getInputType();
            FilterInputTypeEnum ruleInputTypeEnum = FilterInputTypeEnum.find(ruleInputType);
            Function<String, FilterOperation> findOperationFn = null;
            if (ruleInputTypeEnum != null) findOperationFn = ruleInputTypeEnum.getOperation();

            // handle input key not found
            if (in == null) {
                in = FilterAttribute
                        .builder()
                            .field(rule.getField())
                            .inputType(rule.getInputType())
                            .operation(null)
                            .values(new ArrayList<>())
                        .build();
                condition.setActive(false);
                condition.setError(FilterErrorEnum.UnSupportedInputField);
                d.setCondition(condition);
            }

            // handle in-active filter
            else if (!rule.isActive()) {
                condition.setActive(false);
                condition.setError(FilterErrorEnum.InActive);
                d.setCondition(condition);
            }

            // handle input type filter
            else if (!ruleInputType.equals(in.getInputType())) {
                condition.setActive(false);
                condition.setError(FilterErrorEnum.UnSupportedInputType);
                d.setCondition(condition);
            }

            // handle input type operation filter
            else if (
                ruleInputTypeEnum == null || findOperationFn.apply(in.getOperation()) == null
            ) {
                condition.setActive(false);
                condition.setError(FilterErrorEnum.UnSupportedInputOperation);
                d.setCondition(condition);
            }

            // check except operation (is empty and is not empty) must have one or more values
            else if (!in.getOperation().contains("Empty") && in.getValues().size() <= 0) {
                condition.setActive(false);
                condition.setError(FilterErrorEnum.MustHaveOneOrMoreValues);
                d.setCondition(condition);
            }

            // check operation is between must have start and end value
            else if (in.getOperation() == "isBetween" && in.getValues().size() != 2) {
                condition.setActive(false);
                condition.setError(FilterErrorEnum.MustHaveStartAndEndValue);
                d.setCondition(condition);
            }

            // handle valid filter
            else {
                condition.setActive(true);
                condition.setError(null);
                d.setCondition(condition);
            }

            in.setField(rule.getField());
            d.setInput(in);
            d.setRule(rule);
            this.data.put(key, d);
        }

        return this;
    }

    private void fetchRules(Field field) throws FailedFetchFilterRules {
        FilterBuddyRuleSupplier annotation = field.getAnnotation(FilterBuddyRuleSupplier.class);
        Class<? extends FilterBuddyRuleSupplyProvider> readerClazz = annotation.clazz();
        try {
            FilterBuddyRuleSupplyProvider reader = readerClazz.getDeclaredConstructor().newInstance();
            List<FilterRule> listRules = reader.fetchFilterRules();
            for (FilterRule filterRule : listRules) {
                this.rules.put(filterRule.getKey(), filterRule);
            }
        } catch (FailedFetchFilterRules e) {
            throw e;
        } catch (Throwable e) {
            throw new FailedFetchFilterRules("failed initiate clazz filter set rules reader", e);
        }
    }
}
