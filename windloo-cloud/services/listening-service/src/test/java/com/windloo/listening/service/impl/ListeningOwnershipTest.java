package com.windloo.listening.service.impl;
import com.windloo.common.exception.BizException;
import com.windloo.listening.entity.Category;
import com.windloo.listening.mapper.AlbumMapper;
import com.windloo.listening.mapper.CategoryMapper;
import com.windloo.listening.mapper.EpisodeMapper;
import com.windloo.listening.subtitle.SubtitleParserFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListeningOwnershipTest {
    @Mock CategoryMapper categoryMapper;
    @Mock AlbumMapper albumMapper;
    @Mock EpisodeMapper episodeMapper;
    @Mock SubtitleParserFactory parserFactory;
    @Mock ApplicationEventPublisher eventPublisher;
    ListeningServiceImpl svc;

    @BeforeEach void setup() {
        svc = new ListeningServiceImpl(categoryMapper, albumMapper, episodeMapper, parserFactory, eventPublisher);
    }
    @AfterEach void clear() { SecurityContextHolder.clearContext(); }

    private void setUser(long id, String... roles) {
        List<SimpleGrantedAuthority> a = Arrays.stream(roles).map(SimpleGrantedAuthority::new).toList();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(String.valueOf(id), null, a));
    }

    @Test void userCannotUpdateOthersCategory() {
        Category c = new Category(); c.setId(1L); c.setCreatedBy(999L);
        when(categoryMapper.selectById(1L)).thenReturn(c);
        setUser(1L, "ROLE_USER");
        BizException ex = assertThrows(BizException.class, () -> svc.updateCategory(1L, "x", "y", null));
        assertEquals(40300, ex.getCode());
    }
    @Test void userCanUpdateOwnCategory() {
        Category c = new Category(); c.setId(1L); c.setCreatedBy(1L);
        when(categoryMapper.selectById(1L)).thenReturn(c);
        setUser(1L, "ROLE_USER");
        assertDoesNotThrow(() -> svc.updateCategory(1L, "x", "y", null));
    }
    @Test void adminCanUpdateAnyCategory() {
        Category c = new Category(); c.setId(1L); c.setCreatedBy(999L);
        when(categoryMapper.selectById(1L)).thenReturn(c);
        setUser(1L, "ROLE_ADMIN");
        assertDoesNotThrow(() -> svc.updateCategory(1L, "x", "y", null));
    }
}