package functions;

import org.junit.Test;

public class JdbcWriterInitTests {

	@Test
	public void bootstrap() {
		JdbcWriter writer = new JdbcWriter();
		writer.init();
		System.out.printf(
				writer.apply("{\"name\": \"Bob\", \"description\": \"testing\"}"));
	}

}
