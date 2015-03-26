package cn.ict.carc.christine.Preprocesser;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import cn.ict.carc.christine.bean.Law;
import cn.ict.carc.christine.util.PrintHelper;
import cn.ict.carc.christine.util.StringHelper;

public class LawExtractorTest {
	Logger logger = LogManager.getLogger(LawExtractorTest.class);
	
	public void TestXmlExtractor() {
		String xmlFile = "/Users/Catherine/Documents/Test/Law-xml/法律及有关问题的决定(1395)/1.xml";
		LawExtractor extractor = new LawExtractor();
		Law law = extractor.parseFromXml(xmlFile);
		for(Field field : law.getClass().getDeclaredFields()) {
			Method method;
			try {
				method = law.getClass().getMethod("get" + field.getName().substring(0,1).toUpperCase() + field.getName().substring(1));
				logger.debug(field.getName()+" = "+method.invoke(law));
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	
	@Test
	public void TestNPCChapterFileExtractor() {
		String NPCFile = "/Users/Catherine/Documents/Test/laws-utf8";
		File dir = new File(NPCFile);
		
		LawExtractor extractor = new LawExtractor();
		for(File f : dir.listFiles()) {
			if(!f.getName().endsWith(".TXT")) {
				continue;
			}
			logger.info(f.getAbsolutePath());
			List<Law> lists = extractor.parseChapterFromNPCFile(f.getAbsolutePath());
			logger.debug("Get "+ lists.size() +" Laws from "+f.getName());
			String output = "/Users/Catherine/Documents/Test/Chapter-Data/" + f.getName().substring(0, f.getName().length()-4);
			File outputDir = new File(output);
			outputDir.delete();
			outputDir.mkdirs();
			for(Law l: lists) {
				String path = outputDir.getAbsolutePath() + "/" + l.getTitle().replace('/', ' ') + ".txt";
				try {
					FileWriter writer = new FileWriter(path);
					writer.write(StringHelper.join(l.getTitle(), l.getText()));
					writer.flush();
					writer.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					PrintHelper.printLaw(l);
					e.printStackTrace();
				}
			}
		}
	}
	
	@Test
	public void TestNPCFileExtractor() {
		String NPCFile = "/Users/Catherine/Documents/Test/laws-utf8";
		File dir = new File(NPCFile);
		
		LawExtractor extractor = new LawExtractor();
		for(File f : dir.listFiles()) {
			if(!f.getName().endsWith(".TXT")) {
				continue;
			}
			logger.info(f.getAbsolutePath());
			List<Law> lists = extractor.parseFromNPCFile(f.getAbsolutePath());
			logger.debug("Get "+ lists.size() +" Laws from "+f.getName());
			String output = "/Users/Catherine/Documents/Test/Law-Data/" + f.getName().substring(0, f.getName().length()-4);
			File outputDir = new File(output);
			outputDir.delete();
			outputDir.mkdirs();
			for(Law l: lists) {
				String path = outputDir.getAbsolutePath() + "/" + l.getTitle().replace('/', ' ') + ".txt";
				try {
					FileWriter writer = new FileWriter(path);
					writer.write(StringHelper.join(l.getTitle(), l.getText()));
					writer.flush();
					writer.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					PrintHelper.printLaw(l);
					e.printStackTrace();
				}
			}
		}
	}

}
