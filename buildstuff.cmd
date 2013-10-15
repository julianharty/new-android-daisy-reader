@echo This script builds the code and runs the unit tests for DaisyReader
@echo Currently it does not check for failures, please check visually or improve
@echo this command file so it does the hard work for you :)
mkdir /sdcard
cd .\daisy-engine
ant clean jar build-and-test
cd ..\DaisyBookLister
copy ..\daisy-engine\lib\*.jar .\libs\
ant clean debug
@echo PLEASE Check the results of this job before submitting code to github. 
@echo The tests should all pass before submitting the code.
