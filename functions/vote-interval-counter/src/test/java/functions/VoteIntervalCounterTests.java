package functions;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import reactor.core.publisher.Flux;

public class VoteIntervalCounterTests {

	@Test
	public void test() {
		Map<String, Object> votes = new HashMap<>();
		votes.put("foo", 1);
		votes.put("bar", 2);
		votes.put("baz", "ignoreme");
		VoteIntervalCounter vic = new VoteIntervalCounter();
		// TODO: use interval and pass multiple windows
		Flux<Map<String, Object>> flux = Flux.just(votes);
		List<Map<String, Integer>> results = vic.apply(flux).collectList().block();
		assertEquals(1, results.size());
		assertEquals(new Integer(1), results.get(0).get("foo"));
		assertEquals(new Integer(2), results.get(0).get("bar"));
	}
}
