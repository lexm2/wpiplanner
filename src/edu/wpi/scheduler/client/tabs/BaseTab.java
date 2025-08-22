package edu.wpi.scheduler.client.tabs;

import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Widget;

import edu.wpi.scheduler.client.controller.StudentSchedule;

public abstract class BaseTab extends Anchor {

	public final StudentSchedule studentSchedule;
	
	public BaseTab(StudentSchedule studentSchedule, String name, String description) {
		this(studentSchedule, name);
		setTitle(description);
	}

	public BaseTab(StudentSchedule studentSchedule, String name) {
		super(name, false);
		this.studentSchedule = studentSchedule;

		setText(name);
		setHeight("100%");
		setStyleName("sched-NavTab");
		
		// Prevent default anchor behavior
		getElement().setAttribute("href", "javascript:void(0)");
	}

	public abstract Widget getBody();
	
	public void updateView() {
		
	}

}
