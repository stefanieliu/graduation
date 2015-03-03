package cn.ict.carc.christine.util;

import java.io.PrintWriter;
import java.io.Serializable;
import java.lang.reflect.Field;

import org.apache.commons.math3.linear.OpenMapRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SparseRealMatrix;

import cn.ict.carc.christine.bean.Law;
import cn.ict.carc.christine.bean.LinkedMatrix;

public class PrintHelper {
	public static void printLaw(Law law) {
		System.out.println("ID:"+law.getId());
		System.out.println("Title:"+law.getTitle());
		System.out.println("Text:"+law.getText());
		System.out.println("-------------------------");
	}
	
	public static void printRealMatrix(PrintWriter out, RealMatrix m) {
		printRealMatrix(out, m, 6);
	}
	
	public static void printRealMatrix(PrintWriter out, RealMatrix m, int accuracy) {
		out.write(m.getRowDimension()+","+m.getColumnDimension()+"\n");
		for(int i=0; i<m.getRowDimension(); ++i) {
			for(int j=0; j<m.getColumnDimension(); ++j) {
				if(j==0) {
					out.write(String.format("%."+accuracy+"f", m.getEntry(i, j)));
				} else {
					out.write(","+String.format("%."+accuracy+"f", m.getEntry(i, j)));
				}
			}
			out.write("\n");
		}
		out.flush();
		out.close();
	}
	
	public static void printRealMatrix(PrintWriter out, RealMatrix m, int accuracy, double threshold) {
		out.write(m.getRowDimension()+","+m.getColumnDimension()+"\n");
		for(int i=0; i<m.getRowDimension(); ++i) {
			String line = "";
			for(int j=0; j<m.getColumnDimension(); ++j) {
				if(m.getEntry(i, j)>threshold) {
					if(!line.isEmpty()) {
						line+=",";
					}
					line+=String.format("%."+accuracy+"f", m.getEntry(i, j))+"("+j+")";
				}
			}
			out.write(line+"\n");
		}
		out.flush();
		out.close();
	}
	
	public static void printMatrix(PrintWriter out, double[][] m, int rows, int cols) {
		printMatrix(out, m, rows, cols, 6);
	}
	
	public static void printMatrix(PrintWriter out, double[][] m, int rows, int cols , int accuracy) {
		out.write(rows+","+cols+"\n");
		for(int i=0; i<rows; ++i) {
			for(int j=0; j<cols; ++j) {
				if(j==0) {
					out.write(String.format("%."+accuracy+"f", m[i][j]));
				} else {
					out.write(","+String.format("%."+accuracy+"f", m[i][j]));
				}
			}
			out.write("\n");
		}
		out.flush();
		out.close();
	}
	
	public static void printLinkedMatrix(PrintWriter out, LinkedMatrix m, int accuracy, double threshold) {
		m.print(out, accuracy, threshold);
	}
}
