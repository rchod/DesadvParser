DesadvParser
============

A tool to parse a DESADV message and look for errors

![alt tag](http://i.imgur.com/EKxEFbM.png)

## Features

It can detect the following errors:

- repeated segments 
- required segments not present
- wrong number qualifiants specified by Renault
- segments order
- UNT and UNZ check
- numeric and alphanumeric format
- DESADV header errors

Logical errors:

- wrong number of GIR segments
- wrong total of packages

## Quick Start

1. **Prerequisites**: Java 8+, Maven 3.6+

2. **Check your setup** (recommended):
   ```bash
   ./check-setup.sh    # On Linux/Mac
   check-setup.bat     # On Windows
   ```

3. **Install EDIReader library** (REQUIRED):
   - Download `edireader-4.7.3.jar` from [BerryWorks](https://www.berryworks.com/)
   - Create the lib directory: `mkdir -p lib` (or `mkdir lib` on Windows)
   - Place the JAR file in the `lib/` directory
   - See [SETUP.md](SETUP.md) for detailed instructions

4. **Build the project**:
   ```bash
   mvn clean compile
   ```

5. **Run the application**:
   ```bash
   mvn exec:java -Dexec.mainClass="EdiGUI"
   ```

For more details, see [SETUP.md](SETUP.md).

## Project Status

✅ **Updated and modernized:**
- Added Maven build system
- Fixed cross-platform file paths
- Improved resource loading
- Updated to Java 8 compatibility

⚠️ **Note**: The project requires the BerryWorks EDIReader library to be manually installed. See SETUP.md for instructions.
