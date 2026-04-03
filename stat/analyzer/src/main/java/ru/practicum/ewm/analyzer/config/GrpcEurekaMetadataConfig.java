package ru.practicum.ewm.analyzer.config;

import com.netflix.appinfo.ApplicationInfoManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.event.GrpcServerStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GrpcEurekaMetadataConfig {

    private final ApplicationInfoManager applicationInfoManager;

    @EventListener
    public void onGrpcServerStarted(GrpcServerStartedEvent event) {
        int port = event.getServer().getPort();
        log.info("gRPC server started on port {}, registering grpcPort in Eureka metadata", port);
        applicationInfoManager.getInfo().getMetadata().put("grpcPort", String.valueOf(port));
        applicationInfoManager.setInstanceStatus(applicationInfoManager.getInfo().getStatus());
    }
}
