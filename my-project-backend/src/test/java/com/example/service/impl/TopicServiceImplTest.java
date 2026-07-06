package com.example.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.example.entity.dto.Account;
import com.example.entity.dto.Topic;
import com.example.entity.dto.TopicComment;
import com.example.entity.vo.request.AddCommentVO;
import com.example.entity.vo.request.TopicUpdateVO;
import com.example.mapper.AccountMapper;
import com.example.mapper.TopicCommentMapper;
import com.example.mapper.TopicMapper;
import com.example.utils.CacheUtils;
import com.example.utils.Const;
import com.example.utils.FlowUtils;
import com.example.utils.ProhibitedUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TopicServiceImplTest {

    private TopicServiceImpl service;
    private TopicMapper topicMapper;
    private AccountMapper accountMapper;
    private TopicCommentMapper commentMapper;
    private CacheUtils cacheUtils;
    private FlowUtils flowUtils;
    private ProhibitedUtils prohibitedUtils;

    @BeforeEach
    void setUp() {
        service = new TopicServiceImpl();
        topicMapper = mock(TopicMapper.class);
        accountMapper = mock(AccountMapper.class);
        commentMapper = mock(TopicCommentMapper.class);
        cacheUtils = mock(CacheUtils.class);
        flowUtils = mock(FlowUtils.class);
        prohibitedUtils = mock(ProhibitedUtils.class);
        ReflectionTestUtils.setField(service, "baseMapper", topicMapper);
        ReflectionTestUtils.setField(service, "accountMapper", accountMapper);
        ReflectionTestUtils.setField(service, "commentMapper", commentMapper);
        ReflectionTestUtils.setField(service, "cacheUtils", cacheUtils);
        ReflectionTestUtils.setField(service, "flowUtils", flowUtils);
        ReflectionTestUtils.setField(service, "prohibitedUtils", prohibitedUtils);
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

    @Test
    void updateTopicRejectsMutedUserBeforeWriting() {
        ReflectionTestUtils.setField(service, "types", Set.of(1));
        Account account = new Account();
        account.setMute(true);
        when(accountMapper.selectById(3)).thenReturn(account);
        TopicUpdateVO vo = new TopicUpdateVO();
        vo.setId(9);
        vo.setType(1);
        vo.setTitle("title");
        vo.setContent(JSONObject.parseObject("{\"ops\":[{\"insert\":\"hello\"}]}"));

        String result = service.updateTopic(3, vo);

        assertEquals("您已被禁言，无法修改主题", result);
        verify(topicMapper, never()).update(isNull(), any(Wrapper.class));
    }

    @Test
    void updateTopicClearsPreviewCacheWhenChanged() {
        ReflectionTestUtils.setField(service, "types", Set.of(1));
        Account account = new Account();
        account.setMute(false);
        when(accountMapper.selectById(3)).thenReturn(account);
        when(topicMapper.update(isNull(), any(Wrapper.class))).thenReturn(1);
        TopicUpdateVO vo = new TopicUpdateVO();
        vo.setId(9);
        vo.setType(1);
        vo.setTitle("title");
        vo.setContent(JSONObject.parseObject("{\"ops\":[{\"insert\":\"hello\"}]}"));

        assertNull(service.updateTopic(3, vo));

        verify(cacheUtils).deleteCachePattern(Const.FORUM_TOPIC_PREVIEW_CACHE + "*");
    }

    @Test
    void createCommentRejectsLockedTopicBeforeWriting() {
        Topic topic = new Topic();
        topic.setId(9);
        topic.setUid(3);
        topic.setLocked(1);
        when(topicMapper.selectById(9)).thenReturn(topic);
        when(flowUtils.limitPeriodCounterCheck(anyString(), anyInt(), anyInt())).thenReturn(true);
        Account account = new Account();
        account.setId(3);
        when(accountMapper.selectById(3)).thenReturn(account);
        AddCommentVO vo = new AddCommentVO();
        vo.setTid(9);
        vo.setQuote(0);
        vo.setContent("{\"ops\":[{\"insert\":\"hello\"}]}");

        String result = service.createComment(3, vo);

        assertEquals("主题已锁定，无法回复", result);
        verify(commentMapper, never()).insert(any(TopicComment.class));
    }
}
