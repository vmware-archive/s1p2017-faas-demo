package functions;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import reactor.core.publisher.Flux;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

/**
 * @author Mark Fisher
 */
public class VoteIntervalCounter implements Function<Flux<Map<String, Object>>, Flux<Map<String, Object>>> {

	private Function<Map.Entry<String, Object>, Tuple2<String, Integer>> intEntry = entry -> {
		try {
			return Tuples.of(entry.getKey(), Integer.parseInt(entry.getValue().toString()));
		} catch (Exception e) {
			return null;
		}
	};

	private static Map<String, Object> createMap() {
		Map<String, Object> map = new HashMap<>();
		map.put("_list", "demo:votes-log");
		map.put("_time", System.currentTimeMillis());
		return map;
	}

	public Flux<Map<String, Object>> apply(Flux<Map<String, Object>> votes) {
		return votes.concatMap(map -> Flux.fromStream(map.entrySet().stream().map(intEntry).filter(Objects::nonNull)))
				.window(Duration.ofSeconds(2))
				.concatMap(w -> w.collect(VoteIntervalCounter::createMap,
						(Map<String, Object> map, Tuple2<String, Integer> next) ->
							map.compute(next.getT1(), (k,v) -> v != null ? (int)v + next.getT2() : next.getT2())
						));
	}
}
