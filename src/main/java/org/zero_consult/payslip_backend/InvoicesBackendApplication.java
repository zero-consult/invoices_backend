package org.zero_consult.payslip_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class InvoicesBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(InvoicesBackendApplication.class, args);
    }

}
