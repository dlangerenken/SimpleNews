package de.dala.simplenews.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.text.InputType;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.rometools.rome.feed.opml.Opml;
import com.rometools.rome.io.impl.OPML20Generator;
import com.rometools.rome.io.FeedException;

import org.jdom2.output.XMLOutputter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.dala.simplenews.R;
import de.dala.simplenews.common.Category;
import de.dala.simplenews.database.DatabaseHandler;
import de.dala.simplenews.utilities.ColorChooserDialog;
import de.dala.simplenews.utilities.ColorManager;
import de.dala.simplenews.utilities.EmptyObservableRecyclerView;
import de.dala.simplenews.utilities.OpmlConverter;
import de.dala.simplenews.utilities.PrefUtilities;
import recycler.CategoryRecyclerAdapter;
import recycler.ChoiceModeRecyclerAdapter;

public class CategorySelectionFragment extends BaseFragment implements CategoryRecyclerAdapter.OnCategoryClicked, ChoiceModeRecyclerAdapter.ChoiceModeListener {

    private static final String CATEGORIES_KEY = "categories";
    private static final String RSS_PATH_KEY = "path";
    private ActionMode mActionMode;
    private List<Category> categories;
    private EmptyObservableRecyclerView recyclerView;
    private CategoryRecyclerAdapter adapter;
    private String rssPath;
    private ShareActionProvider shareActionProvider;
    private OnCategorySelectionFragmentAction mListener;

    public interface OnCategorySelectionFragmentAction {
        void onMoreClicked(Category category);

        void onRSSSavedClick(Category category, String path);

        void onShowClicked(Category category);

        void onRestore();
    }

    public CategorySelectionFragment() {
    }

    public static CategorySelectionFragment newInstance(ArrayList<Category> categories, String path) {
        CategorySelectionFragment fragment = new CategorySelectionFragment();
        Bundle b = new Bundle();
        b.putParcelableArrayList(CATEGORIES_KEY, categories);
        b.putString(RSS_PATH_KEY, path);
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mListener = (OnCategorySelectionFragmentAction) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnCategorySelectionFragmentAction");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.category_selection, container, false);
        if (rssPath != null) {
            TextView topTextView = (TextView) rootView.findViewById(R.id.topTextView);
            topTextView.setText(getActivity().getString(R.string.category_add));
            topTextView.setVisibility(View.VISIBLE);
        }
        recyclerView = (EmptyObservableRecyclerView) rootView.findViewById(R.id.listView);
        recyclerView.setLayoutManager(new LinearLayoutManager(inflater.getContext()));
        initAdapter();
        return rootView;
    }


    @Override
    public void startSelectionMode() {
        mActionMode = getActivity().startActionMode(new ActionModeCallBack());
    }

    @Override
    public void updateSelectionMode(int numberOfElements) {
        if (mActionMode != null) {
            mActionMode.setTitle(String.valueOf(numberOfElements));
        }
        if (shareActionProvider != null) {
            shareActionProvider.setShareIntent(getShareIntent());
        }
    }

