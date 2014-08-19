#!/bin/sh
#


if [ -z $3 ]; then
    echo "usage: $0 <javadoc-args>"
    echo "              <javadoc-args>:"
    echo "                      -sourcepath <sourcepath>"
    echo "                      -subpackages <package> -subpackages <package>..." 
    echo "                      <package> ..." 
    exit 1
fi

# build the doclet path
dp=target/classes
for x in `find target/dependency`; do dp="$dp;$x"; done

# Notes: 
# 1. JSON output is written to STDOUT.
# 2. Each class/interface/package/method/etc has its own JSON object
# 3. The JSON output is NOT in array notation - it's just a series of JSON objects
#    (i.e. the objects are *not* encapsulated in [], nor are they delimited by ',')
javadoc \
        -doclet com.rga78.javadoc.JsonDoclet \
        -docletpath "$dp" \
        -quiet \
        -J-Xms1024m \
        -J-Xmx4096m \
        $*
        





