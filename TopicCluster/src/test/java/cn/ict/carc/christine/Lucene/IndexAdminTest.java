package cn.ict.carc.christine.Lucene;

import java.io.File;
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
import cn.ict.carc.christine.util.StringHelper;

public class IndexAdminTest {
	Logger logger = LogManager.getLogger(IndexAdminTest.class);
	
	public void test() throws Exception {
		IndexInitializer initializer = new IndexInitializer();
		initializer.init();
		initializer.init4CreateIndex();
		String NPCFile = "/Users/Catherine/Documents/Test/laws-utf8/1.TXT";
		LawExtractor extractor = new LawExtractor();
		List<Law> laws = extractor.parseFromNPCFile(NPCFile);
		IndexAdmin admin = new IndexAdmin(true);
		admin.delAllIndex();
		admin.writeAllLaws(laws);
		
		String output = "/Users/Catherine/Documents/Test/Extract/1/";
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
		IndexAdmin normal = new IndexAdmin(false);
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
		Config.IndexDirectory="indexChapterlog";
		Config.TopicClusterDirectory="topicChapterlog";
		initializer.init4CreateIndex();
		String NPCFile = "/Users/Catherine/Documents/Test/laws-utf8/1.TXT";
		LawExtractor extractor = new LawExtractor();
		List<Law> laws = extractor.parseChapterFromNPCFile(NPCFile);
		IndexAdmin admin = new IndexAdmin(true);
		admin.delAllIndex();
		admin.writeAllLaws(laws);
		
		String output = "/Users/Catherine/Documents/Test/Extract-Chapter/1/";
		File outputDir = new File(output);
		outputDir.delete();
		outputDir.mkdirs();
		for(Law l: laws) {
			String path = outputDir.getAbsolutePath() + "/" + l.getTitle() + ".txt";
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
		IndexAdmin normal = new IndexAdmin(false);
		ArrayList<Law> result = new ArrayList<Law>();
		System.out.println("TotalHits = " + normal.query("离婚后的子女赡养", 0, 10, result));
		for(int i=0; i<result.size(); ++i) {
			System.out.println(i+":" +result.get(i).getTitle());
		}
		System.out.println("-------");
		result.clear();
		//IndexAdmin admin = new IndexAdmin(true);
		System.out.println("TotalHits = " +admin.query("离婚后的子女赡养", 0, 10, result));
		for(int i=0; i<result.size(); ++i) {
			System.out.println(i+":" +result.get(i).getTitle());
		}
	}
	
	
	public void testItem() throws Exception {
		IndexInitializer initializer = new IndexInitializer();
		initializer.init();
		Config.IndexDirectory="indexItem";
		Config.TopicClusterDirectory="topicItem";
		initializer.init4CreateIndex();
		String NPCFile = "/Users/Catherine/Documents/Test/laws-utf8/1.TXT";
		LawExtractor extractor = new LawExtractor();
		List<Law> laws = extractor.parseItemFromNPCFile(NPCFile);
		IndexAdmin admin = new IndexAdmin(true);
		admin.delAllIndex();
		admin.writeAllLaws(laws);
		
		String output = "/Users/Catherine/Documents/Test/Extract-Item/1/";
		File outputDir = new File(output);
		outputDir.delete();
		outputDir.mkdirs();
		for(Law l: laws) {
			String path = outputDir.getAbsolutePath() + "/" + l.getTitle() + ".txt";
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
		IndexAdmin normal = new IndexAdmin(false);
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
}
