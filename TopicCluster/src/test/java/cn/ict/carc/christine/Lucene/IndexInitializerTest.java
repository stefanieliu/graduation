package cn.ict.carc.christine.Lucene;

import static org.junit.Assert.*;

import java.io.IOException;

import org.dom4j.DocumentException;
import org.junit.Test;

public class IndexInitializerTest {

	@Test
	public void test() throws DocumentException, IOException {
		IndexInitializer init = new IndexInitializer();
		init.init();
		init.init4Search();
		IndexReaderPool.getInstance().listTerms();
	}
}
