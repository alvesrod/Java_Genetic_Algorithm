package scheduler;

/**
 * This is a scoring function.
 * 
 * @author Erik Peter Zawadzki
 */

public class Evaluator {

	/**
	 * This is a function that scores a complete assignment of variables. This means that you should give it a complete schedule (all courses have
	 * times assigned for their exams). If the schedule isn't feasible (two exams should be scheduled in the same room at the same time), the function
	 * finds that extremely bad and will print a message to this effect to System.err and return Integer.MAX_VALUE.
	 * 
	 * This particular function treats student constraints as lower priority, and so simply counts up the number of times that a schedule asks a
	 * student to be in two exams at the same time. You could envision other scoring functions.
	 * 
	 * @param pInstance the problem instance
	 * @param pSolution your proposed solution
	 * @return either the number of student constraints violated, or Integer.MAX_VALUE if the schedule is incomplete or infeasible
	 * 
	 */

	public int violatedConstraints(SchedulingProblem pInstance, ScheduleChoice[] pSolution) {
		ScheduleChoice[] schedule = pSolution;
		Course[] courseList = pInstance.getCourseList();
		Student[] studentList = pInstance.getStudentList();

		/* Check for incomplete Schedules */
		if (pSolution.length != courseList.length) {
			System.err.println("Incomplete Exam Schedule!");
			return Integer.MAX_VALUE;
		}
		/* Check for exam collisions */
		for (int i = 0; i < courseList.length; i++) {
			if (schedule[i].getDay() >= pInstance.getExamPeriod() || schedule[i].getDay() < 0) {
				System.err.println("Day " + schedule[i].getDay() + "is an impossible day (" + schedule[i].getCourse().getCourseName() + ")");
				return Integer.MAX_VALUE;
			}
			for (int j = i + 1; j < courseList.length; j++) {
				if (schedule[i].getRoom() == schedule[j].getRoom() && schedule[i].getDay() == schedule[j].getDay()
						&& schedule[i].getTimeSlot() == schedule[j].getTimeSlot()) {
					System.err.println("Clash Between " + schedule[i].getCourse() + " and " + schedule[j].getCourse());
					return Integer.MAX_VALUE;
				}
			}
		}
		/* Check for student conflicts */
		int conflicts = 0;
		for (int i = 0; i < studentList.length; i++) {
			ScheduleChoice[] local = new ScheduleChoice[Student.scheduleSize];
			/* Get the scheduling choices */
			for (int j = 0; j < Student.scheduleSize; j++) {
				for (int k = 0; k < schedule.length; k++) {
					if (schedule[k].getCourse() == studentList[i].getCourse(j)) {
						local[j] = schedule[k];
					}
				}
			}
			/* Pairwise-compare */
			for (int j = 0; j < Student.scheduleSize; j++) {
				for (int k = j + 1; k < Student.scheduleSize; k++) {
					if (local[j].getTimeSlot() == local[k].getTimeSlot() && local[j].getDay() == local[k].getDay() && j != k) {
						conflicts++;
					}
				}
			}
		}
		return conflicts;
	}
}
