package edu.wpi.scheduler.client.permutation;

import java.util.List;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.TextAlign;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.Widget;

import edu.wpi.scheduler.client.IncomingAnimation;
import edu.wpi.scheduler.client.controller.FavoriteEvent;
import edu.wpi.scheduler.client.controller.FavoriteEventHandler;
import edu.wpi.scheduler.client.controller.SchedulePermutation;
import edu.wpi.scheduler.client.controller.StudentSchedule;
import edu.wpi.scheduler.client.generator.ProducerUpdateEvent.UpdateType;
import edu.wpi.scheduler.client.generator.ScheduleProducer;
import edu.wpi.scheduler.client.generator.ScheduleProducer.ProducerEventHandler;
import edu.wpi.scheduler.shared.model.DayOfWeek;
import edu.wpi.scheduler.shared.model.Period;
import edu.wpi.scheduler.shared.model.Section;
import edu.wpi.scheduler.shared.model.Term;
import edu.wpi.scheduler.shared.model.Time;

public class PermutationCanvasList extends FlowPanel implements
		TimeRangeChangEventHandler, ProducerEventHandler, ScrollHandler,
		FavoriteEventHandler, ClickHandler, PermutationSelectEventHandler {

	private PermutationController controller;
	private Canvas background;

	private final FlowPanel scheduleList = new FlowPanel();
	private final FlowPanel favoriteList = new FlowPanel();

	private final ScrollPanel scroll = new ScrollPanel(scheduleList);
	public final ToggleButton favoriteButton = new ToggleButton(
			"Saved Schedules (0)", this);
	
	private final FlowPanel buttonHeaderPanel = new FlowPanel();
	private Button shareButton;
	private ToggleButton starButton;

	public static final double favoriteButtonSize = 20.0;
	public static final double buttonHeaderHeight = 50.0;

	public PermutationCanvasList(PermutationController controller) {
		this.controller = controller;
		updateBackground();

		createHeaderButtons();
		add(buttonHeaderPanel);
		add(favoriteButton);
		favoriteButton.setStyleName("sched-Button");
		add(scroll);

		Style buttonHeaderStyle = buttonHeaderPanel.getElement().getStyle();
		Style favoriteStyle = favoriteButton.getElement().getStyle();
		Style scrollStyle = scroll.getElement().getStyle();

		// Button header at top
		buttonHeaderStyle.setPosition(Position.ABSOLUTE);
		buttonHeaderStyle.setTextAlign(TextAlign.CENTER);
		buttonHeaderStyle.setLeft(0.0, Unit.PX);
		buttonHeaderStyle.setRight(0.0, Unit.PX);
		buttonHeaderStyle.setTop(0.0, Unit.PX);
		buttonHeaderStyle.setHeight(buttonHeaderHeight, Unit.PX);
		buttonHeaderStyle.setProperty("display", "flex");
		buttonHeaderStyle.setProperty("alignItems", "center");
		buttonHeaderStyle.setProperty("justifyContent", "center");

		// Favorites button below button header
		favoriteStyle.setPosition(Position.ABSOLUTE);
		favoriteStyle.setTextAlign(TextAlign.CENTER);
		favoriteStyle.setLeft(0.0, Unit.PX);
		favoriteStyle.setRight(0.0, Unit.PX);
		favoriteStyle.setTop(buttonHeaderHeight, Unit.PX);
		favoriteStyle.setHeight(favoriteButtonSize, Unit.PX);

		// Scroll panel below both headers
		scrollStyle.setPosition(Position.ABSOLUTE);
		scrollStyle.setLeft(0.0, Unit.PX);
		scrollStyle.setRight(0.0, Unit.PX);
		scrollStyle.setTop(120.0, Unit.PX);
		scrollStyle.setBottom(0.0, Unit.PX);
		scrollStyle.setOverflowX(Overflow.HIDDEN);
		scrollStyle.setOverflowY(Overflow.SCROLL);

		scroll.addScrollHandler(this);
	}

	private void createHeaderButtons() {
		// Create share button
		shareButton = new Button("Share", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				final DecoratedPopupPanel simplePopup = new DecoratedPopupPanel(true);
				simplePopup.ensureDebugId("cwBasicPopup-simplePopup");
				simplePopup.setWidth("200px");
				simplePopup.setWidget(new ShareWidget(controller.getSelectedPermutation()));

				Widget source = (Widget) event.getSource();
				int left = source.getAbsoluteLeft() + 10 - 200;
				int top = source.getAbsoluteTop() + 10;
				simplePopup.setPopupPosition(left, top);
				simplePopup.getElement().getStyle().setZIndex(5);

				simplePopup.show();
			}
		});
		shareButton.setStyleName("sched-Button");

		// Create star button (individual favorite toggle)
		starButton = new ToggleButton("Save", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				StudentSchedule studentSchedule = controller.getStudentSchedule();
				SchedulePermutation permutation = controller.getSelectedPermutation();

				if (permutation == null)
					return;

				if (!studentSchedule.containsFavorite(permutation))
					studentSchedule.addFavorite(permutation);
				else
					studentSchedule.removeFavorite(permutation);
			}
		});
		starButton.setStyleName("sched-Button");

		// Add only Share and Star buttons to button header panel
		buttonHeaderPanel.add(shareButton);
		buttonHeaderPanel.add(starButton);

		// Style buttons for inline display
		shareButton.getElement().getStyle().setProperty("display", "inline-block");
		shareButton.getElement().getStyle().setMarginRight(5, Unit.PX);
		
		starButton.getElement().getStyle().setProperty("display", "inline-block");
		starButton.getElement().getStyle().setMarginRight(5, Unit.PX);
	}

	protected Canvas addPermutation(final SchedulePermutation permutation,
			FlowPanel panel) {

		Canvas canvas = Canvas.createIfSupported();
		canvas.setCoordinateSpaceHeight(background.getCoordinateSpaceHeight());
		canvas.setCoordinateSpaceWidth(background.getCoordinateSpaceWidth());
		canvas.setStyleName("permutationCanvas");
		StudentSchedule schedule = controller.getStudentSchedule();

		Context2d context = canvas.getContext2d();

		double columnWidth = ((double) canvas.getCoordinateSpaceWidth())
				/ controller.getValidDaysOfWeek().size();
		double hourHeight = ((double) canvas.getCoordinateSpaceHeight())
				/ (schedule.getEndHour() - schedule.getStartHour());
		List<DayOfWeek> daysOfWeek = controller.getValidDaysOfWeek();

		// context.setFillStyle("black");
		context.drawImage(background.getCanvasElement(), 0.0, 0.0);

		// So many For loops... But it is not that bad.
		for (Section section : permutation.sections) {
			context.setFillStyle(controller.getCourseColor(section.course));

			for (Term term : section.getTerms()) {
				for (int i = 0; i < daysOfWeek.size(); i++) {
					for (Period period : section.periods) {
						if (period.days.contains(daysOfWeek.get(i))) {

							double start = getDayProgress(period.startTime);
							double end = getDayProgress(period.endTime);

							context.fillRect(i * columnWidth + columnWidth
									* term.ordinal() * 0.25,
									start * hourHeight, columnWidth * 0.25,
									(end - start) * hourHeight);
						}
					}
				}
			}
		}

		panel.add(canvas);

		canvas.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				controller.selectPermutation(permutation);
			}
		});

		new IncomingAnimation(canvas.getElement()).run();

		return canvas;
	}

	private double getDayProgress(Time time) {
		return ((double) time.hour) - controller.getStudentSchedule().getStartHour()
				+ ((double) time.minutes) / 60.0;
	}

	/**
	 * If we are near the end of the scroll, we are going to check if there are
	 * any more images to be loaded
	 */
	@Override
	public void onScroll(ScrollEvent event) {
		if (scroll.getVerticalScrollPosition() + 300 < scroll
				.getMaximumVerticalScrollPosition())
			return;

		updateThumbnails();
	}

	@Override
	protected void onLoad() {
		StudentSchedule schedule = controller.getStudentSchedule();

		schedule.addTimeChangeListner(this);
		schedule.addFavoriteHandler(this);
		controller.addProduceHandler(this);
		controller.addSelectListner(this);
		updateBackground();
		scheduleList.clear();
		updateThumbnails();
		update();
	}

	@Override
	protected void onUnload() {
		StudentSchedule schedule = controller.getStudentSchedule();

		schedule.removeTimeChangeListner(this);
		schedule.removeFavoriteHandler(this);
		controller.removeProduceHandler(this);
		controller.removeSelectListner(this);
	}

	@Override
	public void onTimeRangeChange(TimeRangeChangeEvent timeRangeChangeEvent) {
		this.updateBackground();
	}

	public void updateBackground() {
		background = Canvas.createIfSupported();

		background.getElement().setAttribute("width", "150px");
		background.getElement().setAttribute("height", "150px");
		Context2d context = background.getContext2d();
		StudentSchedule schedule = controller.getStudentSchedule();

		double heightPerHour = 150.0 / (schedule.getEndHour() - schedule
				.getStartHour());

		context.setLineWidth(1.0);

		for (double hour = schedule.getStartHour(); hour <= schedule
				.getEndHour(); hour += 1.0) {
			// Draw in the middle:
			// http://stackoverflow.com/questions/9311428/draw-single-pixel-line-in-html5-canvas
			double yValue = Math.floor((hour - schedule.getStartHour())
					* heightPerHour) + 0.5;

			if (hour == 12.0) {
				context.setStrokeStyle(CssColor.make(200, 200, 200));
			} else {
				context.setStrokeStyle(CssColor.make(230, 230, 230));
			}

			context.beginPath();
			context.moveTo(0.0, yValue);
			context.lineTo(150.0, yValue);
			context.stroke();
		}

		double weekDaySize = (double) controller.getValidDaysOfWeek().size();

		for (double i = 1.0; i < weekDaySize; i += 1.0) {
			context.beginPath();
			context.moveTo(i * (150 / weekDaySize), 0.0);
			context.lineTo(i * (150 / weekDaySize), 150.0);
			context.stroke();
		}
	}

	public boolean onFavorites() {
		return favoriteButton.isDown();
	}

	@Override
	public void onPermutationUpdated(UpdateType type) {
		update();

		if (onFavorites())
			return;

		if (type == UpdateType.NEW)
			scheduleList.clear();

		if (scheduleList.getWidgetCount() < 20)
			updateThumbnails();

		if (type == UpdateType.FINISH) {
			ScheduleProducer producer = controller.getProducer();

			if (producer.getPermutations().size() == 0) {
				scheduleList.clear();
				scheduleList.add(new Label("Unable to find schedules..."));
			}
		}
	}

	public void updateThumbnails() {
		List<SchedulePermutation> permutations = controller.getProducer()
				.getPermutations();
		int thumbSize = scheduleList.getWidgetCount();
		int permutationSize = permutations.size();

		if (permutationSize <= thumbSize)
			return;

		int limit = Math.min(thumbSize + 20, permutationSize);

		for (int i = thumbSize; i < limit; i++) {
			addPermutation(permutations.get(i), scheduleList);
		}
	}

	@Override
	public void onClick(ClickEvent event) {
		update();
	}

	private void update() {
		favoriteButton.setText("Saved Schedules ("
				+ controller.getStudentSchedule().favoritePermutations.size()
				+ ")");

		// Update star button
		SchedulePermutation permutation = controller.getSelectedPermutation();
		if (permutation != null) {
			StudentSchedule studentSchedule = controller.getStudentSchedule();
			boolean containsFavorite = studentSchedule.containsFavorite(permutation);
			starButton.setDown(containsFavorite);
			starButton.setHTML(containsFavorite ? "&#9733; Saved" : "Save");
		}

		if (onFavorites()) {
			scroll.setWidget(favoriteList);
		} else {
			scroll.setWidget(scheduleList);
		}
	}

	@Override
	public void onFavoriteUpdate(FavoriteEvent favoriteEvent) {
		update();

		favoriteList.clear();

		for (SchedulePermutation permutation : controller.getStudentSchedule().favoritePermutations)
			addPermutation(permutation, favoriteList);
	}

	@Override
	public void onPermutationSelected(PermutationSelectEvent permutation) {
		update();
	}

}
