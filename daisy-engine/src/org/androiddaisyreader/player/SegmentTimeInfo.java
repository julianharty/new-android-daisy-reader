package org.androiddaisyreader.player;

/**
 * Represents the Time Interval between two times specified in a book's content.
 * 
 * Note: these are not intended to be used more generally.
 * 
 * @author jharty
 */
public enum SegmentTimeInfo {
	OVERLAPPING,  // The time intervals overlap
	CONTIGUOUS,  // The times are contiguous
	GAP;  // There is a gap between the 2 times.
	
	public static SegmentTimeInfo compareTimesForAudioSegments(int timeToStartPlayingFrom, int timeLastSegmentFinished) {
		int difference = Math.abs(timeToStartPlayingFrom - timeLastSegmentFinished);
		if (difference <= 1) {
			return SegmentTimeInfo.CONTIGUOUS;
		}
		else if (timeToStartPlayingFrom > timeLastSegmentFinished) {
			return SegmentTimeInfo.GAP;
		}
		return SegmentTimeInfo.OVERLAPPING;
	}
}
