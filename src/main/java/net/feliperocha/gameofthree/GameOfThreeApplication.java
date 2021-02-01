package net.feliperocha.gameofthree;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class GameOfThreeApplication {

	public static void main(String[] args) {
		SpringApplication.run(GameOfThreeApplication.class, args);
	}

}
