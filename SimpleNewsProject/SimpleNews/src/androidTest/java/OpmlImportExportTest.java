import android.test.InstrumentationTestCase;

import junit.framework.Assert;

import org.xmlpull.v1.XmlPullParserException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

import de.dala.simplenews.test.R;
import de.dala.simplenews.common.Feed;

/**
 * Created by Daniel on 05.03.14.
 */
public class OpmlImportExportTest extends InstrumentationTestCase {

    public void setUp() throws Exception {
        super.setUp();
    }

    public void testExport() throws IOException {
        Writer writer = new StringWriter();
        OpmlWriter.writeDocument(null,writer);
        String result = writer.toString();
        assertNull(result);
    }

    public void testImport() throws IOException, XmlPullParserException {
        InputStream raw = getInstrumentation().getContext().getResources().openRawResource(R.raw.testopml);
        List<Feed> elements = OpmlReader.importFile(new InputStreamReader(raw));
        Assert.assertNotNull(elements);

        raw = getInstrumentation().getContext().getResources().openRawResource(R.raw.testopml2);
        elements = OpmlReader.importFile(new InputStreamReader(raw));
        Assert.assertNotNull(elements);
    }
}
