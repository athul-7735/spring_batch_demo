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
            @Value("${pushgateway.url}") String pushGatewayUrl,
            @Value("${pushgateway.jobname:spring-batch-job}") String metricsJobName) {

        // Build PushGateway instance
        this.pushGateway = PushGateway.builder()
                .address(pushGatewayUrl)  // example: "pg-prometheus-pushgateway.monitoring.svc:9091"
                .job(metricsJobName)
                .build();

        // Define gauges for metrics
        this.batchRunsGauge = Gauge.builder()
                .name("batch_runs_total")
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
            batchRunsGauge.set(1); // each run = +1
            batchDurationGauge.set(durationSeconds);

            // Push metrics
            pushGateway.push();

            System.out.println(">> Metrics pushed to Pushgateway: " + pushGateway);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
//    // Metric: Total data processed (replace with your actual value)
//    private static final Gauge dataProcessedInBytes = Gauge.builder()
//            .name("data_processed_bytes")
//            .help("Data processed in the last Spring Batch job run")
//            .register();
//
//    // Metric: Job duration in seconds
//    private static final Gauge jobDurationSeconds = Gauge.builder()
//            .name("batch_job_duration_seconds")
//            .help("Duration of the Spring Batch job in seconds")
//            .register();
//
//    // Metric: Job success (1) or failure (0)
//    private static final Gauge jobSuccessGauge = Gauge.builder()
//            .name("batch_job_success")
//            .help("1 if job succeeded, 0 if failed")
//            .register();
//
//    private final static String pushGatewayAddress = "pg-prometheus-pushgateway.monitoring.svc:9091";
//
//    private static PushGateway pushGateway = PushGateway.builder()
//            .address(pushGatewayAddress) // not needed as localhost:9091 is the default
//            .job("spring-batch-job")
//            .build();
//
//
//    @Override
//    public void beforeJob(JobExecution jobExecution) {
//        // Store start time in milliseconds
//        jobExecution.getExecutionContext().putLong("startTime", System.currentTimeMillis());
//    }
//
//    @Override
//    public void afterJob(JobExecution jobExecution) {
//        try {
//            // Calculate duration
//            long startTime = jobExecution.getExecutionContext().getLong("startTime");
//            long durationSeconds = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startTime);
//            jobDurationSeconds.set(durationSeconds);
//
//            // Example: Set bytes processed (replace with actual computation from your job)
//            long bytesProcessed = 42L;
//            dataProcessedInBytes.set(bytesProcessed);
//
//            // Set success/failure metric
//            boolean success = jobExecution.getExitStatus().getExitCode().equalsIgnoreCase("COMPLETED");
//            jobSuccessGauge.set(success ? 1 : 0);
//
//            // Add grouping keys (labels in Prometheus)
//            Map<String, String> groupingKey = new HashMap<>();
//            groupingKey.put("jobName", jobExecution.getJobInstance().getJobName());
//            groupingKey.put("exitStatus", jobExecution.getExitStatus().getExitCode());
//
//            // Push metrics to Pushgateway
//
//            pushGateway.pushAdd(io.prometheus.client.CollectorRegistry.defaultRegistry, "spring-batch-job", groupingKey);
//
//            System.out.println(">> Metrics pushed to Pushgateway at " + pushGatewayAddress);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }


//    private final PrometheusMeterRegistry prometheusMeterRegistry;
//    private final String pushGatewayUrl;
//    private final String metricsJobName;
//
//    public JobCompletionNotificationListener(
//            PrometheusMeterRegistry prometheusMeterRegistry,
//            @Value("${pushgateway.url}") String pushGatewayUrl,
//            @Value("${pushgateway.jobname:spring-batch-job}") String metricsJobName) {
//        this.prometheusMeterRegistry = prometheusMeterRegistry;
//        this.pushGatewayUrl = pushGatewayUrl;
//        this.metricsJobName = metricsJobName;
//    }
//
//    @Override
//    public void afterJob(JobExecution jobExecution) {
//        try {
//            // Duration in seconds
//            long durationSeconds = Duration.between(
//                    jobExecution.getStartTime().atZone(ZoneId.systemDefault()).toInstant(),
//                    jobExecution.getEndTime().atZone(ZoneId.systemDefault()).toInstant()
//            ).toSeconds();
//
//            // Micrometer metrics
//            Counter.builder("batch_runs_total")
//                    .description("Number of Spring Batch job runs")
//                    .tag("jobName", jobExecution.getJobInstance().getJobName())
//                    .tag("exitStatus", jobExecution.getStatus().toString())
//                    .register(prometheusMeterRegistry)
//                    .increment();
//
//            AtomicLong durationGauge = new AtomicLong(durationSeconds);
//
//            Gauge.builder("batch_duration_seconds", durationGauge::get)
//                    .description("Duration of Spring Batch job in seconds")
//                    .tag("jobName", jobExecution.getJobInstance().getJobName())
//                    .tag("exitStatus", jobExecution.getStatus().toString())
//                    .register(prometheusMeterRegistry);
//
//            // Push all metrics in Micrometer's registry to Pushgateway
//            CollectorRegistry collectorRegistry = prometheusMeterRegistry.getPrometheusRegistry();
//            PushGateway pg = new PushGateway(pushGatewayUrl);
//            pg.pushAdd(collectorRegistry, metricsJobName);
//
//            System.out.println(">> Metrics pushed to Pushgateway: " + pushGatewayUrl);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}
