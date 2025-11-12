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

**Current Status**: All tests are passing! ✅

**Test Results** (as of latest run):
- ✅ **11 tests passing** - All error detection tests are working correctly
- ✅ **0 tests failing** - All tests are now passing after fixing static variable isolation issues

**Fixed Issues**:
- ✅ NullPointerException when handling invalid qualifiers - Fixed by adding early return when `mappingSeg` is null
- ✅ Test compilation issues - Fixed by moving tests to standard Maven location (`src/test/java`)
- ✅ UNT segment count errors - Fixed by correcting segment counts in all test files
- ✅ DTM date relationship logic - Fixed by reversing comparison operators and adding date value validation
- ✅ GIR content validation - Fixed by updating regex in `_base.xml`
- ✅ LOC+159 mapping selection - Fixed by adding content-based mapping selection in `Parser.java`
- ✅ MEA+AAY mapping selection - Fixed by enhancing `getMappingAttrsSeg` to match by MEA02 subelement content
- ✅ Segment order issues (MEA+AAY, IMD, QTY+12) - Fixed by updating `_segments-ordre.txt`
- ✅ Static variable isolation - Fixed by creating `resetStaticVariables()` method and ensuring proper cleanup between tests
- ✅ Error collection persistence - Fixed by creating copies of error collections in `EDItoXML.main()` instead of adding references

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
- ✅ Invalid qualifier detection (`testInvalidQualifier`)
- ✅ Package count validation (`testWrongPackageCount`)
- ✅ Valid file validation (`testValidDesadv`) - **Now passing**
- ✅ Date value validation (`testInvalidDateValues`) - **Now passing**
- ✅ Error count validation (`testErrorCounts`) - **Now passing**

## Known Issues

None! All tests are passing. ✅

## Recent Fixes

1. **Static Variable Isolation**: Fixed test isolation issues by:
   - Creating a `resetStaticVariables()` method in `Parser.java` to reset all static variables
   - Calling the reset method in test setup (`@Before`) and before processing each test file
   - Clearing the `EDItoXML.result` static list at the start of each `main()` call
   - Creating copies of error collections instead of adding references to avoid persistence across test runs

2. **Error Collection Persistence**: Fixed issue where errors from previous test runs were persisting by:
   - Clearing `EDItoXML.result` list at the start of `main()`
   - Creating copies of `Parser.errors` and `Parser.errorsLines` before adding to result list
   - This ensures each test run gets fresh error collections

## Next Steps

All tests are passing! The parser is now working correctly with proper test isolation.
