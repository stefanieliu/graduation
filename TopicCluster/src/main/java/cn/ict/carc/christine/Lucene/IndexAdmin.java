package cn.ict.carc.christine.Lucene;

import java.io.IOException;
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
import cn.ict.carc.christine.bean.Law;
import cn.ict.carc.christine.util.C;
import cn.ict.carc.christine.util.Config;

public class IndexAdmin {
private final static Logger logger = LogManager.getLogger(IndexAdmin.class);
	
	private boolean withExpansion = false;
	
	public IndexAdmin( boolean withExpansion) {
		this.withExpansion = withExpansion;
	}
	
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
			if(withExpansion) {
				QueryExpander expander = new QueryExpander(Config.TopicClusterDirectory);
				String exquery = expander.expanse(query.getQuery());
				lucenequery = new BooleanQuery();
				Query origin = parser.parse(query.getQuery());
				origin.setBoost(1);
				Query expansion = parser.parse(exquery);
				expansion.setBoost((float) 0.5);
				((BooleanQuery)lucenequery).add(origin,Occur.SHOULD);
				((BooleanQuery)lucenequery).add(expansion,Occur.SHOULD);
			} else {
				lucenequery = parser.parse(query.getQuery());
			}
			lucenequery = searcher.rewrite(lucenequery);
		
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
  
			Highlighter highlighter = new Highlighter(formatter,new QueryScorer(lucenequery));
			highlighter.setTextFragmenter(new SimpleFragmenter(Integer.MAX_VALUE));
			
			for(int i = startoffset; i < endoffset ; i++){ 
			
				Document d = searcher.doc(docs.scoreDocs[i].doc);
				Law law = Law.fromLuceneDocument(d);
				law.setRelScore(docs.scoreDocs[i].score);
				String content = highlighter.getBestFragment(new SmartChineseAnalyzer(), "content", law.getText());
				if(content!=null) {
					law.setText(content);
				}
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
