package de.dala.simplenews;

import android.content.Context;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.view.listener.UndoBarController;
import it.gmariotti.cardslib.library.view.listener.UndoCard;

/**
 * Created by Daniel on 19.12.13.
 */
public class MyCardArrayAdapter extends CardArrayAdapter{
    private CardComparator comparator;

    public MyCardArrayAdapter(Context context, List<Card> cards) {
        super(context, cards);
        comparator = new CardComparator();
    }


    /**
     * Enables an undo message after a swipe action
     *
     * @param enableUndo <code>true</code> to enable an undo message
     */
    public void setEnableUndo(boolean enableUndo, View undobar) {
        mEnableUndo = enableUndo;
        if (enableUndo) {
            mInternalObjects = new HashMap<String, Card>();
            for (int i=0;i<getCount();i++) {
                Card card = getItem(i);
                mInternalObjects.put(card.getId(), card);
            }

            //Create a UndoController
            if (mUndoBarController==null){
                if (undobar != null) {
                    mUndoBarController = new UndoBarController(undobar, this);
                }
            }
        }else{
            mUndoBarController=null;
        }
    }

    @Override
    public void onUndo(Parcelable token) {
        //Restore items in lists (use reverseSortedOrder)
        if (token != null) {

            UndoCard item = (UndoCard) token;
            int[] itemPositions = item.itemPosition;
            String[] itemIds = item.itemId;

            if (itemPositions != null) {
                int end = itemPositions.length;

                for (int i = end - 1; i >= 0; i--) {
                    int itemPosition = itemPositions[i];
                    String id= itemIds[i];

                    if (id==null){
                        Log.w(TAG, "You have to set a id value to use the undo action");
                    }else{
                        Card card = mInternalObjects.get(id);
                        if (card!=null){
                            insert(card, itemPosition);
                            notifyDataSetChanged();
                            if (card.getOnUndoSwipeListListener()!=null)
                                card.getOnUndoSwipeListListener().onUndoSwipe(card);
                        }
                    }
                }
            }
        }
    }

    private class CardComparator implements Comparator<Card> {
            @Override
            public int compare(Card card0, Card card1) {
                if (card0 instanceof NewsCardFragment.NewsCard && card1 instanceof NewsCardFragment.NewsCard){
                    Entry entry0 = ((NewsCardFragment.NewsCard) card0).getEntry();
                    Entry entry1 = ((NewsCardFragment.NewsCard) card1).getEntry();
                    if (entry0.getDate() == null){
                        return -1;
                    }else if (entry1.getDate() == null){
                        return 1;
                    }
                    if (entry0.getDate() > entry1.getDate()){
                        return -1;
                    }else{
                        return 1;
                    }
                }
                return -1;
        }
    }
    private void sort(){
        sort(comparator);
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    @Override
    public void add(Card card) {
        super.add(card);
        mInternalObjects.put(card.getId(), card);
        sort();
    }

}
