package de.dala.simplenews.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;

import com.rometools.rome.feed.opml.Opml;
import com.rometools.rome.io.impl.OPML20Generator;
import com.rometools.rome.io.FeedException;

import org.jdom2.output.XMLOutputter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import colorpicker.ColorPickerDialog;
import colorpicker.ColorUtils;
import colorpicker.OnColorSelectedListener;
import de.dala.simplenews.R;
import de.dala.simplenews.common.Category;
import de.dala.simplenews.database.DatabaseHandler;
import de.dala.simplenews.utilities.BaseNavigation;
import de.dala.simplenews.utilities.ColorManager;
import de.dala.simplenews.utilities.EmptyObservableRecyclerView;
import de.dala.simplenews.utilities.LightAlertDialog;
import de.dala.simplenews.utilities.OpmlConverter;
import de.dala.simplenews.utilities.PrefUtilities;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import recycler.CategoryRecyclerAdapter;
import recycler.ChoiceModeRecyclerAdapter;

public class CategorySelectionFragment extends BaseFragment implements BaseNavigation, CategoryRecyclerAdapter.OnCategoryClicked, ChoiceModeRecyclerAdapter.ChoiceModeListener {

    private static final String CATEGORIES_KEY = "categories";
    private static final String RSS_PATH_KEY = "path";
    private ActionMode mActionMode;
    private List<Category> categories;
    private EmptyObservableRecyclerView recyclerView;
    private CategoryRecyclerAdapter adapter;
    private String rssPath;
    private ViewGroup topView;
    private ShareActionProvider shareActionProvider;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.category_selection, container, false);
        topView = (ViewGroup) rootView.findViewById(R.id.topView);
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
                LightAlertDialog.Builder.create(getActivity()).setTitle(getActivity().getString(R.string.restore_categories_title)).setMessage(getActivity().getString(R.string.restore_categories_message))
                        .setPositiveButton(getActivity().getString(R.string.restore_categories_yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DatabaseHandler.getInstance().removeAllCategories();
                                PrefUtilities.getInstance().saveLoading(false);
                                onRestore();
                            }
                        })
                        .setNegativeButton(getActivity().getString(R.string.restore_categories_no), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create().show();
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
        final EditText input = new EditText(getActivity());
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        String categoryName = input.getText() != null ? input.getText().toString() : "";
                        selectColor(categoryName);
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };

        LightAlertDialog.Builder.create(getActivity()).
                setPositiveButton(getActivity().getString(R.string.submit), dialogClickListener).setNegativeButton(getActivity().getString(R.string.cancel), dialogClickListener).setTitle(getActivity().getString(R.string.create_category_1_2))
                .setMessage(getActivity().getString(R.string.name_of_category)).setView(input).show();
    }

    @Override
    public void editClicked(final Category category) {
        final EditText input = new EditText(getActivity());
        input.setText(category.getName());
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        String newName = input.getText() != null ? input.getText().toString() : "";
                        category.setName(newName);
                        adapter.notifyDataSetChanged();
                        DatabaseHandler.getInstance().updateCategory(category);
                        Toast.makeText(getActivity(), String.format(getActivity().getString(R.string.new_name), newName), Toast.LENGTH_SHORT).show();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };

        LightAlertDialog.Builder.create(getActivity()).
                setPositiveButton(R.string.rename, dialogClickListener).setNegativeButton(R.string.cancel, dialogClickListener).setTitle(R.string.category)
                .setMessage(R.string.change_category_name).setView(input).show();
    }

    private CategoryModifierFragment getCategoryModifierFragment() {
        Fragment fragment = getParentFragment();
        if (fragment instanceof CategoryModifierFragment) {
            return (CategoryModifierFragment) fragment;
        }
        return null;
    }

    @Override
    public void onMoreClicked(Category category) {
        CategoryModifierFragment fragment = getCategoryModifierFragment();
        if (fragment != null) {
            fragment.onMoreClicked(category);
        }
    }

    @Override
    public void onRSSSavedClick(Category category, String rssPath) {
        CategoryModifierFragment fragment = getCategoryModifierFragment();
        if (fragment != null) {
            fragment.onRSSSavedClick(category, rssPath);
        }
    }

    @Override
    public void onShowClicked(Category category) {
        CategoryModifierFragment fragment = getCategoryModifierFragment();
        if (fragment != null) {
            fragment.onShowClicked(category);
        }
    }

    @Override
    public void onRestore() {

    }


    @Override
    public void onColorClicked(final Category category) {
        ColorPickerDialog colorCalendar = ColorPickerDialog.newInstance(
                category.getName(),
                ColorUtils.colorChoice(getActivity()), category.getPrimaryColor(), 4,
                ColorUtils.isTablet(getActivity()) ? ColorPickerDialog.SIZE_LARGE : ColorPickerDialog.SIZE_SMALL, String.format("%s %s:", getString(R.string.color_picker_default_title), category.getName()));
        colorCalendar.setOnColorSelectedListener(new OnColorSelectedListener() {
            @Override
            public void onColorSelected(int color) {
                category.setColorId(ColorManager.getInstance().getIdByColor(color));
                DatabaseHandler.getInstance().updateCategory(category);
                adapter.notifyDataSetChanged();
            }
        });
        colorCalendar.show(getChildFragmentManager(), "dash");
    }

    private void selectColor(final String categoryName) {
        ColorPickerDialog colorCalendar = ColorPickerDialog.newInstance(
                getString(R.string.create_category_2_2),
                ColorUtils.colorChoice(getActivity()), 0, 4,
                ColorUtils.isTablet(getActivity()) ? ColorPickerDialog.SIZE_LARGE : ColorPickerDialog.SIZE_SMALL, getString(R.string.select_color_create_category));
        colorCalendar.setOnColorSelectedListener(new OnColorSelectedListener() {
            @Override
            public void onColorSelected(int color) {
                Category newCategory = new Category();
                newCategory.setName(categoryName);
                newCategory.setColorId(ColorManager.getInstance().getIdByColor(color));

                DatabaseHandler.getInstance().addCategory(newCategory, true, true);
                adapter.add(newCategory);
                Crouton.makeText(getActivity(), R.string.category_created, Style.CONFIRM, topView).show();
            }
        });
        colorCalendar.show(getChildFragmentManager(), "dash");
    }

    private void removeSelectedCategories(List<Category> selectedCategories) {
        for (Category category : selectedCategories) {
            adapter.remove(category);
            DatabaseHandler.getInstance().removeCategory(category.getId(), false, false);
        }
    }

    @Override
    public String getTitle() {
        Context context = getActivity();
        if (context != null) {
            return context.getString(R.string.category_selection_fragment_title);
        }
        return "SimpleNews"; //default
    }

    @Override
    public int getNavigationDrawerId() {
        return NavigationDrawerFragment.CATEGORIES;
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
