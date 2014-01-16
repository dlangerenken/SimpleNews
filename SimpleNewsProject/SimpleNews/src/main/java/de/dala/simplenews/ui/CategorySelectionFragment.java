package de.dala.simplenews.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.internal.s;
import com.haarman.listviewanimations.ArrayAdapter;
import com.haarman.listviewanimations.itemmanipulation.contextualundo.ContextualUndoAdapter;
import com.haarman.listviewanimations.view.DynamicListView;

import java.util.ArrayList;
import java.util.List;

import colorpicker.ColorPickerDialog;
import colorpicker.ColorUtils;
import colorpicker.OnColorSelectedListener;
import de.dala.simplenews.R;
import de.dala.simplenews.common.Category;
import de.dala.simplenews.database.DatabaseHandler;
import de.dala.simplenews.utilities.UIUtils;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

/**
 * Created by Daniel on 29.12.13.
 */
public class CategorySelectionFragment extends Fragment implements ContextualUndoAdapter.DeleteItemCallback {

    public interface OnCategoryClicked {
        void onMoreClicked(Category category);
        void onRSSSavedClick(Category category, String rssPath);
    }

    private List<Category> categories;
    private DynamicListView categoryListView;
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

        categoryListView = (DynamicListView) rootView.findViewById(R.id.listView);
        categoryListView.setDivider(null);
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
        }
        return false;
    }


    private void initAdapter() {
        adapter = new CategoryListAdapter(getActivity(), categories);
        ContextualUndoAdapter undoAdapter = new ContextualUndoAdapter(adapter, R.layout.undo_row, R.id.undo_row_undobutton, 5000, R.id.undo_row_texttv, new MyFormatCountDownCallback());
        undoAdapter.setAbsListView(categoryListView);
        undoAdapter.setDeleteItemCallback(this);
        categoryListView.setAdapter(undoAdapter);
    }

    @Override
    public void deleteItem(int position) {
        Category category = adapter.getItem(position);
        adapter.remove(position);
        adapter.notifyDataSetChanged();

        DatabaseHandler.getInstance().removeCategory(category.getId(), false, false);
        categories.remove(category);
    }

    private class MyFormatCountDownCallback implements ContextualUndoAdapter.CountDownFormatter {

        @Override
        public String getCountDownString(long millisUntilFinished) {
            int seconds = (int) Math.ceil((millisUntilFinished / 1000.0));
            if (seconds > 0) {
                return getResources().getQuantityString(R.plurals.countdown_seconds, seconds, seconds);
            }
            return getString(R.string.countdown_dismissing);
        }
    }

    private class CategoryListAdapter extends ArrayAdapter<Category> {

        private Context context;
        private DatabaseHandler database;

        public CategoryListAdapter(Context context, List<Category> categories) {
            super(categories);
            this.context = context;
            this.database = DatabaseHandler.getInstance();
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
        public View getView(int position, View convertView, ViewGroup parent) {
            Category category = getItem(position);

            if (convertView == null){
                LayoutInflater inflater = LayoutInflater.from(context);
                convertView = inflater.inflate(R.layout.category_modify_item, null, false);
                ViewHolder viewHolder = new ViewHolder();
                viewHolder.name = (TextView) convertView.findViewById(R.id.name);
                viewHolder.color = (ImageView) convertView.findViewById(R.id.color);
                viewHolder.more = (ImageView) convertView.findViewById(R.id.more);
                viewHolder.show = (ImageView) convertView.findViewById(R.id.show);
                viewHolder.edit = (ImageView) convertView.findViewById(R.id.edit);
                convertView.setTag(viewHolder);
            }
            ViewHolder holder = (ViewHolder) convertView.getTag();
            holder.name.setText(category.getName());
            holder.color.setBackgroundColor(category.getColor());
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

            return convertView;
        }

        @Override
        public void swapItems(int positionOne, int positionTwo) {
            Category temp = getItem(positionOne);
            set(positionOne, getItem(positionTwo));
            set(positionTwo, temp);
            database.updateCategoryOrder(getItem(positionOne).getId(), positionOne);
            database.updateCategoryOrder(getItem(positionTwo).getId(), positionTwo);
        }

        class ViewHolder {
            public TextView name;
            public ImageView color;
            public ImageView more;
            public ImageView edit;
            public ImageView show;
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
                    break;
                case R.id.more:
                    categoryClicked.onMoreClicked(category);
                    break;
            }
        }
    }

    private void createCategoryClicked(){
            final EditText input = new EditText(getActivity());
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which){
                        case DialogInterface.BUTTON_POSITIVE:
                            String categoryName = input.getText().toString();
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
                        String newName = input.getText().toString();
                        category.setName(newName);
                        adapter.notifyDataSetChanged();
                        DatabaseHandler.getInstance().updateCategoryName(category.getId(), newName);
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
                R.string.color_picker_default_title,
                ColorUtils.colorChoice(getActivity()), category.getColor(), 4,
                ColorUtils.isTablet(getActivity()) ? ColorPickerDialog.SIZE_LARGE : ColorPickerDialog.SIZE_SMALL, null);
        colorCalendar.setOnColorSelectedListener(new OnColorSelectedListener() {
            @Override
            public void onColorSelected(int color) {
                category.setColor(color);
                DatabaseHandler.getInstance().updateCategoryColor(category.getId(), color);
                adapter.notifyDataSetChanged();
            }
        });
        colorCalendar.show(getChildFragmentManager(), "dash");
    }

    private void selectColor(final String categoryName){
        ColorPickerDialog colorCalendar = ColorPickerDialog.newInstance(
                R.string.create_category_2_2,
                ColorUtils.colorChoice(getActivity()), 0, 4,
                ColorUtils.isTablet(getActivity()) ? ColorPickerDialog.SIZE_LARGE : ColorPickerDialog.SIZE_SMALL, R.string.select_color_create_category);
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
                Crouton.makeText(getActivity(), R.string.category_created, Style.CONFIRM,  topView).show();
            }
        });
        colorCalendar.show(getChildFragmentManager(), "dash");
    }
}
