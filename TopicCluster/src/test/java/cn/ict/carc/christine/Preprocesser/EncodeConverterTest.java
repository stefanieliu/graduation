package cn.ict.carc.christine.Preprocesser;

import java.io.File;
import java.nio.charset.Charset;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

public class EncodeConverterTest {
	Logger logger = LogManager.getLogger(EncodeConverterTest.class);
	//@Test
	public void TestConvertDirectory( ) {
		String srcDir = "/Users/Catherine/Documents/Test/laws";
		String destDir = "/Users/Catherine/Documents/Test/laws-utf8";
		
		File src = new File(srcDir);
		File dest = new File(destDir);

		EncodeConverter converter = new EncodeConverter();
		/*if(src.isDirectory()) {
			for(File file : src.listFiles()) {
				if(file.getName().endsWith(".TXT")) {
				File destFile = new File(destDir+"/"+file.getName());
				converter.convert(file, Charset.forName("gbk"), destFile, Charset.forName("utf-8"));
				}
			}
		}*/
		converter.convertDirectory(src, Charset.forName("gbk"), dest, Charset.forName("utf-8"), "[\\w\\s]+\\.TXT");
	}
	
	@Test
	public void TestConvertMultiLevelDirectory()
	{
		String srcDir = "/Users/Catherine/Documents/Test/Law-xml";
		String destDir = "/Users/Catherine/Documents/Test/Law-xml-utf8";
		
		File src = new File(srcDir);
		File dest = new File(destDir);

		EncodeConverter converter = new EncodeConverter();
		/*if(src.isDirectory()) {
			for(File file : src.listFiles()) {
				if(file.getName().endsWith(".TXT")) {
				File destFile = new File(destDir+"/"+file.getName());
				converter.convert(file, Charset.forName("gbk"), destFile, Charset.forName("utf-8"));
				}
			}
		}*/
		converter.convertDirectory(src, Charset.forName("gbk"), dest, Charset.forName("utf-8"), "[\\w\\s]+\\.xml");
	}
}
