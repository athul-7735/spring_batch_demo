package com.example.batchprocessing;

import io.prometheus.metrics.core.metrics.Gauge;
import io.prometheus.metrics.exporter.pushgateway.PushGateway;
import io.prometheus.metrics.model.snapshots.Unit;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class JobCompletionNotificationListener implements JobExecutionListener {

    private final PushGateway pushGateway;
    private final Gauge batchRunsGauge;
    private final Gauge batchDurationGauge;

    public JobCompletionNotificationListener(
            @Value("${pushgateway.url:pg-prometheus-pushgateway.monitoring.svc:9091}") String pushGatewayUrl,
            @Value("${pushgateway.jobname:spring-batch-job}") String metricsJobName) {

        // Build PushGateway instance
        this.pushGateway = PushGateway.builder()
                .address(pushGatewayUrl)  // example: "pg-prometheus-pushgateway.monitoring.svc:9091"
                .job(metricsJobName)
                .build();

        // Define gauges for metrics
        this.batchRunsGauge = Counter.builder()
                .name("batch_runs")
                .help("Number of Spring Batch job runs")
                .register();

        this.batchDurationGauge = Gauge.builder()
                .name("batch_duration_seconds")
                .help("Duration of Spring Batch job in seconds")
                .unit(Unit.SECONDS)
                .register();
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        try {
            // Calculate duration
            long durationSeconds = Duration.between(
                    jobExecution.getStartTime().atZone(ZoneId.systemDefault()).toInstant(),
                    jobExecution.getEndTime().atZone(ZoneId.systemDefault()).toInstant()
            ).toSeconds();
            // Set metric values
            batchRunsGauge.inc(); // each run = +1
            batchDurationGauge.set(durationSeconds);
            // Push metrics
            pushGateway.push();
            System.out.println(">> Metrics pushed to Pushgateway: " + pushGateway);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}