package com.windloo.listening.subtitle;
import java.util.List;
public interface SubtitleParser {
    boolean accept(String type);
    List<Sentence> parse(String content);
}