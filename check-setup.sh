#!/bin/bash

# Setup check script for DESADV Parser
# This script checks if all required dependencies are in place

echo "Checking DESADV Parser setup..."
echo ""

ERRORS=0

# Check for EDIReader library
EDIREADER_PATH="lib/edireader-4.7.3.jar"
if [ ! -f "$EDIREADER_PATH" ]; then
    echo "❌ ERROR: EDIReader library not found!"
    echo "   Expected location: $EDIREADER_PATH"
    echo ""
    echo "   To fix this:"
    echo "   1. Download edireader-4.7.3.jar from https://www.berryworks.com/"
    echo "   2. Create the lib directory: mkdir -p lib"
    echo "   3. Place the JAR file in the lib/ directory"
    echo "   4. Run this script again to verify"
    echo ""
    ERRORS=$((ERRORS + 1))
else
    echo "✅ EDIReader library found: $EDIREADER_PATH"
fi

# Check for Java
if ! command -v java &> /dev/null; then
    echo "❌ ERROR: Java is not installed or not in PATH"
    ERRORS=$((ERRORS + 1))
else
    JAVA_VERSION=$(java -version 2>&1 | head -n 1)
    echo "✅ Java found: $JAVA_VERSION"
fi

# Check for Maven
if ! command -v mvn &> /dev/null; then
    echo "❌ ERROR: Maven is not installed or not in PATH"
    ERRORS=$((ERRORS + 1))
else
    MVN_VERSION=$(mvn -version | head -n 1)
    echo "✅ Maven found: $MVN_VERSION"
fi

# Check for required config files
CONFIG_FILES=("edi/src/_base.xml" "edi/src/_segments-ordre.txt")
for file in "${CONFIG_FILES[@]}"; do
    if [ ! -f "$file" ]; then
        echo "⚠️  WARNING: Config file not found: $file"
    else
        echo "✅ Config file found: $file"
    fi
done

echo ""
if [ $ERRORS -eq 0 ]; then
    echo "✅ All required dependencies are in place!"
    echo "   You can now run: mvn clean compile"
    exit 0
else
    echo "❌ Setup incomplete. Please fix the errors above before building."
    exit 1
fi

