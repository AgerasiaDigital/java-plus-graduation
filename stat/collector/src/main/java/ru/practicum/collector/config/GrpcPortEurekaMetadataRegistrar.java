package ru.practicum.collector.config;

import com.netflix.appinfo.ApplicationInfoManager;
import net.devh.boot.grpc.server.event.GrpcServerStartedEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConditionalOnBean(ApplicationInfoManager.class)
public class GrpcPortEurekaMetadataRegistrar {

    private final ApplicationInfoManager applicationInfoManager;

    public GrpcPortEurekaMetadataRegistrar(ApplicationInfoManager applicationInfoManager) {
        this.applicationInfoManager = applicationInfoManager;
    }

    @EventListener
    public void onGrpcStarted(GrpcServerStartedEvent event) {
        int port = event.getPort();
        if (port <= 0) {
            return;
        }
        String portStr = String.valueOf(port);
        applicationInfoManager.registerAppMetadata(Map.of(
                "grpc.port", portStr,
                "grpcPort", portStr,
                "gRPC_port", portStr
        ));
    }
}
