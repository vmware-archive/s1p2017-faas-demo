package functions;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class JdbcWriterInitTests {

	@Test
	public void bootstrap() {
		JdbcWriter writer = new JdbcWriter();
		writer.init();
		assertEquals("done", writer.apply("{\"name\": \"Bob\", \"description\": \"testing\"}"));
	}

}
