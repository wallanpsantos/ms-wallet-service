package com.br.walletdataprovider.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Utility class to access and process data from event payloads.
 * The payload is expected to be a Map<String, Object>.
 */
public final class EventPayloadAccessor {

    private EventPayloadAccessor() {
    }

    public static Object cleanPayloadForSerialization(Object payload, List<String> fieldsToExclude) {
        if (payload instanceof Map<?, ?>) {
            Map<String, Object> map = new HashMap<>((Map<String, Object>) payload);
            fieldsToExclude.stream().map(String::toLowerCase).forEach(map::remove);
            return map;
        }
        return payload;
    }

    public static Optional<String> extractStringValue(Object payload, String key) {
        if (payload instanceof Map<?, ?>) {
            Map<String, Object> map = (Map<String, Object>) payload;
            return Optional.ofNullable(map.get(key)).map(Object::toString);
        }
        return Optional.empty();
    }
}