package net.illusioncraft.Doors;

import java.util.Comparator;

public class CorrectComparator implements Comparator<UserData> {

	@Override
	public int compare(UserData o1, UserData o2) {
		return o2.correct - o1.correct;
	}

}
