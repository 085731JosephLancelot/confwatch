package com.confwatch.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActionExecutorTest {

    @Mock
    private WebhookDispatcher webhookDispatcher;

    @Mock
    private ReloadCommandRunner reloadCommandRunner;

    private ActionExecutor actionExecutor;
    private Path testFile;

    @BeforeEach
    void setUp() {
        actionExecutor = new ActionExecutor(webhookDispatcher, reloadCommandRunner);
        testFile = Paths.get("/etc/myapp/config.yaml");
    }

    @Test
    void shouldDispatchWebhooksOnFileChange() throws IOException {
        ActionConfig config = new ActionConfig(
                List.of("http://localhost:8080/reload", "http://localhost:9090/notify"),
                List.of()
        );

        actionExecutor.execute(testFile, config);

        verify(webhookDispatcher, times(1)).dispatch("http://localhost:8080/reload", testFile);
        verify(webhookDispatcher, times(1)).dispatch("http://localhost:9090/notify", testFile);
        verifyNoInteractions(reloadCommandRunner);
    }

    @Test
    void shouldRunReloadCommandsOnFileChange() throws IOException, InterruptedException {
        ActionConfig config = new ActionConfig(
                List.of(),
                List.of("systemctl reload myapp", "kill -HUP 1234")
        );

        actionExecutor.execute(testFile, config);

        verify(reloadCommandRunner, times(1)).run("systemctl reload myapp", testFile);
        verify(reloadCommandRunner, times(1)).run("kill -HUP 1234", testFile);
        verifyNoInteractions(webhookDispatcher);
    }

    @Test
    void shouldContinueExecutingRemainingActionsAfterWebhookFailure() throws IOException {
        doThrow(new IOException("Connection refused"))
                .when(webhookDispatcher).dispatch("http://failing-host/reload", testFile);

        ActionConfig config = new ActionConfig(
                List.of("http://failing-host/reload", "http://localhost:8080/reload"),
                List.of()
        );

        actionExecutor.execute(testFile, config);

        verify(webhookDispatcher, times(1)).dispatch("http://failing-host/reload", testFile);
        verify(webhookDispatcher, times(1)).dispatch("http://localhost:8080/reload", testFile);
    }

    @Test
    void shouldHandleEmptyActionConfig() throws IOException, InterruptedException {
        ActionConfig config = new ActionConfig(List.of(), List.of());

        actionExecutor.execute(testFile, config);

        verifyNoInteractions(webhookDispatcher);
        verifyNoInteractions(reloadCommandRunner);
    }
}
