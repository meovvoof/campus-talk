package com.example.filter;

import com.example.utils.Const;
import com.example.utils.FlowUtils;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class FlowLimitingFilterTest {

    private FlowLimitingFilter filter;
    private StringRedisTemplate template;
    private FlowUtils flowUtils;
    private FilterChain chain;

    @BeforeEach
    void setUp() {
        filter = new FlowLimitingFilter();
        template = mock(StringRedisTemplate.class);
        flowUtils = mock(FlowUtils.class);
        chain = mock(FilterChain.class);
        filter.template = template;
        filter.utils = flowUtils;
        filter.block = 30;
        filter.limit = 50;
        filter.period = 3;
    }

    @Test
    void blocksNormalRequestWhenIpIsAlreadyBlocked() throws Exception {
        MockHttpServletRequest request = request("GET");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(template.hasKey(Const.FLOW_LIMIT_BLOCK + "127.0.0.1")).thenReturn(true);

        filter.doFilter(request, response, chain);

        assertEquals(429, response.getStatus());
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void ignoresCorsPreflightRequest() throws Exception {
        MockHttpServletRequest request = request("OPTIONS");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(template.hasKey(Const.FLOW_LIMIT_BLOCK + "127.0.0.1")).thenReturn(true);

        filter.doFilter(request, response, chain);

        assertEquals(200, response.getStatus());
        verifyNoInteractions(template, flowUtils);
        verify(chain).doFilter(request, response);
    }

    @Test
    void countsNormalRequestAndContinuesWhenAllowed() throws Exception {
        MockHttpServletRequest request = request("POST");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(template.hasKey(Const.FLOW_LIMIT_BLOCK + "127.0.0.1")).thenReturn(false);
        when(flowUtils.limitPeriodCheck(
                Const.FLOW_LIMIT_COUNTER + "127.0.0.1",
                Const.FLOW_LIMIT_BLOCK + "127.0.0.1",
                30, 50, 3
        )).thenReturn(true);

        filter.doFilter(request, response, chain);

        assertEquals(200, response.getStatus());
        verify(chain).doFilter(request, response);
    }

    private MockHttpServletRequest request(String method) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, "/api/topic");
        request.setRemoteAddr("127.0.0.1");
        return request;
    }
}
