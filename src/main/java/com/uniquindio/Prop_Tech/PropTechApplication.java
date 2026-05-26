package com.uniquindio.Prop_Tech;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.uniquindio.Config.LlmProperties;

@SpringBootApplication(scanBasePackages = "com.uniquindio")
@EnableConfigurationProperties(LlmProperties.class)
public class PropTechApplication {

	public static void main(String[] args) {
		SpringApplication.run(PropTechApplication.class, args);

	}

}
