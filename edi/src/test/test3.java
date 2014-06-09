package test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class test3 {
static String text;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

//		String sp = System.lineSeparator();
//		text="DTM+11:200809161118:203'"+sp+"DTM+11:200809161118:203'"+sp+"DTM+11:200809161118:203'";
//		System.out.println(getPosHighlight(2)[0]+"-"+getPosHighlight(2)[1]);
	
		
		String s = "[QTY+12][segment 23] QTY01[subelement:3] content should be PCE";
		String regex = ".*\\[segment ([0-9]+)\\].*";
		Pattern p = Pattern.compile(regex);
	    Matcher m = p.matcher(s);
		if(m.find())
			System.out.println(m.group(1));
	
	
	}
	
	
	private static int[] getPosHighlight(int line) {
		// TODO Auto-generated method stub
		String[] lines = text.split(System.lineSeparator());
		
		
		int i=0;
		int pos = 0;
		while( i < lines.length){
			if(i==line) break;
			pos = pos+lines[i].length();
			
			i++;
		}
		
		int[] s = {pos+1,pos+lines[i].length()};
		return s;
	}

}
