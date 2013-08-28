package scheduler;

/**
 * This is a batch driver file.
 * 
 * @author Erik Peter Zawadzki, David R.M. Thompson
 * @edited Rodrigo Alves
 */

public class Driver {

	/**
	 * The main function that grinds though a batch of nine problems.
	 * 
	 * Java doesn't really have a good way of timing other than the system timer, so other CPU load will be a factor. Don't encode a DVD while you're
	 * running this.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		/* Let's batch solve 9 problems */
		SchedulingProblem[] arrayOfProblems = new SchedulingProblem[150];

		/* We first need to set up our problem generators */
		
		int totalEasyProblems = 1;
		int totalMediumProblems = 1;

		/*
		 * This means 40 courses, 2 rooms, 500 students, and a 'crispness' of 0.95, which means that most of the time students take very similar
		 * courses.
		 */
		Generator easyProblems = new Generator(40, 2, 500, 0.95); //new Generator(40, 2, 500, 0.95);
		for (int i = 0; i < totalEasyProblems; i++) {
			arrayOfProblems[i] = easyProblems.generateProblem(i);
			System.out.println("+++++++++++ Problem " + i + " +++++++++++ ");
			System.out.println(arrayOfProblems[i]);
		}

		System.out.println("\n\n=======================================");
		
		/*
		 * This means 60 courses, 4 rooms, 600 students, and a 'crispness' of 0.9, which means that most of the time students take similar courses.
		 */
		Generator mediumProblems = new Generator(60, 4, 600, 0.9);
		for (int i = totalEasyProblems; i < (totalEasyProblems + totalMediumProblems); i++) {
			arrayOfProblems[i] = mediumProblems.generateProblem(i);
			System.out.println("+++++++++++ Problem " + i + " +++++++++++ ");
			System.out.println(arrayOfProblems[i]);
		}

		System.out.println("\n\n\n---STARTING BATCH SOLVE---");
		Evaluator e = new Evaluator();
		int count1 = 0;
		int count2 = 0;
		long countSeconds1 = 0;
		long countSeconds2 = 0;
		for (int i = 0; i < (totalEasyProblems + totalMediumProblems); i++) {
			Scheduler myScheduler1 = new Scheduler1();
			long time = System.currentTimeMillis();
			ScheduleChoice[] sc = myScheduler1.schedule(arrayOfProblems[i]);
			long delta = (System.currentTimeMillis() - time) / 1000;
			int violations = e.violatedConstraints(arrayOfProblems[i], sc);
			if (violations < 10000) count1 += violations;
			countSeconds1 += delta;
			
			System.out.println("\n=======================================");
			System.out.println(i + ", Genetic Algorithm: took " 
					+ delta + " seconds [Violations: " + violations + "] T: " 
					+ count1 + "|" + countSeconds1 + "s in total. CA:" 
					+ ((long)count1/(long)(i+1)) + " TA:" 
					+ ((long)countSeconds1/(long)(i+1)) );

			System.out.println("Problem " + i + ". " 
					+ "Solution from the Genetic Algorithm: ");
			printSchedule(sc);

			Scheduler myScheduler2 = new Scheduler2();
			time = System.currentTimeMillis();
			sc = myScheduler2.schedule(arrayOfProblems[i]);
			delta = (System.currentTimeMillis() - time) / 1000;
			violations = e.violatedConstraints(arrayOfProblems[i], sc);
			if (violations < 10000) count2 += violations;
			countSeconds2 += delta;
			
			System.out.println("\n=======================================");
			System.out.println(i + ", Simulated Annealing: took " 
					+ delta + " seconds [Violations: " + violations + "] T: " 
					+ count2 + "|" + countSeconds2 + "s in total. CA:" 
					+ ((long)count2/(long)(i+1))+ " TA:" 
					+ ((long)countSeconds2/(long)(i+1)) );
		}
	}

	/* You might like this function for debugging */
	public static void printSchedule(ScheduleChoice[] s) {
		for (int i = 0; i < s.length; i++) {
			System.out.println(s[i].toString());
		}
	}
}
