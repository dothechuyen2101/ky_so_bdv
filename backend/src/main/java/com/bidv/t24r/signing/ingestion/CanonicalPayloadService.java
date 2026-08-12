package com.bidv.t24r.signing.ingestion;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.stereotype.Service;

@Service
public class CanonicalPayloadService {
    private final ObjectMapper mapper;

    public CanonicalPayloadService(ObjectMapper objectMapper) {
        this.mapper = objectMapper.copy()
                .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true)
                .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
    }

    public String canonicalize(T24rRawRecord record) {
        try {
            return mapper.writeValueAsString(record.payload());
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot canonicalize T24R payload " + record.businessKey(), ex);
        }
    }
}
