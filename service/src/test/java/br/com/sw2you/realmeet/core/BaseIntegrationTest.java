package br.com.sw2you.realmeet.core;

import br.com.sw2you.realmeet.Application;
import br.com.sw2you.realmeet.api.ApiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;

@ActiveProfiles(profiles = "integration-test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = Application.class)
public abstract class BaseIntegrationTest {

    @Autowired
    private Flyway flyway;

    @BeforeEach
    void setup() {
        setupFlyway();
        setupEach();
    }

    protected void setupEach() {}

    protected void setLocalHostBasePath(ApiClient apiClient, String path) {

    }

    private void setupFlyway() {
        flyway.clean();
        flyway.migrate();
    }
}
