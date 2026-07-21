package com.windloo.ai.context;
import java.util.List;

public interface ContextProvider {
    List<ContextChunk> retrieve(String query, Long userId, Long episodeId);

    record ContextChunk(String source, String text) {}
}