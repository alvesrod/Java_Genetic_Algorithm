package scheduler;

import java.util.Random;

/**
 * This Scheduler receives the SchedulingProblem and calculates an optimal schedule based
 * on an evaluator (MyEvaluator). It uses a Simulated Annealing to do so.
 * The schedule returned is not necessarily the best one, but it should be one of the bests.
 * The performance of this schedule is inferior to the performance of the Scheduler1.
 * 
 * @author Rodrigo Alves
 */

public class Scheduler2 implements Scheduler {

	/**
	 * @see scheduler.Scheduler#authors()
	 */
	public String authors() {
		return "Rodrigo Alves (ID: 15674112)";
	}
	
	/*
	 * ======== Simulated Annealing ========
	 * And of course, I slightly changed it for performance.
	 */
	
	/*
	 * Evaluator used. Please USE MY EVALUATOR! It's faster and more efficient.
	 */
	//Evaluator evaluator = new Evaluator();
	MyEvaluator evaluator = new MyEvaluator();
	
	
	/*
	 * Constants to play with:
	 * By playing with those constants, we change the efficience of the algorithm for different cases.
	 * I tried choosing what I think it's the best for the current default Scheduling Problem:
	 */
	final int HALT_AFTER_SECONDS = 12; //It will stop after that time. It returns the best solution found so far.
	final int MAX_LOOPS = 100000; //Total of loops for the main loop. If it gets over that, returns the best solution so far.
	final int TABU_LIST_MAX_LENGTH = 200; //Total of schedules to keep track
	final int LOCAL_SEARCH_MAX = 50000; // Max. number of comparisons when choosing the most optimal ScheduleChoice.
	final int REPETITIONS_TO_BE_CONSIDERED_STUCKED = 100; //If the last loops had exactly the same best value, reset search.
	final double COOLING_SCHEDULE = 1; //The cooling schedule for the temperature
	final double STARTING_TEMPERATURE = 100;
	
	/*
	 * Other variables:
	 */
	final int TIME_SLOTS = 4; //Total of time slots for the exams. This must be equal ScheduleChoice.times.length;
	ScheduleChoice[][] tabuList; //Stores the tabu list
	int tabuListLocation = 0; //The tabu list is a circular list, so stores the current index of the circular list.

	ScheduleChoice[] bestSoFar = null; //Keeps track of the best ScheduleChoice[] found so far.
	int violationsForBestSoFar = Integer.MAX_VALUE; //The total of constraint violations from the best choice so far.
	
	/* Declare variables that will keep track if the algorithm is stucked: */
	int lastLoopConstraints = 0;
	int totalLoopsWithSameConstraints = 0;

	/**
	 * @see scheduler.Scheduler#schedule(scheduler.SchedulingProblem)
	 */
	public ScheduleChoice[] schedule(SchedulingProblem pProblem) {
		
		 /* Declare and prepare variables: */
		Course[] course = pProblem.getCourseList();
		ScheduleChoice[] choice = new ScheduleChoice[ course.length ];
		tabuList = new ScheduleChoice[TABU_LIST_MAX_LENGTH][ course.length ];
		
		/* Keep track of time: */
		long time = System.currentTimeMillis();
		
		/* Initialize the temperature: */
		double temperature = STARTING_TEMPERATURE;
		
		/*
		 * We start by assigning random values to the SchedulingChoice[].
		 * Although it is random, it is not fully random:
		 */
		choice = randomSchedule(pProblem);
		
		/* The main loop: */
		for (int j = 0; j < MAX_LOOPS; j++) {
			
			/*
			 * If all constraints are satisfied, we found a solution! 
			 * So, return that solution:
			 */
			if ( getViolations(pProblem, choice) == 0 ) return choice;
			
			
			/* Decides the next path: */
			choice = choosePath(pProblem, choice, temperature);
			
			/* Add the choice to the Tabu list: */
			if (!isInTabuList(choice)) addToTabuList(choice);

			/*
			 * Get the best assignment for the current generation of mutants.
			 * If the best from this generation is the assignment with the least
			 * number of violations found, save it to the "bestSoFar" variable:
			 */
			int bestOfGenerationConstraints = prepareBestSoFar(pProblem, choice);
			
			//if (j % 20 == 0) System.out.println("Best at " + j + ": " + bestOfGenerationConstraints); //DEBUG
			
			/* Reset the choices if the algorithm got stucked: */
			choice = resetIfStucked(pProblem, choice, bestOfGenerationConstraints);
			
			/* Update the temperature: */
			temperature = updateTemperature(temperature);
			
			/* Abandon the loop if it's running for too long: */
			long secondsElapsed = (System.currentTimeMillis() - time) / 1000;
			if (secondsElapsed > HALT_AFTER_SECONDS) break;
			
		}
		//System.out.println("No solution found in time. Sending the one with the least constraints (" + violationsForBestSoFar + ").");
		return bestSoFar;
	}

