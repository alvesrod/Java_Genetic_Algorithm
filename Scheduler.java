package scheduler;

/**
 * The almighty Scheduler interface. We should be able to simply call the {@link Scheduler#schedule(SchedulingProblem)} function, wait for a little
 * while for your program to grind away, and get the solution. We will check the solution with an Evaluator object.
 * 
 * @author Erik Peter Zawadzki, David R.M. Thompson
 */

public interface Scheduler {

	/**
	 * @return names of the authors and their student IDs (1 per line).
	 */
	public String authors();

	/**
	 * This is the interface that your local search schedulers should implement
	 * 
	 * @param pProblem a scheduling problem from the generator
	 * @return an array of SchedulingChoices. Its length should be the the number of classes, as each class needs an exam.
	 */
	public ScheduleChoice[] schedule(SchedulingProblem pProblem);
}
