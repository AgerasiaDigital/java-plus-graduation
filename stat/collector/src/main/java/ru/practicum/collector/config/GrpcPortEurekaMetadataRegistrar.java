package ru.practicum.collector.config;

import com.netflix.appinfo.ApplicationInfoManager;
import net.devh.boot.grpc.server.event.GrpcServerStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class GrpcPortEurekaMetadataRegistrar {

    private final ApplicationInfoManager applicationInfoManager;

    public GrpcPortEurekaMetadataRegistrar(ApplicationInfoManager applicationInfoManager) {
        this.applicationInfoManager = applicationInfoManager;
    }

    @EventListener
    public void onGrpcStarted(GrpcServerStartedEvent event) {
        String port = String.valueOf(event.getPort());
        applicationInfoManager.registerAppMetadata(
                Map.of(
                        "gRPC_port", port,
                        "grpcPort", port));
    }
}
