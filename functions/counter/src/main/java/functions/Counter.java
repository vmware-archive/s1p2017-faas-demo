package functions;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import reactor.core.publisher.Flux;

public class Counter implements Function<Flux<String>, Flux<Map<String,Integer>>> {

	public Flux<Map<String, Integer>> apply(Flux<String> words) {
		return words.window(10)
				.flatMap(f -> f.flatMap(word -> Flux.fromArray(word.split("\\W")))
				.reduce(new HashMap<String, Integer>(), (map, word) -> {
						map.merge(word, 1, Integer::sum);
						return map;
				}));
	}
}
