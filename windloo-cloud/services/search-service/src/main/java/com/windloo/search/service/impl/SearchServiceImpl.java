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

import java.util.List;
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
