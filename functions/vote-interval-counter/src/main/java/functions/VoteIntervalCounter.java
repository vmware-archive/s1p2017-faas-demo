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
public class VoteIntervalCounter implements Function<Flux<Map<String, Object>>, Flux<Map<String,Integer>>> {

	private Function<Map.Entry<String, Object>, Tuple2<String, Integer>> intEntry = entry -> {
		try {
			return Tuples.of(entry.getKey(), Integer.parseInt(entry.getValue().toString()));
		} catch (Exception e) {
			return null;
		}
	};

	public Flux<Map<String, Integer>> apply(Flux<Map<String, Object>> votes) {
		return votes.concatMap(map -> Flux.fromStream(map.entrySet().stream().map(intEntry).filter(Objects::nonNull)))
				.window(Duration.ofSeconds(2))
				.concatMap(w -> w.collect(HashMap::new,
						(Map<String, Integer> map, Tuple2<String, Integer> next) ->
							map.compute(next.getT1(), (k,v) -> v != null ? v + next.getT2() : next.getT2())
						));
	}
}
