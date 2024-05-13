package com.alizufan.filterbuddy.builder.rawmysql;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.alizufan.filterbuddy.enumeration.FilterInputTypeEnum;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class RawMySqlOperation {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Payload {
        private String id;
        private FilterInputTypeEnum type;
        private String tpl;
        private List<String> options;
    }

    public static Payload findPayload(String type, String op) {
        Payload d = new Payload();
        switch (type) {
            case "text":
                d = Text.findPayloadBy(op);
                break;
            case "select":
                d = Select.findPayloadBy(op);
                break;
            case "status":
                d = Status.findPayloadBy(op);
                break;
            case "number":
                d = Number.findPayloadBy(op);
                break;
            case "date":
                d = Date.findPayloadBy(op);
                break;
            case "datetime":
                d = DateTime.findPayloadBy(op);
                break;
            default:
                d = null;
        }
        return d;
    }

    @Getter
    public static enum Text {
        Is("is", ":Field IN(:EachValue)"),
        IsNot("isNot", ":Field NOT IN(:EachValue)"),
        Contains("contains", ":Field LIKE CONCAT('%', :Value, '%')", "REPEATED"),
        DoesNotContain("doesNotContain", ":Field NOT LIKE CONCAT('%', :Value, '%')", "REPEATED"),
        StartWith("startWith", ":Field LIKE CONCAT(:Value, '%')", "REPEATED"),
        EndWith("endWith", ":Field LIKE CONCAT('%', :Value)", "REPEATED"),
        IsEmpty("isEmpty", ":Field IS NULL"),
        IsNotEmpty("isNotEmpty", ":Field IS NOT NULL");

        private String id;
        private FilterInputTypeEnum type = FilterInputTypeEnum.TEXT;
        private String tpl;
        private List<String> options;
        private Payload data;

        Text(String id, String tpl, String... options) {
            this.id = id;
            this.tpl = tpl;
            this.options = Arrays.asList(options);
            this.data = Payload.builder()
                    .id(id)
                    .type(type)
                    .tpl(tpl)
                    .options(this.options)
                    .build();
        }

        public static Payload findPayloadBy(String id) {
            Optional<Text> value = Arrays
                    .stream(values())
                    .filter(v -> {
                        if (v.id == null)
                            return false;
                        return v.id.equals(id);
                    })
                    .findFirst();
            return value.isPresent() ? value.get().getData() : null;
        }
    }

    @Getter
    public static enum Status {
        Is("is", ":Field IN(:EachValue)"),
        IsNot("isNot", ":Field NOT IN(:EachValue)"),
        IsEmpty("isEmpty", ":Field IS NULL"),
        IsNotEmpty("isNotEmpty", ":Field IS NOT NULL");

        private String id;
        private FilterInputTypeEnum type = FilterInputTypeEnum.STATUS;
        private String tpl;
        private List<String> options;
        private Payload data;

        Status(String id, String tpl, String... options) {
            this.id = id;
            this.tpl = tpl;
            this.options = Arrays.asList(options);
            this.data = Payload.builder()
                .id(id)
                .type(type)
                .tpl(tpl)
                .options(this.options)
                .build();
        }

        public static Payload findPayloadBy(String id) {
            Optional<Status> value = Arrays
                    .stream(values())
                    .filter(v -> {
                        if (v.id == null)
                            return false;
                        return v.id.equals(id);
                    })
                    .findFirst();
            return value.isPresent() ? value.get().getData() : null;
        }
    }

    @Getter
    public static enum Select {
        Is("is", ":Field IN(:EachValue)"),
        IsNot("isNot", ":Field NOT IN(:EachValue)"),
        IsEmpty("isEmpty", ":Field IS NULL"),
        IsNotEmpty("isNotEmpty", ":Field IS NOT NULL");

        private String id;
        private FilterInputTypeEnum type = FilterInputTypeEnum.SELECT;
        private String tpl;
        private List<String> options;
        private Payload data;

        Select(String id, String tpl, String... options) {
            this.id = id;
            this.tpl = tpl;
            this.options = Arrays.asList(options);
            this.data = Payload.builder()
                    .id(id)
                    .type(type)
                    .tpl(tpl)
                    .options(this.options)
                    .build();
        }

        public static Payload findPayloadBy(String id) {
            Optional<Select> value = Arrays
                    .stream(values())
                    .filter(v -> {
                        if (v.id == null)
                            return false;
                        return v.id.equals(id);
                    })
                    .findFirst();
            return value.isPresent() ? value.get().getData() : null;
        }
    }

    @Getter
    public static enum Number {
        IsEqual("isEqual", ":Field = :Value"),
        IsNotEqual("isNotEqual", ":Field != :Value"),
        IsGreaterThan("isGreaterThan", ":Field > :Value"),
        IsLessThan("isLessThan", ":Field < :Value"),
        IsGreaterThanOrEqual("isGreaterThanOrEqual", ":Field >= :Value"),
        IsLessThanOrEqual("isLessThanOrEqual", ":Field <= :Value"),
        IsEmpty("isEmpty", ":Field IS NULL"),
        IsNotEmpty("isNotEmpty", ":Field IS NULL");

        private String id;
        private FilterInputTypeEnum type = FilterInputTypeEnum.NUMBER;
        private String tpl;
        private List<String> options;
        private Payload data;

        Number(String id, String tpl, String... options) {
            this.id = id;
            this.tpl = tpl;
            this.options = Arrays.asList(options);
            this.data = Payload.builder()
                    .id(id)
                    .type(type)
                    .tpl(tpl)
                    .options(this.options)
                    .build();
        }

        public static Payload findPayloadBy(String id) {
            Optional<Number> value = Arrays
                    .stream(values())
                    .filter(v -> {
                        if (v.id == null)
                            return false;
                        return v.id.equals(id);
                    })
                    .findFirst();
            return value.isPresent() ? value.get().getData() : null;
        }
    }

    @Getter
    public static enum Date {
        Is("is", ":Field = :Value"),
        IsBefore("isBefore", ":Field < :Value"),
        IsAfter("isAfter", ":Field > :Value"),
        IsOnOrBefore("isOnOrBefore", ":Field <= :Value"),
        IsOnOrAfter("isOnOrAfter", ":Field >= :Value"),
        IsBetween("isBetween", ":Field BETWEEN :StartValue AND :EndValue"),
        IsEmpty("isEmpty", ":Field IS NULL"),
        IsNotEmpty("isNotEmpty", ":Field IS NULL");

        private String id;
        private FilterInputTypeEnum type = FilterInputTypeEnum.DATE;
        private String tpl;
        private List<String> options;
        private Payload data;

        Date(String id, String tpl, String... options) {
            this.id = id;
            this.tpl = tpl;
            this.options = Arrays.asList(options);
            this.data = Payload.builder()
                    .id(id)
                    .type(type)
                    .tpl(tpl)
                    .options(this.options)
                    .build();
        }

        public static Payload findPayloadBy(String id) {
            Optional<Date> value = Arrays
                    .stream(values())
                    .filter(v -> {
                        if (v.id == null)
                            return false;
                        return v.id.equals(id);
                    })
                    .findFirst();
            return value.isPresent() ? value.get().getData() : null;
        }
    }

    @Getter
    public static enum DateTime {
        Is("is", ":Field = :Value"),
        IsBefore("isBefore", ":Field < :Value"),
        IsAfter("isAfter", ":Field > :Value"),
        IsOnOrBefore("isOnOrBefore", ":Field <= :Value"),
        IsOnOrAfter("isOnOrAfter", ":Field >= :Value"),
        IsBetween("isBetween", ":Field IS BETWEEN :StartValue AND :EndValue"),
        IsEmpty("isEmpty", ":Field IS NULL"),
        IsNotEmpty("isNotEmpty", ":Field IS NULL");

        
        private String id;
        private FilterInputTypeEnum type = FilterInputTypeEnum.DATETIME;
        private String tpl;
        private List<String> options;
        private Payload data;

        DateTime(String id, String tpl, String... options) {
            this.id = id;
            this.tpl = tpl;
            this.options = Arrays.asList(options);
            this.data = Payload.builder()
                    .id(id)
                    .type(type)
                    .tpl(tpl)
                    .options(this.options)
                    .build();
        }

        public static Payload findPayloadBy(String id) {
            Optional<DateTime> value = Arrays
                    .stream(values())
                    .filter(v -> {
                        if (v.id == null)
                            return false;
                        return v.id.equals(id);
                    })
                    .findFirst();
            return value.isPresent() ? value.get().getData() : null;
        }
    }
}
