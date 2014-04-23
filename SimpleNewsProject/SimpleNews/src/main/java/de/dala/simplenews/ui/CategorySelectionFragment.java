package de.dala.simplenews.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nhaarman.listviewanimations.ArrayAdapter;

import de.dala.simplenews.utilities.MyDynamicListView;

import java.util.ArrayList;
import java.util.List;

import colorpicker.ColorPickerDialog;
import colorpicker.ColorUtils;
import colorpicker.OnColorSelectedListener;
import de.dala.simplenews.R;
import de.dala.simplenews.common.Category;
import de.dala.simplenews.database.DatabaseHandler;
import de.dala.simplenews.utilities.PrefUtilities;
import de.dala.simplenews.utilities.UIUtils;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

/**
 * Created by Daniel on 29.12.13.
 */
public class CategorySelectionFragment extends Fragment {

    public interface OnCategoryClicked {
        void onMoreClicked(Category category);
        void onRSSSavedClick(Category category, String rssPath);
        void onRestore();
    }

    private ActionMode mActionMode;

    private List<Category> categories;
    private MyDynamicListView categoryListView;
    private CategoryListAdapter adapter;
    private boolean fromRSS;
    private String rssPath;
    private static final String CATEGORIES_KEY = "categories";
    private static final String FROM_RSS_KEY = "rss";
    private static final String RSS_PATH_KEY = "path";
    OnCategoryClicked categoryClicked;

    private TextView topTextView;
    private ViewGroup topView;

    public CategorySelectionFragment(){
    }

