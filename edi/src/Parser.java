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
    private static boolean dtmRelationshipsChecked = false;
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

    /**
     * Reset all static variables to ensure proper test isolation
     * Made public for test access
     */
    public static void resetStaticVariables() {
    	// Reset counters
    	segCounter = 2;
    	girCounter = 0;
        pac = 0;
        qty52 = 0;
        lineCounter = 0;
        
        // Reset collections
    	errors.clear();
    	errorsLines.clear();
    	requiredSegments.clear();
    	notUniqueSegsList.clear();
    	desadvSegsListVar.clear();
    	desadvSegsListP.clear();
    	desadvSegsList.clear();
    	segsOrder.clear();
    	segs.clear();
    	repetitivitySegs.clear();
        
        // Reset DTM date variables
        dtm132 = null;
        dtm137 = null;
        dtm11 = null;
        dtmRelationshipsChecked = false;
        
        // Reset other state variables
        previousSeg = null;
        actualSeg = null;
        previousElm = null;
        actualElm = null;
        previousQualifiant = null;
        actualQualifiant = null;
        previousElmContent = null;
        actualElmContent = null;
        mappingElm = null;
        mappingSeg = null;
        isComposite = "no";
        SegQualifiant = null;
        nadCzCode = null;
        rffAdeCode = null;
        SSS = "";
        mappingSegments = null;
    }
    
    public static void main(String[] args) throws Exception {
    	// Reset all static variables at the start of each parse
    	resetStaticVariables();
    	
    	
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
        // Create a new DocumentBuilderFactory for each parse to avoid caching issues
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setIgnoringComments(ignoreComments);
        dbf.setIgnoringElementContentWhitespace(ignoreWhitespace);
        dbf.setCoalescing(putCDATAIntoText);
        dbf.setExpandEntityReferences(!createEntityRefs);
        DocumentBuilder db = dbf.newDocumentBuilder();
        File desadvFile = PathUtils.resolveEdiSrcPath("_desadv.xml");
        File baseFile = PathUtils.resolveEdiSrcPath("_base.xml");
        
        System.out.println("DEBUG Parser: Reading XML from: " + desadvFile.getAbsolutePath());
        System.out.println("DEBUG Parser: XML file exists: " + desadvFile.exists());
        if (desadvFile.exists()) {
            System.out.println("DEBUG Parser: XML file size: " + desadvFile.length() + " bytes");
            System.out.println("DEBUG Parser: XML file last modified: " + new java.util.Date(desadvFile.lastModified()));
            
            // Verify XML content before parsing - extract DTM+132 date
            try {
                String xmlContent = new String(java.nio.file.Files.readAllBytes(desadvFile.toPath()), "UTF-8");
                int dtm132Index = xmlContent.indexOf("<subelement Sequence=\"1\">132</subelement>");
                if (dtm132Index > 0) {
                    int dateStart = xmlContent.indexOf("<subelement Sequence=\"2\">", dtm132Index);
                    if (dateStart > 0) {
                        int dateEnd = xmlContent.indexOf("</subelement>", dateStart);
                        if (dateEnd > 0) {
                            String dateValue = xmlContent.substring(dateStart + 26, dateEnd);
                            System.out.println("DEBUG Parser: XML DTM+132 date value before parsing: '" + dateValue + "'");
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("DEBUG Parser: Could not read XML file for verification: " + e.getMessage());
            }
        }
        
        // Use InputSource with explicit file to avoid caching issues
        org.xml.sax.InputSource is = new org.xml.sax.InputSource(new java.io.FileInputStream(desadvFile));
        is.setSystemId(desadvFile.toURI().toString());
        Document doc = db.parse(is);
        Document ediMapping = db.parse(baseFile);
        // end reading xml file
        
        mappingSegments = ediMapping.getChildNodes().item(0).getChildNodes();
        
        
        NodeList interchanges = doc.getChildNodes().item(0).getChildNodes();
        

        
        // ******************* start interchanges loop *****************
        System.out.println("DEBUG: Total interchanges: " + interchanges.getLength());
        for(int i=0;i<interchanges.getLength();i++){
        	// Reset DTM date variables for each interchange
        	System.out.println("DEBUG: Processing interchange " + (i+1) + ", resetting DTM dates");
        	dtm132 = null;
        	dtm137 = null;
        	dtm11 = null;
        	dtmRelationshipsChecked = false;
        	
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
				// For segments like MEA and LOC, we need to match based on element content too
				if(actualSeg.equals("MEA") || actualSeg.equals("LOC")){
					mappingSeg = getMappingNodeByContent(n, actualSeg);
				} else {
					mappingSeg = getMappingNode(n,"segment","Id");
				}
			}

			if(mappingSeg==null){
				//errorsLines.add(segCounter);
				errors.add(SSS+"[segment "+segCounter+"] mapping info error! is "+actualSeg+"+"+SegQualifiant+" a valid segment?");
				//throw new Exception("[segment:"+segCounter+"]"+"mapping info error! is "+actualSeg+"+"+SegQualifiant+" a valid segment?");
				// Skip further processing for this segment if mapping is not found
				return;
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
			String dtmDate = getDTM(n);
			String dtmFormat = getDTMFormat(n); // Get format code (102, 203, etc.)
			
			// Validate DTM date format and values
			if(dtmDate != null && dtmDate.length() >= 6) {
				try {
					int month, day;
					
					// Parse date according to format code
					if("203".equals(dtmFormat) || dtmDate.length() == 8) {
						// CCYYMMDD format (8 digits) - format code 203
						month = Integer.parseInt(dtmDate.substring(4, 6));
						day = Integer.parseInt(dtmDate.substring(6, 8));
					} else {
						// YYMMDD format (6 digits) - format code 102
						month = Integer.parseInt(dtmDate.substring(2, 4));
						day = Integer.parseInt(dtmDate.substring(4, 6));
					}
					
					if(month < 1 || month > 12) {
						errors.add(SSS+"[segment "+segCounter+"] DTM+"+SegQualifiant+" has invalid month: "+month+" (should be 01-12)");
					}
					if(day < 1 || day > 31) {
						errors.add(SSS+"[segment "+segCounter+"] DTM+"+SegQualifiant+" has invalid day: "+day+" (should be 01-31)");
					}
					
					// Validate time if present (format: CCYYMMDDHHMM or YYMMDDHHMM)
					if(dtmDate.length() >= 10) {
						int timeStart = ("203".equals(dtmFormat) || dtmDate.length() == 12) ? 8 : 6;
						String timePart = dtmDate.substring(timeStart, timeStart + 4); // HHMM
						int hour = Integer.parseInt(timePart.substring(0, 2));
						int minute = Integer.parseInt(timePart.substring(2, 4));
						
						if(hour < 0 || hour > 23) {
							errors.add(SSS+"[segment "+segCounter+"] DTM+"+SegQualifiant+" has invalid hour: "+hour+" (should be 00-23)");
						}
						if(minute < 0 || minute > 59) {
							errors.add(SSS+"[segment "+segCounter+"] DTM+"+SegQualifiant+" has invalid minute: "+minute+" (should be 00-59)");
						}
					}
				} catch(NumberFormatException e) {
					errors.add(SSS+"[segment "+segCounter+"] DTM+"+SegQualifiant+" contains non-numeric characters: "+dtmDate);
				} catch(StringIndexOutOfBoundsException e) {
					errors.add(SSS+"[segment "+segCounter+"] DTM+"+SegQualifiant+" date format is incorrect: "+dtmDate);
				}
			}

		if(SegQualifiant.equals("11")) {
			System.out.println("DEBUG: Storing DTM+11 date: '" + dtmDate + "' from segment " + segCounter);
			dtm11 = dtmDate;
		}
		
		if(SegQualifiant.equals("132")) {
			System.out.println("DEBUG: Storing DTM+132 date: '" + dtmDate + "' from segment " + segCounter);
			dtm132 = dtmDate;
		}
		
		if(SegQualifiant.equals("137")) {
			System.out.println("DEBUG: Storing DTM+137 date: '" + dtmDate + "' from segment " + segCounter);
			dtm137 = dtmDate;
		}
				
		// Only check date relationships once after all three dates are available
		// This prevents false errors when dates are processed in different orders
		if(!dtmRelationshipsChecked && dtm132 != null && dtm137 != null && dtm11 != null){
			// Trim any whitespace from dates
			dtm132 = dtm132.trim();
			dtm137 = dtm137.trim();
			dtm11 = dtm11.trim();
			
			System.out.println("DEBUG: Checking date relationships at segment " + segCounter + ":");
			System.out.println("  dtm132: '" + dtm132 + "'");
			System.out.println("  dtm137: '" + dtm137 + "'");
			System.out.println("  dtm11:  '" + dtm11 + "'");
			
			// Check: DTM+132 (document date) <= DTM+137 (despatch date) <= DTM+11 (delivery date)
			long dtm132Long = Long.parseLong(dtm132);
			long dtm137Long = Long.parseLong(dtm137);
			long dtm11Long = Long.parseLong(dtm11);
			
			boolean error132_137 = dtm132Long > dtm137Long;
			boolean error137_11 = dtm137Long > dtm11Long;
			boolean error132_11 = dtm132Long > dtm11Long;
			
			System.out.println("  dtm132 > dtm137: " + error132_137);
			System.out.println("  dtm137 > dtm11:  " + error137_11);
			System.out.println("  dtm132 > dtm11:  " + error132_11);
			
			if(error132_137){
				System.out.println("  ERROR: Adding dtm132 > dtm137 error");
				errors.add(SSS+"[segment "+segCounter+"] DTM+132 (document date) must be less than or equal to DTM+137 (despatch date)");
			}
			if(error137_11){
				System.out.println("  ERROR: Adding dtm137 > dtm11 error");
				errors.add(SSS+"[segment "+segCounter+"] DTM+137 (despatch date) must be less than or equal to DTM+11 (delivery date)");
			}
			if(error132_11){
				System.out.println("  ERROR: Adding dtm132 > dtm11 error");
				errors.add(SSS+"[segment "+segCounter+"] DTM+132 (document date) must be less than or equal to DTM+11 (delivery date)");
			}
			dtmRelationshipsChecked = true; // Mark as checked to prevent duplicate checks
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
    
    //############################ getMappingNodeByContent() ##########################
    // Gets mapping for segments that need to match by element content (e.g., MEA+AAY with different MEA02, LOC with different LOC01)
    private static Node getMappingNodeByContent(Node n, String segmentId){
    	String atr = n.getAttributes().getNamedItem("Id").getTextContent();
    	
    	// Get the actual element content to match
    	String elementContent = null;
    	if(segmentId.equals("MEA")){
    		// For MEA, check MEA02 subelement 1 (the second element's first subelement)
    		NodeList elements = n.getChildNodes();
    		for(int i = 0; i < elements.getLength(); i++){
    			Node elem = elements.item(i);
    			if(elem.getNodeName().equals("element")){
    				String elemId = elem.getAttributes().getNamedItem("Id").getTextContent();
    				if("MEA02".equals(elemId) && elem.hasChildNodes()){
    					NodeList subelements = elem.getChildNodes();
    					for(int j = 0; j < subelements.getLength(); j++){
    						Node subelem = subelements.item(j);
    						if(subelem.getNodeName().equals("subelement")){
    							String seq = subelem.getAttributes().getNamedItem("Sequence").getTextContent();
    							if("1".equals(seq)){
    								elementContent = subelem.getTextContent();
    								break;
    							}
    						}
    					}
    					break;
    				}
    			}
    		}
    	} else if(segmentId.equals("LOC")){
    		// For LOC, check LOC01 (the first element)
    		NodeList elements = n.getChildNodes();
    		for(int i = 0; i < elements.getLength(); i++){
    			Node elem = elements.item(i);
    			if(elem.getNodeName().equals("element")){
    				String elemId = elem.getAttributes().getNamedItem("Id").getTextContent();
    				if("LOC01".equals(elemId)){
    					elementContent = elem.getTextContent();
    					break;
    				}
    			}
    		}
    	}
    	
    	// Now find the matching mapping
    	for(int i=0;i<mappingSegments.getLength();i++){
    		Node tmpNode = mappingSegments.item(i);
    		if(tmpNode.getNodeName().equals("segment")){
    			if(tmpNode.getAttributes().getNamedItem("Id").getTextContent().equals(atr)){
    				// Check if element content matches
    				if(elementContent != null){
    					NodeList mappingElements = tmpNode.getChildNodes();
    					for(int j = 0; j < mappingElements.getLength(); j++){
    						Node mappingElem = mappingElements.item(j);
    						if(mappingElem.getNodeName().equals("element")){
    							String elemId = mappingElem.getAttributes().getNamedItem("Id").getTextContent();
    							if(segmentId.equals("MEA") && "MEA02".equals(elemId)){
    								// Check MEA02 subelement 1
    								if(mappingElem.hasChildNodes()){
    									NodeList subelements = mappingElem.getChildNodes();
    									for(int k = 0; k < subelements.getLength(); k++){
    										Node subelem = subelements.item(k);
    										if(subelem.getNodeName().equals("subelement")){
    											String seq = subelem.getAttributes().getNamedItem("Sequence").getTextContent();
    											if("1".equals(seq) && elementContent.equals(subelem.getTextContent())){
    												return tmpNode;
    											}
    										}
    									}
    								}
    							} else if(segmentId.equals("LOC") && "LOC01".equals(elemId)){
    								// Check LOC01 content
    								if(elementContent.equals(mappingElem.getTextContent())){
    									return tmpNode;
    								}
    							}
    						}
    					}
    				} else {
    					// If we couldn't extract element content, return first match (fallback)
    					return tmpNode;
    				}
    			}
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
    	
    	List<Node> matchingNodes = new ArrayList<Node>();
    	for(int i=0;i<mappingSegments.getLength();i++){
    		Node tmpNode = mappingSegments.item(i);
    		if(tmpNode.getNodeName().equals("segment")){
    			if(tmpNode.getAttributes().getNamedItem("Id").getTextContent().equals(atr)){
    				if(notUniqueSegsList.contains(actualSeg)){
    					if(tmpNode.getChildNodes().item(1).hasChildNodes() && tmpNode.getChildNodes().item(1).getChildNodes().getLength()>1){
    						if(tmpNode.getChildNodes().item(1).getChildNodes().item(1).getTextContent().equals(qualifiant))
    							matchingNodes.add(tmpNode);
    					}else{
    						if(tmpNode.getChildNodes().item(1).getTextContent().equals(qualifiant))
    							matchingNodes.add(tmpNode);
    					}
    				}else{
    					
    					return tmpNode;
    					
    				}
    			}
    		}
    	}
    	
    	// If multiple matches found (e.g., MEA+AAY with different MEA02), match by element content
    	if(matchingNodes.size() > 1 && actualSeg.equals("MEA")){
    		// Extract MEA02 content from the actual segment (can be simple element or composite)
    		String elementContent = null;
    		NodeList elements = n.getChildNodes();
    		for(int i = 0; i < elements.getLength(); i++){
    			Node elem = elements.item(i);
    			if(elem.getNodeName().equals("element")){
    				String elemId = elem.getAttributes().getNamedItem("Id").getTextContent();
    				if("MEA02".equals(elemId)){
    					// Check if it's a composite element with subelements
    					if(elem.hasChildNodes()){
    						boolean foundSubelement = false;
    						NodeList subelements = elem.getChildNodes();
    						for(int j = 0; j < subelements.getLength(); j++){
    							Node subelem = subelements.item(j);
    							if(subelem.getNodeName().equals("subelement")){
    								String seq = subelem.getAttributes().getNamedItem("Sequence").getTextContent();
    								if("1".equals(seq)){
    									elementContent = subelem.getTextContent();
    									foundSubelement = true;
    									break;
    								}
    							}
    						}
    						// If no subelement found, it might be a simple element
    						if(!foundSubelement){
    							elementContent = elem.getTextContent().trim();
    						}
    					} else {
    						// Simple element - get text content directly
    						elementContent = elem.getTextContent().trim();
    					}
    					break;
    				}
    			}
    		}
    		
    		// Find matching mapping by MEA02 subelement 1
    		if(elementContent != null){
    			for(Node tmpNode : matchingNodes){
    				NodeList mappingElements = tmpNode.getChildNodes();
    				for(int j = 0; j < mappingElements.getLength(); j++){
    					Node mappingElem = mappingElements.item(j);
    					if(mappingElem.getNodeName().equals("element")){
    						String elemId = mappingElem.getAttributes().getNamedItem("Id").getTextContent();
    						if("MEA02".equals(elemId) && mappingElem.hasChildNodes()){
    							NodeList subelements = mappingElem.getChildNodes();
    							for(int k = 0; k < subelements.getLength(); k++){
    								Node subelem = subelements.item(k);
    								if(subelem.getNodeName().equals("subelement")){
    									String seq = subelem.getAttributes().getNamedItem("Sequence").getTextContent();
    									if("1".equals(seq) && elementContent.equals(subelem.getTextContent())){
    										return tmpNode;
    									}
    								}
    							}
    						}
    					}
    				}
    			}
    		}
    	}
    	
    	// Return first match if no content-based matching or single match
    	if(matchingNodes.size() > 0){
    		return matchingNodes.get(0);
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
    // Extracts the date value from DTM segment (subelement Sequence="2")
    private static String getDTM(Node n){
		Node firstElement = n.getFirstChild();
		if(firstElement != null && firstElement.hasChildNodes()){
			NodeList subelements = firstElement.getChildNodes();
			// Date value is in subelement Sequence="2"
			for(int i = 0; i < subelements.getLength(); i++){
				Node subelem = subelements.item(i);
				if(subelem.getNodeName().equals("subelement")){
					String seq = subelem.getAttributes().getNamedItem("Sequence").getTextContent();
					if("2".equals(seq)){
						return subelem.getTextContent();
					}
				}
			}
		}
		// Fallback to old method if structure is different
		if(n.getFirstChild().hasChildNodes()){
		    return n.getFirstChild().getChildNodes().item(1).getTextContent();
		}else{
			return n.getChildNodes().item(1).getTextContent();
		}
    	
    }
    
    //##################### getDTMFormat(Node n) ############################*
    // Gets the date format code (102, 203, etc.) from DTM segment
    private static String getDTMFormat(Node n){
		Node firstElement = n.getFirstChild();
		if(firstElement != null && firstElement.hasChildNodes()){
			NodeList subelements = firstElement.getChildNodes();
			// Format code is in the 3rd subelement (Sequence="3")
			for(int i = 0; i < subelements.getLength(); i++){
				Node subelem = subelements.item(i);
				if(subelem.getNodeName().equals("subelement")){
					String seq = subelem.getAttributes().getNamedItem("Sequence").getTextContent();
					if("3".equals(seq)){
						return subelem.getTextContent();
					}
				}
			}
		}
		return "102"; // Default to YYMMDD format
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
