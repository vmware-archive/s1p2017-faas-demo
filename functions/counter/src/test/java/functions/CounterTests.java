package functions;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import reactor.core.publisher.Flux;

public class CounterTests {

	@Test
	public void test() {
		Counter counter = new Counter();
		Flux<String> words = Flux.just(1, 2, 2, 3, 3, 3, 4, 4, 4, 4, 5, 5, 5, 5, 5).map(i -> ""+i);
		List<Map<String, Integer>> results = counter.apply(words).collectList().block();
		assertEquals(2, results.size());
		assertEquals(4, results.get(0).size());
		assertEquals(new Integer(1), results.get(0).get("1"));
		assertEquals(new Integer(2), results.get(0).get("2"));
		assertEquals(new Integer(3), results.get(0).get("3"));
		assertEquals(new Integer(4), results.get(0).get("4"));
		assertEquals(1, results.get(1).size());
		assertEquals(new Integer(5), results.get(1).get("5"));
	}
}
