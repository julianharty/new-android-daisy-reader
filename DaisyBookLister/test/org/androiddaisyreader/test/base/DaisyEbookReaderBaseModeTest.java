package org.androiddaisyreader.test.base;

import java.util.Date;
import java.util.Locale;

import org.androiddaisyreader.apps.PrivateException;
import org.androiddaisyreader.base.DaisyEbookReaderBaseMode;
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

    public void testOpenBook202Successful() {
        DaisyBook mBook202;
        try {
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
        } catch (PrivateException e) {
            fail("test case fail");
        }
    }

    public void testOpenBook202WithNullPath() {
        try {
            DaisyEbookReaderBaseMode base = getBaseMode(null, getContext());
            base.openBook202();
            fail("Test case did not throw private exception");
        } catch (PrivateException e) {
            assertEquals("Message must be null", e.getMessage(), null);
        }
    }

    public void testOpenBook202WithWrongPath() {
        try {
            String wrongPath = "wrong_path";
            DaisyEbookReaderBaseMode base = getBaseMode(wrongPath, getContext());
            base.openBook202();
            fail("Test case did not throw private exception");
        } catch (PrivateException e) {
            assertEquals("Message must be null", e.getMessage(), null);
        }
    }

    public void testCreateCurrentInformationSuccessful() {
        DaisyEbookReaderBaseMode base = getBaseMode(PATH_EBOOK_202, getContext());
        String audioName = "audioname";
        String activity = "activity";
        int section = 1;
        int time = 1;
        boolean isPlaying = true;
        CurrentInformation current = base.createCurrentInformation(audioName, activity, section,
                time, isPlaying);
        assertNotNull(current);
        assertEquals("Audio name must be audioname", current.getAudioName(), audioName);
        assertEquals("Current must be activity", current.getActivity(), activity);
        assertEquals("Section must be 1", current.getSection(), section);
        assertEquals("Time must be 1", current.getTime(), time);
        assertEquals("IsPlaying must be true", current.getPlaying(), isPlaying);
    }

    public void testCreateCurrentInformationWithNullAudioName() {
        DaisyEbookReaderBaseMode base = getBaseMode(PATH_EBOOK_202, getContext());
        String audioName = null;
        String activity = "activity";
        int section = 1;
        int time = 1;
        boolean isPlaying = true;
        CurrentInformation current = base.createCurrentInformation(audioName, activity, section,
                time, isPlaying);
        assertNotNull(current);
        assertEquals("Audio name must be audioname", current.getAudioName(), audioName);
        assertEquals("Current must be activity", current.getActivity(), activity);
        assertEquals("Section must be 1", current.getSection(), section);
        assertEquals("Time must be 1", current.getTime(), time);
        assertEquals("IsPlaying must be true", current.getPlaying(), isPlaying);
    }

    private DaisyEbookReaderBaseMode getBaseMode(String path, Context context) {
        DaisyEbookReaderBaseMode base;
        base = new DaisyEbookReaderBaseMode(path, context);
        return base;
    }
}
