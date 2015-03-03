package cn.ict.carc.christine.Lucene;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.index.Term;
import org.apache.lucene.queries.ChainedFilter;
import org.apache.lucene.queries.TermsFilter;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.TermRangeFilter;
import org.apache.lucene.util.BytesRef;

import cn.ict.carc.christine.util.DateHelper;


public class LawQuery {

	private final static Logger logger = LogManager.getLogger(LawQuery.class);
	
	private String query=null; // 查询文本
	private String title=null; //
	private List<String> types=null; // 类别
	private String publishNo=null; // 发文号
	private List<String> publishOrgs=null; // 颁布单位
	private List<String> themes=null; // 主题分类
	private Date publishDateLowerBound=null; // 颁布日期开始
	private Date publishDateUpperBound=null; // 颁布日期结束
	private Date practiceDateLowerBound=null; // 实施日期开始
	private Date practiceDateUpperBound=null; // 实施日期结束
	private String timeliness=null; // 时效性
	private String area=null; // 地域
	private String[] fields=null; //
	
	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public List<String> getTypes() {
		return types;
	}
	
	public void addType(String type) {
		if(this.types==null) {
			this.types = new ArrayList<String>();
		}
		this.types.add(type);
	}

	public void setTypes(List<String> types) {
		this.types = types;
	}

	public String getPublishNo() {
		return publishNo;
	}

	public void setPublishNo(String publishNo) {
		this.publishNo = publishNo;
	}

	public List<String> getPublishOrgs() {
		return publishOrgs;
	}

	public void addPublishOrg(String publishOrg) {
		if(this.publishOrgs==null) {
			this.publishOrgs = new ArrayList<String>();
		}
		this.publishOrgs.add(publishOrg);
	}
	
	public void setPublishOrgs(List<String> publishOrgs) {
		this.publishOrgs = publishOrgs;
	}

	public List<String> getThemes() {
		return themes;
	}
	
	public void addTheme(String theme) {
		if(this.themes==null) {
			this.themes = new ArrayList<String>();
		}
		this.themes.add(theme);
	}

	public void setThemes(List<String> themes) {
		this.themes = themes;
	}

	public Date getPublishDateLowerBound() {
		return publishDateLowerBound;
	}

	public void setPublishDateLowerBound(Date publishDateLowerBound) {
		this.publishDateLowerBound = publishDateLowerBound;
	}

	public Date getPublishDateUpperBound() {
		return publishDateUpperBound;
	}

	public void setPublishDateUpperBound(Date publishDateUpperBound) {
		this.publishDateUpperBound = publishDateUpperBound;
	}

	public Date getPracticeDateLowerBound() {
		return practiceDateLowerBound;
	}

	public void setPracticeDateLowerBound(Date practiceDateLowerBound) {
		this.practiceDateLowerBound = practiceDateLowerBound;
	}

	public Date getPracticeDateUpperBound() {
		return practiceDateUpperBound;
	}

	public void setPracticeDateUpperBound(Date practiceDateUpperBound) {
		this.practiceDateUpperBound = practiceDateUpperBound;
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

	public LawQuery(String query) {
		this.query = query;
		fields = new String[]{"Text"};
	}

	public boolean withFilter() {
		return !((types==null||types.isEmpty())&&
				publishNo==null&&
				(publishOrgs==null||publishOrgs.isEmpty())&&
				(themes==null||themes.isEmpty())&&
				publishDateLowerBound==null&&
				publishDateUpperBound==null&&
				practiceDateLowerBound==null&&
				practiceDateUpperBound==null&&
				timeliness==null&&
				area==null);
	}

	public Filter getLuceneFilter() {
		List<Filter> filters = new ArrayList<Filter>();
		
		if(types!=null&&!types.isEmpty()) {
			filters.add(getTermFilter("Type",types));
		}
		if(publishNo!=null) {
			filters.add(getTermFilter("PublishNo",publishNo));
		}
		if(publishOrgs!=null&&!publishOrgs.isEmpty()) {
			filters.add(getTermFilter("PublishOrg",publishOrgs));
		}
		if(themes!=null&&!themes.isEmpty()) {
			filters.add(getTermFilter("Theme",themes));
		}
		if(publishDateLowerBound!=null||publishDateUpperBound!=null) {
			if(publishDateLowerBound!=null&&publishDateUpperBound!=null) {
				filters.add(getDateRangeFilter("PublishDate", publishDateLowerBound, publishDateUpperBound));
			} else if(publishDateLowerBound!=null) {
				filters.add(getDateRangeFilter("PublishDate", publishDateLowerBound, DateHelper.MaxDate()));
			} else {
				filters.add(getDateRangeFilter("PublishDate", DateHelper.MinDate(), publishDateUpperBound));
			}
		}
		if(practiceDateLowerBound!=null||practiceDateUpperBound!=null) {
			if(practiceDateLowerBound!=null&&practiceDateUpperBound!=null) {
				filters.add(getDateRangeFilter("PracticeDate", practiceDateLowerBound, practiceDateUpperBound));
			} else if(practiceDateLowerBound!=null) {
				filters.add(getDateRangeFilter("PracticeDate", practiceDateLowerBound, DateHelper.MaxDate()));
			} else {
				filters.add(getDateRangeFilter("PracticeDate", DateHelper.MinDate(), practiceDateUpperBound));
			}
		}
		if(timeliness!=null) {
			filters.add(getTermFilter("Timeliness",timeliness));
		}
		if(area!=null) {
			filters.add(getTermFilter("Area",area));
		}
		Filter filter = null;
		if(filters.size()>1) {
			Filter[] fs = new Filter[filters.size()];
			fs = filters.toArray(fs);
			filter = new ChainedFilter(fs, ChainedFilter.AND);
		} else if(filters.size()==1) {
			filter = filters.get(0);
		} else {
			logger.warn("No useable Filters!");
		}
		
		logger.debug(filter.toString());
		return filter;
	}
	
	Filter getTermFilter(String field, String ...value) {
		Term[] terms = new Term[value.length];
		for(int i=0; i<terms.length; ++i) {
			terms[i] = new Term(field, value[i]);
		}
		return new TermsFilter(terms);
	}
	
	Filter getTermFilter(String field, List<String> value) {
		Term[] terms = new Term[value.size()];
		for(int i=0; i<terms.length; ++i) {
			terms[i] = new Term(field, value.get(i));
		}
		return new TermsFilter(terms);
	}
	
	Filter getDateRangeFilter(String field, Date lowerDate, Date upperDate) {
		String lower = DateHelper.format2Day(lowerDate);
		String upper = DateHelper.format2Day(upperDate);
		return new TermRangeFilter(field, new BytesRef(lower.getBytes()), new BytesRef(upper.getBytes()), true, true);
	}

	public String[] getQueryFields() {
		return fields;
	}

}
