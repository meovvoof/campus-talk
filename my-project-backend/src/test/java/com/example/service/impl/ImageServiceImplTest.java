package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.example.entity.dto.Account;
import com.example.entity.dto.StoreImage;
import com.example.mapper.AccountMapper;
import com.example.mapper.ImageStoreMapper;
import com.example.utils.FlowUtils;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ImageServiceImplTest {

    private ImageServiceImpl service;
    private MinioClient minioClient;
    private ImageStoreMapper imageStoreMapper;
    private AccountMapper accountMapper;
    private FlowUtils flowUtils;

    @BeforeEach
    void setUp() {
        service = new ImageServiceImpl();
        minioClient = mock(MinioClient.class);
        imageStoreMapper = mock(ImageStoreMapper.class);
        accountMapper = mock(AccountMapper.class);
        flowUtils = mock(FlowUtils.class);
        ReflectionTestUtils.setField(service, "client", minioClient);
        ReflectionTestUtils.setField(service, "baseMapper", imageStoreMapper);
        ReflectionTestUtils.setField(service, "mapper", accountMapper);
        ReflectionTestUtils.setField(service, "flowUtils", flowUtils);
    }

    @Test
    void uploadImageRemovesObjectWhenDatabaseSaveFails() throws Exception {
        when(flowUtils.limitPeriodCounterCheck(anyString(), anyInt(), anyInt())).thenReturn(true);
        when(minioClient.putObject(any(PutObjectArgs.class))).thenReturn(mock(ObjectWriteResponse.class));
        when(imageStoreMapper.insert(any(StoreImage.class))).thenReturn(0);
        MockMultipartFile file = new MockMultipartFile("file", "image.jpg", "image/jpeg", "data".getBytes());

        String result = service.uploadImage(file, 7);

        assertNull(result);
        ArgumentCaptor<RemoveObjectArgs> captor = ArgumentCaptor.forClass(RemoveObjectArgs.class);
        verify(minioClient).removeObject(captor.capture());
        assertTrue(captor.getValue().object().startsWith("/cache/"));
    }

    @Test
    void uploadAvatarKeepsOldAvatarAndRemovesNewObjectWhenDatabaseUpdateFails() throws Exception {
        when(minioClient.putObject(any(PutObjectArgs.class))).thenReturn(mock(ObjectWriteResponse.class));
        Account account = new Account();
        account.setAvatar("/avatar/old");
        when(accountMapper.selectById(7)).thenReturn(account);
        when(accountMapper.update(isNull(), any(Wrapper.class))).thenReturn(0);
        MockMultipartFile file = new MockMultipartFile("file", "avatar.jpg", "image/jpeg", "data".getBytes());

        String result = service.uploadAvatar(file, 7);

        assertNull(result);
        ArgumentCaptor<RemoveObjectArgs> captor = ArgumentCaptor.forClass(RemoveObjectArgs.class);
        verify(minioClient).removeObject(captor.capture());
        assertTrue(captor.getValue().object().startsWith("/avatar/"));
        assertNotEquals("/avatar/old", captor.getValue().object());
    }

    @Test
    void uploadAvatarReturnsNewAvatarWhenOldAvatarCleanupFailsAfterDatabaseUpdate() throws Exception {
        when(minioClient.putObject(any(PutObjectArgs.class))).thenReturn(mock(ObjectWriteResponse.class));
        Account account = new Account();
        account.setAvatar("/avatar/old");
        when(accountMapper.selectById(7)).thenReturn(account);
        when(accountMapper.update(isNull(), any(Wrapper.class))).thenReturn(1);
        doThrow(new RuntimeException("remove failed")).when(minioClient).removeObject(any(RemoveObjectArgs.class));
        MockMultipartFile file = new MockMultipartFile("file", "avatar.jpg", "image/jpeg", "data".getBytes());

        String result = service.uploadAvatar(file, 7);

        assertNotNull(result);
        assertTrue(result.startsWith("/avatar/"));
    }
}
