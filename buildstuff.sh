#!/bin/bash
FAILED=0
cd ./daisy-engine
ant clean jar
if [ "$?" = 1 ]; then
    echo "daisy-engine build failed!"
    FAILED=1
fi
cd ..
