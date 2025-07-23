package com.example.batchprocessing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.concurrent.CountDownLatch;

@SpringBootApplication
public class BatchProcessingApplication {

//	public static void main(String[] args) {
//		SpringApplication.run(BatchProcessingApplication.class, args);
//	}

	public static void main(String[] args) throws InterruptedException {
		System.out.print("Started the main execution!!!");
		ConfigurableApplicationContext context = SpringApplication.run(BatchProcessingApplication.class, args);

		// Keep application running after batch completes
//		System.out.println("Batch completed. Press Ctrl+C to exit.");
//		new CountDownLatch(1).await(); // Block indefinitely

		System.exit(SpringApplication.exit(SpringApplication.run(BatchProcessingApplication.class, args)));
	}

}


//package com.example.batchprocessing;
//
//import org.springframework.batch.core.Job;
//import org.springframework.batch.core.JobParameters;
//import org.springframework.batch.core.JobParametersBuilder;
//import org.springframework.batch.core.launch.JobLauncher;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//
//@SpringBootApplication
//public class BatchProcessingApplication implements CommandLineRunner {
//
//	private final JobLauncher jobLauncher;
//	private final Job job;
//
//	public BatchProcessingApplication(JobLauncher jobLauncher, Job job) {
//		this.jobLauncher = jobLauncher;
//		this.job = job;
//	}
//
//	public static void main(String[] args) {
//		SpringApplication.run(BatchProcessingApplication.class, args);
//	}
//
////	public static void main(String[] args) {
////		System.exit(SpringApplication.exit(SpringApplication.run(BatchProcessingApplication.class, args)));
////	}
//
//	@Override
//	public void run(String... args) throws Exception {
//		JobParameters params = new JobParametersBuilder()
//				.addLong("time", System.currentTimeMillis())
//				.toJobParameters();
//		jobLauncher.run(job, params);
//	}
//
//}
