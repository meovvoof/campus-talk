package com.example.entity.vo.response;

import com.example.entity.dto.Account;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccountVOTest {

    @Test
    void accountViewObjectIncludesAdminManagementFields() {
        Account account = new Account();
        account.setId(7);
        account.setMute(true);
        account.setBanned(true);

        AccountVO vo = account.asViewObject(AccountVO.class);

        assertEquals(7, vo.getId());
        assertTrue(vo.isMute());
        assertTrue(vo.isBanned());
    }
}
