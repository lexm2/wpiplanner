package edu.wpi.scheduler.client.permutation;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.TextBox;

import edu.wpi.scheduler.client.controller.SchedulePermutation;
import edu.wpi.scheduler.client.storage.StorageSharing;

public class ShareWidget extends ComplexPanel {

	public ShareWidget(SchedulePermutation permutation) {
		setElement(Document.get().createDivElement());
		getElement().getStyle().setWidth(200, Unit.PX);

		TextBox textbox = new TextBox();
		String currentURL = Document.get().getURL();

		// check to see if an old share code is already in the URL. If so, strip it out
		// to replace with new share code
		if (currentURL.contains("?share=")) {
			textbox.setValue(currentURL.substring(0, currentURL.indexOf("?")) + "?share="
					+ StorageSharing.getShareCode(permutation));
		} else {
			textbox.setValue(currentURL + "?share=" + StorageSharing.getShareCode(permutation));
		}
		// textbox.setValue(Document.get().getURL() + "?share=" +
		// StorageSharing.getShareCode(permutation));
		textbox.getElement().getStyle().setWidth(95, Unit.PCT);

		add(textbox, getElement());
	}

}
