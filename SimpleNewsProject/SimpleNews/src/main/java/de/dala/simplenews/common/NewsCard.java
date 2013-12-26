package de.dala.simplenews.common;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ocpsoft.pretty.time.PrettyTime;

import java.util.Date;

import de.dala.simplenews.R;
import de.dala.simplenews.network.AnimatedNetworkImageView;
import de.dala.simplenews.network.VolleySingleton;
import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardExpand;
import it.gmariotti.cardslib.library.internal.CardHeader;

/**
 * Created by Daniel on 18.12.13.
 */
public class NewsCard extends Card {
    protected String mTitleHeader;
    protected String mTitleMain;
    protected Entry entry;
    private Category category;
    private Context context;

    public NewsCard(Context context, Entry entry, Category category) {
        super(context, R.layout.news_card);
        this.mTitleHeader=entry.getTitle();
        this.mTitleMain=entry.getDescription();
        this.category = category;
        this.entry = entry;
        this.context = context;
        init();
    }

    public Entry getEntry(){
        return entry;
    }

    private void init(){
        //Create a CardHeader
        CardHeader header = new CardHeader(getContext(), R.layout.my_inner_base_header){

            @Override
            public void setupInnerViewElements(ViewGroup parent,View view){

                //Add simple title to header
                if (view!=null){
                    TextView mTitleView=(TextView) view.findViewById(R.id.card_header_inner_simple_title);
                    if (mTitleView!=null){
                        mTitleView.setText(mTitle);
                    }

                    AnimatedNetworkImageView mImageView = (AnimatedNetworkImageView) view.findViewById(R.id.card_header_imageView);
                    if ( entry.getImageLink() !=  null && !entry.getImageLink().equals("")){
                        mImageView.setImageUrl(entry.getImageLink(), VolleySingleton.getImageLoader(), category.getColor());
                    }else{
                        mImageView.setVisibility(View.GONE);
                    }
                }

            }
        };

        //Set the header title
        header.setTitle(mTitleHeader);

        //Add a popup menu. This method set OverFlow button to visible
            /*header.setPopupMenu(R.menu.popupmain, new CardHeader.OnClickCardHeaderPopupMenuListener() {
                @Override
                public void onMenuItemClick(BaseCard card, MenuItem item) {
                    //Toast.makeText(getContext(), "Click on card menu" + mTitleHeader + " item=" + item.getTitle(), Toast.LENGTH_SHORT).show();
                }
            });
            */
        addCardHeader(header);
        setShadow(true);

        //Set the card inner text
        setTitle(mTitleMain);
        //setSwipeable(false);

        setOnLongClickListener(new OnLongCardClickListener() {
            @Override
            public boolean onLongClick(Card card, View view) {
                if (entry.getLink() != null){
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(entry.getLink()));
                    context.startActivity(browserIntent);
                }
                return true;
            }
        });

        //Set visible the expand/collapse button
        header.setButtonExpandVisible(true);
        if (mTitleMain != null && !mTitleMain.equals("")){
            CardExpand expand = new NewsCardExpand(context, mTitleMain, category);
            addCardExpand(expand);
        }
    }

    /**
     * This method sets values to header elements and customizes view.
     * <p/>
     * Override this method to set your elements inside InnerView.
     *
     * @param parent parent view (Inner Frame)
     * @param view   Inner View
     */
    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {

        //Add simple title to header
        if (view != null) {
            TextView mTitleView = (TextView) view.findViewById(R.id.card_main_inner_simple_title);
            if (mTitleView != null){
                String prettyTimeString = new PrettyTime().format(new Date(entry.getDate()));
                mTitleView.setText(String.format("%s - %s",entry.getSrcName(), prettyTimeString));
                mTitleView.setTextColor(category.getColor());
                mTitleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            }
        }
    }

    @Override
    public int getType() {
        //Very important with different inner layouts
        return 0;
    }
}