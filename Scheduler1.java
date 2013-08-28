package scheduler;

import java.util.Random;

/**
 * This Scheduler receives the SchedulingProblem and calculates an optimal schedule based
 * on an evaluator (MyEvaluator). It uses a Genetic Algorithm to do so.
 * The schedule returned is not necessarily the best one, but it should be one of the bests.
 * 
 * @author Rodrigo Alves
 */

public class Scheduler1 implements Scheduler {

	/**
	 * @see scheduler.Scheduler#authors()
	 */
	public String authors() {
		return "Rodrigo Alves (ID: 15674112)";
	}
	
	/*
	 * ======== GENETIC ALGORITHM (with Intelligent Design) ========
	 * 
	 * This algorithm is heavily based on the Genetic Algorithm, but mutations
	 * don't happen completly at random. This works much better with my own
	 * modified MyEvaluator.java class.
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
	final int HALT_AFTER_SECONDS = 20; //It will stop after that time. It returns the best solution found so far.
	final int MAX_LOOPS = 100000; //Total of loops for the main loop. If it gets over that, returns the best solution so far.
	final int CHILDREN_PER_COUPLE = 1 * 2; //Total of children a reproduction is going to make. This number has to be even;
	final int POPULATION_SIZE = 3 * CHILDREN_PER_COUPLE; //The population. It has to be a multiple of CHILDREN_PER_COUPLE
	final double COOLING_SCHEDULE = 1; //The cooling schedule for the temperature
	final double STARTING_TEMPERATURE = 10;
	final int TABU_LIST_MAX_LENGTH = 1; //Total of schedules to keep track
	final int LOCAL_SEARCH_MAX = 50000; // Max. number of comparisons when choosing the most optimal ScheduleChoice.
	final int MUTATION_LEVEL = 2; //Total of ScheduleChoice that will be replaced in every mutation.
	final int REPETITIONS_TO_BE_CONSIDERED_STUCKED = 300; //If the last loops had exactly the same best value, reset search.
	
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
		ScheduleChoice[][] choice = new ScheduleChoice[ POPULATION_SIZE ][ course.length ];
		ScheduleChoice[][] tempChoices = new ScheduleChoice[ POPULATION_SIZE ][ course.length ];
		tabuList = new ScheduleChoice[TABU_LIST_MAX_LENGTH][ course.length ];
		
		/* Keep track of time: */
		long time = System.currentTimeMillis();
		
		/* Initialize the temperature: */
		double temperature = STARTING_TEMPERATURE;
		
