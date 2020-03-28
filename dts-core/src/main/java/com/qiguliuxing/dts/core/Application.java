package com.qiguliuxing.dts.core;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = { "com.qiguliuxing.dts.db", "com.qiguliuxing.dts.core" })
@MapperScan("com.qiguliuxing.dts.db.dao")
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}