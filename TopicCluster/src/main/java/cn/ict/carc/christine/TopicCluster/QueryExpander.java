package cn.ict.carc.christine.TopicCluster;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cn.ict.carc.christine.bean.ExpansionWord;
import cn.ict.carc.christine.bean.Law;
import cn.ict.carc.christine.bean.LinkedMatrix;
import cn.ict.carc.christine.bean.MaxHeapElement;
import cn.ict.carc.christine.util.ArrayHelper;
import cn.ict.carc.christine.util.PrintHelper;

public class QueryExpander {
	private final Logger logger = LogManager.getLogger(QueryExpander.class);
	
	private TopicModel model;
	private String output_dir = "output";
	private final static double THRESHOLD = 10e-6;
	private final static int DICTIONARY_ITEM_LENGTH = 100;
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
	
	public void estimateTopicModelOnline(Collection<Law> laws, int numTopics, int numIterations) {
		model = new TopicModel(numTopics, 1, 0.01);
		model.importLaws(laws);
		model.estimate(numIterations);
	}
	
	public void estimateTopicModelWithSaving(File inputDir, int numIterations) throws Exception {
		logger.debug("Estimate Topic Model by "+inputDir.getAbsolutePath() +" with Saving Results");
		model = new TopicModel();
		model.setOutputDir(output_dir);
		model.importData(inputDir);
		model.estimate(numIterations);
		model.saveModel();
		saveMiddleResult();
	}
	public void estimateTopicModelWithSaving(Collection<Law> laws, int numTopics, int numIterations) throws Exception {
		model = new TopicModel(numTopics, 1, 0.01);
		model.setOutputDir(output_dir);
		model.importLaws(laws);
		model.estimate(numIterations);
		model.saveModel();
		saveMiddleResult();
	}
	
	private void saveMiddleResult() throws InterruptedException {
		int[] dictionary = model.getTopWordsWithoutSingleCharacter(DICTIONARY_ITEM_LENGTH, THRESHOLD);
		
		double[][] document_topic = new double[model.getNumDocs()][model.getNumTopics()];
        double[][] word_document = new double[model.getNumWords()][model.getNumDocs()];
        model.generateDocumentTopicMatrix(document_topic, THRESHOLD);
        model.generateWordDocumentMatrixWithLog(word_document);
        double[][] check = model.generateCheck(word_document);
        int threadCount = 20;
        int num = model.getNumTopics() / threadCount;
        WordCoffThread[] threads = new WordCoffThread[threadCount];
        for(int i=0; i<threadCount; ++i) {
        	threads[i] = new WordCoffThread(i*num, num, model.getNumWords(), DICTIONARY_ITEM_LENGTH, document_topic, word_document, check);
        	threads[i].start();
        }
        boolean finish = true;
        do {
        	Thread.currentThread().sleep(1000);
        	finish = true;
        	for(int i=0; i<threadCount; ++i) {
        		if(threads[i].isAlive()) {
        			finish = false;
        			break;
        		}
        	}
        } while(!finish);
	}
	
	private class WordCoffThread extends Thread {
		private int startTopic;
    	private int num;
    	private int numDictWords;
    	private LinkedMatrix word_dictword = null;
    	private double[][] doc_topic = null;
    	private double[][] word_doc = null;
    	private double[][] check = null;
		public WordCoffThread(int startTopic, int num, int numWords, int numDictWords, double[][] doc_topic, double[][] word_doc, double[][] check) {
			this.startTopic = startTopic;
			this.num = num;
			this.word_dictword = new LinkedMatrix(numWords, numWords);
			this.numDictWords = numDictWords;
			this.doc_topic = doc_topic;
			this.word_doc = word_doc;
			this.check = check;
		}
		
