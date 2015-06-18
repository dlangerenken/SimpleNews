package de.dala.simplenews.ui;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.dala.simplenews.R;
import de.dala.simplenews.common.Category;
import de.dala.simplenews.common.Feed;
import de.dala.simplenews.database.DatabaseHandler;
import de.dala.simplenews.database.IDatabaseHandler;
import de.dala.simplenews.utilities.BaseNavigation;

public class AssignDialogFragment extends DialogFragment implements BaseNavigation {

    private ArrayList<Feed> feeds;

    public interface IDialogHandler {
        void assigned();

        void canceled();
    }

    private IDialogHandler dialogHandler;

    public void setDialogHandler(IDialogHandler handler) {
        dialogHandler = handler;
    }

    public AssignDialogFragment() {
    }

    public static AssignDialogFragment newInstance(ArrayList<Feed> feeds) {
        AssignDialogFragment fragment = new AssignDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("feeds", feeds);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.assign_category_list, container, false);
        ListView assignCategoryView = (ListView) rootView.findViewById(R.id.listView);

        Object feedsObject = null;
        if (getArguments() != null) {
            feedsObject = getArguments().getSerializable("feeds");
        } else if (savedInstanceState != null) {
            feedsObject = savedInstanceState.getSerializable("feeds");
        }

        if (feedsObject != null && feedsObject instanceof ArrayList<?>) {
            feeds = (ArrayList<Feed>) feedsObject;
            List<Category> categories = DatabaseHandler.getInstance().getCategories(true, true, null);
            CategoryAdapter adapter = new CategoryAdapter(getActivity(), categories);
            assignCategoryView.setAdapter(adapter);
        }

        getDialog().setTitle(getActivity().getString(R.string.choose_category_for_feeds));

        return rootView;
    }

    @Override
    public String getTitle() {
        return "AssignDialogFragment";
    } // should not be called

    @Override
    public int getNavigationDrawerId() {
        return NavigationDrawerFragment.IMPORT;
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public ImageView image;

        public CategoryViewHolder(View itemView) {
            super(itemView);
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.category_assign_item, parent, false);viewHolder.image = (ImageView) convertView.findViewById(R.id.image);
            viewHolder.name = (TextView) convertView.findViewById(R.id.name);
        }
    }

    private class CategoryAdapter extends RecyclerView.Adapter<CategoryViewHolder> {


        private Context context;
        private IDatabaseHandler database;
        private List<Category> mCategories;

        public CategoryAdapter(Context context, List<Category> categories) {
            mCategories = categories;
            this.context = context;
            this.database = DatabaseHandler.getInstance();
        }

        @Override
        public CategoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return null;
        }

        @Override
        public void onBindViewHolder(CategoryViewHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return 0;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final Category category = getItem(position);

            if (convertView == null) {
                ViewHolder viewHolder = new ViewHolder();

                convertView.setTag(viewHolder);
            }
            final ViewHolder holder = (ViewHolder) convertView.getTag();
            holder.name.setText(category.getName());
            holder.image.setBackgroundColor(category.getPrimaryColor());

            convertView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    assignSelectedEntries(category);
                }

            });
            int pad = getResources().getDimensionPixelSize(R.dimen.card_layout_padding);
            convertView.setPadding(pad, pad, pad, pad);
            return convertView;
        }


        private void assignSelectedEntries(Category category) {
            for (Feed feed : feeds) {
                feed.setCategoryId(category.getId());
                if (feed.getCategoryId() != null && feed.getCategoryId() > 0) {
                    database.addFeed(feed.getCategoryId(), feed, true);
                }
            }
            if (dialogHandler != null) {
                dialogHandler.assigned();
            }
            dismiss();
        }
    }

}
