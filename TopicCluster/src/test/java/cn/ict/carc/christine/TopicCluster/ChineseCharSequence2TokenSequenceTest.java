package cn.ict.carc.christine.TopicCluster;

import static org.junit.Assert.*;

import java.io.File;

import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.junit.Test;

import cc.mallet.pipe.Input2CharSequence;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.types.Instance;
import cc.mallet.types.SingleInstanceIterator;
import cc.mallet.types.TokenSequence;

public class ChineseCharSequence2TokenSequenceTest {

	@Test
	public void test() {
		try {
			//String file = "/Users/Catherine/Documents/Test/laws-utf8/Extract/1/1.txt";
			String file = "/Users/Catherine/Documents/Test/phrase.txt";
			Instance carrier = new Instance (new File(file), null, null, null);
			SerialPipes p = new SerialPipes (new Pipe[] {
			new Input2CharSequence ("utf-8"),
			new ChineseCharSequence2TokenSequence(new SmartChineseAnalyzer())});
			carrier = p.newIteratorFrom (new SingleInstanceIterator(carrier)).next();
			TokenSequence ts = (TokenSequence) carrier.getData();
			System.out.println (ts.toString());
		} catch (Exception e) {
			System.out.println (e);
			e.printStackTrace();
		}
	}

}
