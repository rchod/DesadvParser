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
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;



public class Parser {

    private static PrintStream out;
    private static NodeList mappingSegments;
    private static List<String> requiredSegments= new ArrayList<String>() ;
    private static String[] segsWithoutQualifiant= {"ALI","EQD","CPS","PAC","LIN","IMD"};
    private static String[] notUniqueSegs= {"DTM","NAD","RFF","QTY","MEA"};
    private static String[] arrayReqSegs= {"BGM+351","DTM+137","DTM+11","NAD+CN","NAD+CZ","NAD+SE","LOC+11","RFF+ADE","CPS","PAC","QTY+52","PCI+17","RFF+AAT","GIR+3","LIN","QTY+12","ALI","RFF+ON"};
    private static String[] desadvSegs= {"BGM","DTM","NAD","LOC","CPS","PAC","QTY","PCI","RFF","GIR","ALI","EQD","MEA","PIA","IMD","LIN"};
    private static String[] desadvSegsP= {"BGM+351","DTM+132","DTM+137","DTM+11","DTM+94","NAD+CN","NAD+CZ","NAD+SE","LOC+11","RFF+ADE","CPS","PAC","QTY+52","QTY+12","PCI+17","RFF+AAT","GIR+3","MEA+AAX","MEA+KGM","RFF+CRN","EQD+TE","PIA+1","MEA+AAY","MEA+AAX","LOC+159","RFF+ON","IMD","RFF+AAS","QTY+1","IMD+1"};
    private static String[] desadvSegsVar= {"CPS","PAC","ALI"};
    private static List<ArrayList<ArrayList<String>>> segsOrder = new ArrayList<ArrayList<ArrayList<String>>>();
    private static List<String> notUniqueSegsList = new ArrayList<String>();
    private static List<String> desadvSegsList = new ArrayList<String>();
    private static List<String> desadvSegsListP = new ArrayList<String>();
    private static List<String> desadvSegsListVar = new ArrayList<String>();
    public static HashSet<String> errors = new HashSet<String>();
    public static List errorsLines = new ArrayList();
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
    private static String dtm132 = null;
    private static String dtm11  = null;
    private static String dtm137 = null;
    private static String nadCzCode = null;
    private static String rffAdeCode = null;
    private static int lineCounter = 0;
    private static int segCounter = 2;
    private static int girCounter = 0;
    private static String SSS="";
    private static int pac = 0;
    private static int qty52 = 0;
    
    Parser(PrintStream out) {
        this.out = out;
    }

