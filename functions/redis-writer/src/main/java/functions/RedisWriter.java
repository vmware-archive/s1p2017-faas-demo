package functions;

import java.util.Map;
import java.util.function.Function;

import redis.clients.jedis.Client;

public class RedisWriter implements Function<Map<String, Integer>, String> {

	private Client client = new Client(System.getenv("REDIS_HOST"),
			Integer.parseInt(System.getenv("REDIS_PORT")));

	public String apply(Map<String, Integer> counts) {
		client.connect();
		client.auth(System.getenv("REDIS_PASSWORD"));
		for (Map.Entry<String, Integer> entry : counts.entrySet()) {
			// TODO: config options for different datatypes and operations
			//client.incrBy(entry.getKey().getBytes(), entry.getValue());
			client.set(entry.getKey(), entry.getValue().toString());
		}
		client.close();
		return "done";
	}
}
