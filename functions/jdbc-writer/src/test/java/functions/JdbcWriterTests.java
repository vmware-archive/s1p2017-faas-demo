package functions;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
public class JdbcWriterTests {

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Autowired
	JdbcWriter writer;

	@Test
	public void test() {
		assertEquals("done", writer.apply("{\"name\": \"Bob\", \"description\": \"testing\"}"));
		assertEquals(1, (int)jdbcTemplate.queryForObject(
				"select count(*) from data where name='Bob' and description='testing'", Integer.class));
	}

}
