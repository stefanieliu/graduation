package cn.ict.carc.christine.TopicCluster;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.regex.Matcher;

import cn.ict.carc.christine.bean.Law;
import cc.mallet.types.Instance;

public class LawIterator implements Iterator<Instance> {
	
	Collection<Law> data;
	Iterator<Law> iter;
	
	public LawIterator(Collection<Law> laws) {
		this.data = laws;
		iter =  this.data.iterator();
	}

	@Override
	public boolean hasNext() {
		return iter.hasNext();
	}

	@Override
	public Instance next() {
		Law nextlaw = iter.next();
		return new Instance (nextlaw.getText(), nextlaw.getId(), nextlaw.getTitle(), null);
	}

	@Override
	public void remove() {
		iter.remove();
	}

}
