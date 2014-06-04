import javax.xml.parsers.DocumentBuilder; 
import javax.xml.parsers.DocumentBuilderFactory;

import java.io.File;
import java.io.ObjectInputStream.GetField;
import java.io.PrintStream;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;



public class DOMEcho {

    private static PrintStream out;
    private static NodeList mappingSegments;
    private static List<String> requiredSegments= new ArrayList<String>() ;
    private static String[] segsWithoutQualifiant= {"ALI","EQD","CPS","PAC","LIN","IMD"};
    private static String[] notUniqueSegs= {"DTM","NAD","RFF","QTY","MEA"};
    private static String[] arrayReqSegs= {"BGM+351","DTM+132","DTM+137","DTM+11","NAD+CN","NAD+CZ","NAD+SE","LOC+11","RFF+ADE","CPS","PAC","QTY+52","PCI+17","RFF+AAT","GIR+3","LIN","PIA+1","QTY+12","ALI","RFF+ON"};
    private static String[] desadvSegs= {"BGM","DTM","NAD","LOC","CPS","PAC","QTY","PCI","RFF","GIR","ALI","EQD","MEA","PIA","IMD","LIN"};
    private static String[] desadvSegsP= {"BGM+351","DTM+132","DTM+137","DTM+11","DTM+94","NAD+CN","NAD+CZ","NAD+SE","LOC+11","RFF+ADE","CPS","PAC","QTY+52","QTY+12","PCI+17","RFF+AAT","GIR+3","MEA+AAX","MEA+KGM","RFF+CRN","EQD+TE","PIA+1","MEA+AAY","MEA+AAX","LOC+159","RFF+ON","IMD","RFF+AAS","QTY+1","IMD+1"};
    private static String[] desadvSegsVar= {"CPS","PAC","ALI"};
    private static List<ArrayList<ArrayList<String>>> segsOrder = new ArrayList<ArrayList<ArrayList<String>>>();
    private static List<String> notUniqueSegsList = new ArrayList<String>();
    private static List<String> desadvSegsList = new ArrayList<String>();
    private static List<String> desadvSegsListP = new ArrayList<String>();
    private static List<String> desadvSegsListVar = new ArrayList<String>();
    private static String previousSeg;
    private static String actualSeg;
    private static String previousElm;
    private static String actualElm;
    private static String previousQualifiant;
    private static String actualQualifiant;
    private static String previousElmContent;
    private static String actualElmContent;
    private static Node mappingElm;
    private static Node mappingSeg;
    private static String isComposite = "no";
    private static String SegQualifiant;
    private static List segs= new ArrayList<String>();
    private static List repetitivitySegs= new ArrayList<String>();
    
    DOMEcho(PrintStream out) {
        this.out = out;
    }

    public static void main(String[] args) throws Exception {
        boolean ignoreWhitespace = false;
        boolean ignoreComments = false;
        boolean putCDATAIntoText = false;
        boolean createEntityRefs = false;
                
        for(int i=0;i<notUniqueSegs.length;i++)	notUniqueSegsList.add(notUniqueSegs[i]);
        for(int i=0;i<desadvSegsVar.length;i++)	desadvSegsListVar.add(desadvSegsVar[i]);
        for(int i=0;i<desadvSegsP.length;i++) desadvSegsListP.add(desadvSegsP[i]);
        for(int i=0;i<desadvSegs.length;i++) desadvSegsList.add(desadvSegs[i]);
        for(int i=0;i<arrayReqSegs.length;i++) requiredSegments.add(arrayReqSegs[i]);
        
  
        // start reading xml file 
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setIgnoringComments(ignoreComments);
        dbf.setIgnoringElementContentWhitespace(ignoreWhitespace);
        dbf.setCoalescing(putCDATAIntoText);
        dbf.setExpandEntityReferences(!createEntityRefs);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(new File("src\\DESADV.xml"));
        Document ediMapping = db.parse(new File("src\\base.xml"));
        // end reading xml file
        
        mappingSegments = ediMapping.getChildNodes().item(0).getChildNodes();
        
        
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
       // System.out.println("****************** " + desadvDateD.compareTo(today));


        
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
	        	//System.out.println(segsOrder);
	        	segsOrder.add(tempList1);
	         }
	         
