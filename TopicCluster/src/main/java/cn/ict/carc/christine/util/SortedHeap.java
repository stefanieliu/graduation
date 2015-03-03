package cn.ict.carc.christine.util;

import java.util.ArrayList;

public class SortedHeap<T extends Comparable<?super T>, E> {
	private boolean ismaxheap;
	private ArrayList<HeapElement> heap;
	private class HeapElement {
		T compareKey;
		E attach;
		HeapElement(T compareKey, E attach) {
			this.compareKey = compareKey;
			this.attach = attach;
		}
		
	}
	public SortedHeap(boolean maxheap, int capacity) {
		this.ismaxheap = maxheap;
		this.heap = new ArrayList<HeapElement>();
	}
	
	public void add(T compareKey, E attach) {
		HeapElement e = new HeapElement(compareKey, attach);
		if(heap.isEmpty()) {
			heap.add(e);
		}
	}
}
