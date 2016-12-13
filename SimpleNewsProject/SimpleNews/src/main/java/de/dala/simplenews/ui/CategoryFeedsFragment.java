package de.dala.simplenews.ui;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.rometools.opml.feed.opml.Opml;
import com.rometools.opml.io.impl.OPML20Generator;
import com.rometools.rome.io.FeedException;

import org.jdom2.output.XMLOutputter;

import java.util.ArrayList;
import java.util.List;

import de.dala.simplenews.R;
import de.dala.simplenews.common.Category;
import de.dala.simplenews.common.Feed;
import de.dala.simplenews.utilities.OpmlConverter;
import de.dala.simplenews.utilities.PrefUtilities;
import de.dala.simplenews.utilities.UpdatingFeedTask;
import de.dala.simplenews.recycler.FeedRecyclerAdapter;

public class CategoryFeedsFragment extends BaseFragment implements FeedRecyclerAdapter.CategoryFeedsListener {

    private static final String CATEGORY_KEY = "mCategory";
    private RecyclerView feedListView;
    private FeedRecyclerAdapter mRecyclerAdapter;
    private Category mCategory;
    private UpdatingFeedTask feedTask;


    public CategoryFeedsFragment() {
    }

    public static CategoryFeedsFragment newInstance(Category category) {
        CategoryFeedsFragment fragment = new CategoryFeedsFragment();
        Bundle b = new Bundle();
        b.putParcelable(CATEGORY_KEY, category);
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.feed_selection, container, false);
        feedListView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        feedListView.setLayoutManager(new LinearLayoutManager(inflater.getContext()));
        FloatingActionButton button = (FloatingActionButton) rootView.findViewById(R.id.add_button);
        button.setBackgroundTintList(new ColorStateList(new int[][]{new int[]{0}}, new int[]{PrefUtilities.getInstance().getCurrentColor()}));
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createFeedClicked();
            }
        });

        initAdapter();
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            ActionBar ab = activity.getSupportActionBar();
            if (ab != null) {
                ab.setDisplayHomeAsUpEnabled(true);
            }
        }
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("ActionMode")) {
                Bundle actionModeBundle = savedInstanceState.getBundle("ActionMode");
                mRecyclerAdapter.restoreSelectionStates(actionModeBundle);
            }
        }

        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        this.mCategory = getArguments().getParcelable(CATEGORY_KEY);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.feed_selection_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.share_feeds:
                shareCategory();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initAdapter() {
        mRecyclerAdapter = new FeedRecyclerAdapter(getActivity(), mCategory, this);
        feedListView.setAdapter(mRecyclerAdapter);
    }

    private void createFeedClicked() {
        final MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                .title(R.string.new_feed_url_header)
                .contentGravity(GravityEnum.CENTER)
                .customView(R.layout.text_input, false)
                .positiveText(R.string.ok)
                .negativeText(R.string.cancel)
                .autoDismiss(false).build();
        final TextInputLayout layout = (TextInputLayout) dialog.getCustomView();
        assert layout != null;
        final EditText editText = (EditText) layout.findViewById(R.id.edit_text);
        dialog.getActionButton(DialogAction.NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });
        dialog.getActionButton(DialogAction.POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final View positive = dialog.getActionButton(DialogAction.POSITIVE);
                final View negative = dialog.getActionButton(DialogAction.NEGATIVE);
                feedTask = new UpdatingFeedTask(mCategory, new UpdatingFeedTask.UpdatingFeedListener() {
                    @Override
                    public void success(Feed feed) {
                        dialog.dismiss();
                        positive.setEnabled(true);
                        negative.setEnabled(true);
                        mRecyclerAdapter.add(feed);
                    }

                    @Override
                    public void loading() {
                        positive.setEnabled(false);
                        negative.setEnabled(false);
                        layout.setError(null);
                    }

                    @Override
                    public void fail() {
                        positive.setEnabled(true);
                        negative.setEnabled(true);
                        layout.setError(getActivity().getString(R.string.no_feeds_found));
                    }
                }, null);
                feedTask.execute(editText.getText().toString());
            }
        });
        dialog.show();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (feedTask != null) {
            feedTask.cancel(true);
        }
    }


    @Override
    public void onStop() {
        super.onStop();
        if (feedTask != null) {
            feedTask.cancel(true);
        }
    }

    @Override
    public void onLongClick(Feed feed) {
        final List<Feed> feeds = new ArrayList<>();
        feeds.add(feed);
        new MaterialDialog.Builder(getActivity())
                .items(R.array.feed_options)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog materialDialog, View view, int which, CharSequence charSequence) {
                        switch (which) {
                            case 0: //Share
                                shareFeeds(feeds);
                                break;
                            case 1: // Delete
                                mRecyclerAdapter.removeFeeds(feeds);
                                break;
                        }
                    }
                }).show();
    }

    private void shareCategory() {
        List<Category> categories = new ArrayList<>();
        categories.add(mCategory);
        Opml opml = OpmlConverter.convertCategoriesToOpml(categories);
        shareOpml(opml);

    }

    private void shareOpml(Opml opml) {
        String finalMessage = null;
        try {
            finalMessage = new XMLOutputter().outputString(new OPML20Generator().generate(opml));
        } catch (FeedException e) {
            e.printStackTrace();
        }
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/xml");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                finalMessage);
        startActivity(shareIntent);
    }

    private void shareFeeds(List<Feed> feeds) {
        Opml opml = OpmlConverter.convertFeedsToOpml("SimpleNews - OPML", feeds);
        shareOpml(opml);
    }


}
