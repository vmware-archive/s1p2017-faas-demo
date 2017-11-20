package functions;

import java.util.function.Function;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootApplication
public class FunctionApp {

	public static void main(String[] args) {
		SpringApplication.run(FunctionApp.class, args);
	}

	@Bean
	Function<String, String> function(JdbcTemplate jdbcTemplate) {
		return new JdbcWriter();
	}
}
