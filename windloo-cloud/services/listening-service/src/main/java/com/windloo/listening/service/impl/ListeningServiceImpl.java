package com.windloo.listening.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.windloo.common.exception.BizException;
import com.windloo.common.mybatis.BaseEntity;
import com.windloo.listening.entity.*;
import com.windloo.listening.event.EpisodeRemovedEvent;
import com.windloo.listening.event.EpisodeUpsertedEvent;
import com.windloo.listening.mapper.*;
import com.windloo.listening.service.ListeningService;
import com.windloo.listening.subtitle.Sentence;
import com.windloo.listening.subtitle.SubtitleParser;
import com.windloo.listening.subtitle.SubtitleParserFactory;
import com.windloo.model.dto.ListeningStats;
import com.windloo.model.dto.CategoryStat;
import java.util.ArrayList;
import com.windloo.common.security.SecurityUtil;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ListeningServiceImpl implements ListeningService {
    private final CategoryMapper categoryMapper;
    private final AlbumMapper albumMapper;
    private final EpisodeMapper episodeMapper;
    private final SubtitleParserFactory parserFactory;
    private final ApplicationEventPublisher eventPublisher;

    public ListeningServiceImpl(CategoryMapper cm, AlbumMapper am, EpisodeMapper em,
                                SubtitleParserFactory pf, ApplicationEventPublisher ep) {
        this.categoryMapper = cm; this.albumMapper = am; this.episodeMapper = em;
        this.parserFactory = pf; this.eventPublisher = ep;
    }

    @Override @CacheEvict(value = "categories", allEntries = true)
    public Category createCategory(String cn, String en, String coverUrl) {
        Category c = new Category(); c.setSequenceNumber(maxSeqCat() + 1);
        c.setNameChinese(cn); c.setNameEnglish(en); c.setCoverUrl(coverUrl);
        c.setCreatedBy(SecurityUtil.currentUserId());
        categoryMapper.insert(c); return c;
    }
    @Override @CacheEvict(value = {"categories", "category"}, allEntries = true)
    public Category updateCategory(Long id, String cn, String en, String coverUrl) {
        Category c = requireCategory(id); checkOwnership(c.getCreatedBy());
        c.setNameChinese(cn); c.setNameEnglish(en); c.setCoverUrl(coverUrl);
        categoryMapper.updateById(c); return c;
    }
    @Override @CacheEvict(value = {"categories", "category"}, allEntries = true)
    public void deleteCategory(Long id) { softDelete(categoryMapper, id); }
    @Override @CacheEvict(value = "categories", allEntries = true)
    public void sortCategories(List<Long> sortedIds) { resequence(categoryMapper, sortedIds); }
    @Override @Cacheable("categories")
    public List<Category> getCategories() {
        return categoryMapper.selectList(new LambdaQueryWrapper<Category>().orderByAsc(Category::getSequenceNumber));
    }
    @Override @Cacheable("category")
    public Category getCategory(Long id) { return categoryMapper.selectById(id); }

    @Override @CacheEvict(value = {"albums_by_cat", "album"}, allEntries = true)
    public Album createAlbum(String cn, String en, Long categoryId) {
        Album a = new Album(); a.setSequenceNumber(maxSeqAlbum(categoryId) + 1);
        a.setNameChinese(cn); a.setNameEnglish(en); a.setCategoryId(categoryId); a.setIsVisible(false);
        a.setCreatedBy(SecurityUtil.currentUserId());
        albumMapper.insert(a); return a;
    }
    @Override @CacheEvict(value = {"albums_by_cat", "album"}, allEntries = true)
    public Album updateAlbum(Long id, String cn, String en, Long categoryId) {
        Album a = requireAlbum(id); checkOwnership(a.getCreatedBy());
        a.setNameChinese(cn); a.setNameEnglish(en); a.setCategoryId(categoryId);
        albumMapper.updateById(a); return a;
    }
    @Override @CacheEvict(value = {"albums_by_cat", "album"}, allEntries = true)
    public void deleteAlbum(Long id) { softDelete(albumMapper, id); }
    @Override @CacheEvict(value = "albums_by_cat", allEntries = true)
    public void sortAlbums(List<Long> sortedIds) { resequence(albumMapper, sortedIds); }
    @Override @CacheEvict(value = {"albums_by_cat", "album"}, allEntries = true)
    public void showAlbum(Long id) { setVisible(id, true); }
    @Override @CacheEvict(value = {"albums_by_cat", "album"}, allEntries = true)
    public void hideAlbum(Long id) { setVisible(id, false); }
    @Override @Cacheable("albums_by_cat")
    public List<Album> getAlbumsByCategory(Long categoryId) {
        return albumMapper.selectList(new LambdaQueryWrapper<Album>()
                .eq(Album::getCategoryId, categoryId).eq(Album::getIsVisible, true)
                .orderByAsc(Album::getSequenceNumber));
    }
    @Override
    public List<Album> getAlbumsByCategoryAll(Long categoryId) {
        return albumMapper.selectList(new LambdaQueryWrapper<Album>()
                .eq(Album::getCategoryId, categoryId).orderByAsc(Album::getSequenceNumber));
    }
    @Override @Cacheable("album")
    public Album getAlbum(Long id) { return albumMapper.selectById(id); }

    @Override @CacheEvict(value = {"episodes_by_album", "episode"}, allEntries = true)
    public Episode createEpisode(String cn, String en, Long albumId, String audioUrl, Double dur, String subtitle, String subtitleType) {
        if (parserFactory.get(subtitleType) == null) throw new BizException(400, "不支持的字幕格式: " + subtitleType);
        Episode e = new Episode(); e.setSequenceNumber(maxSeqEpisode(albumId) + 1);
        e.setNameChinese(cn); e.setNameEnglish(en); e.setAlbumId(albumId); e.setAudioUrl(audioUrl);
        e.setDurationInSecond(dur); e.setSubtitle(subtitle); e.setSubtitleType(subtitleType); e.setIsVisible(true);
        e.setCreatedBy(SecurityUtil.currentUserId());
        episodeMapper.insert(e);
        eventPublisher.publishEvent(new EpisodeUpsertedEvent(e.getId(), cn, en, albumId, parseSubtitle(subtitle, subtitleType)));
        return e;
    }
    @Override @CacheEvict(value = {"episodes_by_album", "episode"}, allEntries = true)
    public Episode updateEpisode(Long id, String cn, String en, String audioUrl, Double dur, String subtitle, String subtitleType) {
        if (parserFactory.get(subtitleType) == null) throw new BizException(400, "不支持的字幕格式: " + subtitleType);
        Episode e = requireEpisode(id);
        checkOwnership(e.getCreatedBy());
        e.setNameChinese(cn); e.setNameEnglish(en); e.setAudioUrl(audioUrl);
        e.setDurationInSecond(dur); e.setSubtitle(subtitle); e.setSubtitleType(subtitleType);
        episodeMapper.updateById(e);
        eventPublisher.publishEvent(new EpisodeUpsertedEvent(id, cn, en, e.getAlbumId(), parseSubtitle(subtitle, subtitleType)));
        return e;
    }
    @Override @CacheEvict(value = {"episodes_by_album", "episode"}, allEntries = true)
    public void deleteEpisode(Long id) {
        requireEpisode(id);
        episodeMapper.update(null, new UpdateWrapper<Episode>().eq("id", id)
                .set("is_deleted", 1).set("deletion_time", LocalDateTime.now()));
        eventPublisher.publishEvent(new EpisodeRemovedEvent(id));
    }
    @Override @CacheEvict(value = "episodes_by_album", allEntries = true)
    public void sortEpisodes(List<Long> sortedIds) { resequence(episodeMapper, sortedIds); }
    @Override @CacheEvict(value = {"episodes_by_album", "episode"}, allEntries = true)
    public void showEpisode(Long id) {
        Episode e = requireEpisode(id); e.setIsVisible(true); episodeMapper.updateById(e);
        eventPublisher.publishEvent(new EpisodeUpsertedEvent(id, e.getNameChinese(), e.getNameEnglish(), e.getAlbumId(), parseSubtitle(e.getSubtitle(), e.getSubtitleType())));
    }
    @Override @CacheEvict(value = {"episodes_by_album", "episode"}, allEntries = true)
    public void hideEpisode(Long id) {
        Episode e = requireEpisode(id); e.setIsVisible(false); episodeMapper.updateById(e);
        eventPublisher.publishEvent(new EpisodeRemovedEvent(id));
    }
    @Override @Cacheable("episodes_by_album")
    public List<Episode> getEpisodesByAlbum(Long albumId) {
        return episodeMapper.selectList(new LambdaQueryWrapper<Episode>()
                .eq(Episode::getAlbumId, albumId).eq(Episode::getIsVisible, true)
                .orderByAsc(Episode::getSequenceNumber));
    }
    @Override
    public List<Episode> getEpisodesByAlbumAll(Long albumId) {
        return episodeMapper.selectList(new LambdaQueryWrapper<Episode>()
                .eq(Episode::getAlbumId, albumId).orderByAsc(Episode::getSequenceNumber));
    }
    @Override @Cacheable("episode")
    public Episode getEpisode(Long id) {
        Episode e = episodeMapper.selectById(id);
        if (e != null) e.setSentences(parseSubtitle(e.getSubtitle(), e.getSubtitleType()));
        return e;
    }
    @Override
    public List<Sentence> parseSubtitle(Long episodeId) {
        Episode e = requireEpisode(episodeId);
        return parseSubtitle(e.getSubtitle(), e.getSubtitleType());
    }

        @Override @CacheEvict(value = {"episodes_by_album", "episode"}, allEntries = true)
    public void updateAudioUrl(Long id, String audioUrl) {
        episodeMapper.update(null, new UpdateWrapper<Episode>().eq("id", id).set("audio_url", audioUrl));
    }
    @Override
    public int reindexAll() {
        List<Episode> episodes = episodeMapper.selectList(null);
        for (Episode e : episodes) {
            List<Sentence> sentences = parseSubtitle(e.getSubtitle(), e.getSubtitleType());
            eventPublisher.publishEvent(new EpisodeUpsertedEvent(e.getId(), e.getNameChinese(), e.getNameEnglish(), e.getAlbumId(), sentences));
        }
        return episodes.size();
    }

private List<Sentence> parseSubtitle(String subtitle, String type) {
        SubtitleParser p = parserFactory.get(type);
        return p == null ? List.of() : p.parse(subtitle);
    }
    private int maxSeqCat() { return getCategories().stream().mapToInt(Category::getSequenceNumber).max().orElse(0); }
    private int maxSeqAlbum(Long catId) {
        return albumMapper.selectList(new LambdaQueryWrapper<Album>().eq(Album::getCategoryId, catId))
                .stream().mapToInt(Album::getSequenceNumber).max().orElse(0);
    }
    private int maxSeqEpisode(Long albumId) {
        return episodeMapper.selectList(new LambdaQueryWrapper<Episode>().eq(Episode::getAlbumId, albumId))
                .stream().mapToInt(Episode::getSequenceNumber).max().orElse(0);
    }
    private <T extends BaseEntity> void resequence(com.baomidou.mybatisplus.core.mapper.BaseMapper<T> mapper, List<Long> ids) {
        for (int i = 0; i < ids.size(); i++) {
            mapper.update(null, new UpdateWrapper<T>().eq("id", ids.get(i)).set("sequence_number", i + 1));
        }
    }
    private <T extends BaseEntity> void softDelete(com.baomidou.mybatisplus.core.mapper.BaseMapper<T> mapper, Long id) {
        mapper.update(null, new UpdateWrapper<T>().eq("id", id).set("is_deleted", 1).set("deletion_time", LocalDateTime.now()));
    }
    private void setVisible(Long id, boolean visible) {
        albumMapper.update(null, new UpdateWrapper<Album>().eq("id", id).set("is_visible", visible ? 1 : 0));
    }
    private void checkOwnership(Long createdBy) {
        Long me = SecurityUtil.currentUserId();
        if (!SecurityUtil.isAdmin() && (createdBy == null || !createdBy.equals(me)))
            throw new BizException(40300, "无权修改他人创建的内容");
    }
    @Override
    public ListeningStats getStats() {
        List<Category> cats = categoryMapper.selectList(null);
        List<CategoryStat> list = new ArrayList<>();
        long totalAlbums = 0, totalEpisodes = 0;
        for (Category c : cats) {
            List<Album> albums = albumMapper.selectList(new LambdaQueryWrapper<Album>().eq(Album::getCategoryId, c.getId()));
            long ac = albums.size(), ec = 0;
            if (!albums.isEmpty()) {
                List<Long> ids = albums.stream().map(Album::getId).toList();
                ec = episodeMapper.selectCount(new LambdaQueryWrapper<Episode>().in(Episode::getAlbumId, ids));
            }
            list.add(new CategoryStat(c.getNameChinese(), ac, ec));
            totalAlbums += ac; totalEpisodes += ec;
        }
        return new ListeningStats(cats.size(), totalAlbums, totalEpisodes, list);
    }
    private Category requireCategory(Long id) { Category c = categoryMapper.selectById(id); if (c == null) throw new BizException(40003, "分类不存在"); return c; }
    private Album requireAlbum(Long id) { Album a = albumMapper.selectById(id); if (a == null) throw new BizException(40003, "专辑不存在"); return a; }
    private Episode requireEpisode(Long id) { Episode e = episodeMapper.selectById(id); if (e == null) throw new BizException(40003, "单集不存在"); return e; }
}
