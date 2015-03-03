package cn.ict.carc.christine.TopicCluster;

import java.util.regex.Pattern;

import cc.mallet.util.CharSequenceLexer;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
    	Pattern tokenPattern =
                Pattern.compile("[\\S]+");
    	CharSequenceLexer lexer = new CharSequenceLexer("我们是祖国 的 花朵 ~~ 哗啦啦 ~~","[\\S]+");
    	while(lexer.hasNext()) {
    		System.out.println(lexer.next().toString());
    	}
    }
}