    public static void main(String[] args) throws Exception {
    	segCounter = 2;
    	errors.clear();
    	errorsLines.clear();
    	requiredSegments.clear();
    	notUniqueSegsList.clear();
    	desadvSegsListVar.clear();
    	desadvSegsListP.clear();
    	desadvSegsList.clear();
    	segCounter = 2;
    	girCounter = 0;
        pac = 0;
    	
    	
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
        File desadvFile = PathUtils.resolveEdiSrcPath("_desadv.xml");
        File baseFile = PathUtils.resolveEdiSrcPath("_base.xml");
        Document doc = db.parse(desadvFile);
        Document ediMapping = db.parse(baseFile);
        // end reading xml file
        
        mappingSegments = ediMapping.getChildNodes().item(0).getChildNodes();
        
        
        NodeList interchanges = doc.getChildNodes().item(0).getChildNodes();
        

        
        // ******************* start interchanges loop *****************
        for(int i=0;i<interchanges.getLength();i++){
        	
            String desadvDate = doc.getChildNodes().item(0).getChildNodes().item(0).getAttributes().getNamedItem("Date").getTextContent();
            String desadvTime = doc.getChildNodes().item(0).getChildNodes().item(0).getAttributes().getNamedItem("Time").getTextContent();
            
            // Validate date format: YYMMDD (6 digits)
            if(desadvDate.length() != 6){
            	errors.add(SSS+"[segment "+segCounter+"] DESADV date format is incorrect, should be YYMMDD (6 digits), found: "+desadvDate.length()+" digits");
            } else {
            	// Additional validation: check if date values are valid
            	try {
            		int year = Integer.parseInt(desadvDate.substring(0, 2));
            		int month = Integer.parseInt(desadvDate.substring(2, 4));
            		int day = Integer.parseInt(desadvDate.substring(4, 6));
            		
            		if(month < 1 || month > 12) {
            			errors.add(SSS+"[segment "+segCounter+"] DESADV date has invalid month: "+month+" (should be 01-12)");
            		}
            		if(day < 1 || day > 31) {
            			errors.add(SSS+"[segment "+segCounter+"] DESADV date has invalid day: "+day+" (should be 01-31)");
            		}
            	} catch(NumberFormatException e) {
            		errors.add(SSS+"[segment "+segCounter+"] DESADV date contains non-numeric characters: "+desadvDate);
            	}
            }
            
            // Validate time format: HHMM (4 digits)
            if(desadvTime.length() != 4){
            	errors.add(SSS+"[segment "+segCounter+"] DESADV time format is incorrect, should be HHMM (4 digits), found: "+desadvTime.length()+" digits");
            } else {
            	// Additional validation: check if time values are valid
            	try {
            		int hour = Integer.parseInt(desadvTime.substring(0, 2));
            		int minute = Integer.parseInt(desadvTime.substring(2, 4));
            		
            		if(hour < 0 || hour > 23) {
            			errors.add(SSS+"[segment "+segCounter+"] DESADV time has invalid hour: "+hour+" (should be 00-23)");
            		}
            		if(minute < 0 || minute > 59) {
            			errors.add(SSS+"[segment "+segCounter+"] DESADV time has invalid minute: "+minute+" (should be 00-59)");
            		}
            	} catch(NumberFormatException e) {
            		errors.add(SSS+"[segment "+segCounter+"] DESADV time contains non-numeric characters: "+desadvTime);
            	}
            }
            
            // Validate that document date is not in the future
            Date today = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("yyMMdd");
            try {
            	Date desadvDateD = formatter.parse(desadvDate);
            	if(desadvDateD.compareTo(today) > 0){
            		errors.add(SSS+"[segment "+segCounter+"] DESADV date is in the future: "+desadvDate+" (today: "+formatter.format(today)+")");
            	}
            } catch(Exception e) {
            	errors.add(SSS+"[segment "+segCounter+"] Failed to parse DESADV date: "+desadvDate+" - "+e.getMessage());
            }
            //output = formatter.parse(desadvDate); format(today);
           // System.out.println("****************** " + desadvDateD.compareTo(today));
        	
	         Node sender = interchanges.item(i).getChildNodes().item(0);
	         Node receiver = interchanges.item(i).getChildNodes().item(1);
	         Node group = interchanges.item(i).getChildNodes().item(2);
	         Node transaction = group.getChildNodes().item(0);
	         NodeList segments = group.getChildNodes().item(0).getChildNodes();
	         
         // Validate UNB (Interchange Header) segment attributes
         // These are standard UN/EDIFACT validations
         String docType = transaction.getAttributes().getNamedItem("DocType").getTextContent();
         if(!docType.equals("DESADV")){
        	 errors.add("[UNB] ERROR: Document type should be DESADV, found: "+docType);
         }
         
         String version = transaction.getAttributes().getNamedItem("Version").getTextContent();
         if(!version.equals("D")){
        	 errors.add("[UNB] ERROR: Version should be D (DESADV), found: "+version);
         }
         
         String release = transaction.getAttributes().getNamedItem("Release").getTextContent();
         if(!release.equals("96A")){
        	 errors.add("[UNB] ERROR: Release should be 96A, found: "+release);
         }
         
         String agency = transaction.getAttributes().getNamedItem("Agency").getTextContent();
         if(!agency.equals("UN")){
        	 errors.add("[UNB] ERROR: Agency should be UN, found: "+agency);
         }
         
         // Renault-specific: Association code validation
         // Note: A01052 and A01051 are Renault-specific association codes
         String association = transaction.getAttributes().getNamedItem("Association").getTextContent();
         if(!association.equals("A01052") && !association.equals("A01051")){
        	 errors.add("[UNB] ERROR: Association code should be A01052 or A01051 (Renault-specific), found: "+association);
         }
	         
         // Renault-specific validation: Check if receiver EDI code matches Renault's code
         // Note: This is a Renault-specific requirement, not a general DESADV standard
         String receiverId = receiver.getFirstChild().getAttributes().getNamedItem("Id").getTextContent();
         if(!receiverId.equals("1780129987")){
        	 errors.add(SSS+"[segment "+segCounter+"] ERROR: Receiver EDI code is incorrect, expected Renault code: 1780129987, found: "+receiverId);
         }

         // Renault-specific: Receiver EDI code qualifier should be empty
         String receiverQual = receiver.getFirstChild().getAttributes().getNamedItem("Qual").getTextContent();
         if(!receiverQual.isEmpty()){
        	 errors.add(SSS+"[segment "+segCounter+"] ERROR: Receiver EDI code qualifier should be empty for Renault, found: "+receiverQual);
         }
	         
	         
	         // verification de l'ordre des segments
	         // initialisation de segsOrder 
	         Scanner sc = new Scanner(PathUtils.resolveEdiSrcPath("_segments-ordre.txt"));
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

	         if(requiredSegments.size()>0){
	        	 //errorsLines.add(segCounter);
	        	 errors.add(SSS+"[segment "+segCounter+"] required segments absent:"+ requiredSegments);
	        	 //throw new Exception("[segment:"+segCounter+"]"+"required segments absent:"+ requiredSegments);
        
	         }
	    }
        System.out.println("**************************************");
        System.out.println("************* errors *****************");
        System.out.println("**************************************");
         
        for (String s : errors) {
            System.out.println(s);
        }
    }

