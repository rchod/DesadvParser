import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import org.junit.Assert;

import java.io.*;
import java.nio.file.*;
import java.util.HashSet;
import java.util.List;

// Note: These classes are in the default package
// In a real scenario, they should be in proper packages

/**
 * JUnit tests for DESADV Parser
 * Tests various error scenarios in DESADV EDI files
 */
public class DesadvParserTest {

    private static final String TEST_RESOURCES_DIR = "src/test/resources/";
    private static final String TEMP_DIR = "edi/src/";
    
    @Before
    public void setUp() {
        // Ensure temp directory exists
        new File(TEMP_DIR).mkdirs();
    }
    
    @After
    public void tearDown() {
        // Clean up temporary files
        try {
            Files.deleteIfExists(Paths.get(TEMP_DIR + "_desadv.xml"));
            Files.deleteIfExists(Paths.get(TEMP_DIR + "_desadv.tmp"));
        } catch (IOException e) {
            // Ignore cleanup errors
        }
    }

    /**
     * Helper method to process a test EDI file and return errors
     */
    @SuppressWarnings("unchecked")
    private HashSet<String> processTestFile(String testFileName) throws Exception {
        // Copy test file to temp location
        String testFilePath = TEST_RESOURCES_DIR + testFileName;
        String tempFilePath = TEMP_DIR + "desadv.tmp";
        
        Files.copy(Paths.get(testFilePath), Paths.get(tempFilePath), StandardCopyOption.REPLACE_EXISTING);
        
        // Process the file using EDItoXML (which calls Parser internally)
        String[] args = {tempFilePath};
        List<Object> result = EDItoXML.main(args);
        
        HashSet<String> errors = (HashSet<String>) result.get(0);
        
        return errors;
    }

    @Test
    public void testValidDesadv() throws Exception {
        HashSet<String> errors = processTestFile("desadv_valid.txt");
        
        Assert.assertTrue("Valid DESADV file should have no errors, but found: " + errors, 
                   errors.isEmpty());
    }

    @Test
    public void testMissingRequiredSegment() throws Exception {
        HashSet<String> errors = processTestFile("desadv_missing_required_segment.txt");
        
        Assert.assertFalse("Should detect missing required segments", errors.isEmpty());
        
        boolean foundMissingSegmentError = false;
        for (String error : errors) {
            if (error.contains("required segments absent") || 
                error.contains("required") && error.contains("absent")) {
                foundMissingSegmentError = true;
                break;
            }
        }
        Assert.assertTrue("Should report missing required segments", foundMissingSegmentError);
    }

    @Test
    public void testWrongDateFormat() throws Exception {
        HashSet<String> errors = processTestFile("desadv_wrong_date_format.txt");
        
        Assert.assertFalse("Should detect date format errors", errors.isEmpty());
        
        boolean foundDateFormatError = false;
        for (String error : errors) {
            if (error.contains("date format") || error.contains("DTM")) {
                foundDateFormatError = true;
                break;
            }
        }
        Assert.assertTrue("Should report date format error", foundDateFormatError);
    }

    @Test
    public void testInvalidDateValues() throws Exception {
        HashSet<String> errors = processTestFile("desadv_invalid_date_values.txt");
        
        Assert.assertFalse("Should detect invalid date values", errors.isEmpty());
        
        boolean foundInvalidDateError = false;
        for (String error : errors) {
            if ((error.contains("invalid month") || error.contains("invalid day") || 
                 error.contains("invalid hour") || error.contains("invalid minute"))) {
                foundInvalidDateError = true;
                break;
            }
        }
        Assert.assertTrue("Should report invalid date values (month/day/hour/minute)", foundInvalidDateError);
    }

    @Test
    public void testWrongDateLogic() throws Exception {
        HashSet<String> errors = processTestFile("desadv_wrong_date_logic.txt");
        
        Assert.assertFalse("Should detect date logic errors", errors.isEmpty());
        
        boolean foundDateLogicError = false;
        for (String error : errors) {
            if (error.contains("DTM+132") && error.contains("DTM+137") ||
                error.contains("DTM+137") && error.contains("DTM+11") ||
                error.contains("document date") && error.contains("despatch date") ||
                error.contains("despatch date") && error.contains("delivery")) {
                foundDateLogicError = true;
                break;
            }
        }
        Assert.assertTrue("Should report date logic errors (DTM relationships)", foundDateLogicError);
    }

    @Test
    public void testWrongReceiverEdi() throws Exception {
        HashSet<String> errors = processTestFile("desadv_wrong_receiver_edi.txt");
        
        Assert.assertFalse("Should detect wrong receiver EDI code", errors.isEmpty());
        
        boolean foundReceiverError = false;
        for (String error : errors) {
            if (error.contains("Receiver EDI code") || error.contains("Renault EDI code")) {
                foundReceiverError = true;
                break;
            }
        }
        Assert.assertTrue("Should report wrong receiver EDI code", foundReceiverError);
    }

    @Test
    public void testWrongDocumentType() throws Exception {
        HashSet<String> errors = processTestFile("desadv_wrong_document_type.txt");
        
        Assert.assertFalse("Should detect wrong document type", errors.isEmpty());
        
        boolean foundDocTypeError = false;
        for (String error : errors) {
            if (error.contains("Document type") && error.contains("DESADV")) {
                foundDocTypeError = true;
                break;
            }
        }
        Assert.assertTrue("Should report wrong document type", foundDocTypeError);
    }

    @Test
    public void testMissingGirSegments() throws Exception {
        HashSet<String> errors = processTestFile("desadv_missing_gir_segments.txt");
        
        Assert.assertFalse("Should detect missing GIR segments", errors.isEmpty());
        
        boolean foundGirError = false;
        for (String error : errors) {
            if (error.contains("GIR") && (error.contains("de moins") || error.contains("should be"))) {
                foundGirError = true;
                break;
            }
        }
        Assert.assertTrue("Should report missing GIR segments", foundGirError);
    }

    @Test
    public void testInvalidQualifier() throws Exception {
        HashSet<String> errors = processTestFile("desadv_invalid_qualifier.txt");
        
        Assert.assertFalse("Should detect invalid qualifier", errors.isEmpty());
        
        boolean foundQualifierError = false;
        for (String error : errors) {
            if (error.contains("Unknown qualifier") || 
                (error.contains("Qualifiant") && error.contains("inconnue")) ||
                (error.contains("DTM+999"))) {
                foundQualifierError = true;
                break;
            }
        }
        Assert.assertTrue("Should report invalid qualifier", foundQualifierError);
    }

    @Test
    public void testWrongPackageCount() throws Exception {
        HashSet<String> errors = processTestFile("desadv_wrong_package_count.txt");
        
        // This test checks QTY+12 vs PAC*QTY+52 relationship
        // PAC=2, QTY+52=21, so QTY+12 should be 42, but file has 50
        boolean foundPackageCountError = false;
        for (String error : errors) {
            if (error.contains("QTY+12") && 
                (error.contains("different from") || error.contains("should be"))) {
                foundPackageCountError = true;
                break;
            }
        }
        Assert.assertTrue("Should report wrong package count (QTY+12 != PAC*QTY+52)", foundPackageCountError);
    }

    @Test
    public void testErrorCounts() throws Exception {
        // Test that files with errors actually report them
        HashSet<String> validErrors = processTestFile("desadv_valid.txt");
        HashSet<String> invalidErrors = processTestFile("desadv_wrong_date_logic.txt");
        
        Assert.assertEquals("Valid file should have 0 errors", 0, validErrors.size());
        Assert.assertTrue("Invalid file should have errors", invalidErrors.size() > 0);
    }
}

