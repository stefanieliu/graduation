package cn.ict.carc.christine.TopicCluster;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;

import cc.mallet.extract.StringSpan;
import cc.mallet.extract.StringTokenization;
import cc.mallet.pipe.Pipe;
import cc.mallet.types.Instance;
import cc.mallet.types.TokenSequence;

public class ChineseCharSequence2TokenSequence extends
		Pipe implements Serializable {
	Logger logger = LogManager.getLogger(ChineseCharSequence2TokenSequence.class);
	Analyzer analyzer;
	public ChineseCharSequence2TokenSequence (Analyzer analyzer)
	{
		this.analyzer = analyzer;
	}

	public ChineseCharSequence2TokenSequence ()
	{
		this (new SmartChineseAnalyzer());
	}

	public Instance pipe (Instance carrier)
	{
		CharSequence cs = (CharSequence) carrier.getData();
		try {
			TokenStream stream = analyzer.tokenStream("", new StringReader(cs.toString()));
			TokenSequence ts = new StringTokenization (cs);
			stream.reset();
			while(stream.incrementToken()) {
				CharTermAttribute term = stream.getAttribute(CharTermAttribute.class);
				OffsetAttribute offset = stream.getAttribute(OffsetAttribute.class);
				//logger.debug(term.toString()+"("+offset.startOffset()+","+offset.endOffset()+")");
				ts.add(new StringSpan(cs, offset.startOffset(), offset.endOffset()));
			}
			stream.close();
			carrier.setData(ts);
		} catch(IOException e) {
			e.printStackTrace();
		}
		return carrier;
	}
	
	// Serialization 
	
	private static final long serialVersionUID = 1;
	private static final int CURRENT_SERIAL_VERSION = 0;
	
	private void writeObject (ObjectOutputStream out) throws IOException {
		out.writeInt(CURRENT_SERIAL_VERSION);
		out.writeObject(analyzer);
	}
	
	private void readObject (ObjectInputStream in) throws IOException, ClassNotFoundException {
		int version = in.readInt ();
		analyzer = (Analyzer) in.readObject();
	}

}
