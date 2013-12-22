package de.dala.simplenews.dialog;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;

import de.dala.simplenews.R;
import it.gmariotti.changelibs.library.view.ChangeLogListView;

/**
 * Created by Daniel on 22.12.13.
 */
public class ChangeLogDialog extends DialogFragment {

    public ChangeLogDialog() {
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {



        LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        ChangeLogListView chgList=(ChangeLogListView)layoutInflater.inflate(R.layout.changelog_fragment_dialog, null);

        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.changelog_title)
                .setView(chgList)
                .setPositiveButton(R.string.about_ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                            }
                        }
                )
                .create();
    }
}