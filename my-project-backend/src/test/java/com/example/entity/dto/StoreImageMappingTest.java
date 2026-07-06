package com.example.entity.dto;

import com.baomidou.mybatisplus.annotation.TableId;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class StoreImageMappingTest {

    @Test
    void imageNameIsDeclaredAsMyBatisPrimaryKey() throws NoSuchFieldException {
        Field nameField = StoreImage.class.getDeclaredField("name");

        assertNotNull(nameField.getAnnotation(TableId.class));
    }
}
