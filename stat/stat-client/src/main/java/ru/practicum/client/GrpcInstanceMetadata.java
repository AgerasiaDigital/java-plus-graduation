package ru.practicum.client;

import org.springframework.cloud.client.ServiceInstance;

final class GrpcInstanceMetadata {

    private GrpcInstanceMetadata() {
    }

    static int parseGrpcPort(ServiceInstance i, int fallbackPort) {
        String p = firstNonBlank(
                i.getMetadata().get("grpc.port"),
                i.getMetadata().get("grpcPort"),
                i.getMetadata().get("gRPC_port"));
        if (p == null) {
            return fallbackPort;
        }
        return Integer.parseInt(p);
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String v : values) {
            if (v != null && !v.isBlank()) {
                return v;
            }
        }
        return null;
    }
}