		/*
		 * We start by assigning random values to the SchedulingChoice[].
		 * Although it is random, it is not fully random:
		 */
		choice = assignRandomValues(pProblem);
		

		
		/* The main loop: */
		for (int j = 0; j < MAX_LOOPS; j++) {
			
			/*
			 * If all constraints are satisfied, we found a solution! So,
			 * return that solution:
			 */
			for (int i = 0; i < POPULATION_SIZE; i++) {
				if (satisfiesAllConstraints(pProblem, choice, i)) 
					return choice[i]; 
			}

			/* Prepare the next generation of the population with all the mutated values: */
			for(int i = 0; i < POPULATION_SIZE - 1; i += CHILDREN_PER_COUPLE) {

				/* 
				 * Choose who will reproduce this time based on their fittest. Schedule choices
				 * with less violations have more chances to be seltected for reprodution.
				 */
				ScheduleChoice[] choiceA = chooseRandomAssignment(pProblem, choice, temperature);
				ScheduleChoice[] choiceB = chooseRandomAssignment(pProblem, choice, temperature);

				/*
				 * choiceA and choiceB produces CHILDREN_PER_COUPLE children. Grab those children
				 */
				ScheduleChoice[][] combinedChoice = crossover(choiceA, choiceB, course.length);

				/* 
				 * Add the children to the next generation's population: 
				 * */
				for (int k = 0; k < CHILDREN_PER_COUPLE; k++)
					tempChoices[i+k] = mutate(combinedChoice[k], pProblem);

			}
			
			/* Replace the previous choices with the new generation of mutants: */
			choice = tempChoices;

			/*
			 * Get the best assignment for the current generation of mutants.
			 * If the best from this generation is the assignment with the least
			 * number of violations found, save it to the "bestSoFar" variable:
			 */
			int bestOfGenerationConstraints = prepareBestSoFar(pProblem, choice);
			
			//if (j % 40 == 0) System.out.println("Best assignment of generation " + j + ": " + bestOfGenerationConstraints); //DEBUG
			
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
	 * Check if the param choice[i] satifies all the constraints.
	 * @return true if there are no violations.
	 */
	private boolean satisfiesAllConstraints(SchedulingProblem pProblem, ScheduleChoice[][] choice, int i) {
		return getViolations(pProblem, choice[i]) == 0;
	}
	
	/**
	 * Returns the total of violations from a ScheduleChoice[] choice:
	 */
	public int getViolations(SchedulingProblem pProblem, ScheduleChoice[] choice) {
		return evaluator.violatedConstraints(pProblem, choice);
	}
	
	/**
	 * Check if the ScheduleChoice[][] choice is stucked. It will be
	 * stucked when the last best values of the past 
	 * REPETITIONS_TO_BE_CONSIDERED_STUCKED generations is identical.
	 * Once stucked, return a new ScheduleChoice[][] choice.
	 */
	private ScheduleChoice[][] resetIfStucked(SchedulingProblem pProblem, ScheduleChoice[][] choice, int bestOfGenerationConstraints) {
		if (bestOfGenerationConstraints == lastLoopConstraints)
			totalLoopsWithSameConstraints++;
		else {
			lastLoopConstraints = bestOfGenerationConstraints;
			totalLoopsWithSameConstraints = 0;
		}
		
		if (totalLoopsWithSameConstraints >= REPETITIONS_TO_BE_CONSIDERED_STUCKED) {
			/* It's stucked! Get new values: */
			choice = assignRandomValues(pProblem);
			totalLoopsWithSameConstraints = 0;
			//System.out.println("Mass extinction!");
		}
		return choice;
	}
	
	/**
	 * Gets the best ScheduleChoice from the param choice. Return the
	 * number of violations of this choice.
	 * Also, if this choice is the best so far, save it to the bestSoFar
	 * variable:
	 */
	private int prepareBestSoFar(SchedulingProblem pProblem, ScheduleChoice[][] choice) {
		ScheduleChoice[] getBestAssignmentOfGeneration = chooseAssignmentLeastConstraints(pProblem, choice);
		int bestOfGenerationConstraints = getViolations(pProblem, getBestAssignmentOfGeneration);
		if (bestOfGenerationConstraints <= violationsForBestSoFar) {
			violationsForBestSoFar = bestOfGenerationConstraints;
			bestSoFar = getBestAssignmentOfGeneration;
		}
		return bestOfGenerationConstraints;
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
	 * Mutates a ScheduleChoice[]. The mutated choice will have MUTATION_LEVEL schedules replaced.
	 * @param schedule the choice that will be mutated.
	 * @return the new mutated algorithm
	 */
	private ScheduleChoice[] mutate(ScheduleChoice[] schedule, SchedulingProblem pProblem) {
		Random r = new Random();
		int courseIndex = r.nextInt(schedule.length);
		for (int i = 0; i < MUTATION_LEVEL; i++) {
			schedule[courseIndex] = getBestLocalAssignmentForCourse(courseIndex, schedule, pProblem);
			courseIndex = incrementOrReset(courseIndex, schedule.length);
			
			//System.out.println("New mutation: [" + courseIndex + "] Room:" + schedule[courseIndex].getRoom() + " Date:" 
			//+ schedule[courseIndex].getDay() + "|" + schedule[courseIndex].getTimeSlot());
		}
		if (!isInTabuList(schedule))
			addToTabuList(schedule);
		return schedule;
	}

	private double updateTemperature(double temperature) {
		if (temperature < 0.0001)
			return temperature;
		return temperature *= COOLING_SCHEDULE;
	}

	/**
	 * Combine the choices of choiceA with the choices of choiceB randomly.
	 * Produces CHILDREN_PER_COUPLE children. Returns them in an array:
	 */
	private ScheduleChoice[][] crossover(ScheduleChoice[] choiceA, ScheduleChoice[] choiceB, int size) {
		Random r = new Random();
		ScheduleChoice[][] combinedChoice = new ScheduleChoice[ CHILDREN_PER_COUPLE ][ size ];
		
		for(int i = 0; i < CHILDREN_PER_COUPLE; i++) {
			
			/* Combine the choices of both parents to make one child: */
			for (int k = 0; k < size; k++) {
				if (r.nextInt(2) == 0)
					combinedChoice[i][k] = choiceA[k]; //Use mother schedule
				else
					combinedChoice[i][k] = choiceB[k]; //Use father schedule
			}
		}
			/*
			 * I know this is different from the original crossover from the genetic algorithm.
			 * But, it is better that way, so all the courses keep at the same index.
			 */
		return combinedChoice;
	}

	/**
	 * Grab a ScheduleChoice[] from the param choice.
	 * The ScheduleChoice[] selected is based on the
	 * probability. Choices with more violations have
	 * less chances of being selected.

	 * @param temperature plays a role in the probability.
	 * Higher the temperature, more likely to choose schedules with
	 * less probability.
	 * @return the chosen schedule.
	 */
	private ScheduleChoice[] chooseRandomAssignment(SchedulingProblem pProblem, ScheduleChoice[][] choice, double temperature) {
		
		Random r = new Random();
		double rand = r.nextDouble();
		
		double violations[] = new double[POPULATION_SIZE];
		double probability[] = new double[POPULATION_SIZE];
	
		double totalViolations = 0;
		double sumOfProbabilities = 0;
		
		for (int i = 0; i < POPULATION_SIZE; i++) {
			violations[i] = getViolations(pProblem, choice[i]);
			if (violations[i] == 0) return choice[i];
			violations[i] = 1.0 / violations[i];
			violations[i] = Math.exp(-violations[i]/temperature);
			totalViolations += violations[i];
		}
		
		for (int i = 0; i < POPULATION_SIZE; i++) {
			probability[i] = violations[i] / totalViolations;
			sumOfProbabilities += probability[i];
			if (sumOfProbabilities >= rand) {
				return choice[i];
			}
		}

		System.err.println("All probabilities failed. Sum:" + sumOfProbabilities + " Rand:" + rand);
		return choice[r.nextInt(POPULATION_SIZE)];
	}

	/**
	 * Check every ScheduleChoice[] in the param choice.
	 * @return the choice with the smallest amount of violations.
	 */
	private ScheduleChoice[] chooseAssignmentLeastConstraints(SchedulingProblem pProblem, ScheduleChoice[][] choice) {
		int chosenAssignmentIndex = 0;
		int chosenAssignmentViolations = Integer.MAX_VALUE;
		
		for (int i = 0; i < POPULATION_SIZE; i++) {
			int violations = getViolations(pProblem, choice[i]);
			if (violations <= chosenAssignmentViolations) {
				chosenAssignmentIndex = i;
				chosenAssignmentViolations = violations;
			}
		}
		return choice[chosenAssignmentIndex];
	}

	/**
	 * @return a full ScheduleChoice[POPULATION_SIZE][] array with random values (not so random).
	 */
	private ScheduleChoice[][] assignRandomValues(SchedulingProblem pProblem) {
		Course[] course = pProblem.getCourseList();
		ScheduleChoice[][] choice = new ScheduleChoice[ POPULATION_SIZE ][ course.length ];
		
		for (int i = 0; i < POPULATION_SIZE; i++) {
				choice[i] = randomSchedule(pProblem);
		}
		return choice;
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
	 * Another random selection, with a different way of selecting variables.
	 * I'm not using that one for now, but I'll just leave it here.
	 */
	@SuppressWarnings("unused")
	private ScheduleChoice[] randomScheduleProportionalVariables(SchedulingProblem pProblem) {
		Course[] course = pProblem.getCourseList();
		Room[] room = pProblem.getRoomList();
		Random r = new Random();
		int time = r.nextInt(TIME_SLOTS);
		int day = r.nextInt(pProblem.getExamPeriod());
		int roomIndex = r.nextInt(room.length);
		int variant = r.nextInt(3);
		ScheduleChoice choice[] = new ScheduleChoice[ course.length ];
		
		for (int i = 0; i < course.length; i++) {
			choice[i] = new ScheduleChoice(course[i], room[roomIndex], day, time);
			
			switch (variant) {
			case 0:
				time = incrementOrReset(time, TIME_SLOTS);
			case 1:
				day = incrementOrReset(day, pProblem.getExamPeriod());
			case 2:
				roomIndex = incrementOrReset(roomIndex, room.length);
			}
			variant = incrementOrReset(variant, 3);
			//variant = r.nextInt(3);
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