  //#############################################################
  //################# ediCheck ##################################
  //#############################################################  
    private static void ediCheck(Node n) throws Exception {
    	
    	// Null check - must use == not equals() to avoid NullPointerException
    	if(n == null) return;
    	
    	out = System.out;
    	//out.println("#### start ediCheck ["+n.getNodeName()+"] ####");
    	if(Arrays.asList(segsWithoutQualifiant).contains(actualSeg))
    		SSS = '['+actualSeg+']';
    	else
    		SSS = '['+actualSeg+"+"+SegQualifiant+']';

    	//################# if segment #####################################
		if(n.getNodeName().equals("segment")){ 
			segCounter++;
			
			 SegQualifiant = getQualifiant(n);
			 actualSeg = n.getAttributes().getNamedItem("Id").getTextContent();
			 	 
			 // Check if segment with qualifier is valid
			 if(!Arrays.asList(segsWithoutQualifiant).contains(actualSeg)) {
				if(!desadvSegsListP.contains(actualSeg+"+"+SegQualifiant)){
					errors.add(SSS+"[segment "+segCounter+"] Unknown segment with qualifier: "+actualSeg+"+"+SegQualifiant);
				}
			 }
			 
			// Check if segment type is valid
			if(!desadvSegsList.contains(actualSeg)){
				errors.add(SSS+"[segment "+segCounter+"] Unknown segment type: "+actualSeg);
			}
			
			// Check if qualifier is valid for this segment
			actualElmContent = n.getTextContent();
			if(!desadvSegsListP.contains(actualSeg+"+"+SegQualifiant) && !desadvSegsListVar.contains(actualSeg) && !Arrays.asList(segsWithoutQualifiant).contains(actualSeg)){
				errors.add(SSS+"[segment "+segCounter+"] Unknown qualifier for segment: "+actualSeg+"+"+SegQualifiant);
			}
				
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

			if(mappingSeg==null){
				//errorsLines.add(segCounter);
				errors.add(SSS+"[segment "+segCounter+"] mapping info error! is "+actualSeg+"+"+SegQualifiant+" a valid segment?");
				//throw new Exception("[segment:"+segCounter+"]"+"mapping info error! is "+actualSeg+"+"+SegQualifiant+" a valid segment?");
			}
			
			//************ checking repetitivity
			String repetivity = mappingSeg.getAttributes().getNamedItem("repetivity").getTextContent();
			String required = mappingSeg.getAttributes().getNamedItem("required").getTextContent();
			
			switch(actualSeg+"+"+SegQualifiant){
			case "QTY+52":
			case "MEA+AAY":
			case "PCI+17":
				if((getSegRepetitivity(actualSeg+"+"+SegQualifiant)) > (Integer.parseInt(repetivity)*(getSegRepetitivity("PAC")))){
					//errorsLines.add(segCounter);
					errors.add(SSS+"[segment "+segCounter+"] "+ actualSeg+"+"+SegQualifiant+" de plus !!!");
					//throw new Exception("[segment:"+segCounter+"]"+actualSeg+"+"+SegQualifiant+" de plus !!!");
				}
				break;
			case "RFF+AAT":
			case "GIR+3":
				if((getSegRepetitivity(actualSeg+"+"+SegQualifiant)) > (Integer.parseInt(repetivity)*(getSegRepetitivity("PCI+17")))){
					//errorsLines.add(segCounter);
					errors.add(SSS+"[segment "+segCounter+"] "+actualSeg+"+"+SegQualifiant+" de plus !!!");
					//throw new Exception("[segment:"+segCounter+"]"+actualSeg+"+"+SegQualifiant+" de plus !!!");
				}
				break;
			case "PIA+1":
			case "LOC+159":
			case "RFF+ON":
			case "QTY+12":
				if((getSegRepetitivity(actualSeg+"+"+SegQualifiant)) > (Integer.parseInt(repetivity)*(getSegRepetitivity("LIN")))){
					//errorsLines.add(segCounter);
					errors.add(SSS+"[segment "+segCounter+"] "+actualSeg+"+"+SegQualifiant+" de plus !!!"+getSegRepetitivity(actualSeg+"+"+SegQualifiant)+"==="+(Integer.parseInt(repetivity)*(getSegRepetitivity("LIN"))));
					//throw new Exception("[segment:"+segCounter+"]"+actualSeg+"+"+SegQualifiant+" de plus !!!");
				}
				break;
				
			default:
				System.out.println(actualSeg+"+"+SegQualifiant+" repetitivity:"+getSegRepetitivity(actualSeg+"+"+SegQualifiant));
				if(!Arrays.asList(segsWithoutQualifiant).contains(actualSeg) && (getSegRepetitivity(actualSeg+"+"+SegQualifiant))>Integer.parseInt(repetivity)){
					//errorsLines.add(segCounter);
					errors.add(SSS+"[segment "+segCounter+"] "+actualSeg+"+"+SegQualifiant+" de plus *");
					//throw new Exception("[segment:"+segCounter+"]"+actualSeg+"+"+SegQualifiant+" de plus ");
				}
			
			}
			
			
			switch(actualSeg){
			case "ALI":
			case "IMD":
				if((getSegRepetitivity(actualSeg)) > (Integer.parseInt(repetivity)*(getSegRepetitivity("LIN")))){
					//errorsLines.add(segCounter);
					errors.add(SSS+"[segment "+segCounter+"] "+actualSeg+" de plus !!!"+getSegRepetitivity(actualSeg));
					//throw new Exception("[segment:"+segCounter+"]"+actualSeg+"+"+SegQualifiant+" de plus !!!");
				}
				break;
			}
			//************************************
			//************ end checking repetitivity
	
			
			//************ check GIR segs ********************
			
			if(actualSeg.equals("PAC")){
				
				if(girCounter!=0 && pac!=0){
					if(girCounter>pac){
						//errorsLines.add(segCounter);
						errors.add(SSS+"[segment "+segCounter+"] segment GIR de plus ! should be "+pac+" found "+girCounter);
					}
					if(girCounter<pac){
						//errorsLines.add(segCounter);
						errors.add(SSS+"[segment "+segCounter+"] segment GIR de moins ! should be "+pac+" found "+girCounter);
					}
				}
				
				System.out.println(getQualifiant(n)+":+++++++++++++++++++++++++++++++++");
				pac = Integer.parseInt(getQualifiant(n));

				girCounter = 0;
			}		
			
			if(actualSeg.equals("GIR")){
				girCounter++;
				
				//start check GIR requirements
				String ran = getRAN(n);
		    	System.out.println("############### RAN:"+ran+" #####################");
				//end check GIR requirements
			}
			
			if(actualSeg.equals("LIN")){
				if(girCounter>pac){
					errors.add(SSS+"[segment "+segCounter+"] segment GIR de plus ! should be "+pac+" found "+girCounter);
				}
				if(girCounter<pac){
					//errorsLines.add(segCounter);
					errors.add(SSS+"[segment "+segCounter+"] segment GIR de moins ! should be "+pac+" found "+girCounter);
				}
				girCounter = 0;
			}
			
			
			//************* end check GIR segs ****************
						
			 
			 // check DTM segments  
			 if(actualSeg.equals("DTM")){

				if(SegQualifiant.equals("11")) {
					dtm11 = getDTM(n);
				}
				
				if(SegQualifiant.equals("132")) {
					dtm132 = getDTM(n);
				}
				
				if(SegQualifiant.equals("137")) {
					dtm137 = getDTM(n);
				}
				
				if(dtm132 != null && dtm11 != null)
					if(Long.parseLong(dtm132) <= Long.parseLong(dtm11)){
						//errorsLines.add(segCounter);
						errors.add(SSS+"[segment "+segCounter+"] DTM+132  doit etre superieur � DTM+11");
						//throw new Exception("[segment:"+segCounter+"]"+"DTM+132 ne doit pas etre inferieur � DTM+11");
					}
						
				if(dtm137 != null && dtm11 != null)
					if(Long.parseLong(dtm11) < Long.parseLong(dtm137)){
						//errorsLines.add(segCounter);
						errors.add(SSS+"[segment "+segCounter+"] DTM+11  doit etre superieur � DTM+137");
						//throw new Exception("[segment:"+segCounter+"]"+"DTM+11 ne doit pas etre inferieur � DTM+137");

					}
				if(dtm137 != null && dtm132 != null)
					if(Long.parseLong(dtm132) <= Long.parseLong(dtm137)){
						//errorsLines.add(segCounter);
						errors.add(SSS+"[segment "+segCounter+"] DTM+132  doit etre superieur � DTM+137");
						//throw new Exception("[segment:"+segCounter+"]"+"DTM+132 ne doit pas etre inferieur � DTM+137");

					}
				
			 }
			 // end checking DTM segments 
			 
			
			// start checking segments order
			for(int h=0;h<segsOrder.size();h++){
				if(!Arrays.asList(segsWithoutQualifiant).contains(actualSeg)){
					if(segsOrder.get(h).get(0).get(0).equals(actualSeg+"+"+SegQualifiant)){
						if(!Arrays.asList(segsWithoutQualifiant).contains(previousSeg)){
							if(!segsOrder.get(h).get(1).contains(previousSeg+"+"+previousQualifiant)){
								//errorsLines.add(segCounter);
								errors.add(SSS+"[segment "+segCounter+"] order Exception ! "+actualSeg+"+"+SegQualifiant+" does not come after "+previousSeg+"+"+previousQualifiant);
								//throw new Exception("[segment:"+segCounter+"]"+"order Exception ! "+actualSeg+"+"+SegQualifiant+" does not come after "+previousSeg+"+"+previousQualifiant);
							}
						}else{
							if(!segsOrder.get(h).get(1).contains(previousSeg)){
								//errorsLines.add(segCounter);
								errors.add(SSS+"[segment "+segCounter+"] order Exception ! "+actualSeg+"+"+SegQualifiant+" does not come after "+previousSeg);
								//throw new Exception("[segment:"+segCounter+"]"+"order Exception ! "+actualSeg+"+"+SegQualifiant+" does not come after "+previousSeg+"+"+previousQualifiant);
							}
						}
					}
				}else{
					if(segsOrder.get(h).get(0).get(0).equals(actualSeg)){
						if(!Arrays.asList(segsWithoutQualifiant).contains(previousSeg)){
							if(!segsOrder.get(h).get(1).contains(previousSeg+"+"+previousQualifiant)){
								//errorsLines.add(segCounter);
								errors.add(SSS+"[segment "+segCounter+"] order Exception ! "+actualSeg+" does not come after "+previousSeg+"+"+previousQualifiant);
								//throw new Exception("[segment:"+segCounter+"]"+"order Exception ! "+actualSeg+"+"+SegQualifiant+" does not come after "+previousSeg+"+"+previousQualifiant);
							}
						}else{
							if(!segsOrder.get(h).get(1).contains(previousSeg)){
								//errorsLines.add(segCounter);
								errors.add(SSS+"[segment "+segCounter+"] order Exception ! "+actualSeg+" does not come after "+previousSeg);
								//throw new Exception("[segment:"+segCounter+"]"+"order Exception ! "+actualSeg+"+"+SegQualifiant+" does not come after "+previousSeg+"+"+previousQualifiant);
							}
						}
					}
				}
			}
			// end checking segments order
			
			
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
		    
		    if(mappingElm==null){
		    	//errorsLines.add(segCounter);
		    	errors.add(SSS+"[segment "+segCounter+"] champ "+id.substring(3)+" in "+actualSeg+"+"+SegQualifiant+" element not found in mapping!");
		    	//throw new Exception("[segment:"+segCounter+"]"+"champ "+id.substring(3)+" in "+actualSeg+"+"+SegQualifiant+" element not found in mapping!");
		    }
		    
		    //************ check QTY+12 = #PAC*QTY+52
		    
			if((actualSeg+"+"+SegQualifiant).equals("QTY+52")){
				qty52 = Integer.parseInt(getElmContent(n,1));
			}	
			
			if((actualSeg+"+"+SegQualifiant).equals("QTY+12")){
				int qty12 = Integer.parseInt(getElmContent(n,1));
				if(qty12!=(qty52*pac)){
					//errorsLines.add(segCounter);
					errors.add(SSS+"[segment "+segCounter+"] WARNING: QTY+12 different from PAC*QTY+52! should be "+(qty52*pac)+" found "+qty12);
			
				}
			}
			
		    //************ end QTY+12 = #PAC*QTY+52
			
			
			//************ checking WEIGHT UNIT *********
			if((actualSeg+"+"+SegQualifiant).equals("MEA+AAX")){
				float weight = 0;
				if(id.equals("MEA03")){
					weight  = Float.parseFloat(getElmContent(n,1));
					if(weight==0){
						//errorsLines.add(segCounter);
				    	errors.add(SSS+"[segment "+segCounter+"] weight should not be null");
					}
				}
			}	
			//************ end checking WEIGHT UNIT *********
			
			
			//************ check vendor code ************
			if((actualSeg+"+"+SegQualifiant).equals("NAD+CZ")){
				if(id.equals("NAD02")){
					nadCzCode = getElmContent(n,0);
				}
			}
			if((actualSeg+"+"+SegQualifiant).equals("RFF+ADE")){
				if(id.equals("RFF01")){
					rffAdeCode = getElmContent(n,1);
				}
			}
			
			if(rffAdeCode!=null && nadCzCode!=null){
				if(Integer.parseInt(rffAdeCode)!=Integer.parseInt(nadCzCode.substring(0, nadCzCode.length()-2))){
					//errorsLines.add(segCounter);
					errors.add(SSS+"[segment "+segCounter+"] vendor code does not correspond in RFF+ADE and NAD+CZ");
				}
			rffAdeCode = null;
			nadCzCode = null;
			}	
				
			//************ end check vendor code ************
		    
			if(mappingElm.getAttributes().getLength()>2){
			    String format = mappingElm.getAttributes().getNamedItem("format").getTextContent();
			    String length = mappingElm.getAttributes().getNamedItem("length").getTextContent();
			    String required = mappingElm.getAttributes().getNamedItem("required").getTextContent();

			    System.out.println("***************[element]***************");
			    System.out.println("********** content of "+id+": "+n.getTextContent());
			    System.out.println("********** format of "+id+": "+format);
			    System.out.println("********** length of "+id+": "+length);
			    System.out.println("********** required of "+id+": "+required);
			    
			    //************************************
			    // check format & length requirements
				if(format.equals("an") && !StringUtils.isAlphanumeric(n.getTextContent().replaceAll("\\s+|\\.|-+","")) ){
					//errorsLines.add(segCounter);
					errors.add(SSS+"[segment "+segCounter+"] ["+ actualElm+"] is not Alphanumeric");
					//throw new Exception("[segment:"+segCounter+"]"+actualElm+" is not Alphanumeric");
				}
				
				if(format.equals("n") && !StringUtils.isNumeric(n.getTextContent().replaceAll("\\s+","")) ){
					//errorsLines.add(segCounter);
					errors.add(SSS+"[segment "+segCounter+"] ["+ actualElm+"] is not numeric");
					//throw new Exception("[segment:"+segCounter+"]"+actualElm+" is not numeric");
				}
				
				if(n.getTextContent().length() > Integer.parseInt(length)){
					//errorsLines.add(segCounter);
					errors.add(SSS+"[segment "+segCounter+"] ["+ actualElm+"] length exceeds limit of "+length+" chars");
					//throw new Exception("[segment:"+segCounter+"]"+actualElm+" length exceeds limit of "+length+" chars");
				}
				//************************************
			}
			// if element is comoposite
			if(n.getAttributes().getLength()>1){
				NodeList subelements = n.getChildNodes();
				for(int i=0;i<subelements.getLength();i++){
					ediCheck(subelements.item(i));
				}
			}else{ // if element is not composite
				if(actualElm.equals(actualSeg+"01")){
					Node mp = getMappingElm2(n);
			
				    String format = mp.getAttributes().getNamedItem("format").getTextContent();
				    String length = mp.getAttributes().getNamedItem("length").getTextContent();
				    String required = mp.getAttributes().getNamedItem("required").getTextContent();

				    if(!mp.getTextContent().isEmpty()){
			    		System.out.println("TTTTTTTTTT   CHECK TTTTTTTTTT");
			    		System.out.println(mp.getTextContent());
			    		System.out.println(n.getTextContent());
			    		System.out.println("TTTTTTTTTT end   CHECK TTTTTTTTTT");
				    	if(mp.getTextContent().startsWith("REGEX:")){
				    		String regex = mp.getTextContent().substring(6);
				    		Pattern p = Pattern.compile(regex);
				    	    Matcher m = p.matcher(n.getTextContent());
				    		if(!m.matches()){
				    			//errorsLines.add(segCounter);
				    			errors.add(SSS+"[segment "+segCounter+"] ["+ actualElm+"] element content is incorrect ("+n.getTextContent()+")");
				    	
				    		}
				    	}
				    	else if(!n.getTextContent().equals(mp.getTextContent())){
				    		//errorsLines.add(segCounter);
				    		errors.add(SSS+"[segment "+segCounter+"] ["+ actualElm+"] element content should be "+mp.getTextContent());
				    		//throw new Exception("[segment "+segCounter+"] element content should be "+mp.getTextContent());
				    	}
				    }
					// start segments repitition count
					segs.add(actualSeg+"+"+SegQualifiant);
					// end segments repitition count

					previousElmContent = n.getTextContent();
				}
				Node mp = getMappingElm2(n);
			    if(!mp.getTextContent().isEmpty()){
			    	System.out.println("oooooooooooooooooooooo");
		    		System.out.println(mp.getTextContent());
		    		System.out.println(n.getTextContent());
			    	if(mp.getTextContent().startsWith("REGEX:")){
			    		System.out.println("TTTTTTTTTT REGEX CHECK TTTTTTTTTT");
			    		System.out.println(mp.getTextContent().substring(6));
			    		System.out.println(n.getTextContent());
			    		System.out.println("TTTTTTTTTT end REGEX CHECK TTTTTTTTTT");
			    		String regex = mp.getTextContent().substring(6);
			    		Pattern p = Pattern.compile(regex);
			    	    Matcher m = p.matcher(n.getTextContent());
			    		if(!m.matches()){
			    			//errorsLines.add(segCounter);
			    			errors.add(SSS+"[segment "+segCounter+"] ["+ actualElm+"] element content is incorrect ("+n.getTextContent()+")");
			    	
			    		}
			    	}
			    	else if(!n.getTextContent().equals(mp.getTextContent())){
			    		//errorsLines.add(segCounter);
			    		errors.add(SSS+"[segment "+segCounter+"] ["+ actualElm+"] element content should be "+mp.getTextContent());
			    		//throw new Exception("[segment "+segCounter+"] element content should be "+mp.getTextContent());
			    	}
			    }
			}
			previousElm = id;
		    
		}
		//################# endif element ##################################
		
		//################# if subelement ##################################
		if(n.getNodeName().equals("subelement")){
			String sequence = n.getAttributes().getNamedItem("Sequence").getTextContent();
			
		    Node mappingSubElm = getMappingSubElm(n);
		    
		    if(!mappingSubElm.getTextContent().isEmpty()){
		    	if(mappingSubElm.getTextContent().startsWith("REGEX:")){
		    		System.out.println("TTTTTTTTTT REGEX CHECK TTTTTTTTTT");
		    		System.out.println(mappingSubElm.getTextContent().substring(6));
		    		System.out.println(n.getTextContent());
		    		System.out.println("TTTTTTTTTT end REGEX CHECK TTTTTTTTTT");
		    		String regex = mappingSubElm.getTextContent().substring(6);
		    		Pattern p = Pattern.compile(regex);
		    	    Matcher m = p.matcher(n.getTextContent());
		    		if(!m.matches()){
		    			//errorsLines.add(segCounter);
		    			errors.add(SSS+"[segment "+segCounter+"] "+ actualElm+"[subelement:"+sequence+"] content is incorrect ("+n.getTextContent()+")");
		    	
		    		}
		    	}
		    	else if(!n.getTextContent().equals(mappingSubElm.getTextContent())){
		    		//errorsLines.add(segCounter);
		    		errors.add(SSS+"[segment "+segCounter+"] "+ actualElm+"[subelement:"+sequence+"] content should be "+mappingSubElm.getTextContent());
		    		//throw new Exception("[segment "+segCounter+"] element content should be "+mp.getTextContent());
		    	}
		    }
		    
			if(mappingSubElm.getAttributes().getLength()>2){
			    String format = mappingSubElm.getAttributes().getNamedItem("format").getTextContent();
			    String length = mappingSubElm.getAttributes().getNamedItem("length").getTextContent();
			    String required = mappingSubElm.getAttributes().getNamedItem("required").getTextContent();

			    System.out.println("***************[subelement]***************");
			    System.out.println("********** format of "+sequence+": "+format);
			    System.out.println("********** length of "+sequence+": "+length);
			    System.out.println("********** required of "+sequence+": "+required);
			    
			    //************************************
			    // check format & length requirements
				if(format.equals("an") && !StringUtils.isAlphanumeric(n.getTextContent().replaceAll("\\s+|\\.|-+","")) ){
					//errorsLines.add(segCounter);
					errors.add(SSS+"[segment "+segCounter+"] "+ actualElm+"[subelement:"+sequence+"] is not Alphanumeric");
					//throw new Exception("[segment:"+segCounter+"]"+actualElm+" is not Alphanumeric");
				}
				
				if(format.equals("n") && !StringUtils.isNumeric(n.getTextContent().replaceAll("\\s+","")) ){
					//errorsLines.add(segCounter);
					errors.add(SSS+"[segment "+segCounter+"] "+ actualElm+"[subelement:"+sequence+" is not numeric");
					//throw new Exception("[segment:"+segCounter+"]"+actualElm+" is not numeric");
				}
				
				if(n.getTextContent().length() > Integer.parseInt(length)){
					//errorsLines.add(segCounter);
					errors.add(SSS+"[segment "+segCounter+"] "+ actualElm+"[subelement:"+sequence+" length exceeds limit of "+length+" chars");
					//throw new Exception("[segment:"+segCounter+"]"+actualElm+" length exceeds limit of "+length+" chars");
				}
				//************************************
			    
			}
			
			if(actualElm.equals(actualSeg+"01") && isComposite.equals("yes") && sequence.equals("1")){

				actualElmContent = n.getTextContent();
				
				// start segments repitition count
				segs.add(actualSeg+"+"+actualElmContent);
				// end segments repitition count
				
				if(!desadvSegsListP.contains(actualSeg+"+"+n.getTextContent()) && !desadvSegsList.contains(actualSeg)){
					//errorsLines.add(segCounter);
					errors.add(SSS+"[segment "+segCounter+"] Segment inconnue: "+actualSeg+"+"+n.getTextContent());
					//throw new Exception("[segment:"+segCounter+"]"+"Segment inconnue: "+actualSeg+"+"+n.getTextContent());
				}
				
				out.println("remove=="+actualSeg+"+"+n.getTextContent());
			    requiredSegments.remove(actualSeg+"+"+n.getTextContent());
			    
			    previousElmContent = n.getTextContent();
			}
		}
		//################# endif subelement ###############################
		
		//out.println("#### end ediCheck ####");
    }
    
    
  //############################ String getRAN ##########################
    private static String getRAN(Node n) {
    	System.out.println("start RAN search func ===============");
    	System.out.println(n.getChildNodes().getLength());
    	if(n.getChildNodes().getLength()>= 3)
    		return n.getLastChild().getChildNodes().item(0).getTextContent();
    		
		return null;
	}

    
  //############################ Node getMappingNode ##########################
    private static Node getMappingNode(Node n, String e, String attr){
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
    private static Node getMappingNode(Node n, String nodeType, String attr, String content){
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
    private static Node getMappingAttrsSeg(Node n, String qualifiant){
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
    
    
    //############################ getMappingElm2() ##########################
    private static Node getMappingElm2(Node n){
    	String id = n.getAttributes().getNamedItem("Id").getTextContent();

//    	System.out.println("MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM");
//    	System.out.println("[element] starting search for mapping info of "+id);
//    	System.out.println("MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM");
    	
    	NodeList childnodes = mappingSeg.getChildNodes();
    	for(int i=0;i<childnodes.getLength();i++){
    		if(childnodes.item(i).getNodeName().equals("element")){
    			if(childnodes.item(i).getAttributes().getNamedItem("Id").getTextContent().equals(id)){
    				if(childnodes.item(i).getAttributes().getLength()==2)
    					return childnodes.item(i).getChildNodes().item(1);
    				else
    					return childnodes.item(i);
    			}
    		}
    	}
    	System.out.println("%%%%failed to find mapping elemnt%%%");
		return null;
    
    }
    
    //############################ getMappingElm() ##########################
    private static Node getMappingElm(Node n){
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
    private static Node getMappingSubElm(Node n){
    	
    	System.out.println(n.getAttributes().getLength());
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
    private static String getQualifiant(Node n){
    	
		if(n.getFirstChild().hasChildNodes()){
		    return n.getFirstChild().getFirstChild().getTextContent();
		}else{
			return n.getFirstChild().getTextContent();
		}
    }
    
    //##################### getSegRepetitivity(String s) ############################*
    private static int getSegRepetitivity(String seg){
    	
        for(int k=0;k<repetitivitySegs.size();k++){
	       	 String[] array = (String[]) repetitivitySegs.get(k);
	       	 if(array[0].equals(seg))
	       		 return Integer.parseInt(array[1]);
	     }
		return 0;
    	
    }
    //##################### getDTM(Node n) ############################*
    private static String getDTM(Node n){
		
		if(n.getFirstChild().hasChildNodes()){
		    return n.getFirstChild().getChildNodes().item(1).getTextContent();
		}else{
			return n.getChildNodes().item(1).getTextContent();
		}
    	
    }
    //##################### getElmContent(Node n) ############################*
    private static String getElmContent(Node n,int i){
		
    	try{
			if(n.hasChildNodes()){
			    return n.getChildNodes().item(i).getTextContent();
			}else{
				return n.getTextContent();
			}
    	}catch(Exception e){
    		return "0";
    	}
    }
}
