package org.zero_consult.invoices_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableScheduling
@SpringBootApplication
@EnableTransactionManagement
@EnableWebSecurity
@EnableMethodSecurity
public class InvoicesBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(InvoicesBackendApplication.class, args);
    }

}
