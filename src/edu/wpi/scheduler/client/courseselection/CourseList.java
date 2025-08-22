package edu.wpi.scheduler.client.courseselection;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.google.gwt.dom.client.Document;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.WidgetCollection;

import edu.wpi.scheduler.client.Scheduler;
import edu.wpi.scheduler.client.controller.HasCourse;
import edu.wpi.scheduler.shared.model.Course;
import edu.wpi.scheduler.shared.model.Department;

public class CourseList extends ComplexPanel {

	private final CourseSelectionController selectionController;
	private static final int RESULTS_PER_PAGE = 100;
	private int currentlyDisplayed = 0;
	private List<Course> remainingCourses = new ArrayList<Course>();
	private String currentSearchTerm = "";
	private List<Department> currentDepartments = new ArrayList<Department>();

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
		this.currentSearchTerm = searchTerm;
		List<Course> matchingCourses = new ArrayList<Course>();
		for (Course course : department.courses) {
			if (matchesSearchTerm(course, searchTerm)) {
				matchingCourses.add(course);
			}
		}

		addCoursesToDisplay(matchingCourses, searchTerm);
	}

	private void addCoursesToDisplay(List<Course> courses, String searchTerm) {
		for (Course course : courses) {
			if (currentlyDisplayed >= RESULTS_PER_PAGE) {
				remainingCourses.add(course);
				continue;
			}

			CourseListItemBase item = new CourseListItemBase(selectionController, course);

			// String name = fixCase(course.name); Capitalization handled by Workday now.
			String name = course.name;

			if (!course.hasAvailableSeats()) {
				if (course.hasAvailableWaitlist())
					name = NoSeatButWaitlistWarning + " " + name;
				else
					name = NoSeatWarning + " " + name;
			}

			item.add("128px", new TermView(course));
			item.add(null, name);

			this.add(item);
			currentlyDisplayed++;
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

	public void resetPagination() {
		currentlyDisplayed = 0;
		remainingCourses.clear();
	}

	public void loadMoreResults() {
		if (hasMoreResults()) {
			int toLoad = Math.min(RESULTS_PER_PAGE, remainingCourses.size());

			for (int i = 0; i < toLoad; i++) {
				Course course = remainingCourses.remove(0);

				CourseListItemBase item = new CourseListItemBase(selectionController, course);
				String name = course.name;

				if (!course.hasAvailableSeats()) {
					if (course.hasAvailableWaitlist())
						name = NoSeatButWaitlistWarning + " " + name;
					else
						name = NoSeatWarning + " " + name;
				}

				item.add("128px", new TermView(course));
				item.add(null, name);

				this.add(item);
				currentlyDisplayed++;
			}
		}
	}

	public boolean hasMoreResults() {
		return remainingCourses.size() > 0;
	}

	public int getRemainingCount() {
		return remainingCourses.size();
	}

	@Override
	public void clear() {
		super.clear();
		resetPagination();
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
		super.add(child, getElement());
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
