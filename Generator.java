package scheduler;

import java.util.Random;

/**
 * The generator class. Generates problems instances from a given parameterized distribution.
 * 
 * @author Erik Peter Zawadzki
 */

public class Generator {
	private int numCourses;

	private int numRooms;

	private int numStudents;

	private double crispness;

	Random r;

	/**
	 * Generator for scheduling problems. There are some things to bear in mind if you want to play with the parameters:
	 * <UL>
	 * <LI>Each student takes exactly 5 courses. Thus, the total number of courses must be higher than 5.
	 * <LI>There are five days in the exam period from 0 to 4 and four time slots per day, so there are 20 possible time slots. Thus, the number of
	 * courses must be smaller than 20 times the number of rooms, or ALL your schedules will be infeasible or incomplete.
	 * <LI>This generator creates a number of "Standard Schedules". "Crispness" is the chance that a student will stick to that schedule. So 1.0 will
	 * be a population of good little robots that don't deviate from the standards at all, and 0.0 will be a student body made up of independent free
	 * thinkers playing by nobody's rules.
	 * </UL>
	 * 
	 * @param pNumC the number of courses to generate
	 * @param pNumR the number of rooms to generate
	 * @param pNumS the number of students to generate
	 * @param pCrisp how homogeneous the student population is
	 * 
	 */
	public Generator(int pNumC, int pNumR, int pNumS, double pCrisp) {

		numCourses = pNumC;
		numRooms = pNumR;
		numStudents = pNumS;
		crispness = pCrisp;
		r = new Random();
	}

	/**
	 * Generates a scheduling problem instance
	 * 
	 * @param seed a seed for the random number. Consider System.currentTimeMillis()
	 * @return a random scheduling problem instance.
	 * 
	 */
	public SchedulingProblem generateProblem(long seed) {

		Course[] courseList = new Course[numCourses];
		Room[] roomList = new Room[numRooms];
		Student[] studentList = new Student[numStudents];
		r.setSeed(seed);
		for (int i = 0; i < numCourses; i++) {
			courseList[i] = generateCourse(courseList, i);
		}
		for (int i = 0; i < numRooms; i++) {
			roomList[i] = generateRoom(roomList, i);
		}
		for (int i = 0; i < numStudents; i++) {
			studentList[i] = generateStudent(courseList);
		}

		return new SchedulingProblem(courseList, roomList, studentList);

	}

	private Course generateCourse(Course[] courseList, int k) {
		String name = Course.classPrefixes[r.nextInt(Course.classPrefixes.length)] + " " + (r.nextInt(4) + 1) + this.zeroPad(r.nextInt(20), 2);
		/* Check for duplicate courses */
		for (int i = 0; i < k; i++) {
			/* Duplicate - Regenerate course */
			if (name.compareTo(courseList[i].getCourseName()) == 0) {
				name = Course.classPrefixes[r.nextInt(Course.classPrefixes.length)] + " " + (r.nextInt(4) + 1) + r.nextInt(100);
				i = -1;
			}
		}
		// System.out.println("Generating Course " + name);
		return new Course(name);
	}

	private Room generateRoom(Room[] roomList, int k) {
		String name = Room.buildingSIS[r.nextInt(Room.buildingSIS.length)] + " " + this.zeroPad(r.nextInt(5000), 4);
		/* Check for duplicate rooms */
		for (int i = 0; i < k; i++) {
			/* Duplicate, regeneate room */
			if (name.compareTo(roomList[i].getRoomName()) == 0) {
				name = Room.buildingSIS[r.nextInt(Room.buildingSIS.length)] + " " + (r.nextInt(400) + 100);
				i = -1;
			}
		}
		// System.out.println("Generating Room " + name);
		return new Room(name);
	}

	private Student generateStudent(Course[] courseList) {

		String name = Student.firstNames[r.nextInt(Student.firstNames.length)] + " " + Student.lastNames[r.nextInt(Student.lastNames.length)];
		Course[] schedule = new Course[Student.scheduleSize];
		int standardSchedules = (int) Math.floor((double) numCourses / (double) Student.scheduleSize);
		int startNumber = Student.scheduleSize * r.nextInt(standardSchedules);
		/* Generate schedule */
		for (int i = 0; i < Student.scheduleSize; i++) {
			Course tmp = courseList[startNumber + i];
			if (r.nextDouble() > crispness) {
				/* deviate from the standard schedule */
				tmp = courseList[r.nextInt(courseList.length)];
			}
			/* Check for duplicate courses in schedule */
			for (int j = 0; j < i; j++) {
				/* Duplicate - Regenerate course */
				if (schedule[j].getCourseName().compareTo(tmp.getCourseName()) == 0) {
					tmp = courseList[r.nextInt(courseList.length)];
					j = -1;
				}
			}
			schedule[i] = tmp;
		}
		Student student = new Student(name, schedule);
		for (int i = 0; i < schedule.length; i++) {
			schedule[i].enroll(student);
		}
		return student;
	}

	private String zeroPad(int n, int width) {
		StringBuffer padded = new StringBuffer(Integer.toString(n));
		int pad = width - Integer.toString(n).length();
		for (int i = 0; i < pad; i++) {
			padded.insert(0, '0');
		}
		return padded.toString();
	}
}
