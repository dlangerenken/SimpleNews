package de.dala.simplenews.ui;


import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import de.dala.simplenews.R;
import de.dala.simplenews.common.Category;
import de.dala.simplenews.common.Feed;
import de.dala.simplenews.database.DatabaseHandler;
import de.dala.simplenews.utilities.BaseNavigation;
import de.dala.simplenews.utilities.EmptyObservableRecyclerView;
import recycler.CategoryAssignRecyclerAdapter;

public class AssignDialogFragment extends DialogFragment implements BaseNavigation, CategoryAssignRecyclerAdapter.OnClickListener {

    private List<Feed> feeds;
    private IDialogHandler dialogHandler;

    public interface IDialogHandler {
        void assigned();

        void canceled();
    }

    public AssignDialogFragment() {
    }

    public static AssignDialogFragment newInstance(List<Feed> feeds) {
        AssignDialogFragment fragment = new AssignDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("feeds", new ArrayList<>(feeds));
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
        EmptyObservableRecyclerView assignCategoryView = (EmptyObservableRecyclerView) rootView.findViewById(R.id.listView);

        Object feedsObject = null;
        if (getArguments() != null) {
            feedsObject = getArguments().getSerializable("feeds");
        } else if (savedInstanceState != null) {
            feedsObject = savedInstanceState.getSerializable("feeds");
        }

        if (feedsObject != null && feedsObject instanceof ArrayList<?>) {
            feeds = (ArrayList<Feed>) feedsObject;
            List<Category> categories = DatabaseHandler.getInstance().getCategories(true, true, null);
            CategoryAssignRecyclerAdapter adapter = new CategoryAssignRecyclerAdapter(getActivity(), categories, this);
            assignCategoryView.setAdapter(adapter);
        }

        getDialog().setTitle(getActivity().getString(R.string.choose_category_for_feeds));

        return rootView;
    }

    private void assignSelectedEntries(Category category) {
        for (Feed feed : feeds) {
            feed.setCategoryId(category.getId());
            if (feed.getCategoryId() != null && feed.getCategoryId() > 0) {
                DatabaseHandler.getInstance().addFeed(feed.getCategoryId(), feed, true);
            }
        }
        if (dialogHandler != null) {
            dialogHandler.assigned();
        }
        dismiss();
    }

    @Override
    public String getTitle() {
        return "AssignDialogFragment";
    }

    @Override
    public int getNavigationDrawerId() {
        return NavigationDrawerFragment.IMPORT;
    }


    @Override
    public void onClick(Category category) {
        assignSelectedEntries(category);
    }

    public void setDialogHandler(IDialogHandler handler) {
        dialogHandler = handler;
    }
}
