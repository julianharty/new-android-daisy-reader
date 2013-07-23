#!/bin/bash
FAILED=0
cd ./daisy-engine
ant clean build-and-test
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
exit $FAILED

