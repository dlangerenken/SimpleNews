import android.test.InstrumentationTestCase;

import org.xmlpull.v1.XmlPullParserException;
import java.io.IOException;
import java.io.InputStream;

import de.dala.simplenews.test.R;

/**
 * Created by Daniel on 05.03.14.
 */
public class OpmlImportExportTest extends InstrumentationTestCase {

    public void setUp() throws Exception {
        super.setUp();
    }

    public void testImport() throws IOException, XmlPullParserException {
        InputStream raw = getInstrumentation().getContext().getResources().openRawResource(R.raw.failing_feedly_opml);
       // List<Feed> elements = OpmlReader.importFile(new InputStreamReader(raw));
        //Assert.assertNotNull(elements);

    }
}
