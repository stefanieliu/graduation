package cn.ict.carc.christine.Preprocesser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class EncodeConverter {
	public final static Logger logger = LogManager.getLogger(EncodeConverter.class);
	public final static int BUFFER_SIZE = 4096;
	
	public String convert(String src, Charset from, Charset to) {
		byte[] srcBytes = src.getBytes(from);
		return new String(srcBytes, to);
	}
	
	public void convert(File src, Charset from, File dest, Charset to) {
		logger.info("convert "+src.getAbsolutePath()+"("+from.displayName()+") to "+dest.getAbsolutePath()+"("+to.displayName()+")");
		try {
			Reader reader = new InputStreamReader(new FileInputStream(src),from);
			Writer writer = new OutputStreamWriter(new FileOutputStream(dest),to);
			char[] buffer = new char[BUFFER_SIZE];
			int length = -1;
			while((length = reader.read(buffer, 0, BUFFER_SIZE)) != -1) {
				writer.write(buffer, 0, length);
			}
			writer.flush();
			reader.close();
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void convertDirectory(File srcDir, Charset from, File destDir, Charset to, String fileTypePattern) {
		logger.info("------Directory(" + srcDir.getAbsolutePath() + ") convert Start------");
		destDir.delete();
		destDir.mkdirs();
		for(File file : srcDir.listFiles()) {
			if(file.isDirectory()) {
				convertDirectory(file, from, new File(destDir + "/" + file.getName()), to, fileTypePattern);
			} else if(file.getName().matches(fileTypePattern)) {
				convert(file, from, new File(destDir + "/" + file.getName()), to);
			}
		}
		logger.info("------Directory(" + srcDir.getAbsolutePath() + ") convert Stop-------");
	}
}
