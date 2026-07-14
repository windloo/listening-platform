package com.windloo.encoder.encoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StubEncoder implements MediaEncoder {
    @Override public boolean accept(String outputFormat) { return "m4a".equalsIgnoreCase(outputFormat); }
    @Override public String encode(String sourceUrl, String outputFormat) {
        log.info("[STUB ENCODE] sourceUrl={} -> outputUrl={} (stub, no real transcoding)", sourceUrl, sourceUrl);
        return sourceUrl;
    }
}