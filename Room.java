package scheduler;

/**
 * A class to represent university class rooms
 * 
 * @author Erik Peter Zawadzki
 */

public class Room {

	private String roomName;

	/**
	 * The building prefixes, using UBC's SIS code system. You take class in DMP, for instance.
	 */
	public static final String[] buildingSIS = {"ICCS","DMP","MATH","SCR","OSBR","MCML","CEME"};

	/**
	 * Returns the name of the class room
	 * 
	 * @return roomName, the name of the room
	 */
	public String getRoomName() {

		return roomName;
	}

	/**
	 * Constructs a Room object.
	 * 
	 * @param pName the name of the room (e.g. DMP 101)
	 */

	public Room(String pName) {

		roomName = pName;
	}

	public String toString() {
		return roomName;
	}

}
