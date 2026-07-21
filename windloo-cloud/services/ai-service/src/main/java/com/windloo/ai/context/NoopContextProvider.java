package com.windloo.ai.context;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class NoopContextProvider implements ContextProvider {
    @Override
    public List<ContextChunk> retrieve(String query, Long userId, Long episodeId) {
        return List.of();
    }
}