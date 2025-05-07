package org.maxpri.wagduck;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class WagduckApplication {

    public static void main(String[] args) {
        SpringApplication.run(WagduckApplication.class, args);
    }

}
