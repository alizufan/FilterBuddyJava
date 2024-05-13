package com.alizufan.filterbuddy.builder.rawmysql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;

public class RawMySqlAdditional {
    
    @Data
    public static class Payload {
        private String query;
        private List<String> values = new ArrayList<>();

        Payload(String query, List<String> values) {
            this.query = query;
            this.values = values;
        }
    }

    @Data
    public static class Build {

        private int counter;

        private String key;

        private List<Payload> queries = new ArrayList<>();

        public static Build query() {
            return new Build();
        }

        public static Map<String, List<Payload>> process(Build... values) {
            Map<String, List<Payload>> res = new LinkedHashMap<>();
            for (Build v : values) {
                if (v.getKey().length() <= 0) continue;
                res.put(v.getKey(), v.getQueries());
            }
            return res;
        }

        public Build on(String key) {
            this.key = key;
            return this;
        }

        public Build where(String query, String... values) {
            this.queries.add(
                new Payload(query, Arrays.asList(values))
            );
            return this;
        }

    }
}
