
 

import com.berryworks.edireader.EDIReader;
import com.berryworks.edireader.EDIReaderFactory;
import com.berryworks.edireader.EDISyntaxException;
import com.berryworks.edireader.error.EDISyntaxExceptionHandler;
import com.berryworks.edireader.error.RecoverableSyntaxException;
import com.berryworks.edireader.util.CommandLine;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


public class EDItoXML {
    private final InputSource inputSource;
    private final Writer generatedOutput;
    private final Reader inputReader;
    private boolean namespaceEnabled;
    private boolean recover;
    public static List<Object> result = new ArrayList<Object>();


    public EDItoXML(Reader inputReader, Writer outputWriter) {
        this.inputReader = inputReader;
        inputSource = new InputSource(inputReader);
        generatedOutput = outputWriter;
    }

    /** 
     * Main processing method for the EDItoXML object
     * @throws TransformerException 
     */
    public void run() throws TransformerException {


            XMLReader ediReader = new EDIReader();

            // Tell the ediReader if an xmlns="http://..." is desired
            if (namespaceEnabled) {
                ((EDIReader) ediReader).setNamespaceEnabled(namespaceEnabled);
            }

            // Tell the ediReader to handle EDI syntax errors instead of aborting
            if (recover) {
                ((EDIReader) ediReader).setSyntaxExceptionHandler(new IgnoreSyntaxExceptions());
            }

            // Establish the SAXSource
            SAXSource source = new SAXSource(ediReader, inputSource);
            // Establish a Transformer
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            // Use a StreamResult to capture the generated XML output
            StreamResult result = new StreamResult(generatedOutput);
            // Call the Transformer to generate XML output from the parsed input
            transformer.transform(source, result);
        try {
            inputReader.close();
        } catch (IOException ignored) {
        }
        try {
            // Flush and close output to ensure file is fully written
            if (generatedOutput != null) {
                generatedOutput.flush();
            }
            generatedOutput.close();
        } catch (IOException ignored) {
        }
        
    }


    public void run_alternate1() {

        try {
            // Establish an EDIReader.
            EDIReader ediReader = EDIReaderFactory.createEDIReader(inputSource);

            // Tell the ediReader if an xmlns="http://..." is desired
            if (namespaceEnabled) {
                ediReader.setNamespaceEnabled(namespaceEnabled);
            }

            // Tell the ediReader to handle EDI syntax errors instead of aborting
            if (recover) {
                ediReader.setSyntaxExceptionHandler(new IgnoreSyntaxExceptions());
            }

            // Establish the SAXSource
            SAXSource source = new SAXSource(ediReader, inputSource);

            // Establish a Transformer
            Transformer transformer = TransformerFactory.newInstance().newTransformer();

            // Use a StreamResult to capture the generated XML output
            StreamResult result = new StreamResult(generatedOutput);

            // Call the Transformer to generate XML output from the parsed input
            transformer.transform(source, result);
        } catch (EDISyntaxException e) {
            System.err.println("\nSyntax error while parsing EDI: " + e);
        } catch (IOException e) {
            System.err.println("\nException attempting to read EDI data: " + e);
        } catch (TransformerConfigurationException e) {
            System.err.println("\nUnable to create Transformer: " + e);
        } catch (TransformerException e) {
            System.err.println("\nFailure to transform: " + e);
            System.err.println(e.getMessage());
        }
    }

    public void run_alternate2() {

        try {
            // Establish an XMLReader which is actually an EDIReader.
            System.setProperty("javax.xml.parsers.SAXParserFactory",
                    "com.berryworks.edireader.EDIParserFactory");
            SAXParserFactory sFactory = SAXParserFactory.newInstance();
            SAXParser sParser = sFactory.newSAXParser();
            XMLReader ediReader = sParser.getXMLReader();

            // Tell the ediReader if an xmlns="http://..." is desired
            if (namespaceEnabled) {
                ((EDIReader) ediReader).setNamespaceEnabled(namespaceEnabled);
            }

            // Tell the ediReader to handle EDI syntax errors instead of aborting
            if (recover) {
                ((EDIReader) ediReader).setSyntaxExceptionHandler(new IgnoreSyntaxExceptions());
            }

            // Establish the SAXSource
            SAXSource source = new SAXSource(ediReader, inputSource);

            // Establish a Transformer
            Transformer transformer = TransformerFactory.newInstance().newTransformer();

            // Use a StreamResult to capture the generated XML output
            StreamResult result = new StreamResult(generatedOutput);

            // Call the Transformer to generate XML output from the parsed input
            transformer.transform(source, result);
        } catch (SAXException e) {
            System.err.println("\nUnable to create EDIReader: " + e);
        } catch (ParserConfigurationException e) {
            System.err.println("\nUnable to create EDIReader: " + e);
        } catch (TransformerConfigurationException e) {
            System.err.println("\nUnable to create Transformer: " + e);
        } catch (TransformerException e) {
            System.err.println("\nFailure to transform: " + e);
            System.err.println(e.getMessage());
        }
    }

