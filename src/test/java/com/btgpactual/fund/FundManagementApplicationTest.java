package com.btgpactual.fund;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

class FundManagementApplicationTest {

    @Test
    void mainDelegatesToSpringApplication() {
        ConfigurableApplicationContext ctx = mock(ConfigurableApplicationContext.class);
        try (MockedStatic<SpringApplication> mocked = mockStatic(SpringApplication.class)) {
            mocked.when(() -> SpringApplication.run(any(Class.class), any(String[].class)))
                    .thenReturn(ctx);
            FundManagementApplication.main(new String[]{});
            mocked.verify(() -> SpringApplication.run(FundManagementApplication.class, new String[]{}));
        }
    }
}
