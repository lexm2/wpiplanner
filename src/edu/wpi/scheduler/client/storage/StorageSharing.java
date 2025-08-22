package edu.wpi.scheduler.client.storage;

import com.google.gwt.user.client.Window;

import edu.wpi.scheduler.client.Scheduler;
import edu.wpi.scheduler.client.controller.SchedulePermutation;
import edu.wpi.scheduler.shared.model.Section;

public class StorageSharing {

	public static native void console(String text)
	/*-{
	    console.log(text);
	}-*/;

	public static String getShareCode(SchedulePermutation permutation) {
		assert (permutation.solutions.size() == 0);

		String output = "01"; // Version

		for (Section section : permutation.sections) {
			output += getCrnHex(section.crn);
		}

		// Storage localStorage = Storage.getLocalStorageIfSupported();
		//
		// String output = ""; // Version
		//
		// if (localStorage != null) {
		// output = localStorage.getItem("savedCourse");
		// }

		return output;
	}

	private static String getCrnHex(long crn) {
		String hex = Long.toHexString(crn).toUpperCase();

		while (hex.length() < 18)
			hex = "0" + hex;

		if (hex.length() > 18)
			Window.alert("Error! CRN is too large" + crn);

		return hex;

	}

	public static SchedulePermutation getPermutation(String code) {
		assert (code.substring(0, 2).equals("01"));

		SchedulePermutation permutation = new SchedulePermutation();

		for (int i = 2; i < code.length(); i += 18) {
			long crn = Long.parseLong(code.substring(i, i + 18), 16);

			Section section = Scheduler.getDatabase().getSectionByCRN(crn);

			if (section != null)
				permutation.sections.add(section);
		}

		return permutation;
	}
}
