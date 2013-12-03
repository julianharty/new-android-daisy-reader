package org.androiddaisyreader.test.base;

import java.util.Date;

import org.androiddaisyreader.apps.PrivateException;
import org.androiddaisyreader.base.DaisyEbookReaderBaseMode;
import org.androiddaisyreader.model.BookContext;
import org.androiddaisyreader.model.CurrentInformation;
import org.androiddaisyreader.model.DaisyBook;

import android.content.Context;
import android.os.Environment;
import android.test.AndroidTestCase;

public class DaisyEbookReaderBaseModeTest extends AndroidTestCase {

    private static final String PATH_EBOOK_202 = Environment.getExternalStorageDirectory()
            .getPath() + "/daisybook/testbook/minidaisyaudiobook/ncc.html";

    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testOpenBook202Successful() throws PrivateException {
        DaisyBook mBook202;
        DaisyEbookReaderBaseMode base = getBaseMode(PATH_EBOOK_202, getContext());
        mBook202 = base.openBook202();
        // verify
        assertNotNull(mBook202);
        assertEquals("Title must be A mini DAISY book for testing", mBook202.getTitle(),
                "A mini DAISY book for testing");
        assertEquals("Author must be Julian Harty", mBook202.getAuthor(), "Julian Harty");
        assertEquals("Publisher must be Julian Harty", mBook202.getPublisher(), "Julian Harty");
        Date date = mBook202.getDate();
        String sDate = "";
        if (date != null) {
            sDate = String.format(("%tB %te, %tY %n"), date, date, date);
        }
        assertEquals("Date must be August 28, 2011", sDate.trim(), "August 28, 2011");
    }

    public void testBook202ThrowsPrivateExceptionWhenPathIsNull() {
        boolean thrown = false;
        try {
            DaisyEbookReaderBaseMode base = getBaseMode(null, getContext());
            base.openBook202();
            fail("Test case did not throw private exception");
        } catch (PrivateException e) {
            assertEquals("Book has to be null", e.getMessage(), null);
            thrown = true;
        }
        assertTrue(thrown);
    }

    public void testBook202ThrowsPrivateExceptionWhenPathIsWrong() {
        boolean thrown = false;
        try {
            String wrongPath = "wrong_path";
            DaisyEbookReaderBaseMode base = getBaseMode(wrongPath, getContext());
            base.openBook202();
            fail("Test case did not throw private exception");
        } catch (PrivateException e) {
            assertEquals("Book has to be null", e.getMessage(), null);
            thrown = true;
        }
        assertTrue(thrown);
    }

    public void testCurrentInformationIsCreatedSuccessfully() {
        DaisyEbookReaderBaseMode base = getBaseMode(PATH_EBOOK_202, getContext());
        String audioName = "audioname";
        String activity = "activity";
        int section = 1;
        int time = 1;
        boolean isPlaying = true;
        CurrentInformation current = base.createCurrentInformation(audioName, activity, section,
                time, isPlaying);
        assertCurrentInformationValues(current, audioName, activity, section, time, isPlaying);
    }

    public void testCurrentInformationIsCreatedWithNullAudioName() {
        DaisyEbookReaderBaseMode base = getBaseMode(PATH_EBOOK_202, getContext());
        String audioName = null;
        String activity = "activity";
        int section = 1;
        int time = 1;
        boolean isPlaying = true;
        CurrentInformation current = base.createCurrentInformation(audioName, activity, section,
                time, isPlaying);
        assertCurrentInformationValues(current, audioName, activity, section, time, isPlaying);
    }

    public void testCurrentInformationIsCreatedWithNullActivity() {
        DaisyEbookReaderBaseMode base = getBaseMode(PATH_EBOOK_202, getContext());
        String audioName = "audioname";
        String activity = null;
        int section = 1;
        // The unit of measure for the time is milliseconds
        int time = 1;
        boolean isPlaying = true;
        CurrentInformation current = base.createCurrentInformation(audioName, activity, section,
                time, isPlaying);
        assertCurrentInformationValues(current, audioName, activity, section, time, isPlaying);
    }

    public void testCurrentInformationIsCreatedWithFirstSection() {
        DaisyEbookReaderBaseMode base = getBaseMode(PATH_EBOOK_202, getContext());
        String audioName = "audioname";
        String activity = "activity";
        int section = 0;
        int time = 1;
        boolean isPlaying = true;
        CurrentInformation current = base.createCurrentInformation(audioName, activity, section,
                time, isPlaying);
        assertCurrentInformationValues(current, audioName, activity, section, time, isPlaying);
    }

    public void testCurrentInformationIsCreatedWithZeroMillisecond() {
        DaisyEbookReaderBaseMode base = getBaseMode(PATH_EBOOK_202, getContext());
        String audioName = "audioname";
        String activity = "activity";
        int section = 1;
        int time = 0;
        boolean isPlaying = true;
        CurrentInformation current = base.createCurrentInformation(audioName, activity, section,
                time, isPlaying);
        assertCurrentInformationValues(current, audioName, activity, section, time, isPlaying);
    }

    private DaisyEbookReaderBaseMode getBaseMode(String path, Context context) {
        DaisyEbookReaderBaseMode base;
        base = new DaisyEbookReaderBaseMode(path, context);
        return base;
    }

    private void assertCurrentInformationValues(CurrentInformation current, String audioName,
            String activity, int section, int time, boolean isPlaying) {
        assertNotNull(current);
        assertEquals(String.format("Audio name must be %s", audioName), current.getAudioName(),
                audioName);
        assertEquals(String.format("Current must be %s", activity), current.getActivity(), activity);
        assertEquals(String.format("Section must be %s", section), current.getSection(), section);
        assertEquals(String.format("Time must be %s", time), current.getTime(), time);
        assertEquals(String.format("IsPlaying must be %s", isPlaying), current.getPlaying(),
                isPlaying);
    }

    public void testBookContextIsGottenSuccessfully() throws PrivateException {
        DaisyEbookReaderBaseMode base = getBaseMode(PATH_EBOOK_202, getContext());
        BookContext bookContext = base.getBookContext(PATH_EBOOK_202);
        assertNotNull("Book context is null", bookContext);
    }

    public void testBookContextThrowsPrivateExceptionWhenPathIsNull() {
        boolean thrown = false;
        try {
            DaisyEbookReaderBaseMode base = getBaseMode(null, getContext());
            base.getBookContext(null);
            fail("Test case did not throw private exception");
        } catch (PrivateException e) {
            assertEquals("Book Context has to be null", e.getMessage(), null);
            thrown = true;
        }
        assertTrue(thrown);
    }

}
