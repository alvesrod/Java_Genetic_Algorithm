package scheduler;

import java.util.ArrayList;

/**
 * A class to represent university classes
 * 
 * @author Erik Peter Zawadzki
 */
public class Course {

	private String courseName;

	private ArrayList<Student> students;

	/**
	 * The class prefixes. So stuff like "CPSC". This is so the generator creates courses with nice names like "CPSC 322" and so forth.
	 */
	public static final String[] classPrefixes = {"CPSC","MATH","PHIL","EECE","CHEM","ECON","PHYS","COGS"};

	/**
	 * Returns the name of the course, e.g. "CPSC 322"
	 * 
	 * @return courseName, the name of the course
	 */
	public String getCourseName() {

		return courseName;
	}

	/**
	 * Constructs the Course object.
	 * 
	 * @param pName the name of the class, e.g. "MATH 200"
	 */
	public Course(String pName) {

		courseName = pName;
		students = new ArrayList<Student>();

	}

	public String toString() {
		return courseName;
	}

	/**
	 * Enrolls a student in this course.
	 * 
	 * @param pS the student in question
	 */
	public void enroll(Student pS) {
		students.add((Student) pS);
	}

	/**
	 * Gets a list of all the students in the class.
	 * 
	 * @return the list of students
	 */
	public ArrayList getStudentList() {
		return (ArrayList) students.clone();
	}
}
