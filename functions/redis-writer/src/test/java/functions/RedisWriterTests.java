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

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.springframework.beans.DirectFieldAccessor;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @author Mark Fisher
 */
public class RedisWriterTests {

	private final RedisWriter writer = new RedisWriter();

	private RedisTemplate<String, String> template;

	@Before
	@SuppressWarnings("unchecked")
	public void init() {
		this.writer.init();
		this.template = (RedisTemplate<String, String>)
				new DirectFieldAccessor(this.writer).getPropertyValue("template");
	}

	@After
	public void cleanup() {
		this.template.execute(new RedisCallback<String>() {
			@Override
			public String doInRedis(RedisConnection connection) throws DataAccessException {
				connection.flushAll();
				return null;
			}
		});
	}

	@Test
	public void testSingleValue() {
		this.writer.apply(Collections.singletonMap("test", 7));
		assertEquals("7", this.template.boundValueOps("test").get());
	}

	@Test
	public void testMultipleValues() {
		Map<String, Object> map = new HashMap<>();
		map.put("key1", "value1");
		map.put("key2", "value2");
		this.writer.apply(map);
		assertEquals("value1", this.template.boundValueOps("key1").get());
		assertEquals("value2", this.template.boundValueOps("key2").get());
	}

	@Test
	public void testHash() {
		Map<String, Object> innerMap = new HashMap<>();
		innerMap.put("a", 1);
		innerMap.put("b", 2);
		innerMap.put("c", 3);
		this.writer.apply(Collections.singletonMap("testHash", innerMap));
		assertEquals("1", this.template.boundHashOps("testHash").get("a"));
		assertEquals("2", this.template.boundHashOps("testHash").get("b"));
		assertEquals("3", this.template.boundHashOps("testHash").get("c"));
	}

	@Test
	public void incrementValue() {
		Map<String, Object> map = new HashMap<>();
		map.put("_command", "increment");
		map.put("counter", 1);
		this.writer.apply(map);
		this.writer.apply(map);
		this.writer.apply(map);
		this.writer.apply(map);
		this.writer.apply(map);
		assertEquals("5", this.template.boundValueOps("counter").get());
	}

	@Test
	public void incrementHashValues() {
		Map<String, Object> hash1 = new HashMap<>();
		hash1.put("1A", 1);
		Map<String, Object> hash2 = new HashMap<>();
		hash2.put("10A", 10);
		Map<String, Object> map = new HashMap<>();
		map.put("_command", "increment");
		map.put("hash1", hash1);
		map.put("hash2", hash2);
		this.writer.apply(map);
		hash1.put("1B", 1);
		hash2.put("10B", 10);
		this.writer.apply(map);
		hash1.put("1C", 1);
		hash2.put("10C", 10);
		this.writer.apply(map);
		assertEquals("3", this.template.boundHashOps("hash1").get("1A"));
		assertEquals("2", this.template.boundHashOps("hash1").get("1B"));
		assertEquals("1", this.template.boundHashOps("hash1").get("1C"));
		assertEquals("30", this.template.boundHashOps("hash2").get("10A"));
		assertEquals("20", this.template.boundHashOps("hash2").get("10B"));
		assertEquals("10", this.template.boundHashOps("hash2").get("10C"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void invalidCommand() {
		Map<String, Object> map = new HashMap<>();
		map.put("_command", "nope");
		map.put("oops", "invalid command");
		this.writer.apply(map);		
	}
}
