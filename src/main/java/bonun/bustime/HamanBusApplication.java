package bonun.bustime;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class HamanBusApplication {

	public static void main(String[] args) {
		SpringApplication.run(HamanBusApplication.class, args);
	}

}
