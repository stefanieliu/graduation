package cn.ict.carc.christine.TopicCluster;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

public class QueryExpanderTest {

	@Test
	public void test() throws Exception {
		QueryExpander expander = new QueryExpander("output");
		//expander.
		//expander.EstimateTopicModel(new File("/Users/Catherine/Documents/Test/laws-utf8/Extract-New/1"));
		System.out.println(expander.expanse("子女赡养"));
	}

}
