package com.windloo.ai.context;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NoopContextProviderTest {
    @Test void returns_empty_list() {
        assertTrue(new NoopContextProvider().retrieve("what is present perfect", 1L, null).isEmpty());
    }
}