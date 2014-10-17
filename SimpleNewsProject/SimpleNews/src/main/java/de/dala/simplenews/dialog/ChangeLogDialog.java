package de.dala.simplenews.dialog;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;

import de.dala.simplenews.R;
import de.dala.simplenews.utilities.LightAlertDialog;
import it.gmariotti.changelibs.library.view.ChangeLogListView;

/**
 * Created by Daniel on 22.12.13.
 */
public class ChangeLogDialog extends DialogFragment {

    private DialogInterface mDialogInterface;

    public ChangeLogDialog() {
    }

    public void setDialogInterface(DialogInterface dialogInterface) {
        mDialogInterface = dialogInterface;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        ChangeLogListView chgList = (ChangeLogListView) layoutInflater.inflate(R.layout.changelog_fragment_dialog, null);
        AlertDialog dialog = LightAlertDialog.Builder.create(getActivity())
                .setTitle(R.string.changelog_title)
                .setView(chgList).setPositiveButton(R.string.about_ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                            }
                        }
                )
                .create();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        if (mDialogInterface != null){
                            mDialogInterface.dismiss();
                        }
                    }
                });

        return dialog;
    }
}