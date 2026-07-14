package com.windloo.listening.service;
import com.windloo.listening.entity.*;
import com.windloo.listening.subtitle.Sentence;
import com.windloo.model.dto.ListeningStats;
import java.util.List;

public interface ListeningService {
    Category createCategory(String nameChinese, String nameEnglish, String coverUrl);
    Category updateCategory(Long id, String nameChinese, String nameEnglish, String coverUrl);
    void deleteCategory(Long id);
    void sortCategories(List<Long> sortedIds);
    List<Category> getCategories();
    Category getCategory(Long id);
    Album createAlbum(String nameChinese, String nameEnglish, Long categoryId);
    Album updateAlbum(Long id, String nameChinese, String nameEnglish, Long categoryId);
    void deleteAlbum(Long id);
    void sortAlbums(List<Long> sortedIds);
    void showAlbum(Long id);
    void hideAlbum(Long id);
    List<Album> getAlbumsByCategory(Long categoryId);
    List<Album> getAlbumsByCategoryAll(Long categoryId);
    Album getAlbum(Long id);
    Episode createEpisode(String nameChinese, String nameEnglish, Long albumId, String audioUrl, Double durationInSecond, String subtitle, String subtitleType);
    Episode updateEpisode(Long id, String nameChinese, String nameEnglish, String audioUrl, Double durationInSecond, String subtitle, String subtitleType);
    void deleteEpisode(Long id);
    void sortEpisodes(List<Long> sortedIds);
    void showEpisode(Long id);
    void hideEpisode(Long id);
    List<Episode> getEpisodesByAlbum(Long albumId);
    List<Episode> getEpisodesByAlbumAll(Long albumId);
    Episode getEpisode(Long id);
    List<Sentence> parseSubtitle(Long episodeId);
    ListeningStats getStats();
    void updateAudioUrl(Long id, String audioUrl);
    /** 重建搜索索引：重新发布所有单集的索引事件，返回重建的单集数量 */
    int reindexAll();
}