    @Override
    public void finishSelectionMode() {
        if (mActionMode != null) {
            mActionMode.finish();
        }
        shareActionProvider = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        this.categories = getArguments().getParcelableArrayList(CATEGORIES_KEY);
        this.rssPath = getArguments().getString(RSS_PATH_KEY);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.category_selection_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_category:
                createCategoryClicked();
                return true;
            case R.id.restore_categories:
                new MaterialDialog.Builder(getActivity())
                        .title(R.string.restore_categories_title)
                        .content(R.string.restore_categories_message)
                        .positiveText(R.string.restore_categories_yes)
                        .negativeText(R.string.restore_categories_no)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                DatabaseHandler.getInstance().removeAllCategories();
                                PrefUtilities.getInstance().saveLoading(false);
                                onRestore();
                            }
                        }).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initAdapter() {
        Collections.sort(categories);
        adapter = new CategoryRecyclerAdapter(getActivity(), categories, rssPath, this, this);
        recyclerView.setAdapter(adapter);
    }

    private void createCategoryClicked() {
        new MaterialDialog.Builder(getActivity())
                .title(R.string.create_category_1_2)
                .positiveText(R.string.submit)
                .negativeText(R.string.cancel)
                .content(R.string.name_of_category)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input("", "", new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog materialDialog, CharSequence charSequence) {
                        String categoryName = charSequence != null ? charSequence.toString() : "";
                        selectColor(categoryName);
                    }
                }).show();
    }

    @Override
    public void editClicked(final Category category) {
        new MaterialDialog.Builder(getActivity())
                .title(R.string.category)
                .positiveText(R.string.rename)
                .negativeText(R.string.cancel)
                .content(R.string.change_category_name)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input("", category.getName(), new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog materialDialog, CharSequence charSequence) {
                        String categoryName = charSequence != null ? charSequence.toString() : "";
                        category.setName(categoryName);
                        adapter.notifyDataSetChanged();
                        DatabaseHandler.getInstance().updateCategory(category);
                        Toast.makeText(getActivity(), String.format(getActivity().getString(R.string.new_name), categoryName), Toast.LENGTH_SHORT).show();
                    }
                }).show();
    }


    @Override
    public void onMoreClicked(Category category) {
        mListener.onMoreClicked(category);
    }

    @Override
    public void onRSSSavedClick(Category category, String rssPath) {
        mListener.onRSSSavedClick(category, rssPath);
    }

    @Override
    public void onShowClicked(Category category) {
        mListener.onShowClicked(category);
    }

    @Override
    public void onRestore() {
        mListener.onRestore();
    }


    @Override
    public void onColorClicked(final Category category) {
        new ColorChooserDialog().show(getFragmentManager(), category.getPrimaryColor(), new ColorChooserDialog.Callback() {
            @Override
            public void onColorSelection(int index, int color, int darker) {
                category.setColorId(ColorManager.getInstance().getIdByColor(color));
                DatabaseHandler.getInstance().updateCategory(category);
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void selectColor(final String categoryName) {
        new ColorChooserDialog().show(getFragmentManager(), -1, new ColorChooserDialog.Callback() {

            @Override
            public void onColorSelection(int index, int color, int darker) {
                Category newCategory = new Category();
                newCategory.setName(categoryName);
                newCategory.setColorId(ColorManager.getInstance().getIdByColor(color));

                DatabaseHandler.getInstance().addCategory(newCategory, true, true);
                adapter.add(newCategory);
            }
        });
    }

    private void removeSelectedCategories(List<Category> selectedCategories) {
        for (Category category : selectedCategories) {
            adapter.remove(category);
            DatabaseHandler.getInstance().removeCategory(category.getId(), false, false);
        }
    }

    private Intent getShareIntent() {
        List<Category> categories = adapter.getItems();
        Opml opml = OpmlConverter.convertCategoriesToOpml(categories);
        String finalMessage = "";
        try {
            finalMessage = new XMLOutputter().outputString(new OPML20Generator().generate(opml));
        } catch (FeedException e) {
            e.printStackTrace();
        }
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/xml");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                finalMessage);
        return shareIntent;
    }

    private class ActionModeCallBack implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.contextual_category_selection_menu, menu);
            MenuItem item = menu.findItem(R.id.menu_item_share);
            if (item != null) {
                shareActionProvider = (ShareActionProvider) item.getActionProvider();
                if (shareActionProvider != null) {
                    String shareHistoryFileName = ShareActionProvider.DEFAULT_SHARE_HISTORY_FILE_NAME;
                    shareActionProvider.setShareHistoryFileName(shareHistoryFileName);
                    shareActionProvider.setShareIntent(getShareIntent());
                    shareActionProvider.setOnShareTargetSelectedListener(new ShareActionProvider.OnShareTargetSelectedListener() {
                        @Override
                        public boolean onShareTargetSelected(ShareActionProvider shareActionProvider, Intent intent) {
                            return false;
                        }
                    });
                }
            }
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            List<Category> selectedCategories = adapter.getSelectedItems();
            // close action mode
            switch (item.getItemId()) {
                case R.id.menu_item_remove:
                    removeSelectedCategories(selectedCategories);
                    break;
            }
            adapter.clearSelections();
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            adapter.clearSelections();
        }


    }
}
