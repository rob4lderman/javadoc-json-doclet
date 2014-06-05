#!/bin/sh

if [ -z $1 ]; then
    echo "usage: $0 <mainClass>"
    echo "      <mainClass>: Main, ExtractMain"
    exit 1
fi

MAIN=$1
shift

CP="target/javadoc-json-doclet-1.0-jar-with-dependencies.jar"

echo "java -Xms1024m -Xmx4096m -cp $CP com.surfapi.javadoc.$MAIN $*"
java -Xms1024m -Xmx4096m -cp $CP com.surfapi.javadoc.$MAIN $*

# run doclet manually:
# javadoc -docletpath target/javadoc-json-doclet-1.0-jar-with-dependencies.jar -doclet com.surfapi.javadoc.JsonDoclet -sourcepath src


