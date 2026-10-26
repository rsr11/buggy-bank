package com.buggybank.buggy_bank;

import org.springframework.boot.SpringApplication;

public class TestBuggyBankApplication {

	public static void main(String[] args) {
		SpringApplication.from(BuggyBankApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
