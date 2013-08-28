package scheduler;

/**
 * This is a scoring function.
 * 
 * @author Erik Peter Zawadzki
 * @edited by Rodrigo Alves (check original at Evaluator.java).
 */

public class MyEvaluator {

	/**
	 * This is a function that scores a complete assignment of variables. This means that you should give it a complete schedule (all courses have
	 * times assigned for their exams). If the schedule isn't feasible (two exams should be scheduled in the same room at the same time), the function
	 * finds that extremely bad and will print a message to this effect to System.err.
	 * 
	 * This particular function treats student constraints as lower priority, and so simply counts up the number of times that a schedule asks a
	 * student to be in two exams at the same time. You could envision other scoring functions.
	 * 
	 * @param pInstance the problem instance
	 * @param pSolution your proposed solution
	 * @return either the number of student constraints violated, or Integer.MAX_VALUE if the schedule is incomplete or infeasible
	 * 
	 */
	
	final boolean USE_DEFAULT_EVALUATOR_CLASS = false; //The default class before my modifications
	boolean PRINT_ERROR_MSG = false;
	
	int courseSize[]; //the total number of students in each course.
	int studentCourseIndex[][]; //the course index for each student course (the index is based on the scheduler).
	SchedulingProblem lastProblem = null; //the last SchedulingProblem checked for violations.

	public int violatedConstraints(SchedulingProblem pInstance, ScheduleChoice[] pSolution) {
		ScheduleChoice[] schedule = pSolution;
		Course[] courseList = pInstance.getCourseList();
		Student[] studentList = pInstance.getStudentList();

		/* Check for incomplete Schedules */
		
		if ( (pSolution == null) || (pSolution.length != courseList.length) ) {
			System.err.println("Incomplete Exam Schedule!");
			return Integer.MAX_VALUE;
		}
		
		/*
		 * Initialize the course list and the student list of courses if they
		 * are not yet initialized for the specific SchedulingProblem:
		 */
		
		if ( (courseSize == null) || (studentCourseIndex == null)  || (lastProblem != pInstance) ){
			defineCourseSizes(pInstance);
			defineStudentSchedule(pInstance, pSolution);
			lastProblem = pInstance;
		}
		
		int conflicts = 0;
		
		/* Check for exam collisions */
		for (int i = 0; i < courseList.length; i++) {
			if (schedule[i].getDay() >= pInstance.getExamPeriod() || schedule[i].getDay() < 0) {
				System.err.println("Day " + schedule[i].getDay() + "is an impossible day (" + schedule[i].getCourse().getCourseName() + ")");
				return Integer.MAX_VALUE;
			}
			for (int j = i + 1; j < courseList.length; j++) {
				if (schedule[i].getRoom() == schedule[j].getRoom() && schedule[i].getDay() == schedule[j].getDay()
						&& schedule[i].getTimeSlot() == schedule[j].getTimeSlot()) {
					
					if (PRINT_ERROR_MSG)
						System.err.println("Clash Between " + schedule[i].getCourse() + " and " + schedule[j].getCourse());
					
					if (USE_DEFAULT_EVALUATOR_CLASS)
						return Integer.MAX_VALUE;
					/*
					 * We need to know how much students were in each course.
					 * Add to the conflict the total of students in the conflict 
					 * + 2 instructors that are also upset.
					 */
					conflicts += courseSize[i] + courseSize[j] + 2;
				}
			}
		}
		
		/*
		 * If the number of conflicts between courses is even bigger than
		 * the number of students enrolled, then don't waste time checking
		 * every student! Return a big value that can still be differentiated:
		 */
		if (!USE_DEFAULT_EVALUATOR_CLASS)
			if (conflicts > studentList.length)
				return conflicts * 100;
		
		/* Check for student conflicts */
		for (int i = 0; i < studentList.length; i++) {
				
			/* Pairwise-compare */
			for (int j = 0; j < Student.scheduleSize; j++) {
				int studentConflicts = 0;
				ScheduleChoice local = pSolution[ studentCourseIndex[i][j] ];
				for (int k = j + 1; k < Student.scheduleSize; k++) {
					ScheduleChoice local2 = pSolution[ studentCourseIndex[i][k] ];
					if (local.getTimeSlot() == local2.getTimeSlot() && local.getDay() == local2.getDay() && j != k) {
							studentConflicts++;
						if (!USE_DEFAULT_EVALUATOR_CLASS) 
							conflicts += studentConflicts; //It's better to have 5 students with 1 conflict than 1 student with 5 conflicts!
						/*
						 * The best would be if we could take into consideration previous conflicts from the same student.
						 * In other words, if the student had exam conflict last term, the function would return a higher
						 * number! That would avoid specific students getting too upset with conflicts every year.
						 */
					}
				}
				conflicts += studentConflicts;
			}
		}
		return conflicts;
	}
	
