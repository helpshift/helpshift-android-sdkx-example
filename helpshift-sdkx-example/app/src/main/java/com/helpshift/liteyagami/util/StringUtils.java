package com.helpshift.liteyagami.util;

import java.util.Map;

public class StringUtils {

    public static String generatePrettyStringForMap(Map<String, Object> config, String indentSpace) {
        StringBuilder sb = new StringBuilder();

        sb.append("{").append("\n");

        for (Map.Entry<String, Object> entry : config.entrySet()) {
            sb.append(indentSpace);
            sb.append("\"").append(entry.getKey()).append("\"").append(" : ");

            Object value = entry.getValue();

            if (value instanceof String) {
                sb.append("\"").append(value).append("\"");
            } else if (value instanceof String[]) {
                sb.append("[");
                for (String tag : (String[]) value) {
                    sb.append(tag).append(", ");
                }
                sb.delete(sb.length() - 2, sb.length());
                sb.append("]");
            } else if (value instanceof Map) {
                sb.append(generatePrettyStringForMap((Map<String, Object>) value, indentSpace + "  "));
            } else {
                sb.append(value);
            }
            sb.append("\n").append(indentSpace);
        }

        sb.append("}");

        return sb.toString();
    }
}
