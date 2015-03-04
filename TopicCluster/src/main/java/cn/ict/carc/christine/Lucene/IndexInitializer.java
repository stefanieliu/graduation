package cn.ict.carc.christine.Lucene;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.DocumentException;
import org.xml.sax.SAXException;

import cn.ict.carc.christine.util.Config;

public class IndexInitializer {
	private final static Logger logger = LogManager.getLogger(IndexInitializer.class);
	
	public IndexInitializer() {
		
	}
	public boolean init() throws DocumentException  {
//		try {
			Config.loadConfig();
		/*} catch (Exception e) {
			logger.error(e.getMessage());
			return false;
		}*/
		return true;
	}
	public boolean init4Search() {
		return IndexReaderPool.getInstance().init();
	}
	
	public boolean init4CreateIndex() {
		return IndexWriterPool.getInstance().init();
	}
}
