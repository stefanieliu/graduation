package cn.ict.carc.christine.Preprocesser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import cn.ict.carc.christine.bean.Law;
import cn.ict.carc.christine.util.DateHelper;
import cn.ict.carc.christine.util.StringHelper;

public class LawExtractor {
	public final static Logger logger = LogManager.getLogger(LawExtractor.class);
	
	public Law parseFromXml(String filePath) {
		Law law = null;
        try {
        	File file = new File(filePath);
        	SAXReader saxReader = new SAXReader(); 
			Document document = saxReader.read(file);
			law = new Law();
			Element root = document.getRootElement();
			law.setId(Integer.parseInt(root.elementText("order")));
			law.setTitle(StringHelper.SBC2DBC(root.elementText("title")));
			law.setCaption(StringHelper.SBC2DBC(root.elementText("creditLine")));
			law.setText(StringHelper.join(StringHelper.SBC2DBC(root.elementText("catalog")),StringHelper.SBC2DBC(root.elementText("body")), StringHelper.SBC2DBC(root.elementText("appendix"))));
			//law.setPass_date(DateHelper.parser4Day(root.elementText("passDate")));
			//law.setApprove_date(DateHelper.parser4Day(root.elementText("approveDate")));
			law.setPublishDate(DateHelper.parser4Day(root.elementText("publishDate")));
			law.setPracticeDate(DateHelper.parser4Day(root.elementText("practiceDate")));
			//law.setExpiry_date(DateHelper.parser4Day(root.elementText("failureDate")));
			law.addPublishOrg(StringHelper.SBC2DBC(root.elementText("publishOrg")));
			law.setPublishNo(StringHelper.SBC2DBC(root.elementText("fileNo")));
			//law.setLeg_form(StringHelper.SBC2DBC(root.elementText("legalForm")));
			law.setType(StringHelper.SBC2DBC(root.elementText("legalClass")));
			law.setTimeliness(StringHelper.SBC2DBC(root.elementText("valid")));
			law.setArea(file.getParentFile().getName());
		} catch (DocumentException e) {
			e.printStackTrace();
		}
        return law;
	}
	
