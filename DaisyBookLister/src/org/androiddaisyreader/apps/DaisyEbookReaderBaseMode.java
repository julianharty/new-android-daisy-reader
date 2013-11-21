package org.androiddaisyreader.apps;

import java.io.File;
import java.io.InputStream;

import org.androiddaisyreader.model.BookContext;
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
    // private BookContext mBookContext;
    private String mPath;
    // private DaisyBook mBook;
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
            BookContext bookContext;
            String path = getPathExactlyDaisy30(mPath);
            String opfName = getOpfFileName(mPath);
            bookContext = getBookContext(path);
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
}
