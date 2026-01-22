package adapters.API;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.core.EntityTag;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class ETagHelper {

    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Berechnet einen stabilen ETag für beliebige Objekte.
     * Unterstützt: einfache Typen, Listen, Maps und komplexe Objekte.
     */
    public static EntityTag calculate(Object obj) {
        if (obj == null) return new EntityTag("0");

        try {
            String stableString = toStableString(obj);
            return new EntityTag(Integer.toString(stableString.hashCode()));
        } catch (Exception e) {
            // Fallback bei Fehlern
            return new EntityTag("0");
        }
    }

    /**
     * Vergleicht If-Match Header mit einem EntityTag.
     * Akzeptiert quoted/unquoted Werte und Weak-Tags (W/).
     */
    public static boolean matches(String ifMatchHeader, EntityTag tag) {
        if (ifMatchHeader == null || tag == null) {
            return false;
        }
        String header = ifMatchHeader.trim();
        if (header.equals("*")) {
            return true;
        }
        String tagValue = tag.getValue();
        for (String rawPart : header.split(",")) {
            String part = rawPart.trim();
            if (part.startsWith("W/")) {
                part = part.substring(2).trim();
            }
            if (part.startsWith("\"") && part.endsWith("\"") && part.length() >= 2) {
                part = part.substring(1, part.length() - 1);
            }
            if (part.equals(tagValue)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Erzeugt eine stabile String-Repräsentation für alle Objekttypen.
     * - Listen werden sortiert
     * - Maps werden nach Key sortiert
     * - Komplexe Objekte werden als JSON serialisiert
     */
    @SuppressWarnings("unchecked")
    private static String toStableString(Object obj) throws JsonProcessingException {
        if (obj == null) return "null";

        // Einfache Typen
        if (obj instanceof String || obj instanceof Number || obj instanceof Boolean) {
            return obj.toString();
        }

        // Listen
        if (obj instanceof List<?> list) {
            List<String> items = list.stream()
                    .map(item -> {
                        try {
                            return toStableString(item);
                        } catch (JsonProcessingException e) {
                            return "null";
                        }
                    })
                    .collect(Collectors.toList());
            return String.join("-", items);
        }

        // Maps
        if (obj instanceof Map<?, ?> map) {
            Map<String, Object> sorted = new TreeMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                sorted.put(entry.getKey().toString(), entry.getValue());
            }
            return mapper.writeValueAsString(sorted);
        }

        // Alle anderen Objekte als JSON
        return mapper.writeValueAsString(obj);
    }
}
