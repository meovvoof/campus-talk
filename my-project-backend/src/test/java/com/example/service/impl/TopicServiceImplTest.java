package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.example.entity.dto.Topic;
import com.example.mapper.TopicMapper;
import com.example.utils.CacheUtils;
import com.example.utils.Const;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TopicServiceImplTest {

    private TopicServiceImpl service;
    private TopicMapper topicMapper;
    private CacheUtils cacheUtils;

    @BeforeEach
    void setUp() {
        service = new TopicServiceImpl();
        topicMapper = mock(TopicMapper.class);
        cacheUtils = mock(CacheUtils.class);
        ReflectionTestUtils.setField(service, "baseMapper", topicMapper);
        ReflectionTestUtils.setField(service, "cacheUtils", cacheUtils);
    }

    @Test
    void returnsNullWhenTopicDoesNotExist() {
        when(topicMapper.selectById(99)).thenReturn(null);

        assertNull(service.getTopic(99, 1));
    }

    @Test
    void clearsPreviewCacheWhenTopicTopChanges() {
        when(topicMapper.update(isNull(), any(Wrapper.class))).thenReturn(1);

        service.setTopicTop(7, true);

        verify(cacheUtils).deleteCachePattern(Const.FORUM_TOPIC_PREVIEW_CACHE + "*");
    }

    @Test
    void clearsPreviewCacheWhenTopicLockedChanges() {
        when(topicMapper.update(isNull(), any(Wrapper.class))).thenReturn(1);

        service.setTopicLocked(7, true);

        verify(cacheUtils).deleteCachePattern(Const.FORUM_TOPIC_PREVIEW_CACHE + "*");
    }

    @Test
    void clearsPreviewCacheWhenOneTopicTypeChanges() {
        when(topicMapper.update(isNull(), any(Wrapper.class))).thenReturn(1);

        service.changeTopicType(7, 2);

        verify(cacheUtils).deleteCachePattern(Const.FORUM_TOPIC_PREVIEW_CACHE + "*");
    }
}
