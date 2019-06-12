package net.diamondden.THIS_Incubator_task3.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import com.google.gson.Gson;
import net.diamondden.THIS_Incubator_task3.Tools;
import net.diamondden.THIS_Incubator_task3.data.Paste.TypeAccess;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Transaction;

public class DataManager {

	private JedisPool jedisPool;
	private Gson gson;

	private DataWrapper dataPasteByKey;
	private DataWrapper dataPasteInfoById;
	private DataWrapper dataPasteDataById;

	private List<Paste> lastTenPaste;

	public DataManager() {
		this.gson = new Gson();
		JedisPoolConfig poolConfig = new JedisPoolConfig();
		poolConfig.setMaxTotal(16);

		this.jedisPool = new JedisPool(poolConfig, Tools.getEnv("redis_host", "localhost"),
				Tools.getEnv("redis_port", 6379), 10000, System.getenv("redis_password"));
		this.lastTenPaste = new LimitedList<>(10);
		this.dataPasteByKey = new DataWrapper("%s:%s");
		this.dataPasteInfoById = new DataWrapper("pastes:%d:info");
		this.dataPasteDataById = new DataWrapper("pastes:%d:data");
	}

	/**
	 * Загрузать Paste по его ключу и типу доступа
	 * 
	 * @param
	 * @return
	 * @throws Exception
	 *                       при любой ошибки выходим в главный метод
	 */

	public void addPaste(Paste paste) throws Exception {
		try (Jedis jedis = this.jedisPool.getResource()) {
			paste.setKey(TypeAccess.PUBLIC, Tools.generateKey(8));
			paste.setKey(TypeAccess.UNLISTED, Tools.generateKey(24));

			paste.setId(jedis.incr("last_paste_id") - 1);
			paste.setCompressed(!noNeedToCompressData);

			this.dataPasteInfoById.set(jedis, this.gson.toJson(paste), paste.getId());
			this.dataPasteDataById.set(jedis, this.compressData(paste.getData()), paste.getId());

			Transaction multi = jedis.multi();
			paste.getKeys().entrySet().forEach(entry -> {
				multi.set(entry.getKey().getTableName() + ":" + entry.getValue(), String.valueOf(paste.getId()));
			});
			multi.exec();

			if (paste.getAccess() == TypeAccess.PUBLIC) {
				this.lastTenPaste.add(paste);
			}
		}
	}

	/*
	 * Даём возможность отключать сжатие данных
	 */
	private static boolean noNeedToCompressData = Boolean.getBoolean("noNeedToCompressData");

	private byte[] compressData(String data) throws Exception {
		byte[] input = data.getBytes("UTF-8");
		if (noNeedToCompressData) {
			return input;
		} else {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			GZIPOutputStream gout = new GZIPOutputStream(baos);
			gout.write(input);
			gout.finish();
			return baos.toByteArray();
		}
	}

	private String decompressData(byte[] input) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		GZIPInputStream gin = new GZIPInputStream(new ByteArrayInputStream(input));
		byte[] buffer = new byte[1 << 10];
		int i = 0;
		while ((i = gin.read(buffer)) != -1) {
			baos.write(buffer, 0, i);
		}
		return new String(baos.toByteArray(), "UTF-8");
	}

	/**
	 * Загрузить Paste по ключу и типу доступа
	 * 
	 * @param key
	 * @param typeAccess
	 * @return
	 * @throws Exception
	 *                       при любом исключении выходим в главный метод
	 */

	public Paste getPasteInfoByKey(String key, TypeAccess typeAccess) throws Exception {
		try (Jedis jedis = this.jedisPool.getResource()) {
			String strId = this.dataPasteByKey.get(jedis, typeAccess.getTableName(), key);
			if (strId == null) {
				return null;
			}
			return this.getPasteInfoByID(Long.parseLong(strId));
		}
	}

	/**
	 * Загрузить Paste по ид из базы
	 * 
	 * @param id
	 * @return
	 * @throws Exception
	 *                       при любом исключении выходим в главный метод
	 */

	public Paste getPasteInfoByID(long id) throws Exception {
		try (Jedis jedis = this.jedisPool.getResource()) {
			Paste paste = this.gson.fromJson(this.dataPasteInfoById.get(jedis, id), Paste.class);
			if (this.checkPasteExpiry(paste)) {
				return null;
			}
			byte[] data = this.dataPasteDataById.getByteArray(jedis, paste.getId());
			paste.setData(paste.isCompressed() ? this.decompressData(data) : new String(data, "UTF-8"));
			return paste;
		}
	}

	private boolean checkPasteExpiry(Paste paste) {
		if (paste.getExpiry() != -1 && System.currentTimeMillis() > paste.getExpiry()) {
			try (Jedis jedis = this.jedisPool.getResource()) {
				for (TypeAccess typeAccess : TypeAccess.values()) {
					this.dataPasteByKey.del(jedis, typeAccess.getTableName(), paste.getKey(typeAccess));
				}
				this.dataPasteInfoById.del(jedis, paste.getId());
				this.dataPasteDataById.del(jedis, paste.getId());
				return true;
			}
		}
		return false;
	}

	public long getExpiryTimeById(int id) {
		switch (id) {
			case -1:
				return -1;
			case 0:
				return TimeUnit.MINUTES.toMillis(10);
			case 1:
				return TimeUnit.DAYS.toMillis(1);
			case 2:
				return TimeUnit.DAYS.toMillis(7);
			case 3:
				return TimeUnit.DAYS.toMillis(14);
			case 4:
				return TimeUnit.DAYS.toMillis(31);
			default:
				return 0;
		}
	}

	public List<Paste> getLastTenPublicPaste() {
		return this.lastTenPaste;
	}

	class LimitedList<T> extends LinkedList<T> {

		private static final long serialVersionUID = 8756322626451363633L;

		private int limit;

		public LimitedList(int limit) {
			this.limit = limit;
		}

		@Override
		public boolean add(T e) {
			if (this.limit < this.size() + 1 && this.size() != 0) {
				this.removeLast();
			}
			return super.add(e);
		}
	}

}