    public static CategorySelectionFragment newInstance(ArrayList<Category> categories, boolean fromRSS, String path){
        CategorySelectionFragment fragment = new CategorySelectionFragment();
        Bundle b = new Bundle();
        b.putParcelableArrayList(CATEGORIES_KEY, categories);
        b.putBoolean(FROM_RSS_KEY, fromRSS);
        b.putString(RSS_PATH_KEY, path);
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((ActionBarActivity)getActivity()).getSupportActionBar().setTitle(getString(R.string.categories_title));

        this.categoryClicked = UIUtils.getParent(this, OnCategoryClicked.class);
        if (categoryClicked == null){
            throw new ClassCastException("No Parent with Interface OnCategoryClicked");
        }
        View rootView = inflater.inflate(R.layout.category_selection, container, false);
        topView = (ViewGroup) rootView.findViewById(R.id.topView);
        if (fromRSS){
            topTextView = (TextView) rootView.findViewById(R.id.topTextView);
            topTextView.setText(getActivity().getString(R.string.category_add));
            topTextView.setVisibility(View.VISIBLE);
        }

        categoryListView = (MyDynamicListView) rootView.findViewById(R.id.listView);
        categoryListView.setDivider(null);
        categoryListView.setAdditionalOnLongItemClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                onListItemCheck(position);
                return adapter.getSelectedCount() > 1;
            }
        });

        initAdapter();
        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        this.categories = getArguments().getParcelableArrayList(CATEGORIES_KEY);
        this.fromRSS = getArguments().getBoolean(FROM_RSS_KEY);
        this.rssPath = getArguments().getString(RSS_PATH_KEY);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.category_selection_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.new_category:
                createCategoryClicked();
                return true;
            case R.id.restore_categories:
                new AlertDialog.Builder(getActivity()).setTitle(getActivity().getString(R.string.restore_categories_title)).setMessage(getActivity().getString(R.string.restore_categories_message))
                        .setPositiveButton(getActivity().getString(R.string.restore_categories_yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DatabaseHandler.getInstance().removeAllCategories();
                                PrefUtilities.getInstance().saveLoading(false);
                                categoryClicked.onRestore();
                            }
                        })
                        .setNegativeButton(getActivity().getString(R.string.restore_categories_no), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create().show();
                return true;
            case android.R.id.home:
                Intent intent = new Intent(getActivity(), MainActivity.class);
                startActivity(intent);
                getActivity().finish();
                return true;
        }
        return false;
    }

    private void initAdapter() {
        adapter = new CategoryListAdapter(getActivity(), categories);
        categoryListView.setAdapter(adapter);
    }


    private class CategoryListAdapter extends ArrayAdapter<Category> {

        private int recentChangedViewId = -1;
        private Context context;
        private DatabaseHandler database;
        private SparseBooleanArray mSelectedItemIds;

        public CategoryListAdapter(Context context, List<Category> categories) {
            super(categories);
            this.context = context;
            this.database = DatabaseHandler.getInstance();
            mSelectedItemIds = new SparseBooleanArray();
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public long getItemId(int position) {
            return getItem(position).getId();
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            Category category = getItem(position);

            if (convertView == null){
                LayoutInflater inflater = LayoutInflater.from(context);
                convertView = inflater.inflate(R.layout.category_modify_item, null, false);
                ViewHolder viewHolder = new ViewHolder();
                viewHolder.name = (TextView) convertView.findViewById(R.id.name);
                viewHolder.color = (ImageView) convertView.findViewById(R.id.color);
                viewHolder.more = (ImageView) convertView.findViewById(R.id.more);
                viewHolder.show = (CheckBox) convertView.findViewById(R.id.show);
                viewHolder.edit = (ImageView) convertView.findViewById(R.id.edit);
                convertView.setTag(viewHolder);
            }
            ViewHolder holder = (ViewHolder) convertView.getTag();
            holder.name.setText(category.getName());
            holder.color.setBackgroundColor(category.getColor());
            holder.show.setChecked(category.isVisible());
            if (!fromRSS){
                holder.edit.setOnClickListener(new CategoryItemClickListener(category));
                holder.color.setOnClickListener(new CategoryItemClickListener(category));
                holder.show.setOnClickListener(new CategoryItemClickListener(category));
                holder.more.setOnClickListener(new CategoryItemClickListener(category));
            }else{
                holder.edit.setVisibility(View.GONE);
                holder.show.setVisibility(View.GONE);
                holder.more.setVisibility(View.GONE);
                convertView.setOnClickListener(new CategoryItemRSSClickListener(category));
            }
            switchBackgroundOfView(position, convertView);
           return convertView;
        }

        private void switchBackgroundOfView(int position, View view){
            if (view != null && mSelectedItemIds.size() > position) {
                view.setBackgroundResource(mSelectedItemIds.get(position) ? R.drawable.card_background_blue : R.drawable.card_background_white);
            }
        }

        @Override
        public void swapItems(int positionOne, int positionTwo) {
            Category temp = getItem(positionOne);
            set(positionOne, getItem(positionTwo));
            set(positionTwo, temp);
            Category one = getItem(positionOne);
            one.setOrder(positionOne);
            Category two = getItem(positionTwo);
            two.setOrder(positionTwo);
            database.updateCategory(one);
            database.updateCategory(two);

            if (mActionMode != null){
                mActionMode.finish();
            }
            adapter.removeSelection();
        }

        class ViewHolder {
            public TextView name;
            public ImageView color;
            public ImageView more;
            public ImageView edit;
            public CheckBox show;
        }

        public void toggleSelection(int position) {
            recentChangedViewId = position;
            selectView(position, !mSelectedItemIds.get(position));
        }

        public void selectView(int position, boolean value)
        {
            if(value){
                mSelectedItemIds.put(position, value);
            }else{
                mSelectedItemIds.delete(position);
            }

            switchBackgroundOfView(position, categoryListView.getChildAt(position));
        }

        public int getSelectedCount() {
            return mSelectedItemIds.size();
        }

        public SparseBooleanArray getSelectedIds() {
            return mSelectedItemIds;
        }

        public void removeSelection() {
            mSelectedItemIds = new SparseBooleanArray();
            if (recentChangedViewId > -1){
                switchBackgroundOfView(recentChangedViewId, categoryListView.getChildAt(recentChangedViewId));
            }
        }
    }

    class CategoryItemRSSClickListener implements View.OnClickListener {
        private Category category;

        public CategoryItemRSSClickListener(Category category) {
            this.category = category;
        }

        @Override
        public void onClick(View v) {
            categoryClicked.onRSSSavedClick(category, rssPath);
        }
    }

    class CategoryItemClickListener implements View.OnClickListener {
        private Category category;

        public CategoryItemClickListener(Category category) {
            this.category = category;
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.color:
                    onColorClicked(category);
                    break;
                case R.id.edit:
                    editClicked(category);
                    break;
                case R.id.show:
                    boolean visible = !category.isVisible();
                    category.setVisible(visible);
                    DatabaseHandler.getInstance().updateCategory(category);
                    break;
                case R.id.more:
                    categoryClicked.onMoreClicked(category);
                    break;
            }
        }
    }

    private void onListItemCheck(int position) {
        adapter.toggleSelection(position);
        boolean hasCheckedItems = adapter.getSelectedCount() > 0;

        if (hasCheckedItems && mActionMode == null){
            // there are some selected items, start the actionMode
            mActionMode = ((ActionBarActivity)getActivity()).startSupportActionMode(new ActionModeCallBack());
        }
        else if (!hasCheckedItems && mActionMode != null){
            // there no selected items, finish the actionMode
            mActionMode.finish();
        }

        if(mActionMode != null){
            mActionMode.setTitle(String.valueOf(adapter.getSelectedCount()));
        }
    }

    private void createCategoryClicked(){
            final EditText input = new EditText(getActivity());
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which){
                        case DialogInterface.BUTTON_POSITIVE:
                            String categoryName = input.getText() != null ? input.getText().toString() : "";
                            selectColor(categoryName);
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:
                            break;
                    }
                }
            };

            new AlertDialog.Builder(getActivity()).
                    setPositiveButton(getActivity().getString(R.string.submit), dialogClickListener).setNegativeButton(getActivity().getString(R.string.cancel), dialogClickListener).setTitle(getActivity().getString(R.string.create_category_1_2))
                    .setMessage(getActivity().getString(R.string.name_of_category)).setView(input).show();
    }

    private void editClicked(final Category category) {
        final EditText input = new EditText(getActivity());
        input.setText(category.getName());
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
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

        new AlertDialog.Builder(getActivity()).
                setPositiveButton(R.string.rename, dialogClickListener).setNegativeButton(R.string.cancel, dialogClickListener).setTitle(R.string.category)
                .setMessage(R.string.change_category_name).setView(input).show();
    }

    private void onColorClicked(final Category category){
        ColorPickerDialog colorCalendar = ColorPickerDialog.newInstance(
                category.getName(),
                ColorUtils.colorChoice(getActivity()), category.getColor(), 4,
                ColorUtils.isTablet(getActivity()) ? ColorPickerDialog.SIZE_LARGE : ColorPickerDialog.SIZE_SMALL, String.format("%s %s:", getString(R.string.color_picker_default_title), category.getName()));
        colorCalendar.setOnColorSelectedListener(new OnColorSelectedListener() {
            @Override
            public void onColorSelected(int color) {
                category.setColor(color);
                DatabaseHandler.getInstance().updateCategory(category);
                adapter.notifyDataSetChanged();
            }
        });
        colorCalendar.show(getChildFragmentManager(), "dash");
    }

    private void selectColor(final String categoryName){
        ColorPickerDialog colorCalendar = ColorPickerDialog.newInstance(
                getString(R.string.create_category_2_2),
                ColorUtils.colorChoice(getActivity()), 0, 4,
                ColorUtils.isTablet(getActivity()) ? ColorPickerDialog.SIZE_LARGE : ColorPickerDialog.SIZE_SMALL, getString(R.string.select_color_create_category));
        colorCalendar.setOnColorSelectedListener(new OnColorSelectedListener() {
            @Override
            public void onColorSelected(int color) {
                Category newCategory = new Category();
                newCategory.setName(categoryName);
                newCategory.setColor(color);

                long id = DatabaseHandler.getInstance().addCategory(newCategory, true, true);
                newCategory.setId(id);
                adapter.add(newCategory);
                adapter.notifyDataSetChanged();
                Crouton.makeText(getActivity(), R.string.category_created, Style.CONFIRM, topView).show();
            }
        });
        colorCalendar.show(getChildFragmentManager(), "dash");
    }

    private class ActionModeCallBack implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // inflate contextual menu
            mode.getMenuInflater().inflate(R.menu.contextual_category_selection_menu, menu);
            return true;
        }


        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            // retrieve selected items and print them out
            SparseBooleanArray selected = adapter.getSelectedIds();
            List<Category> selectedCategories = new ArrayList<Category>();
            for (int i = 0; i < selected.size(); i++){
                if (selected.valueAt(i)) {
                    Category selectedItem = adapter.getItem(selected.keyAt(i));
                    selectedCategories.add(selectedItem);
                }
            }
            // close action mode
            switch (item.getItemId()){
                case R.id.menu_item_remove:
                    removeSelectedCategories(selectedCategories);
                    break;
                case R.id.menu_item_share:
                    break;
            }
            mode.finish();
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            // remove selection
            adapter.removeSelection();
            mActionMode = null;
        }


    }

    private void removeSelectedCategories(List<Category> selectedCategories) {
        for (Category category : selectedCategories){
            adapter.remove(category);
            DatabaseHandler.getInstance().removeCategory(category.getId(), false, false);
            categories.remove(category);
        }
        adapter.notifyDataSetChanged();
    }
}
