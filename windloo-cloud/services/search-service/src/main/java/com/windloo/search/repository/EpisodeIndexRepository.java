package com.windloo.search.repository;
import com.windloo.search.entity.EpisodeIndex;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface EpisodeIndexRepository extends ElasticsearchRepository<EpisodeIndex, Long> {}