package com.test.assignment_2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class Assignment2Application {

    public static void main(String[] args) {
        SpringApplication.run(Assignment2Application.class, args);
    }

}
