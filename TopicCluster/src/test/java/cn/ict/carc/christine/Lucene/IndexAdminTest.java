package cn.ict.carc.christine.Lucene;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.xml.sax.SAXException;

import cn.ict.carc.christine.Preprocesser.LawExtractor;
import cn.ict.carc.christine.TopicCluster.QueryExpander;
import cn.ict.carc.christine.bean.Law;
import cn.ict.carc.christine.util.Config;
import cn.ict.carc.christine.util.LawFilter;
import cn.ict.carc.christine.util.StringHelper;

public class IndexAdminTest {
	Logger logger = LogManager.getLogger(IndexAdminTest.class);
	
	public void test() throws Exception {
		IndexInitializer initializer = new IndexInitializer();
		initializer.init();
		initializer.init4CreateIndex();
		String NPCFile = "/home/Test/laws-utf8/1.TXT";
		LawExtractor extractor = new LawExtractor();
		List<Law> laws = extractor.parseFromNPCFile(NPCFile);
		IndexAdmin admin = new IndexAdmin();
		admin.delAllIndex();
		admin.writeAllLaws(laws);
		
		String output = "/home/Test/Extract/1/";
		File outputDir = new File(output);
		outputDir.delete();
		outputDir.mkdirs();
		for(Law l: laws) {
			String path = outputDir.getAbsolutePath() + "/" + l.getId() + ".txt";
			try {
				FileWriter writer = new FileWriter(path);
				writer.write(StringHelper.join(l.getTitle(), l.getText()));
				writer.flush();
				writer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		QueryExpander expander = new QueryExpander(Config.TopicClusterDirectory);
		expander.estimateTopicModelWithSaving(outputDir, 1000);
		
		initializer.init4Search();
		IndexAdmin normal = new IndexAdmin();
		ArrayList<Law> result = new ArrayList<Law>();
		System.out.println("TotalHits = " + normal.query("家庭关系 子女赡养", 0, 10, result));
		for(int i=0; i<result.size(); ++i) {
			System.out.println(i+":" +result.get(i).getTitle());
		}
		System.out.println("-------");
		result.clear();
		System.out.println("TotalHits = " +admin.query("家庭关系 子女赡养", 0, 10, result));
		for(int i=0; i<result.size(); ++i) {
			System.out.println(i+":" +result.get(i).getTitle());
		}
	}
	
	@Test
	public void testChapter() throws Exception {
		IndexInitializer initializer = new IndexInitializer();
		initializer.init();
		initializer.init4CreateIndex();
		IndexAdmin admin = new IndexAdmin();
		
		String NPCDIR = "/home/Test/Topic-Related/Origin/";
		LawFilter filter = new LawFilter("婚姻");
		List<Law> its = new ArrayList<Law>();
		//List<Law> chs = new ArrayList<Law>();
		LawExtractor extractor = new LawExtractor();
		File dir = new File(NPCDIR);
		for(File f : dir.listFiles()) {
			if(f.getName().endsWith(".TXT")) {
				its.addAll(extractor.parseItemFromNPCFileWithFilter(f.getAbsolutePath(),filter));
				//chs.addAll(extractor.parseChapterFromNPCFileWithFilter(f.getAbsolutePath(),filter));
			}
		}
		
		String output = "/home/Test/Topic-Related/StandardLaw/";
		File outputDir = new File(output);
		if(outputDir.exists()) {
			for(File f : outputDir.listFiles()) {
				f.delete();
			}
		} else {
			outputDir.mkdirs();
		}
		System.out.println("Got " + its.size() +" Law Item in total");
		//System.out.println("Got " + chs.size()+ " Law Chapter in total");
		
		for(Law l: its) {
			String path = outputDir.getAbsolutePath() + "/" + l.getId() + ".txt";
			try {
				FileWriter writer = new FileWriter(path);
				writer.write(StringHelper.join(l.getTitle(), l.getText()));
				writer.flush();
				writer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		admin.delAllIndex();
		admin.writeAllLaws(its);
		
		QueryExpander expander = new QueryExpander(Config.TopicClusterDirectory);
		expander.estimateTopicModelWithSaving(its, 1000, 1000); 
		expander.estimateTopicModelWithSaving(outputDir, 1000);
		
		initializer.init4Search();
		//IndexAdmin admin = new IndexAdmin();
		ArrayList<Law> result = new ArrayList<Law>();
		System.out.println("TotalHits = " + admin.query("离婚后的子女抚养问题", 0, 50, result));
		for(int i=0; i<result.size(); ++i) {
			System.out.println(i+":" +result.get(i).getText());
		}
		System.out.println("-------");
		result.clear();
		//IndexAdmin admin = new IndexAdmin(true);
		System.out.println("TotalHits = " +admin.queryWithGlobalExpansion("离婚后的子女抚养问题", 0, 50, result));
		for(int i=0; i<result.size(); ++i) {
			System.out.println(i+":" +result.get(i).getText());
		}
		System.out.println("-------");
		result.clear();
		//IndexAdmin admin = new IndexAdmin(true);
		System.out.println("TotalHits = " +admin.queryWithLocalExpansion("离婚后的子女抚养问题", 0, 50, result));
		for(int i=0; i<result.size(); ++i) {
			System.out.println(i+":" +result.get(i).getText());
		}
	}
	
	//@Test
	public void test4Expasion() throws Exception {
		IndexInitializer initializer = new IndexInitializer();
		initializer.init();
		//initializer.init4CreateIndex();
		IndexAdmin admin = new IndexAdmin();
		
		String queryFile = "/home/testSample.txt";
		BufferedReader reader = new BufferedReader(new FileReader(queryFile));
		FileWriter writer = new FileWriter("console");
		
		//String NPCDIR = "/home/Test/Topic-Related/Origin/";
		/*LawFilter filter = new LawFilter("婚姻农业经济环境");
		List<Law> its = new ArrayList<Law>();
		List<Law> chs = new ArrayList<Law>();
		LawExtractor extractor = new LawExtractor();
		File dir = new File(NPCDIR);
		for(File f : dir.listFiles()) {
			if(f.getName().endsWith(".TXT")) {
				chs.addAll(extractor.parseChapterFromNPCFileWithFilter(f.getAbsolutePath(), filter));
				its.addAll(extractor.parseItemFromNPCFileWithFilter(f.getAbsolutePath(),filter));
				
			}
		}
		
		System.out.println("Got " + its.size() +" Law Item in total");
		System.out.println("Got " + chs.size()+ " Law Chapter in total");
		*/
		/*
		for(Law l: its) {
			String path = outputDir.getAbsolutePath() + "/" + l.getId() + ".txt";
			try {
				FileWriter writer = new FileWriter(path);
				writer.write(StringHelper.join(l.getTitle(), l.getText()));
				writer.flush();
				writer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		admin.delAllIndex();
		admin.writeAllLaws(its);
		*/
		//QueryExpander expander = new QueryExpander(Config.TopicClusterDirectory);
		//expander.estimateTopicModelWithSaving(its, 1000, 1000); 
		//expander.estimateTopicModelWithSaving(outputDir, 1000);
		
		initializer.init4Search();
		//IndexAdmin admin = new IndexAdmin();
		String line = null;
		ArrayList<Law> result = new ArrayList<Law>();
		while((line=reader.readLine())!=null) {
			result.clear();
			writer.write("------"+line+"-------\n");
			writer.write("Normal:\n");
			System.out.println("TotalHits = " + admin.query(line, 0, 50, result));
			for(int i=0; i<result.size(); ++i) {
				writer.write(i+":" +result.get(i).getText()+"\n");
			}
			writer.write("\n");
			result.clear();
			writer.write("Global:\n");
			//IndexAdmin admin = new IndexAdmin(true);
			System.out.println("TotalHits = " +admin.queryWithGlobalExpansion(line, 0, 50, result));
			for(int i=0; i<result.size(); ++i) {
				writer.write(i+":" +result.get(i).getText()+"\n");
			}
			writer.write("\n");
			result.clear();
			writer.write("Local:\n");
			//IndexAdmin admin = new IndexAdmin(true);
			System.out.println("TotalHits = " +admin.queryWithLocalExpansion(line, 0, 50, result));
			for(int i=0; i<result.size(); ++i) {
				writer.write(i+":" +result.get(i).getText()+"\n");
			}
		}
		reader.close();
		writer.close();
	}
	
	public void testItem() throws Exception {
		IndexInitializer initializer = new IndexInitializer();
		initializer.init();
		initializer.init4CreateIndex();
		String NPCFile = "/Users/Catherine/Documents/Test/laws-utf8/1.TXT";
		LawExtractor extractor = new LawExtractor();
		List<Law> laws = extractor.parseItemFromNPCFile(NPCFile);
		IndexAdmin admin = new IndexAdmin();
		admin.delAllIndex();
		admin.writeAllLaws(laws);
		
		QueryExpander expander = new QueryExpander(Config.TopicClusterDirectory);
		expander.estimateTopicModelWithSaving(laws, 1000, 1000);
		
		initializer.init4Search();
		ArrayList<Law> result = new ArrayList<Law>();
		System.out.println("TotalHits = " + admin.query("离婚后的子女抚养问题", 0, 10, result));
		for(int i=0; i<result.size(); ++i) {
			System.out.println(i+":" +result.get(i).getText());
		}
		System.out.println("-------");
		result.clear();
		//IndexAdmin admin = new IndexAdmin(true);
		System.out.println("TotalHits = " +admin.queryWithGlobalExpansion("离婚后的子女抚养问题", 0, 10, result));
		for(int i=0; i<result.size(); ++i) {
			System.out.println(i+":" +result.get(i).getText());
		}
		System.out.println("-------");
		result.clear();
		//IndexAdmin admin = new IndexAdmin(true);
		System.out.println("TotalHits = " +admin.queryWithLocalExpansion("离婚后的子女抚养问题", 0, 10, result));
		for(int i=0; i<result.size(); ++i) {
			System.out.println(i+":" +result.get(i).getText());
		}
	}
}
