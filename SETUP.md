# Setup Instructions

## Prerequisites

- Java 8 or higher
- Maven 3.6 or higher

## Dependencies

This project requires the following external libraries:

1. **Apache Commons Lang3** - Available in Maven Central (automatically downloaded)
2. **BerryWorks EDIReader 4.7.3** - Not available in Maven Central, needs to be installed manually

## Installing EDIReader Library (REQUIRED)

**The BerryWorks EDIReader library is REQUIRED for this project to work.**

The library is not available in public Maven repositories. You must:

1. **Download the EDIReader library (version 4.7.3)**
   - Visit: https://www.berryworks.com/
   - Download the EDIReader library version 4.7.3
   - The file should be named `edireader-4.7.3.jar`

2. **Create the lib directory** (if it doesn't exist):
   ```bash
   mkdir -p lib
   ```

3. **Place the JAR file in the lib/ directory**:
   ```bash
   cp /path/to/edireader-4.7.3.jar lib/
   ```

4. **Verify the file is in place**:
   ```bash
   ls -la lib/edireader-4.7.3.jar
   ```

The Maven build will automatically use it as a system dependency.

### Alternative: Install to Local Maven Repository

If you prefer to use Maven's local repository instead of system scope:

```bash
mvn install:install-file \
  -Dfile=lib/edireader-4.7.3.jar \
  -DgroupId=com.berryworks \
  -DartifactId=edireader \
  -Dversion=4.7.3 \
  -Dpackaging=jar
```

Then update `pom.xml` to remove the `<scope>system</scope>` and `<systemPath>` elements, and change to a regular dependency.

## Building the Project

```bash
mvn clean compile
```

## Running the Application

```bash
mvn exec:java -Dexec.mainClass="EdiGUI"
```

Or build a JAR with dependencies:

```bash
mvn clean package
java -jar target/desadv-parser-1.0.0-jar-with-dependencies.jar
```

## Project Structure

- `edi/src/` - Source code
- `edi/src/_base.xml` - EDI mapping configuration
- `edi/src/_desadv.xml` - Parsed DESADV XML (generated)
- `edi/src/_segments-ordre.txt` - Segment order rules
- `edi/src/EDI-Logo.gif` - Application icon
- `lib/` - External JAR files (create this directory and add EDIReader JAR here)

## Notes

- The application expects to be run from the project root directory
- Temporary files are created in `edi/src/` directory
- Make sure all configuration files (`_base.xml`, `_segments-ordre.txt`) are present in `edi/src/`

