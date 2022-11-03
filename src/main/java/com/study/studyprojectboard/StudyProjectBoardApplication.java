package com.study.studyprojectboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication
public class StudyProjectBoardApplication {

    public static void main(String[] args) {
        SpringApplication.run(StudyProjectBoardApplication.class, args);
    }

}