	/**
	 * Choose a new ScheduleChoice[] for the param choice. Return this schedule.
	 * The schedule is based on Simulated Annealing. It will compare with a random
	 * assignment and might choose it based on probabilities.
	 */
	private ScheduleChoice[] choosePath(SchedulingProblem pProblem, ScheduleChoice[] choice, double temperature) {
		
		ScheduleChoice[] newChoice = randomSchedule(pProblem);
		int oldChoiceViolations = getViolations(pProblem, choice);
		choice = getLocalBestCombination(pProblem, choice);
		newChoice = getLocalBestCombination(pProblem, newChoice );
		
		int newChoiceViolations = getViolations(pProblem, newChoice);
		int choiceViolations = getViolations(pProblem, choice);
		
		if (choiceViolations == 0) return choice;
		if (newChoiceViolations == 0) return newChoice;
		
		if ( (choiceViolations >= oldChoiceViolations) || (choiceViolations >= newChoiceViolations) )
			choice = newChoice;
		else
			choice = chooseBasedOnProbability(choice, temperature, newChoice, newChoiceViolations, choiceViolations);
		return choice;
	}

	/**
	 * Grabs param "choice" and "newChoice" and choose one based on the probability
	 * (with the temperature).
	 * @return the chosen ScheduleChoice[].
	 */
	private ScheduleChoice[] chooseBasedOnProbability(ScheduleChoice[] choice, double temperature, ScheduleChoice[] newChoice,
			int newChoiceViolations, int choiceViolations) {
		Random r = new Random();
		
		double probability = Math.exp(((double)(choiceViolations-newChoiceViolations))/temperature); //new is bigger than choice
		//System.err.println("new: " + newChoiceViolations + " current: " + choiceViolations + " Probability of new: " + probability);
		if (probability > r.nextDouble())
			choice = newChoice;
		
		return choice;
	}

	/**
	 * Grabs the parameter "choice" and chooses schedules with the
	 * least amount of conflicts for each course. Returns the
	 * new choice:
	 */
	private ScheduleChoice[] getLocalBestCombination(SchedulingProblem pProblem, ScheduleChoice[] choice) {
		for (int i = 0; i < choice.length; i++ ) {
			choice[i] = getBestLocalAssignmentForCourse(i, choice, pProblem);
		}
		return choice;
	}
	
	private double updateTemperature(double temperature) {
		if (temperature < 0.0001)
			return temperature;
		return temperature *= COOLING_SCHEDULE;
	}


	/**
	 * Returns the total of violations from a ScheduleChoice[] choice:
	 */
	public int getViolations(SchedulingProblem pProblem, ScheduleChoice[] choice) {
		return evaluator.violatedConstraints(pProblem, choice);
	}
	
	/**
	 * Check if the ScheduleChoice[] choice is stucked. It will be
	 * stucked when the last best values of the past 
	 * REPETITIONS_TO_BE_CONSIDERED_STUCKED generations is identical.
	 * Once stucked, return a new ScheduleChoice[] choice.
	 */
	private ScheduleChoice[] resetIfStucked(SchedulingProblem pProblem, ScheduleChoice[] choice, int bestOfGenerationConstraints) {
		if (bestOfGenerationConstraints == lastLoopConstraints)
			totalLoopsWithSameConstraints++;
		else {
			lastLoopConstraints = bestOfGenerationConstraints;
			totalLoopsWithSameConstraints = 0;
		}
		
		if (totalLoopsWithSameConstraints >= REPETITIONS_TO_BE_CONSIDERED_STUCKED) {
			/* It's stucked! Get new values: */
			choice = randomSchedule(pProblem);
			totalLoopsWithSameConstraints = 0;
		}
		return choice;
	}
	
	/**
	 * Function used for circular values.
	 * @param value the value to be incremented.
	 * @param max the limit that the value must never reach.
	 * @return the value incremented (or reset if it went over the max).
	 */
	private int incrementOrReset(int value, int max) {
		value++;
		if (value >= max)
			value = 0;
		return value;
	}
	
