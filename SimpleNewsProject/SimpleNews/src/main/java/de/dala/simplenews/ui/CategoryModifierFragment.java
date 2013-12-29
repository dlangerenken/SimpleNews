package de.dala.simplenews.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListFragment;

import java.util.List;

import colorpicker.ColorPickerDialog;
import colorpicker.ColorUtils;
import colorpicker.OnColorSelectedListener;
import de.dala.simplenews.R;
import de.dala.simplenews.common.Category;
import de.dala.simplenews.database.DatabaseHandler;

/**
 * Created by Daniel on 29.12.13.
 */
public class CategoryModifierFragment extends DialogFragment implements CategorySelectionFragment.OnCategoryClicked{
    private View rootView;
    private List<Category> categories;

    public CategoryModifierFragment(List<Category> categories) {
        this.categories = categories;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.category_modifier, container, false);

        FragmentTransaction t = getChildFragmentManager().beginTransaction();
        Fragment fragment = new CategorySelectionFragment(categories);
        t.replace(R.id.container, fragment);
        t.addToBackStack(null);
        t.commit();
        return rootView;
    }

    @Override
    public void onColorClicked(final Category category) {
        ColorPickerDialog colorcalendar = ColorPickerDialog.newInstance(
                R.string.color_picker_default_title,
                ColorUtils.colorChoice(getActivity()), 0, 4,
                ColorUtils.isTablet(getActivity()) ? ColorPickerDialog.SIZE_LARGE : ColorPickerDialog.SIZE_SMALL);
        colorcalendar.setOnColorSelectedListener(new OnColorSelectedListener() {
            @Override
            public void onColorSelected(int color) {
                category.setColor(color);
                DatabaseHandler.getInstance().updateCategoryColor(category.getId(), color);
                getChildFragmentManager().popBackStack();
            }
        });
        FragmentTransaction t = getChildFragmentManager().beginTransaction();
        t.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left,  R.anim.slide_in_right, R.anim.slide_out_right);
        t.replace(R.id.container, colorcalendar);
        t.addToBackStack(null);
        t.commit();
    }

    @Override
    public void onMoreClicked(Category category) {
        Toast.makeText(getActivity(), category.getId() + " more", Toast.LENGTH_SHORT).show();
    }
}
