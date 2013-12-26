package de.dala.simplenews.common;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import de.dala.simplenews.R;
import it.gmariotti.cardslib.library.internal.CardExpand;

/**
 * Created by Daniel on 23.12.13.
 */
    public class NewsCardExpand extends CardExpand {

        private String mTitleMain;
        private Category category;

        public NewsCardExpand(Context context, String mTitleMain, Category category) {
            super(context, R.layout.news_card_expand);
            this.mTitleMain = mTitleMain;
            this.category = category;
        }

        @Override
        public void setupInnerViewElements(ViewGroup parent, View view) {
            if (view == null) return;
            view.setBackgroundColor(Color.WHITE);

            TextView mTitleView = (TextView) view.findViewById(R.id.expand_card_main_inner_simple_title);
            if (mTitleView != null){
                mTitleView.setText(mTitleMain);
            }
            View mView = view.findViewById(R.id.colorBorder);
            if (mView != null){
                mView.setBackgroundColor(category.getColor());
            }
        }
    }