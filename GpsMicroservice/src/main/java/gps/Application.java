package gps;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import springfox.documentation.swagger2.annotations.EnableSwagger2;


import java.util.Locale;

@SpringBootApplication
@EnableSwagger2
public class Application {

    public static void main(String[] args) {
        Locale englishLocale = new Locale("en", "EN");
        Locale.setDefault(englishLocale);

        SpringApplication.run(Application.class, args);
    }

}
