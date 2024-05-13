package com.alizufan.filterbuddy.builder.rawmysql;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.alizufan.filterbuddy.entity.input.FilterAttribute;
import com.alizufan.filterbuddy.entity.internal.FilterCasty;
import com.alizufan.filterbuddy.entity.internal.FilterFinalData;
import com.alizufan.filterbuddy.entity.internal.FilterInputCondition;
import com.alizufan.filterbuddy.entity.internal.FilterRule;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class RawMySqlBuild {

    public static RawMySqlBuild start(Map<String, FilterFinalData> finalData) {
        return new RawMySqlBuild(finalData);
    }

    public static RawMySqlBuild start(Supplier<Map<String, FilterFinalData>> finalDataFn) {
        return new RawMySqlBuild(finalDataFn);
    }

    private Map<String, FilterFinalData> finalData = new LinkedHashMap<>();

    public RawMySqlBuild(Map<String, FilterFinalData> finalData) {
        this.finalData = finalData;
    }

    public RawMySqlBuild(Supplier<Map<String, FilterFinalData>> finalDataFn) {
        this.finalData = finalDataFn.get();
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Result {
        private String query;
        private String whereClause;
        private List<String> params;
    }

    public Result build() {
        return this.build(null);
    }

    public Result build(Supplier<Map<String, List<RawMySqlAdditional.Payload>>> additional) {
        Map<String, List<RawMySqlAdditional.Payload>> queries = new LinkedHashMap<>();
        Map<String, List<RawMySqlAdditional.Payload>> add = additional == null ?  new LinkedHashMap<>() : additional.get();

        int paramCounter = 0;
        for (String filterKey : this.finalData.keySet()) {
            FilterFinalData d = this.finalData.get(filterKey);

            FilterRule rule = d.getRule();
            if (rule == null) continue;
            String field = rule.getField();

            FilterInputCondition inputCondition = d.getCondition();
            FilterInputCondition additionalCondition = FilterInputCondition
                .builder()
                .active(inputCondition.isActive())
                .error(inputCondition.getError())
                .build();

            // # Additional Query

            List<RawMySqlAdditional.Payload> fieldData = add.get(field);
            fieldData = fieldData == null ? fieldData : new ArrayList<>();

            // process additionl collection query
            if (fieldData.size() > 0) {
                ProcessAdditionalQuery additionlProcessData = this.additionalQueryProcess(
                    ProcessAdditionalQuery.builder()
                        .counter(paramCounter)
                        .field(field)
                        .condition(additionalCondition)
                        .data(fieldData)
                        .build()
                );
                if (additionlProcessData.isChange()) {
                    fieldData = additionlProcessData.getData();
                    additionalCondition = additionlProcessData.getCondition();
                    paramCounter = additionlProcessData.getCounter();
                }

                // check additional filter condition
                if (!additionalCondition.isActive()) continue;
            }

            // check input filter condition
            if (!inputCondition.isActive()) {
                if (fieldData.size() > 0) queries.put(field, fieldData);
                continue;
            }

            // # Input Filter Query

            // init
            FilterAttribute input = d.getInput();
            String operation = input.getOperation();
            String inputType = input.getInputType();
            List<String> values = input.getValues();

            // find operation payload
            RawMySqlOperation.Payload data = RawMySqlOperation.findPayload(
                inputType,
                operation
            );
            if (data == null) {
                if (fieldData.size() > 0) queries.put(field, fieldData);
                continue;
            }

            // getter template and options data
            String template = data.getTpl();
            List<String> options = data.getOptions();

            // collect all state
            boolean isValue = template.contains(":Value");
            boolean isEachValue = template.contains(":EachValue");
            boolean isRangeValue = template.contains(":StartValue") && template.contains(":EndValue");
            // options state
            boolean isRepeated = options.contains("REPEATED");

            // init query
            List<String> keys = new ArrayList<>();
            List<String> repeatedTemplate = new ArrayList<>();

            FilterCasty casty = rule.getCasty();
            casty = casty == null ? new FilterCasty() : casty;

            // processor template
            //  - template = template
            template = template.replace(":Field", this.castField(casty, field));

            // skip is empty or not empty operation
            if (!data.getId().contains("Empty")) {
                for (int i = 0; i < values.size(); i++) {
                    paramCounter++;
                    String key = this.castValue(casty, String.format("?%d", paramCounter));
                    keys.add(key);
                    if (isRepeated) repeatedTemplate.add(template.replace(":Value", key));
                }
            }

            if (isValue) {
                if (isRepeated) template = String.join(" OR ", repeatedTemplate);
                else template = template.replace(":Value", keys.get(0));
            }
            else if (isEachValue) {
                template = template.replace(":EachValue", String.join(",", keys));
            }
            else if (isRangeValue) {
                template = template.replace(":StartValue", keys.get(0));
                template = template.replace(":EndValue", keys.get(1));
            }

            // group query by field
            fieldData.add(new RawMySqlAdditional.Payload(template, values));
            queries.put(field, fieldData);
        }

        return this.collectionQueryProcessor(queries);
    }

    private String castField(FilterCasty casty, String value) {
        String src = casty.getCastField();
        return src != null ? src.replace(":field", value): value;
    }

    private String castValue(FilterCasty casty, String value) {
        String src = casty.getCastValue();
        return src != null ? src.replace(":value", value): value;
    }

    private Result collectionQueryProcessor(Map<String, List<RawMySqlAdditional.Payload>> queries) {
        // init template sql query
        StringBuilder sql = new StringBuilder();
        List<String> params = new ArrayList<>();
        
        // counter info
        int queryCount = 0;
        int collectionQuerySize = queries.size();
        int lastIdxCollectionQuery = collectionQuerySize - 1;
        
        // map a list of template to sql query
        for (String key : queries.keySet()) {
            List<RawMySqlAdditional.Payload> query = queries.get(key);

            int size = query.size();
            int lastIdxQuery = size - 1;
            boolean isQueryHasValue = size >= 1;

            if(isQueryHasValue) sql.append("(");

            for (int i = 0; i < size; i++) {
                RawMySqlAdditional.Payload item = query.get(i);

                // add open parentheses "(" when condition:
                // - start index 0
                // - and query size greater than 1
                if (i == 0 && size > 1) sql.append("(");

                sql.append(item.getQuery());
                params.addAll(item.getValues());

                // add "OR" clause when condition:
                // - current index less than last index
                if (i < lastIdxQuery) sql.append(" OR ");
                
                // add close parentheses ")" when condition:
                // - current index equal with last index
                // - and query size greater than 1
                if (i == lastIdxQuery && size > 1) sql.append(")");
            }

            if (isQueryHasValue) sql.append(")");

            if (queryCount < lastIdxCollectionQuery) sql.append(" AND ");

            queryCount++;
        }

        String query = sql.toString();
        return Result
            .builder()
            .query(query)
            .whereClause(query.length() > 0 ? "WHERE": "")
            .params(params)
            .build();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    private static class ProcessAdditionalQuery {
        private boolean change;
        private int counter;
        private String field;
        private FilterInputCondition condition;
        private List<RawMySqlAdditional.Payload> data;
    }

    private ProcessAdditionalQuery additionalQueryProcess(ProcessAdditionalQuery d) {
        List<RawMySqlAdditional.Payload> data = d.getData();
        int dataSize = data.size();
        if (dataSize <= 0) return d;

        int counter = d.getCounter();
        FilterInputCondition condition = d.getCondition();

        for (int idxItem = 0; idxItem < dataSize; idxItem++) {
            List<String> keys = new ArrayList<>();
            RawMySqlAdditional.Payload item = data.get(idxItem);

            int paramSize = item.getValues().size();
            String query = item.getQuery();

            for (int idxParam = 0; idxParam < paramSize; idxParam++) {
                counter++;
                String key = String.format("?%d", counter);
                keys.add(key);
                d.setCounter(counter);
            }

            // process state
            boolean isValue = query.contains(":Value");
            boolean isEachValue = query.contains(":EachValue");
            boolean isRangeValue = query.contains(":StartValue") && query.contains(":EndValue");

            // process
            query = query.replace(":Field", d.getField());
            if (isValue) {
                query = query.replace(":Value", keys.get(0));
            }
            else if (isEachValue) {
                query = query.replace(":EachValue", String.join(",", keys));
            }
            else if (isRangeValue) {
                query = query.replace(":StartValue", keys.get(0));
                query = query.replace(":EndValue", keys.get(1));
            }

            item.setQuery(query);
            data.set(idxItem, item);
        }

        condition.setActive(true);
        condition.setError(null);
        d.setCondition(condition);
        d.setChange(true);
        d.setData(data);

        return d;
    }
}
