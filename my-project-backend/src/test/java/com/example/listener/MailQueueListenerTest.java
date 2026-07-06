package com.example.listener;

import com.example.entity.dto.EmailRecord;
import com.example.mapper.EmailRecordMapper;
import org.junit.jupiter.api.Test;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class MailQueueListenerTest {

    @Test
    void sendsEmailRecordAndMarksSuccess() throws Exception {
        MailQueueListener listener = new MailQueueListener();
        JavaMailSender sender = mock(JavaMailSender.class);
        EmailRecordMapper recordMapper = mock(EmailRecordMapper.class);
        ReflectionTestUtils.setField(listener, "sender", sender);
        ReflectionTestUtils.setField(listener, "username", "noreply@campus.test");
        ReflectionTestUtils.setField(listener, "recordMapper", recordMapper);
        EmailRecord record = new EmailRecord("student@campus.test", "Verify", "Code: 123456").setId(9);

        invoke(listener, record);

        var messageCaptor = org.mockito.ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(sender).send(messageCaptor.capture());
        SimpleMailMessage message = messageCaptor.getValue();
        assertEquals("Verify", message.getSubject());
        assertEquals("Code: 123456", message.getText());
        assertEquals("student@campus.test", first(message.getTo()));
        assertEquals("noreply@campus.test", message.getFrom());
        assertEquals(1, record.getStatus());
        verify(recordMapper).updateById(record);
    }

    private void invoke(MailQueueListener listener, EmailRecord record) throws Exception {
        Method method = MailQueueListener.class.getDeclaredMethod("sendMailMessage", EmailRecord.class);
        method.invoke(listener, record);
    }

    private String first(String[] values) {
        return values == null || values.length == 0 ? null : values[0];
    }
}