			 //System.out.println(segsOrder);
			 
			 
			 //*********************************
	         // start counting segments repitition
			 //*********************************
	         for(int j=0;j<segments.getLength();j++){
	        	 Node n = segments.item(j);
	     		 if(n.getNodeName().equals("segment"))	{ 
	     			 String id = n.getAttributes().getNamedItem("Id").getTextContent();
	    			if(Arrays.asList(segsWithoutQualifiant).contains(id))
	    				segs.add(id); 		
	    			else
	    				segs.add(id+"+"+getQualifiant(n));
	     		 }	
	         }
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
	        	 repetitivitySegs.add(new String[]{(String) segs.get(k),String.valueOf(counter)});
	         }
			 //*********************************
	         // end counting segments repitition
			 //*********************************
			 
//	         for(int k=0;k<repetitivitySegs.size();k++){
//	        	 String[] array = (String[]) repetitivitySegs.get(k);
//	        	 System.out.println(array[0]+"///"+array[1]);
//	         }
	         
	         //#######################################
	         for(int j=0;j<segments.getLength();j++){
	        	 
	        	 ediCheck(segments.item(j));
	        	 
	         }	
	         //#######################################

	         if(requiredSegments.size()>0)
	        	 throw new Exception("required segments absent:"+ requiredSegments);
        }
    }

  //#############################################################
  //################# ediCheck ##################################
  //#############################################################  
    private static void ediCheck(Node n) throws Exception {
    	

    	out = System.out;
    	//out.println("#### start ediCheck ["+n.getNodeName()+"] ####");
    	if(n.equals(null)) return;
    	SegQualifiant = getQualifiant(n);

    	//################# if segment #####################################
		if(n.getNodeName().equals("segment")){ 
			
			 actualSeg = n.getAttributes().getNamedItem("Id").getTextContent();
			
			// start checking required segments
			out.println("remove "+actualSeg+"+"+SegQualifiant);
			requiredSegments.remove(actualSeg+"+"+SegQualifiant);
			// end checking required segments
			
			if(notUniqueSegsList.contains(actualSeg)){
				SegQualifiant = getQualifiant(n);
								
				mappingSeg = getMappingAttrsSeg(n, SegQualifiant);				
			
			}else{
				
				mappingSeg = getMappingNode(n,"segment","Id");					
				
			}

			if(mappingSeg==null)
				throw new Exception("mapping info error! ");
			
			
			//************ checking repetitivity
			String repetivity = mappingSeg.getAttributes().getNamedItem("repetivity").getTextContent();
			String required = mappingSeg.getAttributes().getNamedItem("required").getTextContent();
			
			switch(actualSeg+"+"+SegQualifiant){
			case "QTY+52":
			case "MEA+AAY":
				if((getSegRepetitivity(actualSeg+"+"+SegQualifiant)) > (Integer.parseInt(repetivity)*(getSegRepetitivity("PAC"))))
					throw new Exception(actualSeg+"+"+SegQualifiant+" seg de plus !!!");
				break;
			case "RFF+AAT":
			case "GIR+3":
				if((getSegRepetitivity(actualSeg+"+"+SegQualifiant)) > (Integer.parseInt(repetivity)*(getSegRepetitivity("PCI+17"))))
					throw new Exception(actualSeg+"+"+SegQualifiant+" seg de plus !!!");
				break;
			case "PIA+1":
			case "LOC+159":
			case "RFF+ON":
			case "QTY+12":
			case "ALI":
			case "IMD":
				if((getSegRepetitivity(actualSeg+"+"+SegQualifiant)) > (Integer.parseInt(repetivity)*(getSegRepetitivity("LIN"))))
					throw new Exception(actualSeg+"+"+SegQualifiant+" seg de plus !!!");
				break;
				
			default:
				System.out.println(actualSeg+"+"+SegQualifiant+" repetitivity:"+getSegRepetitivity(actualSeg+"+"+SegQualifiant));
				if((getSegRepetitivity(actualSeg+"+"+SegQualifiant))>Integer.parseInt(repetivity))
					throw new Exception(actualSeg+"+"+SegQualifiant+" seg de plus ");
			
			}
			//************ checking repetitivity
			
			
			if(!desadvSegsList.contains(actualSeg))
				throw new Exception("Segment inconnue: "+actualSeg);
			
			out.println("####"+actualSeg+"*repetivity:"+repetivity+"*required:"+required+"#######################");
			NodeList elements = n.getChildNodes();
			for(int i=0;i<elements.getLength();i++)
				ediCheck(elements.item(i));
			previousSeg = actualSeg;    
			previousQualifiant = getQualifiant(n);
			requiredSegments.remove(actualSeg);

		}
		//################# endif segment ##################################
		
		//################# if element #####################################
		if(n.getNodeName().equals("element")){
			String id = n.getAttributes().getNamedItem("Id").getTextContent();

			actualElm = id;  
		    mappingElm = getMappingElm(n);
		    
			if(mappingElm.getAttributes().getLength()>2){
			    String format = mappingElm.getAttributes().getNamedItem("format").getTextContent();
			    String length = mappingElm.getAttributes().getNamedItem("length").getTextContent();
			    String required = mappingElm.getAttributes().getNamedItem("required").getTextContent();

			    System.out.println("***************[element]***************");
			    System.out.println("********** format of "+id+": "+format);
			    System.out.println("********** length of "+id+": "+length);
			    System.out.println("********** required of "+id+": "+required);
			}
			// if element is comoposite
			if(n.getAttributes().getLength()>1){
				NodeList subelements = n.getChildNodes();
				for(int i=0;i<subelements.getLength();i++){
					ediCheck(subelements.item(i));
				}
			}else{ // if element is not composite
				if(actualElm.equals(actualSeg+"01")){
					
					Node mE = null;
					if(mappingElm.getAttributes().getLength()==2)
						mE = getMappingSubElm(n.getFirstChild());
					else mE = mappingElm;
					
					System.out.println(n.getAttributes().getLength());
					System.out.println(mE.getAttributes().getNamedItem("Id"));
					
				    String format = mE.getAttributes().getNamedItem("format").getTextContent();
				    String length = mE.getAttributes().getNamedItem("length").getTextContent();
				    String required = mE.getAttributes().getNamedItem("required").getTextContent();

				    //************************************
				    // check format & length requirements
					if(format.equals("an") && !StringUtils.isAlphanumeric(n.getTextContent()) ){
						throw new Exception("seg is not Alphanumeric");
					}
					
					if(format.equals("n") && !StringUtils.isNumeric(n.getTextContent()) ){
						throw new Exception("seg is not numeric");
					}
					
					if(n.getTextContent().length() > Integer.parseInt(length))
						throw new Exception("seg length exceeds limit");
					//************************************
					
					//System.out.println("Composite????"+);
					actualElmContent = n.getTextContent();
				    if(!desadvSegsListP.contains(actualSeg+"+"+SegQualifiant) && !desadvSegsListVar.contains(actualSeg) )
					  throw new Exception("Qualifiant inconnue: "+actualSeg+"+"+SegQualifiant);
				
					// start segments repitition count
					segs.add(actualSeg+"+"+SegQualifiant);
					// end segments repitition count
					
					// start checking segments order
					for(int h=0;h<segsOrder.size();h++){

						if(segsOrder.get(h).get(0).get(0).equals(actualSeg+"+"+SegQualifiant)){
							if(!segsOrder.get(h).get(1).contains(previousSeg+"+"+previousQualifiant))
								throw new Exception("order Exception ! "+actualSeg+"+"+actualElmContent+" does not come after "+previousSeg+"+"+previousQualifiant);
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
			
		    Node mappingSubElm = getMappingSubElm(n);
		    
			if(mappingSubElm.getAttributes().getLength()>2){
			    String format = mappingSubElm.getAttributes().getNamedItem("format").getTextContent();
			    String length = mappingSubElm.getAttributes().getNamedItem("length").getTextContent();
			    String required = mappingSubElm.getAttributes().getNamedItem("required").getTextContent();

			    System.out.println("***************[subelement]***************");
			    System.out.println("********** format of "+sequence+": "+format);
			    System.out.println("********** length of "+sequence+": "+length);
			    System.out.println("********** required of "+sequence+": "+required);
			}
			
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
		
		//out.println("#### end ediCheck ####");
    }
    
    public static Node getMappingNode(Node n, String e, String attr){
    	String atr = n.getAttributes().getNamedItem(attr).getTextContent();
//    	System.out.println("wwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwww");
//    	System.out.println("[segment] starting search for mapping info of "+atr);
//    	System.out.println("wwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwww");
    	
    	for(int i=0;i<mappingSegments.getLength();i++){
    		if(mappingSegments.item(i).getNodeName().equals(e)){
    			if(mappingSegments.item(i).getAttributes().getNamedItem(attr).getTextContent().equals(atr))
    				return mappingSegments.item(i);
    		}
    	}
    	
		return null;
    
    }
    
    
    //############################ Node getMappingNode ##########################
    public static Node getMappingNode(Node n, String nodeType, String attr, String content){
    	String atr = n.getAttributes().getNamedItem(attr).getTextContent();

    	for(int i=0;i<mappingSegments.getLength();i++){
    		if(mappingSegments.item(i).getNodeName().equals(nodeType)){
    			if(mappingSegments.item(i).getAttributes().getNamedItem(attr).getTextContent().equals(atr))
    				if(mappingSegments.item(i).getTextContent().equals(content) || (mappingSegments.item(i).hasChildNodes() && mappingSegments.item(i).getChildNodes().item(0).getTextContent().equals(content)))
    					return mappingSegments.item(i);
    		}
    	}
    	
		return null;
    
    }
    
    //############################ getMappingAttrsSeg() ##########################
    public static Node getMappingAttrsSeg(Node n, String qualifiant){
    	String atr = n.getAttributes().getNamedItem("Id").getTextContent();
//    	System.out.println("MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM");
//    	System.out.println("[segment] starting search for mapping info of "+atr);
//    	System.out.println("MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM");
    	
    	for(int i=0;i<mappingSegments.getLength();i++){
    		Node tmpNode = mappingSegments.item(i);
    		if(tmpNode.getNodeName().equals("segment")){
    			if(tmpNode.getAttributes().getNamedItem("Id").getTextContent().equals(atr)){
    				if(notUniqueSegsList.contains(actualSeg)){
    					if(tmpNode.getChildNodes().item(1).hasChildNodes() && tmpNode.getChildNodes().item(1).getChildNodes().getLength()>1){
    						if(tmpNode.getChildNodes().item(1).getChildNodes().item(1).getTextContent().equals(qualifiant))
    							return tmpNode;
    					}else{
    						if(tmpNode.getChildNodes().item(1).getTextContent().equals(qualifiant))
    							return tmpNode;
    					}
    				}else{
    					
    					return tmpNode;
    					
    				}
    			}
    		}
    	}
    	
		return null;
    
    }
    
    
    //############################ getMappingElm() ##########################
    public static Node getMappingElm(Node n){
    	String id = n.getAttributes().getNamedItem("Id").getTextContent();

//    	System.out.println("MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM");
//    	System.out.println("[element] starting search for mapping info of "+id);
//    	System.out.println("MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM");
    	
    	NodeList childnodes = mappingSeg.getChildNodes();
    	for(int i=0;i<childnodes.getLength();i++){
    		if(childnodes.item(i).getNodeName().equals("element")){
    			if(childnodes.item(i).getAttributes().getNamedItem("Id").getTextContent().equals(id))
    				return childnodes.item(i);
    		}
    	}
    	System.out.println("%%%%failed to find mapping elemnt%%%");
		return null;
    
    }
    
    //############################ getMappingSubElm() ##########################
    public static Node getMappingSubElm(Node n){
    	String sequence = n.getAttributes().getNamedItem("Sequence").getTextContent();

//    	System.out.println("MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM");
//    	System.out.println("[element] starting search for mapping info of "+id);
//    	System.out.println("MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM");
    	
    	NodeList childnodes = mappingElm.getChildNodes();
    	for(int i=0;i<childnodes.getLength();i++){
    		if(childnodes.item(i).getNodeName().equals("subelement")){
    			if(childnodes.item(i).getAttributes().getNamedItem("Sequence").getTextContent().equals(sequence))
    				return childnodes.item(i);
    		}
    	}
    	System.out.println("%%%%failed to find mapping elemnt%%%");
		return null;
    
    }  
    

    //##################### getQualifiant(Node n) ############################*
    public static String getQualifiant(Node n){
    	
		if(n.getFirstChild().hasChildNodes())
		    return n.getFirstChild().getFirstChild().getTextContent();
		else
			return n.getFirstChild().getTextContent();
    	
    }
    
    //##################### getSegRepetitivity(String s) ############################*
    public static int getSegRepetitivity(String seg){
    	
        for(int k=0;k<repetitivitySegs.size();k++){
	       	 String[] array = (String[]) repetitivitySegs.get(k);
	       	 if(array[0].equals(seg))
	       		 return Integer.parseInt(array[1]);
	     }
		return 0;
    	
    }
}
