package cn.ict.carc.christine.Lucene;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleFragmenter;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.util.Version;

import cn.ict.carc.christine.Exception.IllegalLawException;
import cn.ict.carc.christine.TopicCluster.QueryExpander;
import cn.ict.carc.christine.bean.ExpansionWord;
import cn.ict.carc.christine.bean.Law;
import cn.ict.carc.christine.util.C;
import cn.ict.carc.christine.util.Config;

public class IndexAdmin {
private final static Logger logger = LogManager.getLogger(IndexAdmin.class);
private int defaultTopK = 10;
	
	public int writeLaw(Law doc) {
		return this.addLaw(doc, false);
	}

	
	public int writeAllLaws(Collection<Law> docs) {
		return this.addAllLaws(docs,false);
	}
	
	 
	public int addLaw(Law doc, boolean append) {
		return this.addLaw(doc, Config.DefaultIndex, append);
	}
	
	
	public int addAllLaws(java.util.Collection<Law> docs, boolean append) {
		return this.addAllLaws(docs, Config.DefaultIndex, append);
	}
	
	
	public int addLaw(Law doc, String index, boolean append) {
		IndexWriter writer = IndexWriterPool.getInstance().getDefaultIndexWriter();
		try {
			writer.addDocument(doc.toLuceneDocument());
			writer.commit();
			logger.info("Add 1 documents, Total="+writer.numDocs());
		} catch (IOException | IllegalLawException e) {
			e.printStackTrace();
			try {
				writer.rollback();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			return 0;
		}
		return 1;
	}

	
	public int addAllLaws(Collection<Law> docs, String index, boolean append) {
		IndexWriter writer = IndexWriterPool.getInstance().getDefaultIndexWriter();
		try {
			int count=docs.size();
			List<Document> lucenceDocs = new LinkedList<Document>();
			for(Law law : docs) {
				lucenceDocs.add(law.toLuceneDocument());
			}
			writer.addDocuments(lucenceDocs);
			writer.commit();
			logger.info("Add "+count+" documents, Total="+writer.numDocs());
		} catch (IOException | IllegalLawException e) {
			e.printStackTrace();
			try {
				writer.rollback();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			return 0;
		}
		return docs.size();
	}

	
	public void delAllIndex() {
		try {
			IndexWriter writer = IndexWriterPool.getInstance().getDefaultIndexWriter();
			writer.deleteAll();
			writer.commit();
			logger.info("Delete all documents, Total="+writer.numDocs());
		} catch(Exception e) {
			logger.error(e.getMessage());
		}
	}

	
	public int query(String query, Collection<Law> result) {
		return query(query, 0, C.DEFAULT_TOPK, result);
	}

	
	public int query(String query, int startoffset, int rows,
			Collection<Law> result) {
		LawQuery lawquery = new LawQuery(query);
		return query(lawquery, startoffset, rows, result);
	}

	
	public int query(LawQuery query, Collection<Law> result) {
		return query(query, 0, C.DEFAULT_TOPK, result);
	}

	
	public int query(LawQuery query, int startoffset, int rows,
			Collection<Law> result) {
		MultiReader reader = IndexReaderPool.getInstance().getAllIndexReader();
		IndexSearcher searcher = new IndexSearcher(reader);
		MultiFieldQueryParser parser = new MultiFieldQueryParser(Version.LUCENE_CURRENT, query.getQueryFields(), new SmartChineseAnalyzer());
		SimpleHTMLFormatter formatter = new SimpleHTMLFormatter("<font color=\"red\">", "</font>");
		
		try {
			Query lucenequery;
			lucenequery = parser.parse(query.getQuery());
			lucenequery = searcher.rewrite(lucenequery);
			logger.info(lucenequery);
			int endoffset = startoffset + rows;
			TopDocs docs;
			if(query.withFilter())
			{
				docs = searcher.search(lucenequery, query.getLuceneFilter(), endoffset);
			}
			else
			{
				docs= searcher.search(lucenequery, endoffset);
			}
			endoffset = docs.totalHits>endoffset?endoffset:docs.totalHits;
  
			//Highlighter highlighter = new Highlighter(formatter,new QueryScorer(lucenequery));
			//highlighter.setTextFragmenter(new SimpleFragmenter(Integer.MAX_VALUE));
			
			for(int i = startoffset; i < endoffset ; i++){ 
			
				Document d = searcher.doc(docs.scoreDocs[i].doc);
				Law law = Law.fromLuceneDocument(d);
				law.setRelScore(docs.scoreDocs[i].score);
				//String content = highlighter.getBestFragment(new SmartChineseAnalyzer(), "content", law.getText());
				//if(content!=null) {
				//	law.setText(content);
				//}
				result.add(law);
			}
			return docs.totalHits;
		}
		catch(Exception e) {
			logger.error(e.getMessage());
		}
		return 0;
	}

	public int queryWithGlobalExpansion(String query, int startoffset, int rows,
			Collection<Law> result) {
		LawQuery lawquery = new LawQuery(query);
		return queryWithGlobalExpansion(lawquery, startoffset, rows, result);
	}
	
	public int queryWithGlobalExpansion(LawQuery query, int startoffset, int rows, Collection<Law> result) {
		MultiReader reader = IndexReaderPool.getInstance().getAllIndexReader();
		IndexSearcher searcher = new IndexSearcher(reader);
		MultiFieldQueryParser parser = new MultiFieldQueryParser(Version.LUCENE_CURRENT, query.getQueryFields(), new SmartChineseAnalyzer());
		SimpleHTMLFormatter formatter = new SimpleHTMLFormatter("<font color=\"red\">", "</font>");
		
		try {
			Query lucenequery = new BooleanQuery();

			QueryExpander expander = new QueryExpander(Config.TopicClusterDirectory);
			List<ExpansionWord> exquery = expander.expanseOffline(query.getQuery());
			Query origin = parser.parse(query.getQuery());
			origin.setBoost(1);
			((BooleanQuery)lucenequery).add(origin,Occur.SHOULD);
			double norm = exquery.get(0).probs;
			for(ExpansionWord word : exquery) {
				Query expanse = parser.parse(word.word);
				expanse.setBoost((float)(0.5*word.probs/norm));
				((BooleanQuery)lucenequery).add(expanse,Occur.SHOULD);
			}

			lucenequery = searcher.rewrite(lucenequery);
			logger.info(lucenequery);
			int endoffset = startoffset + rows;
			TopDocs docs;
			if(query.withFilter())
			{
				docs = searcher.search(lucenequery, query.getLuceneFilter(), endoffset);
			}
			else
			{
				docs= searcher.search(lucenequery, endoffset);
			}
			endoffset = docs.totalHits>endoffset?endoffset:docs.totalHits;
  
			//Highlighter highlighter = new Highlighter(formatter,new QueryScorer(lucenequery));
			//highlighter.setTextFragmenter(new SimpleFragmenter(Integer.MAX_VALUE));
			
			for(int i = startoffset; i < endoffset ; i++){ 
			
				Document d = searcher.doc(docs.scoreDocs[i].doc);
				Law law = Law.fromLuceneDocument(d);
				law.setRelScore(docs.scoreDocs[i].score);
				//String content = highlighter.getBestFragment(new SmartChineseAnalyzer(), "content", law.getText());
				//if(content!=null) {
				//	law.setText(content);
				//}
				result.add(law);
			}
			return docs.totalHits;
		}
		catch(Exception e) {
			logger.error(e.getMessage());
		}
		return 0;
	}
	
	public int queryWithLocalExpansion(String query, int startoffset, int rows,
			Collection<Law> result) {
		LawQuery lawquery = new LawQuery(query);
		return queryWithLocalExpansion(lawquery, startoffset, rows, result);
	}
	
	
	public int queryWithLocalExpansion(LawQuery query, int startoffset, int rows, Collection<Law> result) {
		MultiReader reader = IndexReaderPool.getInstance().getAllIndexReader();
		IndexSearcher searcher = new IndexSearcher(reader);
		MultiFieldQueryParser parser = new MultiFieldQueryParser(Version.LUCENE_CURRENT, query.getQueryFields(), new SmartChineseAnalyzer());
		SimpleHTMLFormatter formatter = new SimpleHTMLFormatter("<font color=\"red\">", "</font>");
		
		ArrayList<Law> first_result = new ArrayList<Law>();
		///first search
		try {
			Query firstquery = parser.parse(query.getQuery());
			firstquery = searcher.rewrite(firstquery);
			TopDocs docs;
			if(query.withFilter())
			{
				docs = searcher.search(firstquery, query.getLuceneFilter(), defaultTopK);
			}
			else
			{
				docs= searcher.search(firstquery, defaultTopK);
			}
			for(int i = 0; i < defaultTopK ; i++){ 
				Document d = searcher.doc(docs.scoreDocs[i].doc);
				Law law = Law.fromLuceneDocument(d);
				first_result.add(law);
			}
			
		} catch(Exception e) {
			logger.error(e.getMessage());
			
		}
		
		///second search
		try {
			Query lucenequery = new BooleanQuery();
			QueryExpander expander = new QueryExpander(Config.TopicClusterDirectory);
			expander.estimateTopicModelOnline(first_result, 2, 10);
			List<ExpansionWord> exquery = expander.expanseOnline(query.getQuery());
			Query origin = parser.parse(query.getQuery());
			origin.setBoost(1);
			((BooleanQuery)lucenequery).add(origin,Occur.SHOULD);
			double norm = exquery.get(0).probs;
			for(ExpansionWord word : exquery) {
				Query expanse = parser.parse(word.word);
				expanse.setBoost((float)(0.5*word.probs/norm));
				((BooleanQuery)lucenequery).add(expanse,Occur.SHOULD);
			}

			lucenequery = searcher.rewrite(lucenequery);
			logger.info(lucenequery);
			int endoffset = startoffset + rows;
			TopDocs docs;
			if(query.withFilter())
			{
				docs = searcher.search(lucenequery, query.getLuceneFilter(), endoffset);
			}
			else
			{
				docs= searcher.search(lucenequery, endoffset);
			}
			endoffset = docs.totalHits>endoffset?endoffset:docs.totalHits;
  
			//Highlighter highlighter = new Highlighter(formatter,new QueryScorer(lucenequery));
			//highlighter.setTextFragmenter(new SimpleFragmenter(Integer.MAX_VALUE));
			
			for(int i = startoffset; i < endoffset ; i++){ 
			
				Document d = searcher.doc(docs.scoreDocs[i].doc);
				Law law = Law.fromLuceneDocument(d);
				law.setRelScore(docs.scoreDocs[i].score);
				//String content = highlighter.getBestFragment(new SmartChineseAnalyzer(), "content", law.getText());
				//if(content!=null) {
				//	law.setText(content);
				//}
				result.add(law);
			}
			return docs.totalHits;
		}
		catch(Exception e) {
			logger.error(e.getMessage());
		}
		return 0;
	}
}
