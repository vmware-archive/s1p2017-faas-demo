package functions;

import java.time.Duration;

import org.junit.Ignore;
import org.junit.Test;

import reactor.core.publisher.Flux;

public class VoteStreamProcessorTests {

	Flux<String> input() {
		return Flux.range(1, 99)
				.map(i -> (i % 2 == 0) ? "even" : "odd")
				.delayElements(Duration.ofSeconds(1));
	}

	@Test
	@Ignore
	public void test() {
		VoteStreamProcessor vsp = new VoteStreamProcessor();
		vsp.apply(input()).log().blockLast();
	}
}
