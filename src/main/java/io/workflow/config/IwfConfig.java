package io.workflow.config;

import io.iworkflow.core.Client;
import io.iworkflow.core.ClientOptions;
import io.iworkflow.core.JacksonJsonObjectEncoder;
import io.iworkflow.core.ObjectWorkflow;
import io.iworkflow.core.Registry;
import io.iworkflow.core.UnregisteredClient;
import io.iworkflow.core.WorkerOptions;
import io.iworkflow.core.WorkerService;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IwfConfig {

    @Bean
    public Registry registry() {
        return new Registry();
    }

    @Bean
    public Client client(
        final Registry registry,
        final @Value("${iwf.worker.url}") String workerUrl,
        final @Value("${iwf.server.url}") String serverUrl
    ) {
        return new Client(
            registry,
            ClientOptions
                .builder()
                .workerUrl(workerUrl)
                .serverUrl(serverUrl)
                .objectEncoder(new JacksonJsonObjectEncoder())
                .build()
        );
    }

    @Bean
    public UnregisteredClient unregisteredClient(
        final @Value("${iwf.worker.url}") String workerUrl,
        final @Value("${iwf.server.url}") String serverUrl
    ) {
        return new UnregisteredClient(
            ClientOptions
                .builder()
                .workerUrl(workerUrl)
                .serverUrl(serverUrl)
                .objectEncoder(new JacksonJsonObjectEncoder())
                .build()
        );
    }

    @Bean
    public WorkerService workerService(final Registry registry, final ObjectWorkflow... workflows) {
        Arrays.stream(workflows).forEach(registry::addWorkflow);
        return new WorkerService(registry, WorkerOptions.defaultOptions);
    }
}
