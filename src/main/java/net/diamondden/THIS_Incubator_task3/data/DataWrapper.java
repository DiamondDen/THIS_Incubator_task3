package net.diamondden.THIS_Incubator_task3.data;

import redis.clients.jedis.Jedis;

public class DataWrapper {

	private String pattern;

	public DataWrapper(String pattern) {
		this.pattern = pattern;
	}

	public String set(Jedis jedis, byte[] array, Object... args) {
		return jedis.set(String.format(this.pattern, args).getBytes(), array);
	}
	
	public String set(Jedis jedis, Object value, Object... args) {
		return jedis.set(String.format(this.pattern, args), String.valueOf(value));
	}
	
	public String get(Jedis jedis, Object... args) {
		return jedis.get(String.format(this.pattern, args));
	}

	public byte[] getByteArray(Jedis jedis, Object... args) {
		return jedis.get(String.format(this.pattern, args).getBytes());
	}

	public void del(Jedis jedis, Object... args) {
		jedis.del(String.format(this.pattern, args));
	}
}
