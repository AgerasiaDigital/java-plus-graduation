package ru.practicum.collector.config;

import com.netflix.appinfo.ApplicationInfoManager;
import net.devh.boot.grpc.server.event.GrpcServerStartedEvent;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.netflix.eureka.serviceregistry.EurekaRegistration;
import org.springframework.cloud.netflix.eureka.serviceregistry.EurekaServiceRegistry;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class GrpcPortEurekaMetadataRegistrar {

    private final ApplicationInfoManager applicationInfoManager;
    private final ObjectProvider<EurekaServiceRegistry> eurekaServiceRegistry;
    private final ObjectProvider<EurekaRegistration> eurekaRegistration;

    public GrpcPortEurekaMetadataRegistrar(
            ApplicationInfoManager applicationInfoManager,
            ObjectProvider<EurekaServiceRegistry> eurekaServiceRegistry,
            ObjectProvider<EurekaRegistration> eurekaRegistration) {
        this.applicationInfoManager = applicationInfoManager;
        this.eurekaServiceRegistry = eurekaServiceRegistry;
        this.eurekaRegistration = eurekaRegistration;
    }

    @EventListener
    public void onGrpcStarted(GrpcServerStartedEvent event) {
        String port = String.valueOf(event.getPort());
        applicationInfoManager.registerAppMetadata(
                Map.of(
                        "gRPC_port", port,
                        "grpcPort", port));
        EurekaServiceRegistry registry = eurekaServiceRegistry.getIfAvailable();
        EurekaRegistration registration = eurekaRegistration.getIfAvailable();
        if (registry != null && registration != null) {
            registry.register(registration);
        }
    }
}
