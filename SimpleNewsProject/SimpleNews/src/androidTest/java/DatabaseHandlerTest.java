
import android.test.InstrumentationTestCase;

import junit.framework.Assert;

import java.util.List;

import de.dala.simplenews.parser.XmlParser;
import de.dala.simplenews.test.R;
import de.dala.simplenews.common.Category;
import de.dala.simplenews.database.DatabaseHandler;
import de.dala.simplenews.database.IDatabaseHandler;
import de.dala.simplenews.utilities.PrefUtilities;

/**
 * Created by Daniel on 01.08.2014.
 */
public class DatabaseHandlerTest extends InstrumentationTestCase {
    private IDatabaseHandler db;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        DatabaseHandler.init(getInstrumentation().getContext(), null);
        XmlParser.Init(getInstrumentation().getContext());
        PrefUtilities.init(getInstrumentation().getContext());
        db = DatabaseHandler.getInstance();
    }

    public void testImportXml(){
        db.removeAllCategories();
        Assert.assertEquals((long)0, db.getCategories(null, null, null).size());
        db.loadXmlIntoDatabase(R.raw.categories_test);
        List<Category> categories = db.getCategories(null, null, null);
        Assert.assertEquals((long)8, categories.size());
        Assert.assertEquals((long) 25, db.getFeeds(null, null).size());
        Assert.assertEquals( 3, db.getFeeds(categories.get(0).getId(), null).size());
        db.removeAllCategories();
        Assert.assertEquals((long) 0, db.getCategories(null, null, null).size());
        Assert.assertEquals((long) 0, db.getFeeds(null, null).size());
    }

    public void testAddAndRemoveCategory(){
        Assert.assertEquals(0, db.getCategories(null, null, null).size());
        long returnedId = db.addCategory(getCategory(), null, null);
        Category category = db.getCategory(returnedId, null, null);
        Assert.assertNotNull(category);
        List<Category> categories = db.getCategories(null, null, null);
        Assert.assertEquals(1, categories.size());
        long addedId = categories.get(0).getId();
        Assert.assertEquals(returnedId, addedId);
        db.removeCategory(addedId, null, null);
        Assert.assertEquals(0, db.getCategories(null, null, null).size());

        long id = db.addCategory(getCategory(), null, null);
        long id2 = db.addCategory(getCategory(), null, null);
        Assert.assertNotSame(id, id2);

    }

    private Category getCategory(){
        Category cat = new Category();
        cat.setName("Test");
        cat.setVisible(true);
        cat.setColorId(1337);
        return cat;
    }
}
