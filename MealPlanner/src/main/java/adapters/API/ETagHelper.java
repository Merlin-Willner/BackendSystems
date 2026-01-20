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
