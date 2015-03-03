package cn.ict.carc.christine.bean;

public class MaxHeapElement implements Comparable {
	
	public int index;
	public double value;
	public MaxHeapElement(int index, double value) {
		this.index = index;
		this.value = value;
	}

	@Override
	public int compareTo(Object o) {
		MaxHeapElement e = (MaxHeapElement) o;
		if(this.value > e.value) {
			return -1;
		} else if(this.value < e.value) {
			return 1;
		} else {
			return 0;
		}
	}
}
