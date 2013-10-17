/**
 * 
 */
package org.androiddaisyreader.player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.androiddaisyreader.AudioCallbackListener;
import org.androiddaisyreader.AudioPlayer;
import org.androiddaisyreader.model.Audio;
import org.androiddaisyreader.model.AudioPlayerState;
import org.androiddaisyreader.model.BookContext;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.util.Log;

/**
 * @author Julian Harty
 * 
 */
public class AndroidAudioPlayer implements AudioPlayer, OnCompletionListener {
    private static final String TAG = "DAISYAndroidAudioPlayer";
    private MediaPlayer player;
    private Audio audioSegment;
    private BookContext context;
    private TempFileForAudioContentProvider tempFileCreator;
    private List<AudioCallbackListener> listeners = new ArrayList<AudioCallbackListener>();

    public AndroidAudioPlayer(BookContext context) {
        this.context = context;
        tempFileCreator = new TempFileForAudioContentProvider(context);
        player = new MediaPlayer();
        player.setOnCompletionListener(this);
    }

    public void increaseVolume() {
        // TODO Auto-generated method stub

    }

    public void decreaseVolume() {
        // TODO Auto-generated method stub

    }

    public void toggleMute() {
        // TODO Auto-generated method stub

    }

    public void addCallbackListener(AudioCallbackListener listener) {
        listeners.add(listener);
    }

    public Audio getCurrentSegment() {
        return audioSegment;
    }

    public AudioPlayerState getInternalPlayerState() {
        // TODO Auto-generated method stub
        return null;
    }

    public void play() {
        // TODO 20120514 (jharty): Do I want a play() method in addition to
        String requestedFilename = audioSegment.getAudioFilename();
        String filenameToPlay;
        boolean doesContentNeedUnzipping = tempFileCreator.doesContentNeedUnzipping();
        if (doesContentNeedUnzipping) {
            try {
                File f = tempFileCreator.getFileHandleToTempAudioFile(requestedFilename);
                filenameToPlay = f.getAbsolutePath();
                Log.i(TAG, "Created temporary audio file, " + filenameToPlay);

            } catch (IOException ioe) {
                Log.e(TAG, "Problem obtaining a temporary audio file.", ioe);
                return;
            }
        } else {
            filenameToPlay = context.getBaseUri() + File.separator + requestedFilename;
        }
        Log.i(TAG, filenameToPlay);
        Log.i(TAG, context.getBaseUri());
        player.reset();
        try {
            player.setDataSource(filenameToPlay);
            player.prepare();
        } catch (Exception e) {
            // TODO 20120514 (jharty): Consider how to report exceptions. For
            // now this'll do.
            Log.e("TAG", e.getMessage(), e);
        }
        // TODO 20120514 (jharty): This starts from the start of the clip. Add
        // code to start later in the clip e.g. from a bookmark setting.
        player.seekTo(audioSegment.getClipBegin());
        player.start();

        // Seems we can delete the temporary file now.
        if (doesContentNeedUnzipping) {
            File deleteMe = new File(filenameToPlay);
            deleteMe.delete();
            Log.i(TAG, "Deleting temporary file, " + filenameToPlay);
        }
    }

    public MediaPlayer getCurrentPlayer() {
        return player;
    }

    public void seekTo(int newTimeInMilliseconds) {
        player.pause();
        player.seekTo(newTimeInMilliseconds);
        try {
            player.prepare();
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            Log.e("TAG", e.getMessage(), e);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            Log.e("TAG", e.getMessage(), e);
        }
    }

    public void setCurrentSegment(Audio audioSegment) {
        Log.i(TAG, "setCurrentSegment");
        this.audioSegment = audioSegment;

    }

    public void setInternalPlayerState(AudioPlayerState audioState) {
        // TODO Auto-generated method stub

    }

    public void onCompletion(MediaPlayer mp) {
        Log.i(TAG, "On Completion called. Resetting the state of the Media Player.");
        player.reset();
        for (AudioCallbackListener acl : listeners) {
            acl.endOfAudio();
        }
    }

}