	/**
	 * Gets the best ScheduleChoice (choice or the old best). Return the
	 * number of violations of the best.
	 * Also, if this choice is the best so far, save it to the bestSoFar
	 * variable:
	 */
	private int prepareBestSoFar(SchedulingProblem pProblem, ScheduleChoice[] choice) {
		int localBest = getViolations(pProblem, choice);
		if (localBest <= violationsForBestSoFar) {
			violationsForBestSoFar = localBest;
			bestSoFar = choice;
		}
		return violationsForBestSoFar;
	}

	
	/**
	 * Returns a random ScheduleChoice[] with random schedules.
	 * The result is not so random because it tries to minimize
	 * the number of room, day, and time conflicts.
	 */
	public ScheduleChoice[] randomSchedule(SchedulingProblem pProblem) {
		Course[] course = pProblem.getCourseList();
		Room[] room = pProblem.getRoomList();
		Random r = new Random();
		int time = r.nextInt(TIME_SLOTS);
		int day = r.nextInt(pProblem.getExamPeriod());
		int roomIndex = r.nextInt(room.length);
		double sumOfDomains = TIME_SLOTS + pProblem.getExamPeriod() + room.length;
		
		double timeProbability = TIME_SLOTS / sumOfDomains;
		double dayProbability = pProblem.getExamPeriod() / sumOfDomains;
		
		ScheduleChoice choice[] = new ScheduleChoice[ course.length ];
		
		for (int i = 0; i < course.length; i++) {
			double probability = r.nextDouble();
			choice[i] = new ScheduleChoice(course[i], room[roomIndex], day, time);
			
			if (timeProbability >= probability)
				time = incrementOrReset(time, TIME_SLOTS);
			else {
				probability -= timeProbability;
				if (dayProbability >= probability)
					day = incrementOrReset(day, pProblem.getExamPeriod());
				else
					roomIndex = incrementOrReset(roomIndex, room.length);
			
			}
		}
		return choice;
	}

	/**
	 * Get the param schedule[courseIndex] and change 1 variable of it (start with times, then days, then rooms).
	 * If the new schedule is better than the previous one, keep it. Otherwise, discard.
	 * Repeat it LOCAL_SEARCH_MAX times (or until you have done for all possible variables).
	 * @return the modified schedule[courseIndex].
	 */
	public ScheduleChoice getBestLocalAssignmentForCourse(int courseIndex, ScheduleChoice[] schedule, SchedulingProblem pProblem) {
		Course[] course = pProblem.getCourseList();
		Room[] room = pProblem.getRoomList();
		final int TOTAL_DAYS = pProblem.getExamPeriod();
		int violationsOfBestChoice = getViolations(pProblem, schedule);
		int countSearches = 0;
		
		for (int i = 0; i < room.length; i++) {
			for (int j = 0; j < TOTAL_DAYS; j++) {
				for (int k = 0; k < TIME_SLOTS; k++) {
					ScheduleChoice savedChoice = schedule[courseIndex];
					schedule[courseIndex] = new ScheduleChoice(course[courseIndex], room[i], j, k);
					
					countSearches++;
					if (countSearches > LOCAL_SEARCH_MAX)
						return schedule[courseIndex];
					
					if (isInTabuList(schedule)) {
						schedule[courseIndex] = savedChoice;
					} else {
						int newViolations = getViolations(pProblem, schedule);
						if (newViolations > violationsOfBestChoice)
							schedule[courseIndex] = savedChoice;
						else
							violationsOfBestChoice = newViolations;
					}
				}
			}
		}
		return schedule[courseIndex];
	}
	
	
	/**
	 * Add the schedule to the TabuList that keeps track of visited schedules:
	 */
	private void addToTabuList(ScheduleChoice[] schedule) {
		tabuList[tabuListLocation] = schedule;
		tabuListLocation = incrementOrReset(tabuListLocation, TABU_LIST_MAX_LENGTH);
	}

	/**
	 * Check if a schedule is in the TabuList. O (tabuSize * scheduleSize);
	 * @return true if it is in the list.
	 */
	private boolean isInTabuList(ScheduleChoice schedule[]) {
		for (int i = 0; i < tabuList.length; i++) {
			boolean found = true;
			for (int j = 0; j < schedule.length; j++)
				if (!equalSchedules(schedule[j], tabuList[i][j]))
					found = false;
			if (found) return true;
		}
		return false;
	}

	/**
	 * Compare the two ScheduleChoice params.
	 * @return true if the params are the same.
	 */
	public boolean equalSchedules(ScheduleChoice schedule1, ScheduleChoice schedule2) {
		if ( (schedule1 == null) || (schedule2 == null) ) return false;
		return (schedule1.getCourse() == schedule2.getCourse()) &&
				(schedule1.getRoom() == schedule2.getRoom())     &&
				(schedule1.getDay() == schedule2.getDay())       &&
				(schedule1.getTimeSlot() == schedule2.getTimeSlot());
	}
		
}

