package cn.ict.carc.christine.TopicCluster;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;

import cc.mallet.pipe.Input2CharSequence;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.TokenSequenceRemoveStopwords;
import cc.mallet.pipe.iterator.FileIterator;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.topics.TopicInferencer;
import cc.mallet.types.Alphabet;
import cc.mallet.types.FeatureSelection;
import cc.mallet.types.FeatureSequence;
import cc.mallet.types.FeatureVector;
import cc.mallet.types.IDSorter;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.types.LabelAlphabet;
import cc.mallet.types.LabelSequence;
import cc.mallet.types.Labeling;
import cn.ict.carc.christine.bean.Law;
import cn.ict.carc.christine.bean.LinkedMatrix;
import cn.ict.carc.christine.util.CollectionHelper;
import cn.ict.carc.christine.util.PrintHelper;
import cn.ict.carc.christine.util.TxtFilter;

public class TopicModel {
	
	private final Logger logger = LogManager.getLogger(TopicModel.class);
	
	//parameter for Mallet Topic Model
	private int numTopics;
	private double alphaSum;
	private double beta;
	
	//parameter for Model Training
	private int numThreads = 2;
	private int numIterations = 50;
	
	private InstanceList instances = null;
	private ParallelTopicModel model = null;
	
	//parameter for store the middle results
	private File output_dir;
	private double [][] check;

	public TopicModel() {
		this.numTopics = 100;
		this.alphaSum = 1;
		this.beta = 0.01;
		initInstance();
	}
	
	public TopicModel(int numTopics, double alphaSum, double beta) {
		this.numTopics = numTopics;
		this.alphaSum = alphaSum;
		this.beta = beta;
		initInstance();
	}
	
	private void initInstance() {
		ArrayList<Pipe> pipeList = new ArrayList<Pipe>();
		//file encoder
        pipeList.add( new Input2CharSequence("utf-8"));
        //charset
        pipeList.add( new ChineseCharSequence2TokenSequence(new SmartChineseAnalyzer()) );
        //remove stopwords
        pipeList.add( new TokenSequenceRemoveStopwords(new File("config/stopWords.txt"), "UTF-8", false, false, false) );
        //feature mapping
        pipeList.add( new TokenSequence2FeatureSequence() );
        //print pipe target
        //pipeList.add( new PrintInputAndTarget());
        instances = new InstanceList (new SerialPipes(pipeList));
	}
	
	public void estimate() {
		this.estimate(numIterations);
	}
	
