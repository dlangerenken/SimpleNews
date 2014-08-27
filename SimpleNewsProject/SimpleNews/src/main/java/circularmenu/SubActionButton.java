/*
 *   Copyright 2014 Oguz Bilgener
 */

package circularmenu;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

import de.dala.simplenews.R;
import de.dala.simplenews.utilities.UIUtils;

/**
 * A simple button implementation with a similar look an feel to{@link FloatingActionButton}.
 */
public class SubActionButton extends FrameLayout {

    public static final int THEME_LIGHT = 0;
    public static final int THEME_LIGHTER = 2;

    public SubActionButton(Activity activity, FrameLayout.LayoutParams layoutParams, int theme, View contentView) {
        super(activity);
        setLayoutParams(layoutParams);
        // If no custom backgroundDrawable is specified, use the background drawable of the theme.
        Drawable backgroundDrawable;
        if(theme == THEME_LIGHT) {
                backgroundDrawable = activity.getResources().getDrawable(R.drawable.button_sub_action_selector);
            }
            else if(theme == THEME_LIGHTER) {
                backgroundDrawable = activity.getResources().getDrawable(R.drawable.button_action_selector);
            }
            else {
            throw new RuntimeException("Unknown SubActionButton theme: " + theme);
        }
        UIUtils.setBackground(this, backgroundDrawable);
        if(contentView != null) {
            setContentView(contentView);
        }
        setClickable(true);
    }

    private View contentView;

    /**
     * Sets a content view with custom LayoutParams that will be displayed inside this SubActionButton.
     * @param contentView
     */
    public void setContentView(View contentView) {
        LayoutParams params = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        final int margin = getResources().getDimensionPixelSize(R.dimen.sub_action_button_content_margin);
        params.setMargins(margin, margin, margin, margin);
        this.contentView = contentView;
        contentView.setClickable(false);
        this.addView(contentView, params);
    }

    /**
     * Sets a content view with default LayoutParams
     */
    public View getContentView() {
        return contentView;
    }

    /**
     * A builder for {@link SubActionButton} in conventional Java Builder format
     */
    public static class Builder {

        private Activity activity;
        private FrameLayout.LayoutParams layoutParams;
        private int theme;
        private View contentView;

        public Builder(Activity activity) {
            this.activity = activity;

            // Default SubActionButton settings
            int size = activity.getResources().getDimensionPixelSize(R.dimen.sub_action_button_size);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(size, size, Gravity.TOP | Gravity.LEFT);
            setLayoutParams(params);
            setTheme(SubActionButton.THEME_LIGHT);
        }

        public Builder setLayoutParams(FrameLayout.LayoutParams params) {
            this.layoutParams = params;
            return this;
        }

        public Builder setTheme(int theme) {
            this.theme = theme;
            return this;
        }

        public Builder setContentView(View contentView) {
            this.contentView = contentView;
            return this;
        }

        public SubActionButton build() {
            return new SubActionButton(activity,
                    layoutParams,
                    theme,
                    contentView);
        }
    }
}
