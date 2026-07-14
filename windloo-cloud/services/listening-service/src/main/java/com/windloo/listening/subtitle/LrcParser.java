package com.windloo.listening.subtitle;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class LrcParser implements SubtitleParser {
    private static final Pattern LINE = Pattern.compile("\\[(\\d{2}):(\\d{2})\\.(\\d{2,3})](.*)");
    @Override public boolean accept(String type) { return "lrc".equalsIgnoreCase(type); }
    @Override public List<Sentence> parse(String content) {
        List<Sentence> list = new ArrayList<>();
        List<long[]> starts = new ArrayList<>(); List<String> texts = new ArrayList<>();
        for (String line : content.split("\n")) {
            Matcher m = LINE.matcher(line.trim());
            if (!m.find()) continue;
            long ms = Long.parseLong(m.group(1)) * 60000 + Long.parseLong(m.group(2)) * 1000;
            String frac = m.group(3);
            ms += frac.length() == 2 ? Long.parseLong(frac) * 10 : Long.parseLong(frac);
            starts.add(new long[]{ms}); texts.add(m.group(4));
        }
        for (int i = 0; i < starts.size(); i++) {
            long start = starts.get(i)[0];
            long end = i + 1 < starts.size() ? starts.get(i + 1)[0] : start + 3000;
            list.add(new Sentence(start, end, texts.get(i)));
        }
        return list;
    }
}