	public void estimate(int numIterations) {
		model = new ParallelTopicModel(numTopics, alphaSum, beta);
		//add training data
        model.addInstances(instances);
       
        model.setNumThreads(numThreads);
        // Run the model for 50 iterations and stop (this is for testing only, 
        //  for real applications, use 1000 to 2000 iterations)
        model.setNumIterations(numIterations);
        
        try {
			model.estimate();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void importData(File dir) {
        FileIterator iterator = new FileIterator(new File[] {dir}, new TxtFilter(), FileIterator.LAST_DIRECTORY);
        instances.addThruPipe(iterator);
	}
	
	public void importLaws(Collection<Law> law) {
		LawIterator iterator = new LawIterator(law);
		instances.addThruPipe(iterator);
	}
	
	public void setOutputDir(String output) {
		this.output_dir = new File(output);
		if(!this.output_dir.exists()) {
			this.output_dir.mkdirs();
		}
	}
	
	public void saveModel() {
		File f = new File(this.output_dir + "/model");
		System.out.println(f.getAbsolutePath());
		model.write(f);
	}
	
	public static TopicModel loadModel(String output_dir) throws Exception {
		TopicModel model = new TopicModel();
		model.setOutputDir(output_dir);
		model.model = ParallelTopicModel.read(new File(model.output_dir + "/model"));
		return model;
	}
	
	public double[] predictTopic(String query) {
		return predictTopic(query, 100);
	}
	
	public double[] predictTopic(String query, int numIterations) {
		InstanceList testing = new InstanceList(instances.getPipe());
		testing.addThruPipe(new Instance(query, null, "query instance", null));

		TopicInferencer inferencer = model.getInferencer();
		double[] probs = inferencer.getSampledDistribution(testing.get(0), numIterations, 1, 5);
		return probs;
	}
	
	public double[][] getDocumentTopicDistribution(double threshold, int topK) {
		double[][] doc_topic = new double[this.getNumDocs()][this.getNumTopics()];
		this.generateDocumentTopicMatrix(doc_topic, threshold);
		return doc_topic;
	}
	
	public int[] getQueryWordIndexes(String query) {
		InstanceList testing = new InstanceList(instances.getPipe());
		testing.addThruPipe(new Instance(query, null, "query instance", null));
		
		FeatureSequence fs = (FeatureSequence) testing.get(0).getData();
		int[] indexes = new int[fs.getLength()];
		Arrays.fill(indexes, -1);
		for(int i=0; i<fs.getLength(); ++i) {
			indexes[i] = getWordIndex(fs.get(i).toString());
		}
		return indexes;
	}
	
	public String[] getWords(int[] indexes) {
		String [] words = new String[indexes.length];
		for(int i=0; i<indexes.length; ++i) {
			if(indexes[i]!=-1) {
				words[i]=model.getAlphabet().lookupObject(indexes[i]).toString();
			}
		}
		return words;
	}
	
	public String getWord(int index) {
		return model.getAlphabet().lookupObject(index).toString();
	}
	
	public int getWordIndex(String word) {
		return model.getAlphabet().lookupIndex(word);
	}
	
	public int[] getTopWordForTopic(int topic, int TopK, double threshold) {
		logger.debug("Begin calc Top "+ TopK +" words for Topic " + "topic");
        long start = System.currentTimeMillis();
        ArrayList<TreeSet<IDSorter>> topicSortedWords = model.getSortedWords();
    	Iterator<IDSorter> iterator = topicSortedWords.get(topic).iterator();
    	TreeSet<Integer> topWords = new TreeSet<Integer>();
    	int count = 0;
		while (iterator.hasNext()&&count++<TopK) {
			IDSorter info = iterator.next();
			if(info.getWeight()<threshold) {
				break;
			}
			topWords.add(info.getID());
		}
        logger.debug("Finish calc Top "+ TopK +" words for Topic " + "topic, Calc Time:"+(System.currentTimeMillis() - start) + "Millis");
        return CollectionHelper.toIntArray(topWords);
	}
	
	public int[] getTopWordForTopicWithoutSingleCharacter(int topic, int TopK, double threshold) {
		//logger.debug("Begin calc Top "+ TopK +" words for Topic " + topic);
        long start = System.currentTimeMillis();
        ArrayList<TreeSet<IDSorter>> topicSortedWords = model.getSortedWords();
    	Iterator<IDSorter> iterator = topicSortedWords.get(topic).iterator();
    	TreeSet<Integer> topWords = new TreeSet<Integer>();
    	int count = 0;
		while (iterator.hasNext()&&count<TopK) {
			IDSorter info = iterator.next();
			if(info.getWeight()<threshold) {
				break;
			}
			if(this.getWord(info.getID()).length()>=2) {
				topWords.add(info.getID());
				count++;
			}
		}
		//logger.debug("Finish calc Top "+ TopK +" words for Topic " + "topic, Calc Time:"+(System.currentTimeMillis() - start) + "Millis");
        return CollectionHelper.toIntArray(topWords);
	}
	
	public int[] getTopWords(int TopK, double threshold) {
		logger.debug("Begin calc Top "+ TopK +" words for each Topic");
        long start = System.currentTimeMillis();
        ArrayList<TreeSet<IDSorter>> topicSortedWords = model.getSortedWords();
        TreeSet<Integer> topWords = new TreeSet<Integer>();
        for(int i=0; i<this.getNumTopics(); ++i) {
	    	Iterator<IDSorter> iterator = topicSortedWords.get(i).iterator();
	    	int count = 0;
			while (iterator.hasNext()&&count++<TopK) {
				IDSorter info = iterator.next();
				if(info.getWeight()<threshold) {
					break;
				}
				topWords.add(info.getID());
			}
        }
        logger.debug("Finish calc Top "+ TopK +" words for each Topic, Calc Time:"+(System.currentTimeMillis() - start) + "Millis");
        return CollectionHelper.toIntArray(topWords);
	}
	
	public int[] getTopWordsWithoutSingleCharacter(int TopK, double threshold) {
		logger.debug("Begin calc Top "+ TopK +" words for each Topic");
        long start = System.currentTimeMillis();
        ArrayList<TreeSet<IDSorter>> topicSortedWords = model.getSortedWords();
        TreeSet<Integer> topWords = new TreeSet<Integer>();
        for(int i=0; i<this.getNumTopics(); ++i) {
	    	Iterator<IDSorter> iterator = topicSortedWords.get(i).iterator();
	    	int count = 0;
			while (iterator.hasNext()&&count<TopK) {
				IDSorter info = iterator.next();
				if(info.getWeight()<threshold) {
					break;
				}
				if(this.getWord(info.getID()).length()>=2) {
					topWords.add(info.getID());
					count++;
				}
			}
        }
        logger.debug("Finish calc Top "+ TopK +" words for each Topic, Calc Time:"+(System.currentTimeMillis() - start) + "Millis");
        return CollectionHelper.toIntArray(topWords);
	}
	
    public void saveResult() throws Exception {
    	//Save model information for review
        FileWriter w1 = new FileWriter(output_dir + "/topic.xml");
        model.topicXMLReport(new PrintWriter(w1), 20);
        w1.close();
        
        FileWriter w2 = new FileWriter(output_dir + "/topicPhrase.xml");
        model.topicPhraseXMLReport(new PrintWriter(w2), 20);
        w2.close();
        
        model.printDocumentTopics(new File(output_dir+"/documentTopics.txt"));
        
        model.printTopicWordWeights(new File(output_dir+"/topicWord.txt"));
        
        model.alphabet.dump(new PrintWriter(new FileWriter(output_dir+"/alphabet.txt")));
        
        //generate middle result of Query Expansion
        //document_topic = new double[this.getNumDocs()][this.getNumTopics()];
        //word_document = new double[this.getNumWords()][this.getNumDocs()];
        LinkedMatrix word_topic = new LinkedMatrix(this.getNumWords(), this.getNumTopics());
        
        //generateDocumentTopicMatrix(document_topic, 0);
        //generateWordDocumentMatrix(word_document);
        //generateWordTopicMatrix(word_topic, 0);
        
        //PrintHelper.printMatrix(new PrintWriter(new FileWriter(output_dir+"/doc_topic.txt")), document_topic, this.getNumDocs(), this.getNumTopics());
        
        PrintHelper.printLinkedMatrix(new PrintWriter(new FileWriter(output_dir+"/word_topic.txt")), word_topic, 6, 10e-6);
        
        word_topic.clear();
    }
    
    private double[][] genCheck(double[][] word_document) {
    	logger.debug("Begin calc Check Matrix");
        long start = System.currentTimeMillis();
    	RealMatrix rm = new BlockRealMatrix(word_document);
    	double[][] ret = rm.multiply(rm.transpose()).getData();
    	rm = null;
    	logger.debug("Finish calc Check Matrix, Calc Time:"+(System.currentTimeMillis() - start) + "Millis");
    	return ret;
	}
    
    private double[][] generateCheck(double[][] word_document) {
    	logger.debug("Begin calc Check Matrix(Custom)"); 
    	long start = System.currentTimeMillis();
    	double [][] result = new double[this.getNumWords()][this.getNumWords()];
    	for(int i=0; i<this.getNumWords(); ++i) {
    		for(int j=0; j<this.getNumWords(); ++j) {
    			result[i][j]=0;
    			for(int k=0; k<this.getNumDocs(); ++k) {
    				result[i][j]+=word_document[i][k]*word_document[j][k];
    			}
    		}
    	}
    	logger.debug("Finish calc Check Matrix, Calc Time:"+(System.currentTimeMillis() - start) + "Millis");
    	return result;
    }
	public void generateWordTopicMatrix(LinkedMatrix m, double threshold) {
        logger.debug("Begin calc Word-Topic Matrix");
        long start = System.currentTimeMillis();
        for(int i=0; i<this.getNumTopics(); ++i) {
        	ArrayList<TreeSet<IDSorter>> topicSortedWords = model.getSortedWords();
    		Iterator<IDSorter> iterator = topicSortedWords.get(i).iterator();
			while (iterator.hasNext()) {
				IDSorter info = iterator.next();
				if(info.getWeight()>=threshold) {
					m.set(info.getID(), i, Math.round(info.getWeight()));
				}
			}
        }
        logger.debug("Finish calc Word-Topic Matrix, Calc Time:"+(System.currentTimeMillis() - start) + "Millis");
    }
    
    public void generateDocumentTopicMatrix(double[][] doc_topic, double threshold) {
    	logger.debug("Begin calc Document-Topic Matrix");
    	long start = System.currentTimeMillis();

    	for(int i=0; i<this.getNumDocs(); ++i) {
    		Arrays.fill(doc_topic[i], 0);
    	}
    	
		int []topicCounts = new int[this.getNumTopics()];
		
		for (int i = 0; i < this.getNumDocs(); ++i) {
			Arrays.fill(topicCounts, 0);
			LabelSequence topicSequence = model.getData().get(i).topicSequence;
			int[] currentDocTopics = topicSequence.getFeatures();

			int docLen = currentDocTopics.length;

			for (int j=0; j < docLen; ++j) {
				topicCounts[ currentDocTopics[j] ]++;
			}

			for (int j = 0; j < this.getNumTopics(); ++j) {
				double probability = (model.alpha[j] + topicCounts[j]) / (docLen + model.alphaSum);
				if(probability >= threshold) {
					doc_topic[i][j] = probability;
				}
			}
		}
		logger.debug("Finish calc Document-Topic Matrix, Calc Time:"+(System.currentTimeMillis() - start) + "Millis");
    }

    public void generateTopicCooccurrenceMatrix(int topic_id, LinkedMatrix m , double[][] word_document, double[][] document_topic, double[][] check, double threshold) {
    	logger.debug("Begin on calc Word_Word Matrix on Topic " + topic_id);
    	long start = System.currentTimeMillis();
    	
    	for (int i=0; i< this.getNumWords(); ++i) {
    		for(int j=i+1; j<this.getNumWords(); ++j) {
    			if(check[i][j]>0) {
	     			double value = 0;
	    			for(int k=0; k<this.getNumDocs(); ++k) {
	    				double cooccurence = word_document[i][k] * word_document[j][k];
	    				value += cooccurence*document_topic[k][topic_id];
	    			}
	    			if(value>=threshold) {
	    				m.set(i, j, value);
	    			}
    			}
    		}
    	}
    	logger.debug("Finish calc Word_Word Matrix on Topic " + topic_id + ", Calc Time:"+(System.currentTimeMillis() - start) + "Millis");
    }
    
    public void generateTopicCooccurrenceMatrix(int topic_id, LinkedMatrix m , double[][] word_document, double[][] document_topic, double threshold) {
    	logger.debug("Begin on calc Word_Word Matrix on Topic " + topic_id);
    	long start = System.currentTimeMillis();
    	
    	for (int i=0; i< this.getNumWords(); ++i) {
    		for(int j=i+1; j<this.getNumWords(); ++j) {
	     		double value = 0;
	    		for(int k=0; k<this.getNumDocs(); ++k) {
	    			double cooccurence = word_document[i][k] * word_document[j][k];
	    			value += cooccurence*document_topic[k][topic_id];
	  			}
	   			if(value>=threshold) {
	   				m.set(i, j, value);
    			}
    		}
    	}
    	logger.debug("Finish calc Word_Word Matrix on Topic " + topic_id + ", Calc Time:"+(System.currentTimeMillis() - start) + "Millis");
    }
    
	public void generateWordDocumentMatrixWithLog(double[][] word_document) {
		logger.debug("Begin on calc Word_Document Matrix");
		long start = System.currentTimeMillis();
    	for(int i=0; i< this.getNumDocs(); ++i) {
    		Instance instance = model.getData().get(i).instance;
    		FeatureSequence tokens = (FeatureSequence) instance.getData();
    		for(int j=0; j<tokens.getLength(); ++j) {
    			word_document[tokens.getIndexAtPosition(j)][i]++;
    		}
    	}
    	for(int i=0; i< this.getNumWords(); ++i) {
    		for(int j=0; j<this.getNumDocs(); ++j) {
    			word_document[i][j]=Math.log(1+word_document[i][j]);
    		}
    	}
    	logger.debug("Finish calc Word_Document Matrix, Calc Time:"+(System.currentTimeMillis() - start) + "Millis");
	}
	
	public void generateWordDocumentMatrix(double[][] word_document) {
		logger.debug("Begin on calc Word_Document Matrix");
		long start = System.currentTimeMillis();
    	for(int i=0; i< this.getNumDocs(); ++i) {
    		Instance instance = model.getData().get(i).instance;
    		FeatureSequence tokens = (FeatureSequence) instance.getData();
    		for(int j=0; j<tokens.getLength(); ++j) {
    			word_document[tokens.getIndexAtPosition(j)][i]++;
    		}
    	}
    	for(int i=0; i< this.getNumWords(); ++i) {
    		for(int j=0; j<this.getNumDocs(); ++j) {
    			word_document[i][j]=1+word_document[i][j];
    		}
    	}
    	logger.debug("Finish calc Word_Document Matrix, Calc Time:"+(System.currentTimeMillis() - start) + "Millis");
	}
	
	public int getNumTopics() {
		return model.numTopics;
	}
	public void setNumTopics(int numTopics) {
		this.numTopics = numTopics;
	}
	public double getAlphaSum() {
		return model.alphaSum;
	}
	public void setAlphaSum(double alphaSum) {
		this.alphaSum = alphaSum;
	}
	public double getBeta() {
		return model.beta;
	}
	public void setBeta(double beta) {
		this.beta = beta;
	}
	public int getNumThreads() {
		return numThreads;
	}
	public void setNumThreads(int numThreads) {
		this.numThreads = numThreads;
	}
	public int getNumIterations() {
		return model.numIterations;
	}
	public void setNumIterations(int numIterations) {
		this.numIterations = numIterations;
	}
	public int getNumWords() {
		return model.getAlphabet().size();
	}
	public int getNumDocs() {
		return model.getData().size();
	}

	public void generateWordCoffMatrixWithDictionary(int topic_id, LinkedMatrix m, int dict_length, double[][] word_doc, double[][] doc_topic,
			double threshold) {
		logger.debug("Begin on calc Word_Word Matrix on Topic " + topic_id);
    	long start = System.currentTimeMillis();
    	int[] dicts = this.getTopWordForTopicWithoutSingleCharacter(topic_id, dict_length, threshold);
    	for (int i=0; i< this.getNumWords(); ++i) {
    		for(int j=0; j<dicts.length; ++j) {
	     		double value = 0;
	    		for(int k=0; k<this.getNumDocs(); ++k) {
	    			double cooccurence = word_doc[i][k] * word_doc[dicts[j]][k];
	    			value += cooccurence*doc_topic[k][topic_id];
	  			}
	   			if(value>=threshold) {
	   				m.set(i, dicts[j], value);
    			}
    		}
    	}
    	logger.debug("Finish calc Word_Word Matrix on Topic " + topic_id + ", Calc Time:"+(System.currentTimeMillis() - start) + "Millis");
	}

	public void generateWordCoffMatrixWithQueryAndDictionary(int topic_id,
			LinkedMatrix m, int[] queries, int dict_length, double[][] word_doc,
			double[][] doc_topic, double threshold) {
		logger.debug("Begin on calc Mini size("+queries.length+","+dict_length+") of Word_Word Matrix on Topic " + topic_id);
    	long start = System.currentTimeMillis();
    	int[] dicts = this.getTopWordForTopicWithoutSingleCharacter(topic_id, dict_length, threshold);
    	for (int i=0; i< queries.length; ++i) {
    		if(m.existRow(queries[i])) {
	    		for(int j=0; j<dicts.length; ++j) {
		     		double value = 0;
		    		for(int k=0; k<this.getNumDocs(); ++k) {
		    			double cooccurence = word_doc[queries[i]][k] * word_doc[dicts[j]][k];
		    			value += cooccurence*doc_topic[k][topic_id];
		  			}
		   			if(value>=threshold) {
		   				m.set(queries[i], dicts[j], value);
	    			}
	    		}
    		}
    	}
    	logger.debug("Finish calc Mini size of Word_Word Matrix on Topic " + topic_id + ", Calc Time:"+(System.currentTimeMillis() - start) + "Millis");
	}
}
