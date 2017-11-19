package functions;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = {"spring.datasource.url=jdbc:h2:mem:test",
		"spring.datasource.platform=h2",
		"spring.datasource.initialize=true"})
public class JdbcWriterTests {

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Autowired
	JdbcWriter writer;

 	@Test
	public void test() {
		System.out.printf(writer.apply("{\"name\": \"Bob\", \"description\": \"testing\"}"));
	}
}
