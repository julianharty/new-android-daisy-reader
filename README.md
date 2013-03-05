new-android-daisy-reader
========================

New Daisy Reader Codebase. This is based on the codebase created in 2012 on the Google Code repository http://code.google.com/p/android-daisy-epub-reader

The project consists of a core 'engine' that represents the structure of an eBook and some Android apps.

Engine code
===========
The current implementation supports DAISY 2.02 books. The code for the engine is plain java code without dependencies on Android. We hope this will make the code more portable e.g. to other Java-based systems, and possibly even .Net platforms. 

Apps
====
Currently we have a proof-of-concept Android app which has a basic UI. New apps are being developed by various people.

Working with the code
=====================
You should be able to work with the code in an IDE or from the command line. The project includes ant build scripts. As the Android platform is revised frequently please also check the android development documentation for the android platform if you encounter problems with the builds. 'android update project' may help to address some of the build issues for the apps.

To build the daisy-engine, run ant in the root of the daisy-engine folder. The default build target will build the jar file. This can be then used in the apps project(s).

Running the tests for daisy-engine
----------------------------------
We're close to getting all the tests to run cleanly using ant. At the moment one test is excluded until we discover why the callback doesn't seem to be called.

To run the tests, in the daisy-engine folder, from a command-prompt / terminal run: ant test

The first time the tests run they want to copy some test data to /sdcard/ If ant cannot create the folder e.g. owning to security restrictions, then use sudo to create the folder and grant permissions for other accounts to create sub-folders, etc.

sudo mkdir /sdcard/
sudo chmod a+wrx /sdcard/


Julian Harty
 
