package cn.ict.carc.christine.util;

import java.util.Collection;

public class ArrayHelper {
	public static int[] toIntArray(Collection<Integer> collections) {
		int[] array = new int[collections.size()];
		int index =0;
		for(Integer i : collections) {
			array[index++] = i;
		}
		return array;
	}
	
	public static double[] plus(double[] array, double times) {
		double[] result = new double[array.length];
		for(int i=0; i<array.length; ++i) {
			result[i] = array[i] * times;
		}
		return result;
	}
	
	public static void plusInPlace(double[] array, double times) {
		for(int i=0; i<array.length; ++i) {
			array[i] *= times;
		}
	}
	
	public static double[] add(double[] array, double[] add) {
		assert(array.length == add.length);
		double[] result = new double[array.length];
		for(int i=0; i<array.length; ++i) {
			result[i] = array[i] + add[i];
		}
		return result;
	}
	
	public static void addInPlace(double[] array, double[] add) {
		assert(array.length == add.length);
		for(int i=0; i<array.length; ++i) {
			array[i] += add[i];
		}
	}
}
