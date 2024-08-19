package net.illusioncraft.Doors;

import java.util.Comparator;

public class PercentageComparator implements Comparator<UserData> {

	@Override
	public int compare(UserData o1, UserData o2) {
		Float val1 = (float) (((float) o2.correct / (float) (o2.correct + o2.incorrect)) * 100);
		Float val2 = (float) (((float) o1.correct / (float) (o1.correct + o1.incorrect)) * 100);
		return (int) Math.signum(val1 - val2);
	}

}
