package cn.ict.carc.christine.util;

import java.util.Collection;

public class CollectionHelper {
	public static int[] toIntArray(Collection<Integer> collections) {
		int[] array = new int[collections.size()];
		int index =0;
		for(Integer i : collections) {
			array[index++] = i;
		}
		return array;
	}
}