		@Override
		public void run() {
			for(int i=startTopic; i<startTopic + num; ++i) {
				this.word_dictword.clear();
				model.generateWordCoffMatrixWithDictionary(i, word_dictword, numDictWords ,word_doc, doc_topic, check ,10e-6);
            	try {
					PrintHelper.printLinkedMatrix(new PrintWriter(new FileWriter(output_dir+"/word_word("+i+").txt")), word_dictword, 6, 10e-6);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public double [] predictTopicProbs(String query) throws Exception {
		return model.predictTopic(query);
	}
	
	public List<ExpansionWord> expanseOffline(String query) throws Exception {
		if(model==null) {
			logger.debug("Load Topic Model by "+output_dir+"/model");
			model = TopicModel.loadModel(output_dir);
		}
		double [] wordProbs = new double[model.getNumWords()];
		int numwords = model.getNumWords();
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
		
		Arrays.fill(wordProbs, 0);
		while(iter.hasNext()&&k<TOP_K) {
			MaxHeapElement e = iter.next();
			if(e.value >= THRESHOLD) {
				logger.debug("Calc with Topic " + e.index + " for expanse("+e.value+")");
				ArrayHelper.addInPlace(wordProbs, this.addExpansionOfTopic(e.index, topicProbs[e.index], queryword, numwords));
			}
			++k;
		}
		return getTopExpansionWords(wordProbs,TOP_K);
	}
	
	public List<ExpansionWord> expanseOnline(String query) throws Exception {
		double[][] document_topic = new double[model.getNumDocs()][model.getNumTopics()];
        double[][] word_document = new double[model.getNumWords()][model.getNumDocs()];
        model.generateDocumentTopicMatrix(document_topic, THRESHOLD);
        model.generateWordDocumentMatrixWithLog(word_document);
        double [] wordProbs = new double[model.getNumWords()];
        int numwords = model.getNumWords();
        
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
		
		Arrays.fill(wordProbs, 0);
		while(iter.hasNext()&&k<TOP_K) {
			MaxHeapElement e = iter.next();
			if(e.value >= THRESHOLD) {
				logger.debug("Calc with Topic " + e.index + " for expanse("+e.value+")");
				ArrayHelper.addInPlace(wordProbs, this.addExpansionOfTopicOnline(e.index, e.value, queryword, numwords, document_topic, word_document));
			}
			++k;
		}
		return getTopExpansionWords(wordProbs,TOP_K);
	}
	
	private double[] addExpansionOfTopicOnline(int topic_id, double topic_probs ,
			int[] queryword, int numwords, double[][] doc_topic, double[][] word_doc) {
		long start = System.currentTimeMillis();
		//LinkedMatrix lm = LinkedMatrix.load(output_dir+"/word_word("+topic_id+").txt");
		LinkedMatrix lm = new LinkedMatrix(numwords, numwords);
		model.generateWordCoffMatrixWithQueryAndDictionary(topic_id, lm, queryword, TOP_K ,word_doc, doc_topic, 10e-6);
		double[] probs = new double[numwords];
		long ioend = System.currentTimeMillis();
		for(int i=0; i<queryword.length; ++i) {
			if(lm.existRow(queryword[i])) {
				Map<Integer, Double> rows = lm.getRow(queryword[i]);
				Iterator<Entry<Integer, Double> > iter = rows.entrySet().iterator();
				while(iter.hasNext()) {
					Entry<Integer,Double> entry = iter.next();
					if(probs[entry.getKey()]==0) {
						probs[entry.getKey()]=entry.getValue();
					} else {
						probs[entry.getKey()]*=entry.getValue();
					}
				}
			}
		}
		ArrayHelper.plusInPlace(probs, topic_probs);
		long caend = System.currentTimeMillis();
		logger.debug("IO Time:" + (ioend - start)+" Millis, Cal Time:"+(caend - ioend) +" Millis");
		return probs;
	}

	private TreeSet<MaxHeapElement> getMaxHeap(double[] array) {
		TreeSet<MaxHeapElement> heap = new TreeSet<MaxHeapElement>();
		for(int i=0; i<array.length; ++i) {
			heap.add(new MaxHeapElement(i, array[i]));
		}
		return heap;
	}

	private List<ExpansionWord> getTopExpansionWords(double[] probs, int topK) {
		List<ExpansionWord> words = new ArrayList<ExpansionWord>();
		TreeSet<MaxHeapElement> heap = getMaxHeap(probs);
		Iterator<MaxHeapElement> iter =  heap.iterator();
		int k=0;
		while(iter.hasNext()&&k<topK) {
			MaxHeapElement e = iter.next();
			logger.debug(e.index+":"+ model.getWord(e.index)+ "(" +e.value+ ")");
			words.add(new ExpansionWord(model.getWord(e.index), e.value));
			++k;
		}
		return words;
	}
	
	private double[] addExpansionOfTopic(int topic_id, double query_prob, int[] queryword, int numwords) throws IOException {
		long start = System.currentTimeMillis();
		LinkedMatrix lm = LinkedMatrix.load(output_dir+"/word_word("+topic_id+").txt");
		long ioend = System.currentTimeMillis();
		double[] probs = new double[numwords];
		Arrays.fill(probs, 0);
		for(int i=0; i<queryword.length; ++i) {
			if(lm.existRow(queryword[i])) {
				Map<Integer, Double> rows = lm.getRow(queryword[i]);
				Iterator<Entry<Integer, Double> > iter = rows.entrySet().iterator();
				while(iter.hasNext()) {
					Entry<Integer,Double> entry = iter.next();
					if(probs[entry.getKey()]==0) {
						probs[entry.getKey()]=entry.getValue();
					} else {
						probs[entry.getKey()]*=entry.getValue();
					}
				}
			}
		}
		ArrayHelper.plusInPlace(probs, query_prob);
		long caend = System.currentTimeMillis();
		logger.debug("IO Time:" + (ioend - start)+" Millis, Cal Time:"+(caend - ioend) +" Millis");
		return probs;
	}
}
