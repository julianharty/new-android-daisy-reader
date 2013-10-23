package org.androiddaisyreader.player;

/**
 * Represents the Time Interval between two times specified in a book's content.
 * 
 * Note: these are not intended to be used more generally.
 * 
 * @author jharty
 */
public enum SegmentTimeInfo {
    // The time intervals overlap
    OVERLAPPING,
    // The times are contiguous
    CONTIGUOUS,
    // There is a gap between the 2 times.
    GAP;

    public static SegmentTimeInfo compareTimesForAudioSegments(int timeToStartPlayingFrom,
            int timeLastSegmentFinished) {
        int difference = Math.abs(timeToStartPlayingFrom - timeLastSegmentFinished);
        if (difference <= 1) {
            return SegmentTimeInfo.CONTIGUOUS;
        } else if (timeToStartPlayingFrom > timeLastSegmentFinished) {
            return SegmentTimeInfo.GAP;
        }
        return SegmentTimeInfo.OVERLAPPING;
    }
}
