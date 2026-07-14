package com.windloo.listening.subtitle;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class SubtitleParserTest {
    @Test void srt_parse_two_blocks() {
        SrtParser p = new SrtParser();
        String srt = "1\n00:00:01,000 --> 00:00:03,000\nHello world\n\n2\n00:00:04,000 --> 00:00:06,500\nGoodbye\n";
        List<Sentence> r = p.parse(srt);
        assertEquals(2, r.size());
        assertEquals(1000, r.get(0).startMs());
        assertEquals(3000, r.get(0).endMs());
        assertEquals("Hello world", r.get(0).text());
        assertEquals(6500, r.get(1).endMs());
    }
    @Test void lrc_parse_lines() {
        LrcParser p = new LrcParser();
        String lrc = "[00:01.00]Hello\n[00:03.50]world\n";
        List<Sentence> r = p.parse(lrc);
        assertEquals(2, r.size());
        assertEquals(1000, r.get(0).startMs());
        assertEquals(3500, r.get(1).startMs());
        assertEquals(3500, r.get(0).endMs());
        assertEquals("Hello", r.get(0).text());
    }
    @Test void json_parse_array() {
        JsonParser p = new JsonParser();
        String json = "[{\"startMs\":100,\"endMs\":200,\"text\":\"hi\"}]";
        List<Sentence> r = p.parse(json);
        assertEquals(1, r.size());
        assertEquals(100, r.get(0).startMs());
        assertEquals("hi", r.get(0).text());
    }
    @Test void factory_dispatches_by_type() {
        SubtitleParserFactory f = new SubtitleParserFactory(List.of(new SrtParser(), new LrcParser(), new JsonParser()));
        assertNotNull(f.get("srt"));
        assertNotNull(f.get("LRC"));
        assertNotNull(f.get("json"));
        assertNull(f.get("unknown"));
    }
}