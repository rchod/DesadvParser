package test;

public class test3 {
static String text;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		String sp = System.lineSeparator();
		text="DTM+11:200809161118:203'"+sp+"DTM+11:200809161118:203'"+sp+"DTM+11:200809161118:203'";
		System.out.println(getPosHighlight(2)[0]+"-"+getPosHighlight(2)[1]);
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
