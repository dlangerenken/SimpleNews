package de.dala.simplenews.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import de.dala.simplenews.R;
import de.dala.simplenews.common.Category;
import de.dala.simplenews.database.DatabaseHandler;
import de.dala.simplenews.utilities.UIUtils;

/**
 * Created by Daniel on 29.12.13.
 */
public class CategorySelectionFragment extends Fragment {


    public interface OnCategoryClicked {
        public void onColorClicked(Category category);
        public void onMoreClicked(Category category);
    }

    private List<Category> categories;
    private ListView categoryListView;
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
        categoryListView = (ListView) rootView.findViewById(R.id.listView);
        initAdapter();
        return rootView;
    }

    private void initAdapter() {
        adapter = new CategoryListAdapter(getActivity(), categories);
        categoryListView.setAdapter(adapter);
    }


    private class CategoryListAdapter extends ArrayAdapter<Category> {

        public CategoryListAdapter(Context context, List<Category> categories) {
            super(context, 0, categories);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Category category = getItem(position);

            if (convertView == null){
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.category_modify_item, null, false);
                ViewHolder viewHolder = new ViewHolder();
                viewHolder.name = (TextView) convertView.findViewById(R.id.name);
                viewHolder.remove = (ImageView) convertView.findViewById(R.id.remove);
                viewHolder.color = (ImageView) convertView.findViewById(R.id.color);
                viewHolder.more = (ImageView) convertView.findViewById(R.id.more);
                convertView.setTag(viewHolder);
            }
            ViewHolder holder = (ViewHolder) convertView.getTag();
            holder.color.setBackgroundColor(category.getColor());
            holder.name.setText(category.getName());
            holder.remove.setOnClickListener(new CategoryItemClickListener(category));
            holder.name.setOnClickListener(new CategoryItemClickListener(category));
            holder.color.setOnClickListener(new CategoryItemClickListener(category));
            holder.more.setOnClickListener(new CategoryItemClickListener(category));

            return convertView;
        }

        class ViewHolder {
            public TextView name;
            public ImageView color;
            public ImageView remove;
            public ImageView more;
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
                        categoryClicked.onColorClicked(category);
                        break;
                    case R.id.name:
                        Toast.makeText(getContext(), "Name clicked", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.remove:
                        Toast.makeText(getContext(), "Remove clicked", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.more:
                        categoryClicked.onMoreClicked(category);
                        break;
                }
            }
        }
    }



}
