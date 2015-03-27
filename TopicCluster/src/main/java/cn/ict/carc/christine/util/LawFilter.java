package cn.ict.carc.christine.util;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;

import cn.ict.carc.christine.bean.Law;

public class LawFilter {
	private String query;
	List<String> keywords;
	public LawFilter(String query) throws IOException {
		this.query = query;
		this.keywords = new ArrayList<String>();
		init();
	}
	
	private void init() throws IOException {
		SmartChineseAnalyzer analyzer = new SmartChineseAnalyzer();
		TokenStream stream = analyzer.tokenStream("", new StringReader(query));
		stream.reset();
		while(stream.incrementToken()) {
			CharTermAttribute term = stream.getAttribute(CharTermAttribute.class);
			OffsetAttribute offset = stream.getAttribute(OffsetAttribute.class);
			keywords.add(term.toString());
		}
		stream.close();
	}
	
	public boolean accept(Law law) {
		for(String key : keywords) {
			if(law.getText().contains(key)) {
				return true;
			}
		}
		return false;
	}
	
	public List<Law> filter(List<Law> origin) {
		List<Law> result = new ArrayList<Law>();
		Iterator<Law> iter = origin.iterator();
		while(iter.hasNext()) {
			Law current = iter.next();
			if(accept(current)) {
				result.add(current);
			}
		}
		return result;
	}
}
