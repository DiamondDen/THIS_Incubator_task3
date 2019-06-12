package net.diamondden.THIS_Incubator_task3;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import com.google.gson.JsonObject;

public class Tools {

	public static void jsonPatternError(JsonObject json, int id, String msg) {
		JsonObject error = new JsonObject();
		error.addProperty("errorId", id);
		error.addProperty("errorMsg", msg);
		json.add("error", error);
	}

	private static char[] keyPattern = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();

	public static String generateKey(int length) {
		Random r = ThreadLocalRandom.current();
		StringBuilder build = new StringBuilder();
		for (int i = 0, n = keyPattern.length; i < length; i++) {
			build.append(keyPattern[r.nextInt(n)]);
		}
		return build.toString();
	}

	public static int getEnv(String name, int def) {
		String value = System.getenv(name);
		if (value != null) {
			try {
				return Integer.parseInt(value);
			} catch (Exception e) {}
		}
		return def;
	}

	public static String getEnv(String name, String def) {
		String value = System.getenv(name);
		return value == null ? def : value;
	}
}
