package functions;

import java.util.Collections;

import org.junit.Test;

public class RedisWriterTests {

	@Test
	public void test() {
		RedisWriter writer = new RedisWriter();
		writer.apply(Collections.singletonMap("test", 7));
	}
}
