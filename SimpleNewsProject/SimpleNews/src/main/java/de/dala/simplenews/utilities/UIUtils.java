/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.dala.simplenews.utilities;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.regex.Pattern;

import de.dala.simplenews.common.Category;
import de.dala.simplenews.common.News;
import de.dala.simplenews.parser.XmlParser;

/**
 * Created by Daniel on 29.12.13.
 */
public class UIUtils {

    /**
     * Regex to search for HTML escape sequences.
     * <p/>
     * <p></p>Searches for any continuous string of characters starting with an ampersand and ending with a
     * semicolon. (Example: &amp;amp;)
     */
    private static final Pattern REGEX_HTML_ESCAPE = Pattern.compile(".*&\\S;.*");

    private static final Pattern VALID_URL = Pattern.compile("/((([A-Za-z]{3,9}:(?://)?)(?:[-;:&=\\+\\$,\\w]+@)?[A-Za-z0-9.-]+|(?:www.|[-;:&=\\+\\$,\\w]+@)[A-Za-z0-9.-]+)((?:/[\\+~%/]*)?\\??(?:[-\\+=&;%@.\\w_]*)#?(?:[\\w]*))?)/");

    public static boolean isValideUrl(String url) {
        return URLUtil.isValidUrl(url);
    }

    public static void setBackground(View view, Drawable drawable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackground(drawable);
        }
        else {
            view.setBackgroundDrawable(drawable);
        }
    }
    /**
     * Populate the given {@link TextView} with the requested text, formatting
     * through {@link Html#fromHtml(String)} when applicable. Also sets
     * {@link TextView#setMovementMethod} so inline links are handled.
     */
    public static void setTextMaybeHtml(TextView view, String text) {
        if (TextUtils.isEmpty(text)) {
            view.setText("");
            return;
        }
        if ((text.contains("<") && text.contains(">")) || REGEX_HTML_ESCAPE.matcher(text).find()) {
            view.setText(Html.fromHtml(text));
            view.setMovementMethod(LinkMovementMethod.getInstance());
        } else {
            view.setText(text);
        }
    }

    @SuppressWarnings("unchecked") // Casts are checked using runtime methods
    public static <T> T getParent(Fragment frag, Class<T> callbackInterface) {
        Fragment parentFragment = frag.getParentFragment();
        if (parentFragment != null
                && callbackInterface.isInstance(parentFragment)) {
            return (T) parentFragment;
        } else {
            FragmentActivity activity = frag.getActivity();
            if (activity != null && callbackInterface.isInstance(activity)) {
                return (T) activity;
            }
        }
        return null;
    }

    public static StateListDrawable getStateListDrawableByColor(int color) {
        Drawable colorDrawable = new ColorDrawable(color);
        return getStateListDrawableByColorDrawable(colorDrawable);

    }

    public static StateListDrawable getStateListDrawableByColorDrawable(Drawable colorDrawable) {
        StateListDrawable stateList = new StateListDrawable();
        stateList.addState(new int[]{android.R.attr.state_pressed}, colorDrawable);
        stateList.addState(new int[]{android.R.attr.state_focused}, colorDrawable);
        stateList.addState(new int[]{android.R.attr.state_checked}, colorDrawable);
        stateList.addState(new int[]{android.R.attr.state_selected}, colorDrawable);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD_MR1) {
            stateList.addState(new int[]{android.R.attr.state_activated}, colorDrawable);
        }
        stateList.addState(new int[]{}, new ColorDrawable(Color.WHITE));
        return stateList;
    }

    public static ColorStateList getColorTextStateList() {
        ColorStateList colorStateList;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD_MR1) {
            colorStateList = new ColorStateList(
                    new int[][]{
                            new int[]{android.R.attr.state_pressed},
                            new int[]{android.R.attr.state_focused},
                            new int[]{android.R.attr.state_activated},
                            new int[]{android.R.attr.state_selected},
                            new int[]{}
                    },
                    new int[]{
                            Color.WHITE,
                            Color.WHITE,
                            Color.WHITE,
                            Color.WHITE,
                            Color.BLACK
                    }
            );
        } else {
            colorStateList = new ColorStateList(
                    new int[][]{
                            new int[]{android.R.attr.state_pressed},
                            new int[]{android.R.attr.state_focused},
                            new int[]{android.R.attr.state_selected},
                            new int[]{}
                    },
                    new int[]{
                            Color.WHITE,
                            Color.WHITE,
                            Color.WHITE,
                            Color.BLACK
                    }
            );
        }
        return colorStateList;
    }

    public static boolean isNullOrEmpty(String s) {
        return s == null || s.length() == 0;
    }


    public static String fixEncoding(String latin1) {
        try {
            byte[] bytes = latin1.getBytes("ISO-8859-1");
            if (!validUTF8(bytes))
                return latin1;
            return new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // Impossible, throw unchecked
            throw new IllegalStateException("No Latin1 or UTF-8: " + e.getMessage());
        }

    }

    public static boolean validUTF8(byte[] input) {
        int i = 0;
        // Check for BOM
        if (input.length >= 3 && (input[0] & 0xFF) == 0xEF
                && (input[1] & 0xFF) == 0xBB & (input[2] & 0xFF) == 0xBF) {
            i = 3;
        }

        int end;
        for (int j = input.length; i < j; ++i) {
            int octet = input[i];
            if ((octet & 0x80) == 0) {
                continue; // ASCII
            }

            // Check for UTF-8 leading byte
            if ((octet & 0xE0) == 0xC0) {
                end = i + 1;
            } else if ((octet & 0xF0) == 0xE0) {
                end = i + 2;
            } else if ((octet & 0xF8) == 0xF0) {
                end = i + 3;
            } else {
                // Java only supports BMP so 3 is max
                return false;
            }

            while (i < end) {
                i++;
                octet = input[i];
                if ((octet & 0xC0) != 0x80) {
                    // Not a valid trailing byte
                    return false;
                }
            }
        }
        return true;
    }
}
