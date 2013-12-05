package org.androiddaisyreader.test.base;

import java.util.Date;
import java.util.List;

import org.androiddaisyreader.apps.PrivateException;
import org.androiddaisyreader.base.DaisyEbookReaderBaseMode;
import org.androiddaisyreader.model.Audio;
import org.androiddaisyreader.model.BookContext;
import org.androiddaisyreader.model.CurrentInformation;
import org.androiddaisyreader.model.DaisyBook;
import org.androiddaisyreader.model.Navigable;
import org.androiddaisyreader.model.Navigator;
import org.androiddaisyreader.model.Part;
import org.androiddaisyreader.model.Section;
import org.androiddaisyreader.model.Snippet;

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
        assertEquals(Environment.getExternalStorageDirectory().getPath()
                + "/daisybook/testbook/minidaisyaudiobook", bookContext.getBaseUri());
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

    public void testPartsAreGottenSuccessfully() throws PrivateException {
        DaisyEbookReaderBaseMode base = getBaseMode(PATH_EBOOK_202, getContext());
        Part[] parts = base.getPartsFromSection(getSection(), PATH_EBOOK_202, true);
        List<Audio> audioElements = parts[0].getAudioElements();
        assertNotNull("Parts are null", parts);
        assertEquals("Clip begin has to be 0", 0, audioElements.get(0).getClipBegin());
        assertEquals("Clip end has to be 5100", 5100, audioElements.get(0).getClipEnd());
        List<Snippet> snippetElements = parts[0].getSnippets();
        assertEquals("Clip end has to be 5 Numbers", "5 Numbers", snippetElements.get(0).getText()
                .toString());
    }

    public void testPartsThrowPrivateExceptionWhenSectionIsNull() {
        boolean thrown = false;
        try {
            DaisyEbookReaderBaseMode base = getBaseMode(PATH_EBOOK_202, getContext());
            base.getPartsFromSection(null, PATH_EBOOK_202, true);
            fail("Test case did not throw private exception");
        } catch (PrivateException e) {
            assertEquals("Section has to be null", e.getMessage(), "Section was null");
            thrown = true;
        }
        assertTrue(thrown);
    }

    public void testPartsThrowPrivateExceptionWhenPathIsNull() {
        boolean thrown = false;
        try {
            DaisyEbookReaderBaseMode base = getBaseMode(PATH_EBOOK_202, getContext());
            base.getPartsFromSection(getSection(), null, true);
            fail("Test case did not throw private exception");
        } catch (PrivateException e) {
            assertEquals("The path has to be null", e.getMessage(), null);
            thrown = true;
        }
        assertTrue(thrown);
    }

    public void testPartsThrowPrivateExceptionWhenPathIsWrong() {
        boolean thrown = false;
        try {
            DaisyEbookReaderBaseMode base = getBaseMode(PATH_EBOOK_202, getContext());
            base.getPartsFromSection(getSection(), "wrong_path", true);
            fail("Test case did not throw private exception");
        } catch (PrivateException e) {
            assertEquals("The path has to be wrong", e.getMessage(), null);
            thrown = true;
        }
        assertTrue(thrown);
    }

    private Section getSection() throws PrivateException {
        DaisyEbookReaderBaseMode base = getBaseMode(PATH_EBOOK_202, getContext());
        DaisyBook book = base.openBook202();
        Navigator navigator = new Navigator(book);
        Navigable n = navigator.next();
        return (Section) n;
    }

}
