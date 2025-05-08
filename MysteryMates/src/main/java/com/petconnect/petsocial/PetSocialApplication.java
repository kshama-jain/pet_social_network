package com.petconnect.petsocial;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class PetSocialApplication {

    public static void main(String[] args) {
        SpringApplication.run(PetSocialApplication.class, args);
    }
}
