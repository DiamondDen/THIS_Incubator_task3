package net.diamondden.THIS_Incubator_task3.data;

import java.util.EnumMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Paste {

	/**
	 * id - Уникальный ид "пасты"
	 * date - Дата когда была созданна "паста"
	 * expiry - Когда истечёт срок действия
	 * keys - Ключи для разных типов доступа
	 * data - Текст "пасты"
	 * compressed - Указывает на сжатность текста
	 */
	
	private long id, date, expiry;
	private TypeAccess access;
	private Map<TypeAccess, String> keys;
	private transient String data;
	private boolean compressed;
	
	public Paste() {
		this.keys = new EnumMap<>(TypeAccess.class);
	}
	
	/*
	 * Метод для добавления ключа
	 */
	public void setKey(TypeAccess typeAccess, String value) {
		this.keys.put(typeAccess, value);
	}
	
	/*
	 * Метод для получения ключа
	 */
	public String getKey(TypeAccess typeAccess) {
		return this.keys.get(typeAccess);
	}

	/*
	 * Доступные типы приватности
	 */
	@Getter
	@AllArgsConstructor
	public static enum TypeAccess {
		PUBLIC(
				"public_paste"
		), UNLISTED(
				"unlisted_paste"
		);

		private String tableName;
	}
	
}
