package tourGuide;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Locale;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        Locale englishLocale = new Locale("en", "EN");
        Locale.setDefault(englishLocale);

        SpringApplication.run(Application.class, args);
    }

}
