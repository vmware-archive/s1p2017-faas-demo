package functions;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import reactor.core.publisher.Flux;

public class VoteIntervalCounterTests {

	@Test
	public void test() {
		VoteIntervalCounter vic = new VoteIntervalCounter();
		// TODO: use interval and pass multiple windows
		Flux<String> flux = Flux.just("foo", "bar", "bar");
		List<Map<String, Object>> results = vic.apply(flux).collectList().block();
		assertEquals(1, results.size());
		assertEquals(new Integer(1), results.get(0).get("foo"));
		assertEquals(new Integer(2), results.get(0).get("bar"));
	}
}
