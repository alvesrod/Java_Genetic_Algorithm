package scheduler;

/**
 * This particular class stores all the information needed to describe an exam scheduling problem.
 * 
 * @author Erik Peter Zawadzki
 * 
 */

public class SchedulingProblem {

	private final int examPeriod = 5;

	private Course[] courseList;

	private Room[] roomList;

	private Student[] studentList;

	/**
	 * Sets up a scheduling problem. Maybe be done manually, but you probably want to generate one of these using the Generator class.
	 */
	public SchedulingProblem(Course[] pCList, Room[] pRList, Student[] pSList) {
		courseList = pCList;
		roomList = pRList;
		studentList = pSList;
	}

	/**
	 * The number of days in the exam period. This number will always be 5.
	 */
	public int getExamPeriod() {
		return examPeriod;
	}

	public Course[] getCourseList() {
		return courseList;
	}

	public Room[] getRoomList() {
		return roomList;
	}

	public Student[] getStudentList() {
		return studentList;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Courses:");
		for (int i = 0; i < courseList.length; i++) {
			sb.append("\n\t" + courseList[i].toString());
		}
		sb.append("\nRooms:");
		for (int i = 0; i < roomList.length; i++) {
			sb.append("\n\t" + roomList[i].toString());
		}
		sb.append("\nStudents:");
		for (int i = 0; i < studentList.length; i++) {
			sb.append("\n\t" + studentList[i].toString());
		}
		return sb.toString();
	}

}
