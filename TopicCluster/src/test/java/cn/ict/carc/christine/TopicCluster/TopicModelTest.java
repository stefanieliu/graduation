package cn.ict.carc.christine.TopicCluster;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

public class TopicModelTest {
	@Test
	public void test() throws Exception {
		TopicModel model = new TopicModel();
		model.importData(new File("/Users/Catherine/Documents/Test/laws-utf8/Extract-New/1"));
		model.setOutputDir("output_2");
		model.estimate(1000);
		model.saveModel();
		model.saveResult();
		
	}
}
