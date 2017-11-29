/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package functions;

import java.util.Map;
import java.util.function.Function;

import javax.annotation.PostConstruct;

import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Mark Fisher
 */
public class RedisWriter implements Function<Map<String, Object>, String> {

	private static final String COMMAND_KEY = "_command";

	private enum Command { set, increment }

	private String hashKey;

	private String listKey;

	private Command defaultCommand = Command.set;

	private final RedisTemplate<String, String> template = new RedisTemplate<>();

	private final ObjectMapper mapper = new ObjectMapper();

	@PostConstruct
	public void init() {
		if (System.getenv("HASH_KEY") != null) {
			this.hashKey = System.getenv("HASH_KEY");
		}
		if (System.getenv("LIST_KEY") != null) {
			this.listKey = System.getenv("LIST_KEY");
		}
		if (System.getenv("DEFAULT_COMMAND") != null) {
			try {
				this.defaultCommand = Command.valueOf(System.getenv("DEFAULT_COMMAND"));
			}
			catch (IllegalArgumentException e) {
				throw new IllegalArgumentException(
						"unsupported command: " + System.getenv("DEFAULT_COMMAND"));
			}
		}
		JedisConnectionFactory connectionFactory = new JedisConnectionFactory();
		if (System.getenv("REDIS_HOST") != null) {
			connectionFactory.setHostName(System.getenv("REDIS_HOST"));
		}
		if (System.getenv("REDIS_PORT") != null) {
			connectionFactory.setPort(Integer.parseInt(System.getenv("REDIS_PORT")));
		}
		if (System.getenv("REDIS_PASSWORD") != null) {
			connectionFactory.setPassword(System.getenv("REDIS_PASSWORD"));
		}
		connectionFactory.afterPropertiesSet();
		this.template.setConnectionFactory(connectionFactory);
		this.template.setDefaultSerializer(new StringRedisSerializer());
		this.template.afterPropertiesSet();
	}

	public String apply(Map<String, Object> input) {
		Command command = this.defaultCommand;
		if (input.containsKey(COMMAND_KEY)) {
			try {
				command = Command.valueOf(input.get(COMMAND_KEY).toString());
			}
			catch (IllegalArgumentException e) {
				throw new IllegalArgumentException(
						"unsupported command: " + input.get(COMMAND_KEY).toString());
			}
		}
		if (this.listKey != null) {
			try {
				this.template.boundListOps(this.listKey).rightPush(this.mapper.writeValueAsString(input));
			}
			catch (JsonProcessingException e) {
				e.printStackTrace();
			}
			return "done";
		}
		for (Map.Entry<String, Object> entry : input.entrySet()) {
			String key = entry.getKey();
			if (COMMAND_KEY.equals(key)) {
				continue;
			}
			Object value = entry.getValue();
			if (value instanceof Map) { // hash
				@SuppressWarnings("unchecked")
				Map<String, Object> innerMap = (Map<String, Object>) value;
				for (Map.Entry<String, Object> innerEntry : innerMap.entrySet()) {
					String innerKey = innerEntry.getKey();
					switch (command) {
					case increment:
						this.template.boundHashOps(key).increment(innerKey,
								Long.parseLong(innerEntry.getValue().toString()));
						break;
					default:
						this.template.boundHashOps(key).put(innerKey,
								innerEntry.getValue().toString());
						break;
					}
				}
			}
			else if (this.hashKey != null) {
				switch (command) {
				case increment:
					this.template.boundHashOps(this.hashKey).increment(key,
							Long.parseLong(value.toString()));
					break;
				default:
					this.template.boundHashOps(this.hashKey).put(key, value.toString());
					break;
				}
			}
			else {
				switch (command) {
				case increment:
					this.template.boundValueOps(key).increment(Long.parseLong(value.toString()));
					break;
				default:
					this.template.boundValueOps(key).set(value.toString());
					break;
				}
			}
		}
		return "done";
	}
}
