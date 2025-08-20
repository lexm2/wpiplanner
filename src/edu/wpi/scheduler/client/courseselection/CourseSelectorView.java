/**
 * 
 */
package edu.wpi.scheduler.client.courseselection;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.TextBox;

import edu.wpi.scheduler.client.IncomingAnimation;
import edu.wpi.scheduler.client.controller.StudentSchedule;
import edu.wpi.scheduler.shared.model.Course;
import edu.wpi.scheduler.shared.model.Department;

/**
 * @author Nican
 * 
 */
public class CourseSelectorView extends Composite implements
		CourseSelectedEventHandler, ChangeHandler, KeyUpHandler, ClickHandler {

	private static CourseSelectorViewUiBinder uiBinder = GWT
			.create(CourseSelectorViewUiBinder.class);

	interface CourseSelectorViewUiBinder extends
			UiBinder<DockLayoutPanel, CourseSelectorView> {
	}

	final CourseSelectionController selectionController;

	@UiField(provided = true)
	DepartmentListBox departmentList;

	@UiField(provided=true)
	CourseList courseList;

	@UiField
	CourseDescriptionInfo courseDescription;

	@UiField(provided = true)
	CourseSelection courseSelection;

	@UiField
	TextBox searchBox;

	@UiField
	CheckBox selectedDepartmentsOnly;

	/**
	 * Because this class has a default constructor, it can be used as a binder
	 * template. In other words, it can be used in other *.ui.xml files as
	 * follows: <ui:UiBinder xmlns:ui="urn:ui:com.google.gwt.uibinder"
	 * xmlns:g="urn:import:**user's package**">
	 * <g:**UserClassName**>Hello!</g:**UserClassName> </ui:UiBinder> Note that
	 * depending on the widget that is used, it may be necessary to implement
	 * HasHTML instead of HasText.
	 * 
	 * @param studentSchedule
	 */
	public CourseSelectorView(StudentSchedule studentSchedule) {
		selectionController = new CourseSelectionController(studentSchedule);
		courseSelection = new CourseSelection(selectionController);
		departmentList = new DepartmentListBox(studentSchedule);
		courseList = new CourseList(selectionController);

		initWidget(uiBinder.createAndBindUi(this));

		getElement().getStyle().setLeft(0, Unit.PX);
		getElement().getStyle().setRight(0, Unit.PX);
		getElement().getStyle().setTop(0, Unit.PX);
		getElement().getStyle().setBottom(0, Unit.PX);
		getElement().getStyle().setPosition(Position.ABSOLUTE);

		selectionController.addCourseSelectedListner(this);
		
		departmentList.update();
		departmentList.addChangeHandler(this);
		searchBox.addKeyUpHandler(this);
		selectedDepartmentsOnly.setValue(false);
		selectedDepartmentsOnly.addClickHandler(this);
		updateCourseList();
	}

	/**
	 * Choose the new department from the dropdown menu, and display the
	 * corresponding body.
	 * 
	 * @param department
	 */
	public void updateCourseList() {
		// Clear the body from any existing elements
		courseList.clear();
		
		String searchTerm = searchBox.getText().trim().toLowerCase();
		List<Department> departments;
		
		if (selectedDepartmentsOnly.getValue()) {
			// Search only selected departments (original behavior)
			departments = departmentList.getSelectedDepartments();
		} else {
			// Global search across all departments
			departments = courseList.getAllDepartments();
		}
		
		for( Department department : departments ){
			courseList.addDeparment(department, searchTerm);
		}
		
		// Only try to select a course if we have departments and the first department has courses
		// and there are actually visible courses in the list after filtering
		if( departments.size() > 0 && selectionController.getSelectedCourse() == null ){
			// Find the first department with courses that match the search filter
			for (Department dept : departments) {
				if (dept.courses.size() > 0) {
					// Find first course in this department that matches the search
					for (Course course : dept.courses) {
						if (courseList.matchesSearchTerm(course, searchTerm)) {
							selectionController.selectCourse(course);
							break;
						}
					}
					if (selectionController.getSelectedCourse() != null) {
						break;
					}
				}
			}
		}
		
		new IncomingAnimation( courseList.getElement() ).run();
	}

	/**
	 * Called when a course is selected in the main view
	 */
	@Override
	public void onCourseSelected(CourseSelectedEvent event) {
		// Update the description box with the latest course
		courseDescription.setCourse(event.getCourse());
	}

	@Override
	public void onChange(ChangeEvent event) {
		updateCourseList();
	}

	@Override
	public void onKeyUp(KeyUpEvent event) {
		updateCourseList();
	}

	@Override
	public void onClick(ClickEvent event) {
		updateCourseList();
	}

}
