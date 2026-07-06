package com.example.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.example.utils.Const;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityConfigurationTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    StringRedisTemplate redisTemplate;

    @BeforeEach
    void setUp() {
        ValueOperations<String, String> valueOperations = mockValueOperations();
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisTemplate.hasKey(startsWith(Const.JWT_BLACK_LIST))).thenReturn(false);
    }

    @Test
    @WithMockUser(roles = Const.ROLE_DEFAULT)
    void defaultUserCannotAccessAdminApi() throws Exception {
        mockMvc.perform(get("/api/admin/not-existing"))
                .andExpect(status().isForbidden());
    }

    @Test
    void bannedJwtIsRejectedBeforeRouting() throws Exception {
        when(redisTemplate.hasKey(Const.BANNED_BLOCK + "7")).thenReturn(true);

        mockMvc.perform(get("/api/topic/not-existing")
                        .header("Authorization", "Bearer " + token(7, Const.ROLE_DEFAULT)))
                .andExpect(status().isForbidden());
    }

    private String token(int userId, String role) {
        return JWT.create()
                .withJWTId(UUID.randomUUID().toString())
                .withClaim("id", userId)
                .withClaim("name", "tester")
                .withClaim("authorities", List.of("ROLE_" + role))
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + 3600_000))
                .sign(Algorithm.HMAC256("abcdefghijklmn"));
    }

    @SuppressWarnings("unchecked")
    private ValueOperations<String, String> mockValueOperations() {
        return org.mockito.Mockito.mock(ValueOperations.class);
    }
}