	/**
	 * Fill up the studentCourseIndex[A][B] array where
	 * A = the student index and
	 * B = the course index that the student is taking.
	 * This is done just once for each problem because
	 * there is no need to do it twice since the student
	 * schedule never changes. Also, according to both
	 * my algorithms, the course index in the pSolution
	 * never changes either. This is crucial for this to 
	 * work.
	 */

	private void defineStudentSchedule(SchedulingProblem pInstance, ScheduleChoice[] pSolution) {
		
		Student[] studentList = pInstance.getStudentList();
		studentCourseIndex = new int[studentList.length][Student.scheduleSize];
		int totalSchedulesCreated = 0;
		
		for (int i = 0; i < studentList.length; i++) {
			
			/* Get the scheduling choices */
			for (int j = 0; j < Student.scheduleSize; j++) {
				boolean courseFound = false;
				for (int k = 0; k < pSolution.length; k++) {
					if (pSolution[k].getCourse() == studentList[i].getCourse(j)) {

						studentCourseIndex[i][j] = k;
						totalSchedulesCreated++;
						
						if (courseFound == false)
							courseFound = true;
						else
							System.err.println("There are more than one schedule for course " + studentList[i].getCourse(j));
					}
				}
				/*
				 * Make sure there's a schedule for every course the student is taking,
				 * otherwise we could get a null pointer exeption later:
				 */
				if (courseFound == false)
						System.err.println("Can't find schedule for " + studentList[i].getStudentName() + ". Missing course " + studentList[i].getCourse(j));
			}
		}
		if ( (PRINT_ERROR_MSG) && (totalSchedulesCreated > 0) )
			System.out.println(totalSchedulesCreated + " new schedules created.");
	}

	/**
	 * Decide how many students are in each course. Since students won't change course during
	 * the Scheduler, this can be done just once for each SchedulingProblem. 
	 */
	
	private void defineCourseSizes(SchedulingProblem pProblem) {
		Course[] course = pProblem.getCourseList();
		Student[] studentList = pProblem.getStudentList();
		courseSize = new int[course.length];
		for (int i = 0; i < courseSize.length; i++)
			courseSize[i] = getCourseSize(course[i], studentList);
	}

	/**
	 * This function returns an integer with the total of students enrolled in a specific course.
	 * @param course the course that will return the size.
	 * @param studentList the list of all students to be able to find which students are in the course.
	 */
	private int getCourseSize(Course course, Student[] studentList) {
		int totalStudentsInCourse = 0;
		for (int i = 0; i < studentList.length; i++) {
			for (int j = 0; j < Student.scheduleSize; j++) {
				if (studentList[i].getCourse(j) == course) {
					/* This student is in the course */
					totalStudentsInCourse++;
					break; //A student can't be in the same course twice
				}
			}
		}
		//System.out.println("[DEBUG] Students in course " + course + ": " + totalStudentsInCourse);
		return totalStudentsInCourse;
		
	}
}

