import junit.framework.TestCase;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

import de.dala.simplenews.common.Feed;
import de.dala.simplenews.parser.OpmlReader;
import de.dala.simplenews.parser.OpmlWriter;

/**
 * Created by Daniel on 05.03.14.
 */
public class OpmlImportExportTest extends TestCase{

    public void setUp() throws Exception {
        super.setUp();
    }

    public void testImport() throws IOException, XmlPullParserException {
        List<Feed> elements = OpmlReader.importFile(new StringReader(""));
    }

    public void testExport() throws IOException {
        Writer writer = new StringWriter();
        OpmlWriter.writeDocument(null,writer);
        String result = writer.toString();
        assertNull(result);
    }
}
