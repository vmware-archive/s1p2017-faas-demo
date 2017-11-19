package functions;

import java.sql.Driver;
import java.util.function.Function;

import com.jsoniter.JsonIterator;
import com.jsoniter.any.Any;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

public class JdbcWriter implements Function<String, String> {

	private static Log logger = LogFactory.getLog(JdbcWriter.class);

	@Autowired
	private JdbcTemplate jdbcTemplate;

	public JdbcWriter() {
		// temporary hack until https://github.com/markfisher/sk8s/issues/157 is resolved
		String springProfilesActive = System.getenv("SPRING_PROFILES_ACTIVE");
		if ("kubernetes".equals(springProfilesActive)) {
			String springDatasourceUrl = System.getenv("SPRING_DATASOURCE_URL");
			String springDatasourceDriverClassName = System.getenv("SPRING_DATASOURCE_DRIVER_CLASS_NAME");
			String springDatasourceUsername = System.getenv("SPRING_DATASOURCE_USERNAME");
			String springDatasourcePassword = System.getenv("SPRING_DATASOURCE_PASSWORD");
			Class<? extends Driver> driver = null;
			try {
				driver =(Class<? extends Driver>) Class.forName(springDatasourceDriverClassName);
			} catch (ClassNotFoundException e) {
				logger.error("Error loading JDBC driver '" + springDatasourceDriverClassName + "'", e);
			}
			SimpleDriverDataSource dataSource = new SimpleDriverDataSource();
			dataSource.setDriverClass(driver);
			dataSource.setUrl(springDatasourceUrl);
			dataSource.setUsername(springDatasourceUsername);
			dataSource.setPassword(springDatasourcePassword);
			this.jdbcTemplate = new JdbcTemplate(dataSource);
		}
	}

	public String apply(String data) {
		logger.info("Received: " + data);
		Any values = JsonIterator.deserialize(data);
		String name = values.toString("name");
		String description = values.toString("description");
		logger.info("Inserting into data table: [" + name + ", " + description +"]");
		int count = jdbcTemplate.update("insert into data (name, description) values(?,?)", new String[] {name, description});
		logger.info("Wrote: " + count +" rows");
		return "done";
	}
}
