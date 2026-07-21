package com.windloo.search.service.impl;

import com.windloo.common.api.PageResult;
import com.windloo.model.dto.EpisodeIndexDTO;
import com.windloo.model.dto.SentenceDTO;
import com.windloo.search.entity.EpisodeIndex;
import com.windloo.search.repository.EpisodeIndexRepository;
import com.windloo.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class SearchServiceImpl implements SearchService {
    @Autowired EpisodeIndexRepository repository;
    @Autowired ElasticsearchOperations operations;

    @Override
    public void upsert(EpisodeIndexDTO dto) {
        EpisodeIndex idx = new EpisodeIndex();
        idx.setId(dto.getId());
        idx.setNameChinese(dto.getNameChinese());
        idx.setNameEnglish(dto.getNameEnglish());
        idx.setAlbumId(dto.getAlbumId());
        idx.setSentences(toDocs(dto.getSentences()));
        repository.save(idx);
    }

    @Override
    public void remove(Long id) {
        repository.deleteById(id);
    }

    @Override
    public PageResult<EpisodeIndexDTO> search(String keyword, int page, int size) {
        String kw = keyword == null ? "" : keyword.trim();
        String kwLower = kw.toLowerCase();

        // 布尔查询：命中单集名称 或 命中任一句子文本均可
        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.bool(b -> b
                        .should(s -> s.match(m -> m.field("nameChinese").query(keyword)))
                        .should(s -> s.match(m -> m.field("nameEnglish").query(keyword)))
                        .should(s -> s.nested(n -> n
                                .path("sentences")
                                .query(nq -> nq.match(m -> m.field("sentences.text").query(keyword)))
                        ))
                ))
                .withPageable(org.springframework.data.domain.PageRequest.of(page, size))
                .build();

        SearchHits<EpisodeIndex> hits = operations.search(query, EpisodeIndex.class);

        List<EpisodeIndexDTO> dtos = hits.getSearchHits().stream().map(h -> {
            EpisodeIndex idx = h.getContent();
            EpisodeIndexDTO d = new EpisodeIndexDTO();
            d.setId(idx.getId());
            d.setNameChinese(idx.getNameChinese());
            d.setNameEnglish(idx.getNameEnglish());
            d.setAlbumId(idx.getAlbumId());
            // 仅返回真正包含关键字的句子（带时间戳），供前端精确定位跳播
            d.setSentences(matchedSentences(idx.getSentences(), kwLower));
            return d;
        }).collect(Collectors.toList());

        return new PageResult<>(dtos, hits.getTotalHits(), page, size);
    }

    @Override
    public List<SentenceDTO> searchInEpisode(Long episodeId, String keyword, int size) {
        String q = keyword == null ? "" : keyword.trim();
        if (q.isEmpty()) return List.of();
        return repository.findById(episodeId).map(idx -> {
            List<EpisodeIndex.SentenceDoc> sentences = idx.getSentences();
            if (sentences == null || sentences.isEmpty()) return List.<SentenceDTO>of();
            List<Long> times = parseTimestampsMs(q);
            if (!times.isEmpty()) return sentencesAround(sentences, times.get(0), size);
            return matchedByKeywords(sentences, q, size);
        }).orElse(List.of());
    }

    /** 从问题里解析时间戳(毫秒),支持「X分Y秒」「X:Y[:Z]」「X分」「Y秒」。 */
    private List<Long> parseTimestampsMs(String q) {
        List<Long> times = new ArrayList<>();
        Matcher m = Pattern.compile("(\\d+)\\s*分\\s*(\\d+)\\s*秒").matcher(q);
        while (m.find()) times.add((Long.parseLong(m.group(1)) * 60 + Long.parseLong(m.group(2))) * 1000L);
        if (!times.isEmpty()) return times;
        m = Pattern.compile("(\\d+):(\\d+)(?::(\\d+))?").matcher(q);
        while (m.find()) {
            long a = Long.parseLong(m.group(1)), b = Long.parseLong(m.group(2));
            long c = m.group(3) != null ? Long.parseLong(m.group(3)) : -1;
            times.add(c >= 0 ? (a * 3600 + b * 60 + c) * 1000L : (a * 60 + b) * 1000L);
        }
        if (!times.isEmpty()) return times;
        m = Pattern.compile("(\\d+)\\s*分").matcher(q);
        while (m.find()) times.add(Long.parseLong(m.group(1)) * 60 * 1000L);
        m = Pattern.compile("(\\d+)\\s*秒").matcher(q);
        while (m.find()) times.add(Long.parseLong(m.group(1)) * 1000L);
        return times;
    }

    /** 返回以 ms 附近为中心、长度 size 的句子窗口。 */
    private List<SentenceDTO> sentencesAround(List<EpisodeIndex.SentenceDoc> sentences, long ms, int size) {
        int center = 0; long bestDist = Long.MAX_VALUE;
        for (int i = 0; i < sentences.size(); i++) {
            long mid = (sentences.get(i).getStartMs() + sentences.get(i).getEndMs()) / 2;
            long d = Math.abs(mid - ms);
            if (d < bestDist) { bestDist = d; center = i; }
        }
        int half = Math.max(1, size / 2);
        int from = Math.max(0, center - half);
        int to = Math.min(sentences.size(), from + size);
        from = Math.max(0, to - size);
        return sentences.subList(from, to).stream()
                .map(s -> new SentenceDTO(s.getStartMs(), s.getEndMs(), s.getText()))
                .toList();
    }

    /** 按关键词(任意命中)检索,按命中数排序,取 top-K。关键词按标点/空格切分,长度>=2。 */
    private List<SentenceDTO> matchedByKeywords(List<EpisodeIndex.SentenceDoc> sentences, String q, int size) {
        List<String> kws = Arrays.stream(q.toLowerCase().split("[，。？！,.?!:：;；\\s]+"))
                .filter(s -> s.length() >= 2).toList();
        if (kws.isEmpty()) return List.of();
        List<int[]> scores = new ArrayList<>();
        for (int i = 0; i < sentences.size(); i++) {
            String text = sentences.get(i).getText() == null ? "" : sentences.get(i).getText().toLowerCase();
            int c = 0;
            for (String kw : kws) if (text.contains(kw)) c++;
            if (c > 0) scores.add(new int[]{i, c});
        }
        return scores.stream()
                .sorted((a, b) -> Integer.compare(b[1], a[1]))
                .limit(size)
                .map(s -> { var d = sentences.get(s[0]); return new SentenceDTO(d.getStartMs(), d.getEndMs(), d.getText()); })
                .toList();
    }

    /** 把 DTO 中的带时间戳句子转换为 ES 嵌套文档。 */
    private List<EpisodeIndex.SentenceDoc> toDocs(List<SentenceDTO> sentences) {
        if (sentences == null || sentences.isEmpty()) return List.of();
        return sentences.stream().map(s -> {
            EpisodeIndex.SentenceDoc doc = new EpisodeIndex.SentenceDoc();
            doc.setStartMs(s.startMs());
            doc.setEndMs(s.endMs());
            doc.setText(s.text());
            return doc;
        }).toList();
    }

    /** 在命中的单集中，筛选出真正包含关键字的句子（带时间戳）。 */
    private List<SentenceDTO> matchedSentences(List<EpisodeIndex.SentenceDoc> sentences, String kwLower) {
        if (sentences == null || sentences.isEmpty() || kwLower.isEmpty()) return List.of();
        return sentences.stream()
                .filter(s -> s.getText() != null && s.getText().toLowerCase().contains(kwLower))
                .map(s -> new SentenceDTO(s.getStartMs(), s.getEndMs(), s.getText()))
                .toList();
    }
}
