package com.spring.sec.error.springissue;

import com.spring.sec.error.springissue.api.SimpleResponse;
import com.spring.sec.error.springissue.config.RequestLoggingFilter;
import java.util.EnumSet;
import javax.servlet.DispatcherType;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import org.slf4j.Logger;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SpringIssueApplicationTests {

    @LocalServerPort
    int port;

    private final TestRestTemplate restTemplate = new TestRestTemplate();

    @Captor
    private ArgumentCaptor<String> msgCaptor;
    private static Logger testLogger;

    @BeforeAll
    public static void onlyOnce() {
        testLogger = Mockito.mock(Logger.class);
    }

    @Test
    void shouldHaveBody() {
        // when
        ResponseEntity<SimpleResponse> response = restTemplate.getForEntity("http://localhost:" + port + "/response", SimpleResponse.class);

        // then
        verify(testLogger, timeout(1000).times(2)).info(msgCaptor.capture());

        assertThat(msgCaptor.getAllValues().get(1)).isEqualTo("HTTP Response:\nStatus: 200\nPayload: {\"message\":\"Some message\"}");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getMessage()).isEqualTo("Some message");
    }


    @TestConfiguration
    static class ContextConfiguration {

        @Bean
        public FilterRegistrationBean<RequestLoggingFilter> requestLoggingFilter() {
            FilterRegistrationBean<RequestLoggingFilter> registrationBean =
                    new FilterRegistrationBean<>(new RequestLoggingFilter(testLogger));
            registrationBean.setDispatcherTypes(EnumSet.allOf(DispatcherType.class));
            return registrationBean;
        }
    }
}
