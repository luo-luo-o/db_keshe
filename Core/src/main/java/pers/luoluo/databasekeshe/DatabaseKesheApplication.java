package pers.luoluo.databasekeshe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DatabaseKesheApplication {

    public static void main(String[] args) {
        SpringApplication.run(DatabaseKesheApplication.class, args);
    }

}
