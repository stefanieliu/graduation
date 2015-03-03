package cn.ict.carc.christine.Lucene;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import cn.ict.carc.christine.util.C;
import cn.ict.carc.christine.util.Config;

public class IndexWriterPool {
	
	private final static Logger logger = LogManager.getLogger(IndexWriterPool.class);
	private static IndexWriterConfig config;
	private final ArrayList<String> subIndexes;
	
    private final Map<String, IndexWriter> indexWriterMap;
    
    private static IndexWriterPool instance = new IndexWriterPool();
    private IndexWriterPool()
    {
    	subIndexes = new  ArrayList<String>();
    	indexWriterMap = new ConcurrentHashMap<String, IndexWriter>(); 

    	config = new IndexWriterConfig(Version.LUCENE_CURRENT, new SmartChineseAnalyzer());
    	config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
    	config.setWriteLockTimeout(C.WRITE_LOCK_TIMEOUT);
    	
    }
    public static IndexWriterPool getInstance()
    {
    	return instance;
    }
    
	public List<String> getSubIndexes() {
		return this.subIndexes;
	}
  
	public int size() {  
        return indexWriterMap.size();  
    }
	
    public boolean init() {
    	File indexDir = new File(Config.IndexDirectory);
    	if(!indexDir.exists()) {
    		logger.debug("The Index Directory is not exists. Try to create ...");
    		File defaultIndex = new File(Config.IndexDirectory +"/"+Config.DefaultIndex);
    		defaultIndex.mkdirs();
    	}
    	for(File sub : indexDir.listFiles()) {
    		if(sub.isDirectory()) {
    			subIndexes.add(sub.getName());
   			}
   		}
    	if(subIndexes.size()==0) {
    		File defaultIndex = new File(Config.IndexDirectory +"/"+Config.DefaultIndex);
    		defaultIndex.mkdir();
    		subIndexes.add(Config.DefaultIndex);
    	}
   		logger.info("Find "+subIndexes.size() +" Indexes.");
   		for(String sub : subIndexes) {
   			try {  
   				IndexWriter indexWriter = createIndexWriter(Config.IndexDirectory+"/"+sub);  
                if (indexWriter != null) { 
                	indexWriterMap.put(sub, indexWriter);  
                }
            } catch (IOException e) {
                logger.warn("IOException while creating IndexWriter of "+ Config.IndexDirectory+"/"+sub);
            }
  		}
   		if(indexWriterMap.size()==0)
   		{
   			logger.error("Initialized IndexWriterPool falied, no useable Index");
   			return false;
   		}
   		else
   		{
   			logger.info("Initialized IndexWriterPool");
   			logger.info(this.toString());
   			return true;
   		}
    }
    public void destroy() {  
        Iterator<Entry<String, IndexWriter>> iterator = indexWriterMap.entrySet().iterator();  
        while (iterator.hasNext()) {  
            Entry<String, IndexWriter> entry = iterator.next();  
            IndexWriter indexWriter = entry.getValue();  
            try {
            	indexWriter.commit();
                indexWriter.close();  
                indexWriter = null;  
            } catch (IOException e) {  
                logger.warn("IOException while closing IndexWriter of "+ entry.getKey());  
            }
        }  
        indexWriterMap.clear();  
        logger.info("Destroyed IndexWriterPool");  
    }
    
    private IndexWriter createIndexWriter(String indexDir) throws CorruptIndexException, IOException {  
        File indexFile = new File(indexDir);  
        return new IndexWriter(FSDirectory.open(indexFile), config);
    }
    
	public boolean containIndexWriter(String indexDir)
	{
		return indexWriterMap.containsKey(indexDir);
	}
	
	public IndexWriter getDefaultIndexWriter() {
		return getIndexWriter(Config.DefaultIndex);
	}
	
	public IndexWriter getIndexWriter(String indexDir) {  
        IndexWriter indexWriter = indexWriterMap.get(indexDir);  
        if (indexWriter != null)  {
        	synchronized (indexWriterMap) {  
        		if (!indexWriterMap.containsKey(indexDir)) {  
        			try {  
        				indexWriter = createIndexWriter(indexDir);  
        			} catch (IOException e) {  
        				logger.warn("IOException while creating IndexWriter of "+ indexDir);  
        			}
        			if (indexWriter != null)  {
        				indexWriterMap.put(indexDir, indexWriter);  
        			}
        		}
            }  
        }  
        return indexWriterMap.get(indexDir);  
    }  
    
    public void commit() {   
        synchronized (indexWriterMap) {   
            Iterator<Entry<String, IndexWriter>> iterator = indexWriterMap.entrySet().iterator();   
            while (iterator.hasNext()) {   
                Entry<String, IndexWriter> entry = iterator.next();   
                IndexWriter indexWriter = entry.getValue();   
                try {   
                    indexWriter.commit();   
                } catch (IOException e) {   
                    logger.error("IOException commiting pending updates of" + entry.getKey());   
                    destoryIndexWriter(iterator, indexWriter);   
                }   
            }   
        }   
        logger.info("Commit for all IndexWiters");   
    }   
  
    public void optimize() {   
        synchronized (indexWriterMap) {   
            Iterator<Entry<String, IndexWriter>> iterator = indexWriterMap.entrySet().iterator();   
            while (iterator.hasNext()) {   
                Entry<String, IndexWriter> entry = iterator.next();   
                IndexWriter indexWriter = entry.getValue();   
                try {   
                    indexWriter.commit();   
                } catch (IOException e) {   
                	logger.error("IOException commiting pending updates of" + entry.getKey());
                    destoryIndexWriter(iterator, indexWriter);   
                }   
            }   
        }
        logger.info("Optimize for all IndexWiters");
    }   
      
    private void destoryIndexWriter(Iterator<Entry<String, IndexWriter>> iterator, IndexWriter indexWriter) {   
        try {   
            indexWriter.close();   
        } catch (CorruptIndexException e) {   
            logger.error("CorruptIndexException while closing indexWriter because of " + e.getMessage());   
        } catch (IOException e) {   
        	logger.error("IOException while closing indexWriter because of " + e.getMessage());   
        }   
        iterator.remove();   
        logger.info("Destroy " + indexWriter);
    }   
    
    public String toString() {  
       return "[IndexWriterPool] IndexDirectory=" + Config.IndexDirectory
    		   + " SubIndexes.Size="+subIndexes.size()
    		   + " IndexWriterPool.Size="+indexWriterMap.size();
    }
}
