
 

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
import java.util.HashSet;
import java.util.List;


public class EDItoXML {
    private final InputSource inputSource;
    private final Writer generatedOutput;
    private final Reader inputReader;
    private boolean namespaceEnabled;
    private boolean recover;

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
    public static HashSet<String> main(String args[]) throws Exception {
        CommandLine commandLine = new CommandLine(args) {
            @Override
            public String usage() {
                return "EDItoXML [inputfile] [-o outputfile] [-n true|false] [-r true|false]";
            }
        };
        String inputFileName = "src\\desadv.tmp";
        if(args.length>0)
        	inputFileName = args[0];
        String outputFileName = "src\\DESADV.xml";
        boolean namespaceEnabled = "true".equals(commandLine.getOption("n"));
        boolean recover = "true".equals(commandLine.getOption("r"));

        // Establish input
        Reader inputReader;
        if (inputFileName == null) {
            inputReader = new InputStreamReader(System.in);
        } else {
            try {
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
                generatedOutput = new OutputStreamWriter(new FileOutputStream(
                        outputFileName), "ISO-8859-1");
                System.out.println("Output file " + outputFileName + " opened");
            } catch (IOException e) {
                System.out.println(e.getMessage());
                throw new RuntimeException(e.getMessage());
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
        
        PrintStream out = null;
		Parser e = new Parser(out);
	    e.main(args);
        return e.errors;
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
