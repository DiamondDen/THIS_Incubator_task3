package net.diamondden.THIS_Incubator_task3.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.diamondden.THIS_Incubator_task3.Tools;
import net.diamondden.THIS_Incubator_task3.data.DataManager;
import net.diamondden.THIS_Incubator_task3.data.Paste;
import net.diamondden.THIS_Incubator_task3.data.Paste.TypeAccess;

@RestController
@SpringBootApplication
public class WebManager {

	private static DataManager dataManager;

	@RequestMapping(value = "/list", method = RequestMethod.GET, produces = "application/json")
	public String lastTenPublicPaste() {
		JsonObject json = new JsonObject();
		try {
			JsonObject response = new JsonObject();
			JsonArray array = new JsonArray();
			dataManager.getLastTenPublicPaste().forEach(paste -> array.add(paste.getKey(TypeAccess.PUBLIC)));
			response.add("list", array);
			json.add("response", response);
		} catch (Exception e) {
			e.printStackTrace();
			Tools.jsonPatternError(json, 0, "Internal error");
		}
		return json.toString();
	}

	@RequestMapping(value = "/pub/{key}", method = RequestMethod.GET, produces = "application/json")
	public String publicPaste(@PathVariable String key) {
		return this.getPaste(key, TypeAccess.PUBLIC);
	}

	@RequestMapping(value = "/unl/{key}", method = RequestMethod.GET, produces = "application/json")
	public String unlistedPaste(@PathVariable String key) {
		return this.getPaste(key, TypeAccess.UNLISTED);
	}

	private String getPaste(String key, TypeAccess typeAccess) {
		JsonObject json = new JsonObject();
		try {
			Paste paste = dataManager.getPasteInfoByKey(key, typeAccess);
			if (paste == null || paste.getAccess() != typeAccess) {
				Tools.jsonPatternError(json, 10, "Paste not found");
			} else {
				JsonObject response = new JsonObject();
				response.addProperty("data", paste.getData());
				json.add("response", response);
			}
		} catch (Exception e) {
			e.printStackTrace();
			Tools.jsonPatternError(json, 0, "Internal error");
		}
		return json.toString();
	}

	@PostMapping(value = "/document")
	public String add(@RequestBody String body, @RequestParam(name = "typeAccess", defaultValue = "0") int typeAccess,
			@RequestParam(name = "expiry", defaultValue = "-1") int expiryId) {
		JsonObject json = new JsonObject();
		l0: try {
			Paste paste = new Paste();

			paste.setData(body);
			paste.setDate(System.currentTimeMillis());
			long expiryTime = dataManager.getExpiryTimeById(expiryId);
			paste.setExpiry(expiryTime == -1 ? -1 : System.currentTimeMillis() + expiryTime);
			try {
				paste.setAccess(TypeAccess.values()[typeAccess]);
			} catch (Exception e) {
				Tools.jsonPatternError(json, 20, "TypeAccess is invalid");
				break l0;
			}
			dataManager.addPaste(paste);

			JsonObject response = new JsonObject();
			response.addProperty("type", paste.getAccess().name());
			response.addProperty("key", paste.getKey(paste.getAccess()));
			json.add("response", response);
		} catch (Exception e) {
			e.printStackTrace();
			Tools.jsonPatternError(json, 0, "Internal error");
		}
		return json.toString();
	}

	public static void initApplication(DataManager dMng) {
		dataManager = dMng;
		SpringApplication.run(WebManager.class, new String[0]);
	}

}
