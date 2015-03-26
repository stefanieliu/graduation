package cn.ict.carc.christine.bean;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LinkedMatrix {

	public final static Logger logger = LogManager.getLogger(LinkedMatrix.class);
	
	private ArrayList<TreeMap<Integer,Double>> values;
	
	private int rowLength;
	private int colLength;
	
	public LinkedMatrix(int rowlen, int collen) {
		this.rowLength = rowlen;
		this.colLength = collen;
		this.values = new ArrayList<TreeMap<Integer,Double>>(this.rowLength);
		for(int i=0; i<this.rowLength; ++i) {
			this.values.add(new TreeMap<Integer,Double>());
		}
	}
	
	public void reset(int rowlen, int collen) {
		this.rowLength = rowlen;
		this.colLength = collen;
		this.values.clear();
		for(int i=0; i<this.rowLength; ++i) {
			this.values.add(new TreeMap<Integer,Double>());
		}
	}
	
	public void clear() {
		for(int i=0; i<this.rowLength; ++i) {
			this.values.get(i).clear();
		}
	}
	
	public void set(int row, int col, double value) {
		if(row<0||row>=this.rowLength) {
			throw new IndexOutOfBoundsException("Row Index:"+row);
		}
		if(col<0||col>=this.colLength) {
			throw new IndexOutOfBoundsException("Column Index:"+col);
		}
		this.values.get(row).put(col, value);
	}
	
	public double get(int row, int col) {
		if(row<0||row>=this.rowLength) {
			throw new IndexOutOfBoundsException("Row Index:"+row);
		}
		if(col<0||col>=this.colLength) {
			throw new IndexOutOfBoundsException("Column Index:"+col);
		}
		if(this.values.get(row).containsKey(col)) {
			return this.values.get(row).get(col);
		} else {
			return 0;
		}
	}
	
	public int getRowLength() {
		return rowLength;
	}

	public void setRowLength(int rowLength) {
		this.rowLength = rowLength;
	}

	public int getColLength() {
		return colLength;
	}

	public void setColLength(int colLength) {
		this.colLength = colLength;
	}

	public boolean existRow(int row) {
		return row>=0&&row<this.rowLength;
	}
	
	public Map<Integer, Double> getRow(int row) {
		if(row<0||row>=this.rowLength) {
			throw new IndexOutOfBoundsException("Row Index:"+row);
		}
		return this.values.get(row);
	}
	
	public void print(PrintWriter out, int accuracy, double threshold) {
		out.write(this.rowLength+","+this.colLength+"\n");
		for(int i=0; i<this.rowLength; ++i) {
			StringBuilder line = new StringBuilder();
			for(Entry<Integer,Double> entry : this.values.get(i).entrySet()) {
				if(entry.getValue()>=threshold) {
					if(line.length()!=0) {
						line.append(",");
					}
					line.append(String.format("%."+accuracy+"f", entry.getValue())+"("+entry.getKey()+")");
				}
			}
				
			line.append("\n");
			out.write(line.toString());
		}
		out.flush();
		out.close();
	}
	
	public static LinkedMatrix load(String file) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line = reader.readLine();
		String[] infos = line.split(",");
		int rowlen = Integer.parseInt(infos[0]);
		int collen = Integer.parseInt(infos[1]);
		LinkedMatrix m = new LinkedMatrix(rowlen, collen);
		for(int i=0; i< rowlen; ++i) {
			line = reader.readLine();
			String[] details = line.split(",");
			for(int j=0; j<details.length; ++j) {
				if(details[j].isEmpty()) {
					continue;
				}
				double value = Double.parseDouble(details[j].substring(0, details[j].indexOf("(")));
				int col = Integer.parseInt(details[j].substring(details[j].indexOf("(")+1,details[j].length()-1));
				m.set(i, col, value);
			}
		}
		reader.close();
		return m;
	}
	
}
