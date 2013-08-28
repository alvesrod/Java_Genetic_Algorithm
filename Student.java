package scheduler;

/**
 * A class to represent university students. The students are simply names associate with a list of five classes. 
 * 
 * @author Erik Peter Zawadzki
 */
public class Student {

	/**
	 * An array of first names
	 */
	public static String[] firstNames = {"JAMES","JOHN","ROBERT","MARY","MICHAEL","WILLIAM","DAVID","CAROL","RICHARD","CHARLES","JOSEPH","THOMAS",
			"PATRICIA","SANDRA","LEANNE","LINDA","CHRISTOPHER","BARBARA","DANIEL","PAUL","MARK","ELIZABETH","JENNIFER","LIN","MARIA"};

	/**
	 * An array of last names
	 */
	public static String[] lastNames = {"JOHNSON","WILLIAMS","JONES","BROWN","DAVIS","MILLER","YANG","WILSON","MOORE","WONG","TAYLOR","WU",
			"ANDERSON","SHEN","THOMAS","JACKSON","CHAN","LUM","WHITE","HARRIS","TSAI","MARTIN","THOMPSON","GARCIA","MARTINEZ","KAO","ROBINSON "};

	private String studentName;

	/**
	 * The number of courses that all students take
	 */
	public static final int scheduleSize = 5;

	private Course[] schedule;

	/**
	 * Returns the name of the student
	 * 
	 * @return studentName, the name of the student
	 */
	public String getStudentName() {

		return studentName;
	}

	public Course getCourse(int i) {
		return schedule[i];
	}

	public Course[] getCourses() {
		return (Course[]) schedule.clone();
	}

	/**
	 * Checks whether a student goes to a particular course.
	 * 
	 * @param pC the course in question
	 * @return true if the student goes to the course
	 * 
	 */
	public boolean goesTo(Course pC) {

		for (int i = 0; i < schedule.length; i++) {
			if (pC.getCourseName().compareTo(schedule[i].getCourseName()) == 0) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Constructs a Student object.
	 * 
	 * @param pName the name of the room (e.g. DMP 101)
	 * @param pSch the student's schedule
	 */
	public Student(String pName, Course[] pSch) {

		studentName = pName;
		schedule = pSch;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(studentName + " takes ");
		for (int i = 0; i < schedule.length - 1; i++) {
			sb.append(schedule[i].getCourseName() + ", ");
		}
		sb.append("and " + schedule[schedule.length - 1].getCourseName());
		return sb.toString();
	}

}
