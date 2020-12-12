package com.qiguliuxing.dts.admin.api;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(scanBasePackages = { "com.qiguliuxing.dts.db", "com.qiguliuxing.dts.core",
		"com.qiguliuxing.dts.admin" })
@MapperScan({ "com.qiguliuxing.dts.db.dao", "com.qiguliuxing.dts.db.dao.ex" })
@EnableTransactionManagement
@EnableScheduling
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}