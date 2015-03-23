package cn.ict.carc.christine.Lucene;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import cn.ict.carc.christine.util.Config;


public class IndexReaderPool {
	
	private final static Logger logger = LogManager.getLogger(IndexReaderPool.class);
	
	private final ArrayList<String> subIndexes;
	
    private final Map<String, IndexReader> indexReaderMap;
    private final Map<Long, IndexReader> staleIndexReadersMap;  
    private static final int STALE_INDEXREADER_SURVIVAL_TIME = 5000; 
    
    private static IndexReaderPool instance = new IndexReaderPool();
    private IndexReaderPool()
    {
    	subIndexes = new  ArrayList<String>();
    	indexReaderMap = new ConcurrentHashMap<String, IndexReader>(); 
    	staleIndexReadersMap = new ConcurrentHashMap<Long, IndexReader>();
    }
    public static IndexReaderPool getInstance()
    {
    	return instance;
    }
    
	public List<String> getSubIndexes() {
		return this.subIndexes;
	}
  
	public int size() {  
        return indexReaderMap.size();  
    }
	
    public boolean init() {
    	File indexDir = new File(Config.IndexDirectory);
    	if(!indexDir.exists()) {
    		logger.error("The Index Directory is not exists.");
    		return false;
    	} else {
    		for(File sub : indexDir.listFiles()) {
    			if(sub.isDirectory()) {
    				subIndexes.add(sub.getName());
    			}
    		}
    		logger.info("Find "+subIndexes.size() +" Indexes.");
    		for(String sub : subIndexes) {
    			try {  
                    IndexReader indexReader = createIndexReader(Config.IndexDirectory+"/"+sub);  
                    if (indexReader != null) { 
                        indexReaderMap.put(sub, indexReader);  
                    }
                } catch (IOException e) {
                    logger.warn("IOException while creating IndexReader of "+ Config.IndexDirectory+"/"+sub);
                }
    		}
    		if(indexReaderMap.size()==0)
            {
            	logger.error("Initialized IndexReaderPool falied, no useable Index");
            	return false;
            }
            else
            {
            	logger.info("Initialized IndexReaderPool");
            	logger.info(this.toString());
            	return true;
            }
    	}
    }
    public void destroy() {  
        Iterator<Entry<String, IndexReader>> iterator = indexReaderMap.entrySet().iterator();  
        while (iterator.hasNext()) {  
            Entry<String, IndexReader> entry = iterator.next();  
            IndexReader indexReader = entry.getValue();  
            try {
                indexReader.close();  
                indexReader = null;  
            } catch (IOException e) {  
                logger.warn("IOException while closing IndexReader of "+ entry.getKey());  
            }
        }  
        indexReaderMap.clear();  
        logger.info("Destroyed IndexReaderPool");  
    }
    
    private IndexReader createIndexReader(String indexDir) throws CorruptIndexException, IOException {  
        File indexFile = new File(indexDir);  
        if (!indexFile.exists()||!indexFile.isDirectory()||indexFile.list().length==0) {
        	return null;  
        }
        else{
        	return IndexReader.open(FSDirectory.open(indexFile));  
        }
    }
    
	public MultiReader getMultiIndexReader(Collection<String> Indexes) {
		ArrayList<IndexReader> indexReaderList = new ArrayList<IndexReader>();
		for(String sub: Indexes)
		{
			if(this.containIndexReader(sub))
			{
				try {
					indexReaderList.add(this.getIndexReader(sub));
				} catch (CorruptIndexException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		IndexReader[] readers = new IndexReader[indexReaderList.size()];
		readers = indexReaderList.toArray(readers);
		MultiReader reader = new MultiReader(readers);
		return reader;
	}
	public MultiReader getAllIndexReader() {
		return this.getMultiIndexReader(this.subIndexes);
	}
	
	public void listTerms() throws IOException {
		IndexReader reader = this.getAllIndexReader();
		Fields fields = MultiFields.getFields(reader);
        Iterator<String> fieldsIterator = fields.iterator();
        while(fieldsIterator.hasNext()){
            String field = fieldsIterator.next();
            if(field.equals("text")) {
	            Terms terms = fields.terms(field);
	            TermsEnum termsEnums = terms.iterator(null);
	            BytesRef byteRef = null;
	            System.out.println("field : "+ field);
	            while((byteRef = termsEnums.next()) != null) {
	                String term = new String(byteRef.bytes, byteRef.offset, byteRef.length);
	                System.out.println(term + ":" + reader.totalTermFreq(new Term(term)));
	            }
            }
        }
	}
	
	public boolean containIndexReader(String indexDir)
	{
		return indexReaderMap.containsKey(indexDir);
	}
    public IndexReader getIndexReader(String indexDir) throws CorruptIndexException, IOException {  
        IndexReader indexReader = indexReaderMap.get(indexDir);  
        if (indexReader != null)  
            return refreshIndexReader(indexDir, indexReader);  
        synchronized (indexReaderMap) {  
            if (!indexReaderMap.containsKey(indexDir)) {  
                try {  
                    indexReader = createIndexReader(indexDir);  
                } catch (IOException e) {  
                    logger.warn("IOException while creating IndexReader of "+ indexDir);  
                }  
                if (indexReader != null)  
                    indexReaderMap.put(indexDir, indexReader);  
            }  
        }  
        return indexReaderMap.get(indexDir);  
    }  
    private synchronized IndexReader refreshIndexReader(String indexDir, IndexReader indexReader) throws CorruptIndexException, IOException {  
        closeStaleIndexReaders(staleIndexReadersMap);   
		IndexReader newIndexReader = createIndexReader(indexDir);
		if (newIndexReader!=null) {  
		    IndexReader oldIndexReader = indexReader;  
		    staleIndexReadersMap.put(System.currentTimeMillis(), oldIndexReader);  
		    indexReaderMap.put(indexDir, newIndexReader);
		    return newIndexReader;
		}
        return indexReader;
    }  
    private void closeStaleIndexReaders(Map<Long, IndexReader> staleIndexReadersMap) {  
        Iterator<Entry<Long, IndexReader>> entryIterator = staleIndexReadersMap.entrySet().iterator();  
        while (entryIterator.hasNext()) {  
            Entry<Long, IndexReader> entry = entryIterator.next();  
            if ((System.currentTimeMillis() - entry.getKey()) >= STALE_INDEXREADER_SURVIVAL_TIME) {  
                try {  
                    entry.getValue().close();  
                } catch (IOException e) {  
                    logger.warn("IOException while closing IndexReader " + entry.getValue().hashCode());  
                } finally {  
                    entryIterator.remove();  
                }
            }  
        }  
    }  
    public String toString() {  
       return "[IndexReaderPool] IndexDirectory=" + Config.IndexDirectory
    		   + " SubIndexes.Size="+subIndexes.size()
    		   + " IndexReaderPool.Size="+indexReaderMap.size();
    }
}
