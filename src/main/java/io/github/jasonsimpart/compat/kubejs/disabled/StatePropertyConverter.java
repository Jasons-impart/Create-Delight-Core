package io.github.jasonsimpart.compat.kubejs.disabled;

import java.util.LinkedHashMap;
import java.util.Map;

final class StatePropertyConverter {
    private StatePropertyConverter() {
    }

    static Map<String, String> toStringMap(Map<?, ?> properties) {
        Map<String, String> result = new LinkedHashMap<>();
        if (properties == null) {
            return result;
        }

        for (Map.Entry<?, ?> entry : properties.entrySet()) {
            result.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
        }
        return result;
    }
}
