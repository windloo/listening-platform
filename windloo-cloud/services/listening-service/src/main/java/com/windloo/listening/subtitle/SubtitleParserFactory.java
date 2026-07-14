package com.windloo.listening.subtitle;
import org.springframework.stereotype.Component;
import java.util.List;
@Component
public class SubtitleParserFactory {
    private final List<SubtitleParser> parsers;
    public SubtitleParserFactory(List<SubtitleParser> parsers) { this.parsers = parsers; }
    public SubtitleParser get(String type) {
        return parsers.stream().filter(p -> p.accept(type)).findFirst().orElse(null);
    }
}