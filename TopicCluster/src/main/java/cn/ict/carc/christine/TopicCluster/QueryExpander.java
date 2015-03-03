package cn.ict.carc.christine.TopicCluster;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cn.ict.carc.christine.bean.LinkedMatrix;
import cn.ict.carc.christine.bean.MaxHeapElement;

public class QueryExpander {
	private final Logger logger = LogManager.getLogger(QueryExpander.class);
	
	private TopicModel model;
	private String output_dir = "output";
	private final static double THRESHOLD = 10e-3;
	private final static int TOP_K=10;
	
	public QueryExpander(String output_dir) {
		model = null;
		this.output_dir = output_dir;
	}
	
	public void estimateTopicModel(File inputDir, int numIterations) {
		logger.debug("Estimate Topic Model by "+inputDir.getAbsolutePath());
		model = new TopicModel();
		model.importData(inputDir);
		model.estimate(numIterations);
	}
	
	public void estimateTopicModelWithSaving(File inputDir, int numIterations) throws Exception {
		logger.debug("Estimate Topic Model by "+inputDir.getAbsolutePath() +" with Saving Results");
		model = new TopicModel();
		model.setOutputDir(output_dir);
		model.importData(inputDir);
		model.estimate(numIterations);
		model.saveModel();
		model.saveResult();
	}
	
	public double [] predictTopicProbs(String query) throws Exception {
		if(model==null) {
			logger.debug("Load Topic Model by "+output_dir+"/model");
			model = TopicModel.loadModel(output_dir);
		}
		return model.predictTopic(query);
	}
	
	public String expanse(String query) throws Exception {
		double [] topicProbs = this.predictTopicProbs(query);
		int[] queryword = model.getQueryWordIndexes(query);
		for(int i=0; i<queryword.length; ++i) {
			if(queryword[i]!=-1) {
				logger.debug("("+i+")"+queryword[i]+":"+model.getWord(queryword[i]));
			}
		}
		TreeSet<MaxHeapElement> heap = getMaxHeap(topicProbs);
		Iterator<MaxHeapElement> iter =  heap.iterator();
		int k=0;
		double [] wordProbs = new double[model.getNumWords()];
		Arrays.fill(wordProbs, 0);
		while(iter.hasNext()&&k<TOP_K) {
			MaxHeapElement e = iter.next();
			if(e.value >= THRESHOLD) {
				logger.debug("Calc with Topic " + e.index + " for expanse("+e.value+")");
				this.addExpansionOfTopic(e.index, topicProbs[e.index], queryword, wordProbs);
			}
			++k;
		}
		return getTopWords(wordProbs,TOP_K);
	}
	
	private TreeSet<MaxHeapElement> getMaxHeap(double[] array) {
		TreeSet<MaxHeapElement> heap = new TreeSet<MaxHeapElement>();
		for(int i=0; i<array.length; ++i) {
			heap.add(new MaxHeapElement(i, array[i]));
		}
		return heap;
	}

	private String getTopWords(double[] probs, int topK) {
		StringBuilder builder = new StringBuilder();
		TreeSet<MaxHeapElement> heap = getMaxHeap(probs);
		Iterator<MaxHeapElement> iter =  heap.iterator();
		int k=0;
		while(iter.hasNext()&&k<topK) {
			MaxHeapElement e = iter.next();
			logger.debug(e.index+":"+ model.getWord(e.index)+ "(" +e.value+ ")");
			builder.append((builder.length()==0?"":" ")+model.getWord(e.index));
			++k;
		}
		return builder.toString();
	}
	
	private void addExpansionOfTopic(int topic_id, double query_prob, int[] queryword, double[] out_probs) throws IOException {
		LinkedMatrix lm = LinkedMatrix.load(output_dir+"/word_word("+topic_id+").txt");
		for(int i=0; i<queryword.length; ++i) {
			if(queryword[i]!=-1) {
				for(int j=0; j<out_probs.length; ++j) {
					out_probs[j]+=lm.get(queryword[i], j)*query_prob;
				}
			}
		}
	}
}