	public List<Law> parseFromNPCFile(String filePath) {
		ArrayList<Law> list = new ArrayList<Law>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(filePath));
			String line = null;
			Law law = null;
			String field = null;
			line = reader.readLine();
			boolean skip = false;
			while( line!= null) {
				if(!skip) {
					if(line.startsWith("#")) {
						if(line.matches("#[\\d*>|\\d+][\\S\\s]+$")) {
							if(line.indexOf('>')!=-1) {
								field += ("\n" + line.substring(line.indexOf('>')+1,line.length()-1));
							} else if(line.indexOf('第')!=-1){
								field += ("\n" + line.substring(line.indexOf('第'),line.length()-1));
							} else {
								field += ("\n" + line);
							}
						} else if(line.startsWith("#唯一标识")) {
							if(law != null) {
								addField2Law(law, field);
								if(law.getText()!=null) {
									optimize(law);
									list.add(law);
								}
							}
							law = new Law();
							field = line;
						} else if(line.startsWith("#正文")||line.startsWith("#目录")) {
							addField2Law(law, field);
							field = line.substring(0,5);
							line = line.substring(5);
							continue;
						} else {
							addField2Law(law, field);
							field = line;
						}
					} else if(line.startsWith("@")) {
						if(line.matches("@\\d+>[\\S\\s]+$")) {
							if(line.indexOf('>')!=-1) {
								field += ("\n" + line.substring(line.indexOf('>')+1));
							} else{
								field += ("\n" + line.substring(line.indexOf('第')));
							}
						}
					} else if(line.equals("<pre>")) {
						skip = true;
					} else if(!line.isEmpty()) {
						field += line;
					}
				} else {
					if(line.equals("</pre>")) {
						skip=false;
					}
				}
				line = reader.readLine();
			}
			reader.close();
		} catch(FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return list;
	}

	private void optimize(Law law) {
		String optimizeText = law.getText().replaceAll("<go[\\(|（]((((\\'*[a-zA-Z]+\\'*)+||\\d+),)*\\d+,\\d+[\\)|）]>)*", "");
		optimizeText = optimizeText.replaceAll("</*[a-zA-Z]+>", "");
		
		Pattern p = Pattern.compile("<\\[0-9a-zA-Z,]+>");
		Matcher m = p.matcher(optimizeText);
		while(m.find()) {
			logger.debug(law.getTitle()+"|Text: " + m.group());
		}
		String optimizeTitle = law.getTitle().replaceAll("<go[\\(|（]((((\\'*[a-zA-Z]+\\'*)+||\\d+),)*\\d+,\\d+[\\)|）]>)*", "");
		optimizeTitle = optimizeTitle.replaceAll("</*[a-zA-Z]+>", "");
		m = p.matcher(optimizeTitle);
		while(m.find()) {
			logger.debug(law.getTitle()+"|Title: " + m.group());
		}
		
		law.setTitle(StringHelper.SBC2DBC(optimizeTitle));
		law.setText(StringHelper.SBC2DBC(optimizeText));
		law.setCaption(StringHelper.SBC2DBC(law.getCaption()));
		
		//law.setTitle(optimizeTitle);
		//law.setText(optimizeText);
		//law.setCaption(StringHelper.SBC2DBC(law.getCaption()));
	}

	private void addField2Law(Law law, String field) {
		String[] vals = field.split("=:");
		if(vals.length==2) {
			switch(vals[0]) {
				case "#文号": law.setPublishNo(vals[1]); break;
				case "#来源":  law.setSource(vals[1]); break;
				case "#标题": law.setTitle(vals[1]); break;
				case "#正文": law.setText(vals[1]); break;
				case "#目录":  break;
				case "#类别": law.setType(vals[1]); break;
				case "#级别":  break;
				case "#题注": law.setCaption(vals.length==2?vals[1]:null); break;
				case "#分类号":  break;
				case "#时效性": law.setTimeliness(vals[1]); break;
				case "#入库时间":  law.setIncludeDate(DateHelper.parser4Day(vals[1])); break;
				case "#内容分类":  law.setTheme(vals[1]); break;
				case "#单位代码":  break;
				case "#唯一标识": law.setId(Integer.parseInt(vals[1])); break;
				//case "#失效时间": law.setExpiry_date(DateHelper.parser4Day(vals[1])); break;
				case "#实施时间": law.setPracticeDate(DateHelper.parser4Day(vals[1])); break;
				//case "#批准时间": law.setApprove_date(DateHelper.parser4Day(vals[1])); break;
				case "#相关文件":  break;
				case "#相关资料":  break;
				case "#采用标识":  break;
				case "#颁布单位": law.setPublishOrg(vals[1]); break;
				case "#颁布时间": law.setPublishDate(DateHelper.parser4Day(vals[1])); break;
			}
		}
	}

	public List<Law> parseChapterFromNPCFile(String filePath) {
		ArrayList<Law> list = new ArrayList<Law>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(filePath));
			String line = null;
			Law law = null;
			String field = null;
			line = reader.readLine();
			boolean skip = false;
			while( line!= null) {
				if(!skip) {
					if(line.startsWith("#")) {
						if(line.matches("#[\\d*>|\\d+][\\S\\s]+$")) {
							field += ("\n" + line);
						} else if(line.startsWith("#唯一标识")) {
							if(law != null) {
								addField2Law(law, field);
								if(law.getText()!=null) {
									optimize(law);
									List<Law> chapters = spiltToChapter(law);
									list.addAll(chapters);
								}
							}
							law = new Law();
							field = line;
						} else if(line.startsWith("#正文")||line.startsWith("#目录")) {
							addField2Law(law, field);
							field = line.substring(0,5);
							line = line.substring(5);
							continue;
						} else {
							addField2Law(law, field);
							field = line;
						}
					} else if(line.startsWith("@")) {
						if(line.matches("@\\d+>[\\S\\s]+$")) {
							if(line.indexOf('>')!=-1) {
								field += ("\n" + line.substring(line.indexOf('>')+1));
							} else{
								field += ("\n" + line.substring(line.indexOf('第')));
							}
						}
					} else if(line.equals("<pre>")) {
						skip = true;
					} else if(!line.isEmpty()) {
						field += line;
					}
				} else {
					if(line.equals("</pre>")) {
						skip=false;
					}
				}
				line = reader.readLine();
			}
			reader.close();
		} catch(FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return list;
	}
	
	private List<Law> spiltToChapter(Law law) {
		String text = law.getText();
		List<Law> result = new ArrayList<Law>();
		String[] lines = text.split("\n");
		String chapter = "";
		String chapterTitle = "";
		int id = 1;
		for(String line :  lines) {
			if(line.matches("#[\\d*>|\\d+][\\S\\s]+$")) {
				if(!chapter.isEmpty()) {
					Law ch = law.deepCopy();
					ch.setText(chapter);
					ch.setId(ch.getId()*1000+id++);
					ch.setTitle(ch.getTitle().replace('/', ' ')+" "+chapterTitle);
					chapter = "";
					chapterTitle ="";
					result.add(ch);
				}
				int end = line.indexOf("&");
				if(end==-1) {
					end = line.indexOf("(");
				}
				if(end==-1) {
					end = line.length()-1;
				}
				if(line.indexOf('>')!=-1) {
					chapterTitle = line.substring(line.indexOf('>')+1,end);
					chapter = line.substring(line.charAt(end)=='&'?end+1:end);
				} else if(line.indexOf('第')!=-1){
					chapterTitle = line.substring(line.indexOf('第'),end);
					chapter = line.substring(line.charAt(end)=='&'?end+1:end);
				} else {
					System.out.println(line);
				}
			} else if(!line.isEmpty()) {
				chapter += ((chapter.isEmpty()?"":"\n") + line);
			}
		}
		if(result.isEmpty()) {
			law.setTitle(law.getTitle().replace('/', ' '));
			result.add(law);
		} else if(!chapter.isEmpty()) {
			Law ch = law.deepCopy();
			ch.setText(chapter);
			ch.setId(ch.getId()*1000+id++);
			ch.setTitle(ch.getTitle().replace('/', ' ')+" "+chapterTitle);
			chapter = "";
			chapterTitle ="";
			result.add(ch);
		}
		return result;
	}

	public List<Law> parseItemFromNPCFile(String filePath) {
		ArrayList<Law> list = new ArrayList<Law>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(filePath));
			String line = null;
			Law law = null;
			String field = null;
			line = reader.readLine();
			boolean skip = false;
			while( line!= null) {
				if(!skip) {
					if(line.startsWith("#")) {
						if(line.matches("#[\\d*>|\\d+][\\S\\s]+$")) {
							field += ("\n" + line);
						} else if(line.startsWith("#唯一标识")) {
							if(law != null) {
								addField2Law(law, field);
								if(law.getText()!=null) {
									optimize(law);
									List<Law> items = spiltToItem(spiltToChapter(law));
									list.addAll(items);
								}
							}
							law = new Law();
							field = line;
						} else if(line.startsWith("#正文")||line.startsWith("#目录")) {
							addField2Law(law, field);
							field = line.substring(0,5);
							line = line.substring(5);
							continue;
						} else {
							addField2Law(law, field);
							field = line;
						}
					} else if(line.startsWith("@")) {
						if(line.matches("@\\d+>[\\S\\s]+$")) {
							field += ("\n" + line);
						}
					} else if(line.equals("<pre>")) {
						skip = true;
					} else if(!line.isEmpty()) {
						field += line;
					}
				} else {
					if(line.equals("</pre>")) {
						skip=false;
					}
				}
				line = reader.readLine();
			}
			reader.close();
		} catch(FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return list;
	}

	private List<Law> spiltToItem(List<Law> chapters) {
		List<Law> result = new ArrayList<Law>();
		int id = 1;
		for(Law chapter : chapters) {
			String text = chapter.getText();
			String[] lines = text.split("\n");
			String item = "";
			for(String line :  lines) {
				if(line.matches("@\\d+>[\\S\\s]+$")) {
					if(!item.isEmpty()) {
						Law i = chapter.deepCopy();
						i.setText(item);
						i.setId(i.getId()*1000+id);
						i.setTitle(i.getTitle()+" "+String.format("%3d", id++));
						item="";
						result.add(i);
					}
					if(line.indexOf('>')!=-1) {
						item = line.substring(line.indexOf('>')+1);
					} else{
						item = line.substring(line.indexOf('第'));
					}
				} else if(!line.isEmpty()) {
					item += ("\n" + line);
				}
			}
			if(result.isEmpty()) {
				result.add(chapter);
			} else if(!item.isEmpty()) {
				Law i = chapter.deepCopy();
				i.setText(item);
				i.setId(i.getId()*1000+id);
				i.setTitle(i.getTitle()+" "+String.format("%3d", id++));
				item="";
				result.add(i);
			}
		}
		return result;
	}
}
