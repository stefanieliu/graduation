package cn.ict.carc.christine.bean;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexableField;

import cn.ict.carc.christine.Exception.IllegalLawException;
import cn.ict.carc.christine.util.DateHelper;


public class Law {

	private long id=0; // #唯一标识

	private String title=null; //  #标题
	private String caption=null; // #题注

	private String text=null;    // #正文
	private String type=null;    // #类别
	private String publishNo=null; // #文号
	//max 19
	private List<String> publishOrg; // #颁布单位
	//max 6
	private List<String> theme; // #内容分类

	private String chapterNo=null; // 章节号
	private Date publishDate=null; // #颁布时间
	private Date practiceDate=null; // #实施时间
	private String timeliness=null; // #时效性
	private String area=null; // 行政区域

	private String source=null; // #来源

	private Date includeDate=null; // #入库时间
	private double relScore = 0;
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getCaption() {
		return caption;
	}
	public void setCaption(String caption) {
		this.caption = caption;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getPublishNo() {
		return publishNo;
	}
	public void setPublishNo(String publishNo) {
		this.publishNo = publishNo;
	}
	public List<String> getPublishOrg() {
		return publishOrg;
	}
	
	public void setPublishOrg(List<String> publishOrg) {
		this.publishOrg = publishOrg;
	}
	public List<String> getTheme() {
		return theme;
	}
	public void setTheme(List<String> theme) {
		this.theme = theme;
	}
	public String getChapterNo() {
		return chapterNo;
	}
	public void setChapterNo(String chapterNo) {
		this.chapterNo = chapterNo;
	}
	public Date getPublishDate() {
		return publishDate;
	}
	public void setPublishDate(Date publishDate) {
		this.publishDate = publishDate;
	}
	public Date getPracticeDate() {
		return practiceDate;
	}
	public void setPracticeDate(Date practiceDate) {
		this.practiceDate = practiceDate;
	}
	public String getTimeliness() {
		return timeliness;
	}
	public void setTimeliness(String timeliness) {
		this.timeliness = timeliness;
	}
	public String getArea() {
		return area;
	}
	public void setArea(String area) {
		this.area = area;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public Date getIncludeDate() {
		return includeDate;
	}
	public void setIncludeDate(Date includeDate) {
		this.includeDate = includeDate;
	}
	public double getRelScore() {
		return relScore;
	}
	public void setRelScore(double relScore) {
		this.relScore = relScore;
	}
	
	public Law() {
		this.publishOrg = new ArrayList<String>();
		this.theme = new ArrayList<String>();
	}
	
	public Document toLuceneDocument() throws IllegalLawException {
		Document doc = new Document();
		if(text==null) {
			throw new IllegalLawException("Text Field must be not null!");
		}
		doc.add(new StoredField("ID", id));
		if(title!=null) {
			doc.add(new TextField("Title", title, Store.YES));
		}
		if(caption!=null) {
			doc.add(new StoredField("Caption", caption));
		}
		if(text!=null) {
			doc.add(new TextField("Text", text, Store.YES));
		}
		if(type!=null) {
			doc.add(new StoredField("Type", type));
		}
		if(publishNo!=null) {
			doc.add(new StoredField("PublishNo", publishNo));
		}
		for(int i=0; i<publishOrg.size(); ++i) {
			doc.add(new StoredField("PublishOrg", publishOrg.get(i)));
		}
		for(int i=0; i<theme.size(); ++i) {
			doc.add(new StoredField("Theme", theme.get(i)));
		}
		if(chapterNo!=null) {
			doc.add(new StoredField("ChapterNo", chapterNo));
		}
		if(publishDate!=null) {
			doc.add(new StoredField("PublishDate", DateHelper.format2Day(publishDate)));
		}
		if(practiceDate!=null) {
			doc.add(new StoredField("PracticeDate", DateHelper.format2Day(practiceDate)));
		}
		if(timeliness!=null) {
			doc.add(new StoredField("Timeliness", timeliness));
		}
		if(area!=null) {
			doc.add(new StoredField("Area", area));
		}
		if(source!=null) {
			doc.add(new StoredField("Source", source));
		}
		if(includeDate!=null) {
			doc.add(new StoredField("IncludeDate", DateHelper.format2Day(includeDate)));
		}
		return doc;
	}
	public static Law fromLuceneDocument(Document doc) {
		Law law = new Law();
		law.id = Integer.parseInt(doc.get("ID"));
		law.title = doc.get("Title");
		law.caption = doc.get("Caption");
		law.text = doc.get("Text");
		law.type = doc.get("Type");
		law.publishNo = doc.get("PublishNo");
		//find publish org
		IndexableField [] orgs = doc.getFields("PublishOrg");
		for(IndexableField org : orgs) {
			law.publishOrg.add(org.stringValue());
		}
		IndexableField[] themes = doc.getFields("Theme");
		for(IndexableField theme : themes) {
			law.theme.add(theme.stringValue());
		}

		law.chapterNo = doc.get("ChapterNo");
		law.publishDate = DateHelper.parser4Day(doc.get("PublishDate"));
		law.practiceDate = DateHelper.parser4Day(doc.get("PracticeDate"));
		law.timeliness = doc.get("Timeliness");
		law.area = doc.get("Area");
		law.source = doc.get("Source");
		law.includeDate = DateHelper.parser4Day(doc.get("IncludeDate"));
		return law;
	}
	public void addPublishOrg(String org) {
		if(this.publishOrg==null) {
			this.publishOrg = new ArrayList<String>();
		}
		this.publishOrg.add(org);
	}
	public void setPublishOrg(String orgs) {
		if(this.publishOrg==null) {
			this.publishOrg = new ArrayList<String>();
		}
		String[] orgArray = orgs.split(" ");
		for(String org: orgArray) {
			this.publishOrg.add(org);
		}
		
	}
	public void setTheme(String themes) {
		if(this.theme==null) {
			this.theme = new ArrayList<String>();
		}
		String[] themeArray = themes.split(" ");
		for(String theme: themeArray) {
			this.theme.add(theme);
		}
	}
	
	public Law deepCopy() {
		Law law = new Law();
		law.id=this.id;// #唯一标识

		law.title=this.title; //  #标题
		law.caption=this.caption; // #题注

		law.text=this.text;    // #正文
		law.type=this.type;    // #类别
		law.publishNo=this.publishNo; // #文号
		//max 19
		law.publishOrg=new ArrayList<String>(this.publishOrg); // #颁布单位
		//max 6
		law.theme = new ArrayList<String>(this.theme); // #内容分类

		law.chapterNo=this.chapterNo; // 章节号
		law.publishDate=this.publishDate; // #颁布时间
		law.practiceDate=this.practiceDate; // #实施时间
		law.timeliness=this.timeliness; // #时效性
		law.area=this.area; // 行政区域

		law.source=this.source; // #来源

		law.includeDate=this.includeDate; // #入库时间
		law.relScore = this.relScore;
		return law;
	}
}
