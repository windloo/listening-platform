package com.windloo.listening.subtitle;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class SrtParser implements SubtitleParser {
    private static final Pattern TIME = Pattern.compile(
        "(\\d{2}):(\\d{2}):(\\d{2}),(\\d{3})\\s*-->\\s*(\\d{2}):(\\d{2}):(\\d{2}),(\\d{3})");
    @Override public boolean accept(String type) { return "srt".equalsIgnoreCase(type); }
    @Override public List<Sentence> parse(String content) {
        List<Sentence> list = new ArrayList<>();
        String[] blocks = content.trim().split("\n\\s*\n");
        for (String block : blocks) {
            String[] lines = block.trim().split("\n");
            int timeIdx = -1; Matcher m = null;
            for (int i = 0; i < lines.length; i++) {
                Matcher mm = TIME.matcher(lines[i]);
                if (mm.find()) { m = mm; timeIdx = i; break; }
            }
            if (timeIdx < 0) continue;
            long start = toMs(m.group(1), m.group(2), m.group(3), m.group(4));
            long end = toMs(m.group(5), m.group(6), m.group(7), m.group(8));
            String text = String.join("\n", Arrays.copyOfRange(lines, timeIdx + 1, lines.length));
            list.add(new Sentence(start, end, text));
        }
        return list;
    }
    private long toMs(String h, String mi, String s, String ms) {
        return Long.parseLong(h) * 3600000 + Long.parseLong(mi) * 60000 + Long.parseLong(s) * 1000 + Long.parseLong(ms);
    }
}