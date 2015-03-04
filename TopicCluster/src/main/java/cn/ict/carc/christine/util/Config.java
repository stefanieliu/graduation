package cn.ict.carc.christine.util;

import java.io.File;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.xml.sax.SAXException;

public class Config {
	private final static Logger logger = LogManager.getLogger(Config.class);
	
	public static String IndexDirectory=null;
	public static String DefaultIndex=null;
	public static String TopicClusterDirectory=null;
	//public static String ICTCLASDirectory=null;

	public static void loadConfig() throws DocumentException {
		SAXReader reader = new SAXReader();
		Document doc = reader.read(new File(C.FILE_CONFIG));
		Element config = doc.getRootElement();
		
		IndexDirectory = config.elementText(C.TAG_INDEX_DIRECTORY);
		DefaultIndex = config.elementText(C.TAG_DEFAULT_INDEX);
		TopicClusterDirectory = config.elementText(C.TAG_TOPIC_CLUSTER);
		//ICTCLASDirectory = nodes.item(0).getTextContent();
		logger.info("Load Config: IndexDirectory="+IndexDirectory+", DefaultIndex="+DefaultIndex+", TopicClusterDirectory="+TopicClusterDirectory);//+", ICTCLASDirectory="+ICTCLASDirectory);
	}
}
