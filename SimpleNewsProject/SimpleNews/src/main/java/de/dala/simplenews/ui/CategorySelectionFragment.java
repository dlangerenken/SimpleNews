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
import android.widget.TextView;
import android.widget.Toast;

import com.haarman.listviewanimations.ArrayAdapter;
import com.haarman.listviewanimations.itemmanipulation.contextualundo.ContextualUndoAdapter;
import com.haarman.listviewanimations.view.DynamicListView;

import java.util.List;

import colorpicker.ColorPickerDialog;
import colorpicker.ColorUtils;
import colorpicker.OnColorSelectedListener;
import de.dala.simplenews.R;
import de.dala.simplenews.common.Category;
import de.dala.simplenews.database.DatabaseHandler;
import de.dala.simplenews.utilities.UIUtils;

/**
 * Created by Daniel on 29.12.13.
 */
public class CategorySelectionFragment extends Fragment implements ContextualUndoAdapter.DeleteItemCallback {

    public interface OnCategoryClicked {
        public void onMoreClicked(Category category);
    }

    private List<Category> categories;
    private DynamicListView categoryListView;
    private CategoryListAdapter adapter;
    OnCategoryClicked categoryClicked;

    public CategorySelectionFragment(List<Category> categories) {
        this.categories = categories;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.categoryClicked = UIUtils.getParent(this, OnCategoryClicked.class);
        if (categoryClicked == null){
            throw new ClassCastException("No Parent with Interface OnCategoryClicked");
        }
        View rootView = inflater.inflate(R.layout.category_selection, container, false);
        categoryListView = (DynamicListView) rootView.findViewById(R.id.listView);
        categoryListView.setDivider(null);
        initAdapter();
        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
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
            holder.color.setBackgroundColor(category.getColor());
            holder.name.setText(category.getName());
            holder.edit.setOnClickListener(new CategoryItemClickListener(category));
            holder.color.setOnClickListener(new CategoryItemClickListener(category));
            holder.show.setOnClickListener(new CategoryItemClickListener(category));
            holder.more.setOnClickListener(new CategoryItemClickListener(category));

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
                    setPositiveButton("Ok", dialogClickListener).setNegativeButton("Cancel", dialogClickListener).setTitle(getActivity().getString(R.string.create_category_1_2))
                    .setMessage("Name for the Category").setView(input).show();
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
                        Toast.makeText(getActivity(), "New name: " + newName, Toast.LENGTH_SHORT).show();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };

        new AlertDialog.Builder(getActivity()).
                setPositiveButton("Rename", dialogClickListener).setNegativeButton("Cancel", dialogClickListener).setTitle("Category")
                .setMessage("Change the name o the category").setView(input).show();
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
                Toast.makeText(getActivity(), "Category created", Toast.LENGTH_SHORT).show();
            }
        });
        colorCalendar.show(getChildFragmentManager(), "dash");
    }
}
