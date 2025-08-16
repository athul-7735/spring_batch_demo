package com.example.batchprocessing;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public  class MetricsConfig {


//    @Bean
//    public PrometheusMeterRegistry prometheusMeterRegistry() {
//        return new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
//    }
//
//    @Bean
//    public PrometheusPushGatewayManager pushGatewayManager(PrometheusMeterRegistry registry) {
//        return new PrometheusPushGatewayManager(
//                registry,
//                new PushGatewayConfig() {
//                    @Override
//                    public String job() {
//                        return "spring-batch-job";
//                    }
//                    @Override
//                    public String uri() {
//                        return "http://pg-prometheus-pushgateway.monitoring.svc:9091"; // <-- change if needed
//                    }
//                    @Override
//                    public String get(String key) {
//                        return null;
//                    }
//                },
//                PrometheusPushGatewayManager.ShutdownOperation.PUSH
//        );
//    }
}