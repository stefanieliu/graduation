package cn.ict.carc.christine.util;

public class C {
  public final static String FILE_CONFIG = "config/retrieval.xml";
  public final static String TAG_INDEX_DIRECTORY = "index-directory";
  public final static String TAG_DEFAULT_INDEX = "default-index";
  public final static String TAG_TOPIC_CLUSTER = "topic-cluster";
  public final static String TAG_ICTCLAS_DIRECTORY = "ictclas-directory";
  public final static int DEFAULT_TOPK = 10;
  public final static String FILE_SEGMENTS = "segment.gen";
  public final static int WRITE_LOCK_TIMEOUT = 36000000;//unit：ms

  /*
   * Used to represent date time on web.
   */
  public final static String DATETIME_FORMAT = "yyyy-MM-dd";

  // In ascending order
  public final static char[] CHINESE_NUMBER = { '〇', '一', '七', '三', '九', '二', '五', '八', '六', '十', '四', '零'};

  public final static int LONG_STRING_SNIPPET_LENGTH = 30;

  public final static String[] DELIMITER_SET = { "：", "、", "&", "．"};  
}
