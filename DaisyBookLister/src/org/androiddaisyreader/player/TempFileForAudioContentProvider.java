/**
 * 
 */
package org.androiddaisyreader.player;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.androiddaisyreader.model.BookContext;
import org.androiddaisyreader.utils.Constants;
import org.apache.commons.io.IOUtils;

import android.os.StatFs;

/**
 * Helper class to provide the audio contents on Android.
 * 
 * This capability is needed as the MediaPlayer does not accept input streams,
 * essentially it expects to work with files.
 * 
 * @author Julian Harty
 */
public class TempFileForAudioContentProvider {
    private BookContext context;

    TempFileForAudioContentProvider(BookContext context) {
        this.context = context;
    }

    /**
     * Is the original content contained in a zip file?
     * 
     * @return true if it is, else false.
     */
    boolean doesContentNeedUnzipping() {
        return context.getBaseUri().endsWith(".zip");
    }

    /**
     * OK the name may be over precise, however it serves my purpose for now :)
     * 
     * @param sourceFilename the name of the source file
     * @return a file handle to the
     * @throws IOException
     */
    File getFileHandleToTempAudioFile(String sourceFilename) throws IOException {
        if (!doesContentNeedUnzipping()) {
            throw new IllegalStateException(
                    "Called incorrectly, should only be used to create temp files for zipped content.");
        }
        InputStream in = context.getResource(sourceFilename);
        File tempFile = File.createTempFile(Constants.PREFIX_AUDIO_TEMP_FILE,
                Constants.SUFFIX_AUDIO_TEMP_FILE);
        // check available space
        if (isEnoughSpace(tempFile, (long) in.available())) {
            FileOutputStream out = new FileOutputStream(tempFile);
            IOUtils.copy(in, out);
            File f = tempFile;
            if (f.exists()) {
                return f;
            }
        }
        // delete file has just create if the space is not enough, before return
        // null.
        tempFile.delete();
        return null;
    }

    private boolean isEnoughSpace(File tempFile, long sizeOfSourceFile) {
        StatFs stat = new StatFs(tempFile.getParent());
        double availableBlocks = (double) stat.getAvailableBlocks() * (double) stat.getBlockSize();
        return availableBlocks > sizeOfSourceFile;
    }

}
