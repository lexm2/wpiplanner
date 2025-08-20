package edu.wpi.scheduler.client.courseselection;

import java.util.Comparator;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.WidgetCollection;

import edu.wpi.scheduler.client.controller.HasCourse;
import edu.wpi.scheduler.client.Scheduler;
import edu.wpi.scheduler.shared.model.Course;
import edu.wpi.scheduler.shared.model.Department;
import edu.wpi.scheduler.shared.model.Section;

import java.util.List;

public class CourseList extends ComplexPanel {

	private final CourseSelectionController selectionController;

	public static class CourseComparator implements Comparator<HasCourse> {

		@Override
		public int compare(HasCourse o1, HasCourse o2) {
			Course course1 = o1.getCourse();
			Course course2 = o2.getCourse();

			return course1.number.compareToIgnoreCase(course2.number);
		}

	}

	public static final CourseComparator comparator = new CourseComparator();
	
	public static final String NoSeatWarning = "<span style=\"color: red; font-weight: bold;\" title=\"There are no seats left.\">&#9888;</span>";
	public static final String NoSeatButWaitlistWarning = "<span style=\"color: blue; font-weight: bold;\" title=\"There are no seats left, but there are spots left on the waitlist.\">&#9888;</span>";

	public CourseList(CourseSelectionController selectionController) {
		this.setElement(Document.get().createTableElement());
		this.selectionController = selectionController;

		this.setStyleName("courseList");
	}

	public void addDeparment(Department department) {
		addDeparment(department, "");
	}

	public void addDeparment(Department department, String searchTerm) {
		for (Course course : department.courses) {
			if (!matchesSearchTerm(course, searchTerm)) {
				continue;
			}

			CourseListItemBase item = new CourseListItemBase(selectionController, course);

			//String name = fixCase(course.name);  Capitalization handled by Workday now. 
			String name = course.name;

			if (!course.hasAvailableSeats()) {
				if (course.hasAvailableWaitlist()) name = NoSeatButWaitlistWarning + " " + name;
				else name = NoSeatWarning + " " + name;
			}

			item.add("128px", new TermView(course));
			item.add(null, name);

			this.add(item);
		}
	}

	public boolean matchesSearchTerm(Course course, String searchTerm) {
		if (searchTerm == null || searchTerm.isEmpty()) {
			return true;
		}

		String courseName = course.name.toLowerCase();
		String courseNumber = course.number.toLowerCase();
		String departmentAbbrev = course.department.abbreviation.toLowerCase();
		String courseAbbrev = course.toAbbreviation().toLowerCase();

		return courseName.contains(searchTerm) || 
		       courseNumber.contains(searchTerm) || 
		       departmentAbbrev.contains(searchTerm) ||
		       courseAbbrev.contains(searchTerm);
	}

	public List<Department> getAllDepartments() {
		return Scheduler.getDatabase().departments;
	}

	public void add(CourseListItemBase child) {
		WidgetCollection children = getChildren();

		for (int i = 0; i < children.size(); i++) {
			if (comparator.compare(child, (HasCourse) children.get(i)) <= 0) {
				this.insert(child, this.getElement(), i, true);
				return;
			}
		}

		// Could not find a place to insert, put it at the end!
		this.add(child, this.getElement());
	}

	public static String fixCase(String input) {
		RegExp reg = RegExp.compile("^I+V*$");
		// Break it into words
		String strings[] = input.split("\\s+");
		for (int i = 0; i < strings.length; i++) {
			// If its I, II, etc, just leave it uppercase
			if (!reg.test(strings[i])) {
				// Make it all lowercase
				strings[i] = strings[i].toLowerCase();
				// If it should be capitalized, capitalize it
				if (shouldBeCapitalized(strings[i])) {
					if (strings[i].length() > 1)
						strings[i] = Character.toUpperCase(strings[i].charAt(0)) + strings[i].substring(1);
					else
						strings[i] = Character.toUpperCase(strings[i].charAt(0)) + "";
				}
			}

		}
		String returnstring = strings[0];
		for (int i = 1; i < strings.length; i++) {
			returnstring = returnstring + " " + strings[i];
		}
		return returnstring;
	}

	public static boolean shouldBeCapitalized(String s) {
		if (s.compareTo("a") == 0 || s.compareTo("and") == 0 || s.compareTo("the") == 0 || s.compareTo("in") == 0
				|| s.compareTo("an") == 0 || s.compareTo("or") == 0 || s.compareTo("at") == 0 || s.compareTo("of") == 0)
			return false;
		return true;
	}

}
