package functions;

import java.util.function.Function;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import com.jsoniter.JsonIterator;
import com.jsoniter.any.Any;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

public class JdbcWriter implements Function<String, String> {

	private static Log logger = LogFactory.getLog(JdbcWriter.class);

	@Autowired
	private JdbcTemplate jdbcTemplate;

	private ConfigurableApplicationContext context;

	@PostConstruct
	public void init() {
		if (this.jdbcTemplate == null) {
			context = new SpringApplication(FunctionApp.class).run();
			this.jdbcTemplate = context.getBean(JdbcTemplate.class);
		}
	}

	@PreDestroy
	public void close() {
		if (context != null) {
			context.close();
		}
	}

	@Override
	public String apply(String data) {
		logger.info("Received: " + data);
		Any values = JsonIterator.deserialize(data);
		String name = values.toString("name");
		String description = values.toString("description");
		logger.info("Inserting into data table: [" + name + ", " + description + "]");
		int count = jdbcTemplate.update(
				"insert into data (name, description) values(?,?)", name, description);
		logger.info("Wrote: " + count + " rows");
		return "done";
	}
}
