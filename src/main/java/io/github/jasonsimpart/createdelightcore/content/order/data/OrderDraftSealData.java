package io.github.jasonsimpart.createdelightcore.content.order.data;

public record OrderDraftSealData(String type, String key, OrderSpecData spec) {
    public OrderDraftSealData {
        type = type == null ? "" : type;
        key = key == null ? "" : key;
        spec = spec == null ? OrderSpecData.EMPTY : spec;
    }
}
