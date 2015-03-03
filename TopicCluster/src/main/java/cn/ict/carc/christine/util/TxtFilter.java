package cn.ict.carc.christine.util;

import java.io.File;
import java.io.FileFilter;

public class TxtFilter implements FileFilter {

	@Override
	public boolean accept(File pathname) {
		// TODO Auto-generated method stub
		return pathname.getName().toLowerCase().endsWith(".txt");
	}

}
