package cn.ict.carc.christine.util;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Config {
	private final static Logger logger = LogManager.getLogger(Config.class);
	
	public static String IndexDirectory=null;
	public static String DefaultIndex=null;
	public static String TopicClusterDirectory=null;
	//public static String ICTCLASDirectory=null;

	public static void loadConfig() throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document doc = builder.parse(C.FILE_CONFIG);
		Element root = doc.getDocumentElement();
		NodeList nodes = root.getElementsByTagName(C.TAG_INDEX_DIRECTORY);
		assert(nodes.getLength()==1);
		IndexDirectory = nodes.item(0).getTextContent();
		nodes = root.getElementsByTagName(C.TAG_DEFAULT_INDEX);
		assert(nodes.getLength()==1);
		DefaultIndex = nodes.item(0).getTextContent();
		nodes = root.getElementsByTagName(C.TAG_TOPIC_CLUSTER);
		assert(nodes.getLength()==1);
		TopicClusterDirectory = nodes.item(0).getTextContent();
		//ICTCLASDirectory = nodes.item(0).getTextContent();
		logger.info("Load Config: IndexDirectory="+IndexDirectory+", DefaultIndex="+DefaultIndex+", TopicClusterDirectory="+TopicClusterDirectory);//+", ICTCLASDirectory="+ICTCLASDirectory);
	}
}
