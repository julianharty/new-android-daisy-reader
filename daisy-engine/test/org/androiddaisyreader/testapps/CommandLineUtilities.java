package org.androiddaisyreader.testapps;

/**
 * Provides command line utilities to other command-line programs.
 * 
 * @author Julian Harty
 */
public class CommandLineUtilities {

	/**
	 * Print the command line usage for a given named program, where the
	 * program takes a single file name on the command line.
	 *  
	 * @param programName The name of the program to display. 
	 */
	static void printUsage(String programName) {
		System.out.println("Usage: \n\t" + programName + " filename");
	}
}
