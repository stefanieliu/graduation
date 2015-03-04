package cn.ict.carc.christine.Preprocesser;

import static org.junit.Assert.*;

import org.dom4j.DocumentException;
import org.junit.Test;

import cn.ict.carc.christine.util.Config;

public class ConfigTest {

	@Test
	public void test() throws DocumentException {
		Config.loadConfig();
	}

}
