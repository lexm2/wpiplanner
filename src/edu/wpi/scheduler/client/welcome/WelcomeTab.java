package edu.wpi.scheduler.client.welcome;

import com.google.gwt.user.client.ui.Widget;

import edu.wpi.scheduler.client.controller.StudentSchedule;
import edu.wpi.scheduler.client.tabs.BaseTab;

public class WelcomeTab extends BaseTab {
	Widget welcomeView;

	public WelcomeTab(StudentSchedule studentSchedule) {
		super(studentSchedule, "Info", "Description on how to use the scheduler");
		this.setEnabled(true);
		welcomeView = new WelcomeView();

	}

	@Override
	public Widget getBody() {
		return welcomeView;
	}
}
