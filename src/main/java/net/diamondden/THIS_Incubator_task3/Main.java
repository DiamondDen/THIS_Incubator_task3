package net.diamondden.THIS_Incubator_task3;

import net.diamondden.THIS_Incubator_task3.data.DataManager;
import net.diamondden.THIS_Incubator_task3.web.WebManager;

public class Main {

	private DataManager dataManager;

	public Main() {
		try {
			this.dataManager = new DataManager();
			WebManager.initApplication(this.dataManager);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new Main();
	}

}
