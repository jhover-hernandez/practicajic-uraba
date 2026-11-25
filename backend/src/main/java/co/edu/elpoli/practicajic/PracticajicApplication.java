package co.edu.elpoli.practicajic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/** {@code @EnableScheduling}: cierre automático de convocatorias vencidas (RF-20). */
@SpringBootApplication
@EnableScheduling
public class PracticajicApplication {

    public static void main(String[] args) {
        SpringApplication.run(PracticajicApplication.class, args);
    }
}
