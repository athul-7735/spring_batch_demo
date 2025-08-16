package com.example.batchprocessing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.concurrent.CountDownLatch;

@SpringBootApplication
public class BatchProcessingApplication {

	public static void main(String[] args) throws InterruptedException {
		System.out.print("Started the main execution!!!");
		ConfigurableApplicationContext context = SpringApplication.run(BatchProcessingApplication.class, args);
		int exitCode = SpringApplication.exit(context);
		System.exit(exitCode);
	}

}