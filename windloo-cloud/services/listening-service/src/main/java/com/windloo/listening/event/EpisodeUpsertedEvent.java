package com.windloo.listening.event;
import com.windloo.listening.subtitle.Sentence;
import java.util.List;
public record EpisodeUpsertedEvent(Long id, String nameChinese, String nameEnglish, Long albumId, List<Sentence> sentences) {}