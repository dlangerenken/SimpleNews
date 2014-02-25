package de.dala.simplenews.ui;

import android.content.ActivityNotFoundException;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.astuetz.PagerSlidingTabStrip;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.List;

import de.dala.simplenews.R;
import de.dala.simplenews.common.Category;
import de.dala.simplenews.common.News;
import de.dala.simplenews.database.DatabaseHandler;
import de.dala.simplenews.database.IDatabaseHandler;
import de.dala.simplenews.parser.XmlParser;
import de.dala.simplenews.utilities.PrefUtilities;
import de.dala.toasty.Toasty;
import de.keyboardsurfer.android.widget.crouton.Configuration;
import de.keyboardsurfer.android.widget.crouton.Crouton;

/**
 * Created by Daniel on 20.02.14.
 */
public class NewsOverViewFragment extends SherlockFragment implements ViewPager.OnPageChangeListener {
    private static String TAG = "NewsOverViewFragment";

    private IDatabaseHandler databaseHandler;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private PagerSlidingTabStrip tabs;
    private ViewPager pager;
    private MyPagerAdapter adapter;

    private View progressView;
    private Drawable oldBackground = null;
    private int currentColor = 0xFF666666;
    private List<Category> categories;
    private RelativeLayout bottomView;
    private Crouton crouton;
    private int loadingNews = -1;
    private TextView progressText;
    private MainActivity mainActivity;
    private int entryType;

    public NewsOverViewFragment(int entryType) {
        this.entryType = entryType;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity() instanceof MainActivity){
            this.mainActivity = (MainActivity)getActivity();
        }else{
            throw new ActivityNotFoundException("MainActivity not found");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.news_overview, container, false);
        databaseHandler = DatabaseHandler.getInstance();
        if (!PrefUtilities.getInstance().xmlIsAlreadyLoaded()) {
            loadXml();
        }
        mainActivity.getSupportActionBar().setTitle(getString(R.string.simple_news_title));
        mainActivity.getSupportActionBar().setHomeButtonEnabled(true);
        categories = databaseHandler.getCategories(null, null, true);

        if (categories != null && !categories.isEmpty()){
            pager = (ViewPager) rootView.findViewById(R.id.pager);
            adapter = new MyPagerAdapter(getChildFragmentManager());
            pager.setAdapter(adapter);

            tabs = (PagerSlidingTabStrip) rootView.findViewById(R.id.tabs);
            tabs.setViewPager(pager);
            tabs.setOnPageChangeListener(this);

            final int pageMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources()
                    .getDisplayMetrics());
            pager.setPageMargin(pageMargin);
            bottomView = (RelativeLayout) rootView.findViewById(R.id.bottom_view);
            createProgressView();

            onPageSelected(0);
        }
        return rootView;
    }

    private void changeColor(int newColor) {
        tabs.setIndicatorColor(newColor);

        Drawable colorDrawable = new ColorDrawable(newColor);
        Drawable bottomDrawable = getResources().getDrawable(R.drawable.actionbar_bottom);
        LayerDrawable ld = new LayerDrawable(new Drawable[]{colorDrawable, bottomDrawable});

        if (oldBackground == null) {
            getSherlockActivity().getSupportActionBar().setBackgroundDrawable(ld);
        } else {
            //getSupportActionBar().setBackgroundDrawable(ld); //BUG otherwise
            TransitionDrawable td = new TransitionDrawable(new Drawable[] { oldBackground, ld });
            getSherlockActivity().getSupportActionBar().setBackgroundDrawable(td);
            td.startTransition(400);
        }
        mainActivity.changeDrawerColor(ld, newColor);

        progressView.setBackgroundColor(newColor);
        oldBackground = ld;
        currentColor = newColor;

        // http://stackoverflow.com/questions/11002691/actionbar-setbackgrounddrawable-nulling-background-from-thread-handler
        getSherlockActivity().getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSherlockActivity().getSupportActionBar().setDisplayShowTitleEnabled(true);
    }

    @Override
    public void onPageScrolled(int i, float v, int i2) {
    }

    @Override
    public void onPageSelected(int i) {
        if (categories != null && categories.size() > i){
            changeColor(categories.get(i).getColor());
        }
    }

    @Override
    public void onPageScrollStateChanged(int i) {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);
    }

    public void updateNews() {
        if (crouton != null) {
            crouton.cancel();
        }
        Configuration config = new Configuration.Builder().setOutAnimation(R.anim.abc_slide_out_bottom).setInAnimation(R.anim.abc_slide_in_bottom).setDuration(Configuration.DURATION_INFINITE).build();
        crouton = Crouton.make( getSherlockActivity(), createProgressView(), bottomView);
        crouton.setConfiguration(config);
        crouton.show();
    }

    public void showLoadingNews() {
        loadingNews++;
        if (loadingNews == 0) {
            updateNews();
        }
    }

    public void cancelLoadingNews() {
        loadingNews--;
        if (loadingNews >= -1) {
            crouton.cancel();
            loadingNews = -1;
        }
    }

    public void updateNews(String text, long categoryId) {
        if (progressText != null){
            progressText.setText(text);
        }
    }

    public static Fragment getInstance(int entryType) {
        return new NewsOverViewFragment(entryType);
    }


    public class MyPagerAdapter extends FragmentPagerAdapter {

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return categories.get(position).getName();
        }

        @Override
        public int getCount() {
            return categories.size();
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = ExpandableNewsFragment.newInstance(categories.get(position), entryType);
            return fragment;
        }
    }

    private void loadXml() {
        try {
            News news = new XmlParser(getSherlockActivity()).readDefaultNewsFile();
            for (Category category : news.getCategories()) {
                if (category != null) {
                    databaseHandler.addCategory(category, false, false);
                }
            }
            PrefUtilities.getInstance().saveLoading(true);
        } catch (XmlPullParserException e) {
            Toasty.LOGE(TAG, "Error in adding xml to Database");
            e.printStackTrace();
        } catch (IOException io) {
            Toasty.LOGE(TAG, "Error in adding xml to Database");
            io.printStackTrace();
        }
    }

    private View createProgressView() {
        progressView = mainActivity.getLayoutInflater().inflate(R.layout.progress_layout, null);
        progressView.setBackgroundColor(currentColor);
        progressText = (TextView) progressView.findViewById(R.id.progress_text);
        progressText.setText(getString(R.string.update_news));
        return progressView;
    }

}
