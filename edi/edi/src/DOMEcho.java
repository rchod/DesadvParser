import javax.xml.parsers.DocumentBuilder; 
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class DOMEcho {

    private static PrintStream out;
    private static List<String> requiredSegments= new ArrayList<String>() ;
    private static String[] arrayReqSegs= {"BGM+351","DTM+132","DTM+137","DTM+11","NAD+CN","NAD+CZ","NAD+SE","LOC+11","RFF+ADE","CPS","PAC","QTY+52","PCI+17","RFF+AAT","GIR+3","LIN","PIA+1","QTY+12","ALI","RFF+ON"};
    private static String[] desadvSegs= {"BGM","DTM","NAD","LOC","RFF","CPS","PAC","QTY","PCI","RFF","GIR","ALI","EQD","MEA","PIA","IMD","LIN"};
    private static String[] desadvSegsP= {"BGM+351","DTM+132","DTM+137","DTM+11","DTM+94","NAD+CN","NAD+CZ","NAD+SE","LOC+11","RFF+ADE","CPS","PAC","QTY+52","QTY+12","PCI+17","RFF+AAT","GIR+3","MEA+AAX","MEA+KGM","RFF+CRN","EQD+TE","PIA+1","MEA+AAY","MEA+AAX","LOC+159","RFF+ON","IMD","RFF+AAS","QTY+1","IMD+1"};
    private static String[] desadvSegsVar= {"CPS","PAC","ALI"};
    private static List<ArrayList<ArrayList<String>>> segsOrder = new ArrayList<ArrayList<ArrayList<String>>>();
    private static List<String> desadvSegsList = new ArrayList<String>();
    private static List<String> desadvSegsListP = new ArrayList<String>();
    private static List<String> desadvSegsListVar = new ArrayList<String>();
    private static String previousSeg;
    private static String actualSeg;
    private static String previousElm;
    private static String actualElm;
    private static String previousElmContent;
    private static String actualElmContent;
    private static String isComposite = "no";
    private static List segs= new ArrayList<String>();

    
    DOMEcho(PrintStream out) {
        this.out = out;
    }

    public static void main(String[] args) throws Exception {
        String filename = "C:\\Users\\user\\workspace3\\edi\\src\\DESADV.xml";
        boolean ignoreWhitespace = false;
        boolean ignoreComments = false;
        boolean putCDATAIntoText = false;
        boolean createEntityRefs = false;
        
        
        for(int i=0;i<desadvSegsVar.length;i++)
        	desadvSegsListVar.add(desadvSegsVar[i]);
		
        for(int i=0;i<desadvSegsP.length;i++)
        	desadvSegsListP.add(desadvSegsP[i]);
        
        for(int i=0;i<desadvSegs.length;i++)
        	desadvSegsList.add(desadvSegs[i]);
        
        for(int i=0;i<arrayReqSegs.length;i++)
        	requiredSegments.add(arrayReqSegs[i]);
        
  
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        
        dbf.setIgnoringComments(ignoreComments);
        dbf.setIgnoringElementContentWhitespace(ignoreWhitespace);
        dbf.setCoalescing(putCDATAIntoText);
        dbf.setExpandEntityReferences(!createEntityRefs);
        
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(new File(filename));
        
        
        
        NodeList interchanges = doc.getChildNodes().item(0).getChildNodes();
        
        String desadvDate = doc.getChildNodes().item(0).getChildNodes().item(0).getAttributes().getNamedItem("Date").getTextContent();
        String desadvTime = doc.getChildNodes().item(0).getChildNodes().item(0).getAttributes().getNamedItem("Time").getTextContent();
        
        
        if(desadvDate.length() != 6)
        	throw new Exception("the desadv's date format is incorrect, should be YYMMDD");
        
        if(desadvTime.length() != 4)
        	throw new Exception("the desadv's time format is incorrect, should be HHMM");
              
        
        Date today = new Date();
        SimpleDateFormat formatter;
        formatter = new SimpleDateFormat("yyMMdd");
        Date desadvDateD = formatter.parse(desadvDate);
        
        if(desadvDateD.compareTo(today)==1)
        	throw new Exception("the desadv's date is greater than today's date");
        	
        //output = formatter.parse(desadvDate); format(today);
        System.out.println("****************** " + desadvDateD.compareTo(today));


        
        for(int i=0;i<interchanges.getLength();i++){
	         Node sender = interchanges.item(i).getChildNodes().item(0);
	         Node receiver = interchanges.item(i).getChildNodes().item(1);
	         Node group = interchanges.item(i).getChildNodes().item(2);
	         Node transaction = group.getChildNodes().item(0);
	         NodeList segments = group.getChildNodes().item(0).getChildNodes();
	         
	         // check if receiver edi code is equal to renault edi code
	         if(!receiver.getFirstChild().getAttributes().getNamedItem("Id").toString().substring(4, 14).equals("1780129987"))
	         	throw new Exception("ERROR: Renault EDI code is incorrect, should be 1780129987");

	         // check if receiver edi code is equal to renault edi code
	         try{
	         if(!receiver.getFirstChild().getAttributes().getNamedItem("Qual").toString().equals("ZZ"))
	         	throw new Exception("ERROR: Renault EDI code qualifiant is incorrect");
	         }catch(Exception e){ System.out.println("Qualifiant doesnt exist");
	        	 
	         }
	         
	         
	         // verification de l'ordre des segments
	         // initialisation de segsOrder 
	         Scanner sc = new Scanner(new File("src\\segments-ordre.txt"));
	         String line = null;
			 while(sc.hasNext()){
				if(line==null)
				   line = sc.nextLine();
				ArrayList<ArrayList<String>> tempList1 = new  ArrayList<ArrayList<String>>();
				ArrayList<String> tempList2 = new  ArrayList<String>();
	        	if(line.equals("[segment]")){
	        		tempList2.add(sc.nextLine());
	        		tempList1.add(tempList2);
	        		if(sc.nextLine().equals("[predecesors]")){
	        			line = sc.nextLine();
	        			tempList2 = new  ArrayList<String>();
	        		while(!line.equals("[segment]")){
	        			tempList2.add(line);
	        			if(!sc.hasNext()) break;
	        			line = sc.nextLine();
	        		}
	        		tempList1.add(tempList2);
	        		}
	        	}
	        	System.out.println(segsOrder);
	        	segsOrder.add(tempList1);
	         }
	         
			 System.out.println(segsOrder);
			 
	         //#######################################
	         for(int j=0;j<segments.getLength();j++){
	        	 
	        	 ediCheck(segments.item(j));
	        	 
	         }	
	         //#######################################
			 
	         // counting segments repitition
	         System.out.println(segs);
	         List tempsegs= new ArrayList<String[]>();
	         for(int k=0;k<segs.size();k++){
	        	 int counter = 1;
	        	 boolean more=true;
	        	 int kj=k+1;
	        	 while(more){
	        		 if(kj<segs.size())
	        		 if(segs.get(k).equals(segs.get(kj))){
	        			 counter++;
	        			 segs.remove(kj);
	        		 }else
	        			 kj++;
	        		 if(kj==segs.size()) more = false;
	        	 }
	        	 tempsegs.add(new String[]{(String) segs.get(k),String.valueOf(counter)});
	         }
	         
	         
	         for(int k=0;k<tempsegs.size();k++){
	        	 String[] array = (String[]) tempsegs.get(k);
	        	 System.out.println(array[0]+"///"+array[1]);
	         }
	         if(requiredSegments.size()>0)
	        	 throw new Exception("required segments absent:"+ requiredSegments);
        }
    }

  //#############################################################
  //################# ediCheck ##################################
  //#############################################################  
    private static void ediCheck(Node n) throws Exception {
    	

    	out = System.out;
    	out.println("#### start ediCheck ####");
    	if(n.equals(null)) return;

    	//################# if segment #####################################
		if(n.getNodeName().equals("segment")){ 
			String id = n.getAttributes().getNamedItem("Id").getTextContent();
			actualSeg = id; 
			
			if(!desadvSegsList.contains(id))
				throw new Exception("Segment inconnue: "+id);
			
			out.println("####################"+id+"#######################");
			NodeList elements = n.getChildNodes();
			for(int i=0;i<elements.getLength();i++)
				ediCheck(elements.item(i));
			previousSeg = id;    
			requiredSegments.remove(id);

		}
		//################# endif segment ##################################
		
		//################# if element #####################################
		if(n.getNodeName().equals("element")){
			String id = n.getAttributes().getNamedItem("Id").getTextContent();

			actualElm = id;  
			isComposite = "no";
			if(n.getAttributes().getLength()>1){
			  isComposite = n.getAttributes().getNamedItem("Composite").toString();
			  isComposite = isComposite.substring(11);
			  isComposite = isComposite.substring(0,isComposite.length()-1);
			}
			out.println("  "+id);
			if(isComposite.equals("yes")){
				out.println("isComposite ================== yes! ");
				NodeList subelements = n.getChildNodes();
				for(int i=0;i<subelements.getLength();i++){
					out.println("subelement "+i);
					ediCheck(subelements.item(i));
				}
			}else{
				if(actualElm.equals(actualSeg+"01")){
					
					actualElmContent = n.getTextContent();
				    if(!desadvSegsListP.contains(actualSeg+"+"+n.getTextContent()) && !desadvSegsListVar.contains(actualSeg) )
					  throw new Exception("Qualifiant inconnue: "+actualSeg+"+"+n.getTextContent());
				
					// start segments repitition count
					segs.add(actualSeg+"+"+actualElmContent);
					// end segments repitition count
					
					// start checking required segments
					out.println("remove=="+actualSeg+"+"+n.getTextContent());
					requiredSegments.remove(actualSeg+"+"+n.getTextContent());
					// end checking required segments
					
					// start checking segments order
					System.out.println(">>>>>>>>>>>>>>>>>");
					for(int h=0;h<segsOrder.size();h++){
						System.out.println("???????????????????????????????");
						System.out.println(segsOrder.get(h).get(0).get(0));
						System.out.println(actualSeg+"+"+n.getTextContent());
						if(segsOrder.get(h).get(0).get(0).equals(actualSeg+"+"+n.getTextContent())){
							System.out.println(">>>>>>>>>>>>>>>>> found it!!!");
							System.out.println(segsOrder.get(h).get(1));
							System.out.println((previousSeg+"+"+previousElmContent));
							if(!segsOrder.get(h).get(1).contains(previousSeg+"+"+previousElmContent))
								throw new Exception("order Exception ! "+actualSeg+"+"+actualElmContent+" does not come after "+previousSeg+"+"+previousElmContent);
						}
					}
					// end checking segments order
					
			
					
					previousElmContent = n.getTextContent();
				}
				
			}
			previousElm = id;
		    
		}
		//################# endif element ##################################
		
		//################# if subelement ##################################
		if(n.getNodeName().equals("subelement")){
			String sequence = n.getAttributes().getNamedItem("Sequence").getTextContent();

			out.println(n.getTextContent());
			
			if(actualElm.equals(actualSeg+"01") && isComposite.equals("yes") && sequence.equals("1")){

				actualElmContent = n.getTextContent();
				
				// start segments repitition count
				segs.add(actualSeg+"+"+actualElmContent);
				// end segments repitition count
				
				if(!desadvSegsListP.contains(actualSeg+"+"+n.getTextContent()) && !desadvSegsList.contains(actualSeg))
					throw new Exception("Segment inconnue: "+actualSeg+"+"+n.getTextContent());
				
				out.println("remove=="+actualSeg+"+"+n.getTextContent());
			    requiredSegments.remove(actualSeg+"+"+n.getTextContent());
			    
			    previousElmContent = n.getTextContent();
			}
		}
		//################# endif subelement ###############################
		
		out.println("#### end ediCheck ####");
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    


}
