package functions;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import reactor.core.publisher.Flux;

/**
 * @author Mark Fisher
 */
public class VoteStreamProcessor implements Function<Flux<String>, Flux<Map<String, Object>>> {

	public Flux<Map<String, Object>> apply(Flux<String> words) {
		return Flux.merge(intervals(words), windows(words));
	}

	public Flux<Map<String, Object>> intervals(Flux<String> words) {
		return words.window(Duration.ofSeconds(2))
				.concatMap(w -> w.collect(VoteAggregate::new, VoteAggregate::sum)
				.map(VoteAggregate::intervalMap));
	}

	public Flux<Map<String, Object>> windows(Flux<String> words) {
		return words.window(Duration.ofSeconds(60), Duration.ofSeconds(2))
				.concatMap(w -> w.collect(VoteAggregate::new, VoteAggregate::sum)
				.map(VoteAggregate::windowMap), Integer.MAX_VALUE);
	}

	class VoteAggregate {
		long timestamp = System.currentTimeMillis();
		Map<String, Integer> votes = new HashMap<>();

		Map<String, Object> intervalMap() {
			Map<String, Object> map = map();
			map.put("_list", "demo:votes-log");
			return map;
		}

		Map<String, Object> windowMap() {
			Map<String, Object> map = map();
			map.put("_list", "demo:votes-windows");
			return map;
		}

		Map<String, Object> map() {
			Map<String, Object> map = new HashMap<>(votes);
			map.put("_time", timestamp);
			return map;
		}

		void sum(String word) {
			votes.merge(word, 1, Integer::sum);
		}
	}
}
