package com.br.walletconfig;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@ComponentScan(basePackages = {
        "com.br.walletcore",
        "com.br.walletdataprovider",
        "com.br.walletentrypoint",
        "com.br.walletconfig"
})
@EnableTransactionManagement
@EnableScheduling
public class WalletConfigApplication {

    public static void main(String[] args) {
        SpringApplication.run(WalletConfigApplication.class, args);
    }
}