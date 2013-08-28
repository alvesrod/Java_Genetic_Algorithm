package scheduler;

/**
 * This class stores the scheduling information for one exam. A solution to a particular instance is an array of these with a ScheduleChoice for every
 * exam
 * 
 * @author Erik Peter Zawadzki
 * 
 */
public class ScheduleChoice {
	public static String[] times = {"8:30","12:00","3:30","7:00"};

	private Room room;

	private Course course;

	private int timeSlot;

	private int day;

	public int getDay() {
		return day;
	}

	public int getTimeSlot() {
		return timeSlot;
	}

	public Room getRoom() {
		return room;
	}

	public Course getCourse() {
		return course;
	}

	/**
	 * Changes the TimeSlot and Day for ScheduleChoice.
	 * 
	 * @param pDay the day of the exam
	 * @param pTime the time slot. 0 is 8:30, 1 is 12:00, 2 is 3:30, 3 is 7:00
	 * 
	 */
	public void setTime(int pDay, int pTime) {
		System.out.println("Changing (" + day + ", " + timeSlot + ") to (" + pDay + ", " + pTime + ")");
		day = pDay;
		timeSlot = pTime;
	}

	/**
	 * Represents an scheduling decision about a course
	 * 
	 * @param pCor the course to be scheduled
	 * @param pRm the room the exam will be in
	 * @param pDy the day of the exam
	 * @param pTm the time slot. 0 is 8:30, 1 is 12:00, 2 is 3:30, 3 is 7:00
	 * 
	 */
	public ScheduleChoice(Course pCor, Room pRm, int pDy, int pTm) {
		course = pCor;
		room = pRm;
		day = pDy;

		if (pTm < 0 || pTm >= times.length) {
			timeSlot = 0;
			System.err.println(course.getCourseName() + " is at an impossible time, " + pTm + ", defaulting to 8:30 exam");
		} else {
			timeSlot = pTm;
		}
	}

	public ScheduleChoice(ScheduleChoice s) {
		course = s.getCourse();
		room = s.getRoom();
		day = s.getDay();
		timeSlot = s.getTimeSlot();
	}

	public String toString() {
		return course.getCourseName() + " in " + room.getRoomName() + " on the " + day + " at " + times[timeSlot];
	}
}
