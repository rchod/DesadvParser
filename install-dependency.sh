#!/bin/bash

# Script to install EDIReader library to local Maven repository

JAR_FILE="lib/edireader-4.7.3.jar"

if [ ! -f "$JAR_FILE" ]; then
    echo "❌ ERROR: EDIReader library not found!"
    echo "   Expected location: $JAR_FILE"
    echo ""
    echo "   Please:"
    echo "   1. Download edireader-4.7.3.jar from https://www.berryworks.com/"
    echo "   2. Place it in the lib/ directory"
    echo "   3. Run this script again"
    exit 1
fi

echo "Installing EDIReader library to local Maven repository..."
echo ""

mvn install:install-file \
  -Dfile="$JAR_FILE" \
  -DgroupId=com.berryworks \
  -DartifactId=edireader \
  -Dversion=4.7.3 \
  -Dpackaging=jar \
  -DgeneratePom=true

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ EDIReader library successfully installed to local Maven repository!"
    echo "   You can now run: mvn clean compile"
else
    echo ""
    echo "❌ Failed to install EDIReader library"
    exit 1
fi

