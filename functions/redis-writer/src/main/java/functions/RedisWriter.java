package functions;

import java.util.Map;
import java.util.function.Function;

import redis.clients.jedis.Client;

public class RedisWriter implements Function<Map<String, Integer>, String> {

	private Client client = new Client(System.getenv("REDIS_HOST"),
			Integer.parseInt(System.getenv("REDIS_PORT")));

	public String apply(Map<String, Integer> counts) {
		client.connect();
		for (Map.Entry<String, Integer> entry : counts.entrySet()) {
			client.incrBy(entry.getKey().getBytes(), entry.getValue());
		}
		client.close();
		return "done";
	}
}
