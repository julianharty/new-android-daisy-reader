#!/bin/bash
FAILED=0
cd ./daisy-engine
ant clean sources-jar
if [ "$?" = 1 ]; then
    echo "daisy-engine build failed!"
    FAILED=1
fi
cd ../DaisyBookLister
cp ../daisy-engine/lib/*.jar ./libs/
if [ "$?" = 1 ]; then
    echo "copy of the daisy-engine jar files failed."
    FAILED=1
fi
ant clean debug
if [ "$?" = 1 ]; then
   echo "Debug build of the DaisyBookLister app failed."
   FAILED=1
fi 
exit $FAILED