    /**
     * Main for EDItoXML.
     *
     * @param args command line arguments
     * @return 
     * @throws Exception 
     */
    public static List main(String args[]) throws Exception {
        // Clear the static result list to ensure fresh results for each call
        // This is critical for test isolation
        result.clear();
        
        CommandLine commandLine = new CommandLine(args) {
            @Override
            public String usage() {
                return "EDItoXML [inputfile] [-o outputfile] [-n true|false] [-r true|false]";
            }
        };
        String inputFileName = null;
        if(args.length>0)
        	inputFileName = args[0];
        else
        	inputFileName = PathUtils.resolveEdiSrcPath("_desadv.tmp").getAbsolutePath();
        String outputFileName = PathUtils.resolveEdiSrcPath("_desadv.xml").getAbsolutePath();
        boolean namespaceEnabled = "true".equals(commandLine.getOption("n"));
        boolean recover = "true".equals(commandLine.getOption("r"));

        // Establish input
        Reader inputReader;
        if (inputFileName == null) {
            inputReader = new InputStreamReader(System.in);
        } else {
            try {
                System.out.println("DEBUG EDItoXML: Reading from file: " + inputFileName);
                File inputFile = new File(inputFileName);
                if (!inputFile.exists()) {
                    throw new RuntimeException("Input file does not exist: " + inputFileName);
                }
                // Verify file content
                java.util.List<String> fileLines = java.nio.file.Files.readAllLines(inputFile.toPath());
                for (String line : fileLines) {
                    if (line.startsWith("DTM+132:")) {
                        System.out.println("DEBUG EDItoXML: DTM+132 line in input file: " + line);
                        break;
                    }
                }
                inputReader = new InputStreamReader(
                        new FileInputStream(inputFileName), "ISO-8859-1");
            } catch (IOException e) {
                System.out.println(e.getMessage());
                throw new RuntimeException(e.getMessage());
            }
        }

        // Establish output
        Writer generatedOutput;
        if (outputFileName == null) {
            generatedOutput = new OutputStreamWriter(System.out);
        } else {
            try {
                // Delete existing XML file to ensure fresh generation
                File outputFile = new File(outputFileName);
                if (outputFile.exists()) {
                    System.out.println("DEBUG EDItoXML: Deleting existing XML file: " + outputFileName);
                    outputFile.delete();
                    // Wait to ensure file system has processed deletion
                    Thread.sleep(50);
                }
                generatedOutput = new OutputStreamWriter(new FileOutputStream(
                        outputFileName), "ISO-8859-1");
                System.out.println("DEBUG EDItoXML: Output file " + outputFileName + " opened (fresh file)");
            } catch (IOException e) {
                System.out.println(e.getMessage());
                throw new RuntimeException(e.getMessage());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        }

        EDItoXML theObject = new EDItoXML(inputReader, generatedOutput);
        theObject.setNamespaceEnabled(namespaceEnabled);
        theObject.setRecover(recover);
        theObject.run();
        String s = System.getProperty("line.separator");
        System.out.print(s + "Transformation complete" + s);
        System.out.println("#############################");
        //////////////////////////////////////////////////////
        
        // Ensure XML file is fully written and synced to disk before parsing
        if (outputFileName != null) {
            try {
                // Force file system sync
                java.io.FileOutputStream fos = new java.io.FileOutputStream(outputFileName, true);
                fos.getFD().sync();
                fos.close();
                
                File xmlFile = new File(outputFileName);
                // Wait a bit to ensure file system has processed the sync
                Thread.sleep(100);
                if (xmlFile.exists()) {
                    System.out.println("DEBUG EDItoXML: XML file exists, size: " + xmlFile.length() + " bytes");
                    // Verify XML content - extract DTM+132 date value
                    try {
                        String xmlContent = new String(java.nio.file.Files.readAllBytes(xmlFile.toPath()), "UTF-8");
                        // Look for DTM+132 segment and extract date value (subelement Sequence="2")
                        int dtm132Index = xmlContent.indexOf("<subelement Sequence=\"1\">132</subelement>");
                        if (dtm132Index > 0) {
                            // Find the next subelement with Sequence="2" (the date value)
                            int dateStart = xmlContent.indexOf("<subelement Sequence=\"2\">", dtm132Index);
                            if (dateStart > 0) {
                                int dateEnd = xmlContent.indexOf("</subelement>", dateStart);
                                if (dateEnd > 0) {
                                    String dateValue = xmlContent.substring(dateStart + 26, dateEnd); // 26 = length of "<subelement Sequence=\"2\">"
                                    System.out.println("DEBUG EDItoXML: XML DTM+132 date value: '" + dateValue + "'");
                                }
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("DEBUG EDItoXML: Could not read XML file for verification: " + e.getMessage());
                    }
                } else {
                    System.out.println("DEBUG EDItoXML: WARNING - XML file does not exist after conversion!");
                }
            } catch (IOException e) {
                System.out.println("DEBUG EDItoXML: Could not sync XML file: " + e.getMessage());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        PrintStream out = null;
		Parser e = new Parser(out);
	    e.main(args);
	    // Create copies of the error collections to avoid reference issues
	    // This ensures that errors from one test run don't persist to the next
	    HashSet<String> errorsCopy = new HashSet<String>(e.errors);
	    List errorsLinesCopy = new ArrayList(e.errorsLines);
	    result.add(errorsCopy);
	    result.add(errorsLinesCopy);
	    
	    
        return result;
    }

    public boolean isNamespaceEnabled() {
        return namespaceEnabled;
    }

    public void setNamespaceEnabled(boolean namespaceEnabled) {
        this.namespaceEnabled = namespaceEnabled;
    }

    public void setRecover(boolean recover) {
        this.recover = recover;
    }

    static class IgnoreSyntaxExceptions implements EDISyntaxExceptionHandler {

        public boolean process(RecoverableSyntaxException syntaxException) {
            System.out.println("Syntax Exception. class: " + syntaxException.getClass().getName() + "  message:" + syntaxException.getMessage());
            // Return true to indicate that you want parsing to continue.
            return true;
        }
    }

}
