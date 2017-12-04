package functions;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import reactor.core.publisher.Flux;

public class WordCounter implements Function<Flux<String>, Flux<Map<String, Integer>>> {
	public Flux<Map<String, Integer>> apply(Flux<String> words) {
		return words.flatMap(word -> Flux.fromArray(word.split("\\W")))
				.window(Duration.ofSeconds(10))
				.flatMap(w -> w.collect(HashMap::new, (map, word) ->
								map.merge(word, 1, Integer::sum)));
	}
}