package org.androiddaisyreader.base;

import java.io.File;
import java.io.InputStream;
import java.util.UUID;

import org.androiddaisyreader.apps.PrivateException;
import org.androiddaisyreader.model.BookContext;
import org.androiddaisyreader.model.CurrentInformation;
import org.androiddaisyreader.model.DaisyBook;
import org.androiddaisyreader.model.DaisySection;
import org.androiddaisyreader.model.NccSpecification;
import org.androiddaisyreader.model.OpfSpecification;
import org.androiddaisyreader.model.Part;
import org.androiddaisyreader.model.Section;
import org.androiddaisyreader.utils.Constants;
import org.androiddaisyreader.utils.DaisyBookUtil;
import android.content.Context;

public class DaisyEbookReaderBaseMode {
    private String mPath;
    private Context mContext;

    public DaisyEbookReaderBaseMode(String path, Context context) {
        this.mPath = path;
        this.mContext = context;
    }

    /**
     * Open Daisy book with format 2.02.
     * 
     * @throws PrivateException
     */
    public DaisyBook openBook202() throws PrivateException {
        try {
            InputStream contents;
            BookContext mBookContext = getBookContext(mPath);
            contents = mBookContext.getResource(Constants.FILE_NCC_NAME_NOT_CAPS);
            DaisyBook book = NccSpecification.readFromStream(contents);
            return book;
        } catch (Exception e) {
            PrivateException ex = new PrivateException(e, mContext, mPath);
            throw ex;
        }
    }

    /**
     * Open Daisy book with format 3.0.
     * 
     * @throws PrivateException
     */
    public DaisyBook openBook30() throws PrivateException {

        try {
            InputStream contents;
            String path = getPathExactlyDaisy30(mPath);
            String opfName = getOpfFileName(mPath);
            BookContext bookContext = getBookContext(path);
            contents = bookContext.getResource(opfName);
            DaisyBook book = OpfSpecification.readFromStream(contents, bookContext);
            return book;
        } catch (Exception e) {
            PrivateException ex = new PrivateException(e, mContext, mPath);
            throw ex;
        }
    }

    /**
     * Gets the book context.
     * 
     * @param path the path of the book
     * @return the book context
     * @throws PrivateException
     */
    public BookContext getBookContext(String path) throws PrivateException {
        try {
            return DaisyBookUtil.openBook(path);
        } catch (Exception e) {
            PrivateException ex = new PrivateException(e, mContext, path);
            throw ex;
        }

    }

    /**
     * Read from stream.
     * 
     * @param contents the contents
     * @return the daisy book
     * @throws PrivateException
     */
    public DaisyBook readFromStream(InputStream contents) throws PrivateException {
        try {
            return NccSpecification.readFromStream(contents);
        } catch (Exception e) {
            PrivateException ex = new PrivateException(e, mContext, mPath);
            throw ex;
        }
    }

    /**
     * Gets the path exactly daisy30.
     * 
     * @param path the path
     * @return the path exactly daisy30
     */
    public String getPathExactlyDaisy30(String path) {
        String result = null;
        if (path.endsWith(Constants.SUFFIX_ZIP_FILE)) {
            result = path;
        } else {
            result = path + File.separator + DaisyBookUtil.getOpfFileName(path);
        }
        return result;
    }

    /**
     * Gets the parts from section.
     * 
     * @param section the current section
     * @param path the path of the book
     * @param isFormat202 true if books' format is Daisy 2.02.
     * @return the parts from section
     * @throws PrivateException
     */
    public Part[] getPartsFromSection(Section section, String path, boolean isFormat202)
            throws PrivateException {
        Part[] parts = null;
        DaisySection currentSection = null;
        BookContext bookContext = null;
        try {
            bookContext = getBookContext(path);
            currentSection = new DaisySection.Builder().setHref(section.getHref())
                    .setContext(bookContext).build();
            parts = currentSection.getParts(isFormat202);
            return parts;
        } catch (PrivateException e) {
            PrivateException ex = new PrivateException(e, mContext, path);
            throw ex;
        }
    }

    private String getOpfFileName(String path) {
        String result = null;
        if (path.endsWith(Constants.SUFFIX_ZIP_FILE)) {
            result = DaisyBookUtil.getOpfFileNameInZipFolder(path);
        } else {
            result = DaisyBookUtil.getOpfFileName(path);
        }
        return result;
    }

    /**
     * Creates the current information.
     * 
     * @param audioName the name of audio
     * @param activity the name of activity which call this function
     * @param section the current section of book
     * @param time the current time of audio
     * @param isPlaying the status of audio
     * @return the current information
     */
    public CurrentInformation createCurrentInformation(String audioName, String activity,
            int section, int time, boolean isPlaying) {
        // create a current information
        CurrentInformation current = new CurrentInformation();
        try {
            current.setAudioName(audioName);
            current.setPath(mPath);
            current.setSection(section);
            current.setTime(time);
            current.setPlaying(isPlaying);
            current.setSentence(1);
            current.setActivity(activity);
            current.setFirstNext(false);
            current.setFirstPrevious(true);
            current.setAtTheEnd(false);
            current.setId(UUID.randomUUID().toString());
        } catch (Exception e) {
            PrivateException ex = new PrivateException(e, mContext);
            ex.writeLogException();
        }
        return current;
    }

    /**
     * Update current information.
     * 
     * @param current the current information
     * @param audioName the name of audio
     * @param activity the name of activity which call this function
     * @param section the current section of book
     * @param sentence the current sentence of book
     * @param time the current time of audio
     * @param isPlaying the status of audio
     * @return the current information updated
     */
    public CurrentInformation updateCurrentInformation(CurrentInformation current,
            String audioName, String activity, int section, int sentence, int time,
            boolean isPlaying) {
        CurrentInformation newCurrentInfomation = current;
        try {
            newCurrentInfomation.setAudioName(audioName);
            newCurrentInfomation.setTime(time);
            newCurrentInfomation.setSection(section);
            newCurrentInfomation.setSentence(sentence);
            newCurrentInfomation.setActivity(activity);
            newCurrentInfomation.setPlaying(isPlaying);
        } catch (Exception e) {
            PrivateException ex = new PrivateException(e, mContext);
            ex.writeLogException();
        }
        return newCurrentInfomation;
    }
}
