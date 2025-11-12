# Testing Guide

## Test Structure

Tests are located in the standard Maven test directory structure:
- **Test class**: `src/test/java/DesadvParserTest.java`
- **Test resources**: `src/test/resources/*.txt`

## Running Tests

```bash
# Compile tests
mvn test-compile

# Run all tests
mvn test

# Run a specific test
mvn test -Dtest=DesadvParserTest#testValidDesadv
```

## Test Files Created

Comprehensive test files with various error scenarios:

### Test Files:

1. **desadv_valid.txt** - Valid DESADV file (no errors expected)
2. **desadv_missing_required_segment.txt** - Missing RFF+ON segment
3. **desadv_wrong_date_format.txt** - Invalid date format (wrong length)
4. **desadv_invalid_date_values.txt** - Invalid date/time values:
   - Month > 12 (20141303)
   - Day > 31 (20140432)
   - Hour > 23 (2530)
5. **desadv_wrong_date_logic.txt** - Incorrect date relationships:
   - DTM+132 (document date) > DTM+137 (despatch date) - WRONG
   - DTM+137 (despatch date) > DTM+11 (delivery date) - WRONG
6. **desadv_wrong_receiver_edi.txt** - Wrong receiver EDI code (9999999999 instead of 1780129987)
7. **desadv_wrong_document_type.txt** - Wrong document type (INVOIC instead of DESADV)
8. **desadv_missing_gir_segments.txt** - Missing GIR segments (PAC=2 but only 1 GIR found)
9. **desadv_invalid_qualifier.txt** - Invalid qualifier (DTM+999)
10. **desadv_wrong_package_count.txt** - Wrong package count (QTY+12=50 but should be PAC*QTY+52=42)

## Test Status

**Current Status**: Tests are compiling and running successfully.

**Test Results** (as of latest run):
- ✅ **8 tests passing** - Error detection tests are working correctly
- ⚠️ **3 tests failing** - Test data or parser logic needs adjustment:
  - `testValidDesadv` - The "valid" test file contains errors according to the parser (date logic, GIR content, segment order issues)
  - `testInvalidDateValues` - Parser may not be detecting all invalid date value scenarios
  - `testErrorCounts` - Related to the valid file issue

**Fixed Issues**:
- ✅ NullPointerException when handling invalid qualifiers - Fixed by adding early return when `mappingSeg` is null
- ✅ Test compilation issues - Fixed by moving tests to standard Maven location (`src/test/java`)
- ✅ UNT segment count errors - Fixed by correcting segment counts in all test files

## Manual Testing (Alternative)

If you want to test manually without running JUnit tests:

### Option 1: Manual Testing via GUI
1. Open the application: `mvn exec:java -Dexec.mainClass="EdiGUI"`
2. Load each test file from `src/test/resources/`
3. Click "Exécuter" to validate
4. Check the error messages

### Option 2: Command Line Testing
```bash
# Copy test file to temp location
cp src/test/resources/desadv_valid.txt edi/src/desadv.tmp

# Run the parser
mvn exec:java -Dexec.mainClass="EDItoXML" -Dexec.args="edi/src/desadv.tmp"
```

## Test Coverage

The test files cover:
- ✅ Missing required segments (`testMissingRequiredSegment`)
- ✅ Date format validation (`testWrongDateFormat`)
- ✅ Date logic validation (`testWrongDateLogic`)
- ✅ Receiver EDI code validation (`testWrongReceiverEdi`)
- ✅ Document type validation (`testWrongDocumentType`)
- ✅ GIR segment count validation (`testMissingGirSegments`)
- ✅ Invalid qualifier detection (`testInvalidQualifier`) - **Fixed: No longer throws NullPointerException**
- ✅ Package count validation (`testWrongPackageCount`)
- ⚠️ Valid file validation (`testValidDesadv`) - **Needs adjustment: test file contains errors**
- ⚠️ Date value validation (`testInvalidDateValues`) - **Needs adjustment: parser may need enhanced validation**

## Known Issues

1. **testValidDesadv failure**: The "valid" test file (`desadv_valid.txt`) is reporting errors:
   - Date logic issues (DTM+132, DTM+137, DTM+11 relationships)
   - GIR content validation issues
   - Segment order issues (MEA+AAY, IMD, QTY+12 order)
   - LOC qualifier issues
   
   **Action needed**: Either fix the test file to be truly valid, or adjust parser logic if the file is actually valid.

2. **testInvalidDateValues failure**: The parser may not be detecting all invalid date value scenarios (month > 12, day > 31, hour > 23, minute > 59).
   
   **Action needed**: Review date validation logic in `Parser.java` to ensure all invalid values are caught.

3. **testErrorCounts failure**: Related to the valid file issue above.

## Next Steps

1. ✅ ~~Fix Maven test configuration~~ - **DONE**: Tests moved to standard location
2. ✅ ~~Fix NullPointerException for invalid qualifiers~~ - **DONE**: Added early return
3. ⏳ Fix test data or parser logic for "valid" file validation
4. ⏳ Enhance date value validation if needed
5. ⏳ Add more test cases as needed

