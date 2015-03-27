package cn.ict.carc.christine.util;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import cn.ict.carc.christine.bean.Law;

public class LawFilterTest {

	@Test
	public void test() throws IOException {
		LawFilter filter = new LawFilter("户籍证明");
		List<Law> ls = new ArrayList<Law>();
		String [] texts = new String[] {"国科大户籍办",
				"英伟达奇偶思",
				"忘记佛得角度搜福建省都放假司法解释",
				"婚姻证明如何办理"};
		for(int i=0; i<texts.length; ++i) {
			Law l = new Law();
			l.setText(texts[i]);
			ls.add(l);
		}
		ls = filter.filter(ls);
		for(Law l: ls) {
			System.out.println(l.getText());
		}
	}

}
