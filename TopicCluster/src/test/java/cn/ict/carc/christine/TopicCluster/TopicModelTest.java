package cn.ict.carc.christine.TopicCluster;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import cn.ict.carc.christine.Preprocesser.LawExtractor;
import cn.ict.carc.christine.bean.Law;

public class TopicModelTest {
	//@Test
	public void test() throws Exception {
		TopicModel model = new TopicModel();
		model.importData(new File("/Users/Catherine/Documents/Test/laws-utf8/Extract-New/1"));
		model.setOutputDir("output_2");
		model.estimate(1000);
		model.saveModel();
		model.saveResult();
	}
	
	@Test
	public void testLaw() {
		TopicModel model = new TopicModel();
		model.setOutputDir("/Users/Catherine/Documents/Output/LawTotalDirect");
		
		String NPCDir = "/Users/Catherine/Documents/Test/laws-utf8/";
		File dir = new File(NPCDir);
		List<Law> laws = new ArrayList<Law>();
		for(File f : dir.listFiles()) {
			if(f.getName().endsWith(".TXT")) {
				System.out.print("Extract "+f.getAbsolutePath());
				LawExtractor extractor = new LawExtractor();
				List<Law> l = extractor.parseChapterFromNPCFile(f.getAbsolutePath());
				System.out.println("="+l.size());
				laws.addAll(l);
			}
		}
		System.out.println("Total="+laws.size());
		model.importLaws(laws);
		model.estimate(2000);
		model.saveModel();
	}
}
