package com.windloo.listening.subtitle;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.windloo.common.exception.BizException;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class JsonParser implements SubtitleParser {
    private final ObjectMapper mapper = new ObjectMapper();
    @Override public boolean accept(String type) { return "json".equalsIgnoreCase(type); }
    @Override public List<Sentence> parse(String content) {
        try { return mapper.readValue(content, new TypeReference<List<Sentence>>() {}); }
        catch (Exception e) { throw new BizException(400, "JSON字幕解析失败: " + e.getMessage()); }
    }
}