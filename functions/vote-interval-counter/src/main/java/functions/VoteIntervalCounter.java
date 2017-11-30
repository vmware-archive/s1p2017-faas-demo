package functions;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import reactor.core.publisher.Flux;

/**
 * @author Mark Fisher
 */
public class VoteIntervalCounter implements Function<Flux<String>, Flux<Map<String, Object>>> {
	
	public Flux<Map<String, Object>> apply(Flux<String> words) {
		return words.window(Duration.ofSeconds(2))
				.concatMap(w -> w.collect(VoteAggregate::new, VoteAggregate::sum)
				.map(VoteAggregate::serialize));
	}

	class VoteAggregate {
		long timestamp = System.currentTimeMillis();
		Map<String, Integer> votes = new HashMap<>();

		Map<String, Object> serialize() {
			Map<String, Object> map = new HashMap<>(votes);
			map.put("_list", "demo:votes-log");
			map.put("_time", timestamp);
			return map;
		}

		void sum(String word) {
			votes.merge(word, 1, Integer::sum);
		}
	}
}
