package de.dala.simplenews.ui;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import de.dala.simplenews.R;
import de.dala.simplenews.utilities.PrefUtilities;

/**
 * Created by Daniel on 20.01.14.
 * based on http://blog.zeezonline.com/2012/07/android-rate-my-app-dialog/
 */
public class RateMyApp {
    private final static String APP_TITLE = "SimpleNews - RSS Reader";
    private final static String APP_PACKAGE_NAME = "de.dala.simplenews";
    private final static int DAYS_UNTIL_PROMPT = 5;
    private final static int LAUNCHES_UNTIL_PROMPT = 7;

    private RateMyApp() {
    }

    public static void appLaunched(Context mContext) {
        PrefUtilities utilities = PrefUtilities.getInstance();
        boolean rateAgain = utilities.shouldAskForRatingAgain();
        if (!rateAgain) {
            return;
        }
        utilities.increaseLaunchCountForRating();
        long date = PrefUtilities.getInstance().getDateOfFirstLaunch();
        if (date == 0) {
            date = System.currentTimeMillis();
            utilities.setDateOfFirstLaunch(date);
        }

        if (utilities.getLaunchCount() >= LAUNCHES_UNTIL_PROMPT) {
            if (System.currentTimeMillis() >= date + (DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000)) {
                showRateDialog(mContext);
            }
        }
    }

    public static void showRateDialog(final Context mContext) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        String message = String.format(mContext.getString(R.string.rate_my_app_desc), APP_TITLE);
        builder.setMessage(message)
                .setTitle(String.format(mContext.getString(R.string.rate_my_app_title), APP_TITLE))
                .setIcon(mContext.getApplicationInfo().icon)
                .setCancelable(false)
                .setPositiveButton(mContext.getString(R.string.rate_my_app_now),
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                PrefUtilities.getInstance().shouldNotAskForRatingAnymore();
                                try {
                                    mContext.startActivity(new Intent(
                                            Intent.ACTION_VIEW, Uri
                                            .parse("market://details?id="
                                                    + APP_PACKAGE_NAME)
                                    ));
                                } catch (ActivityNotFoundException e) {
                                    Toast.makeText(mContext, mContext.getString(R.string.playstore_not_found), Toast.LENGTH_LONG);
                                }
                                dialog.dismiss();
                            }
                        }
                )
                .setNeutralButton(mContext.getString(R.string.rate_my_app_later),
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                dialog.dismiss();

                            }
                        }
                )
                .setNegativeButton(mContext.getString(R.string.rate_my_app_never),
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                PrefUtilities.getInstance().shouldNotAskForRatingAnymore();
                                dialog.dismiss();

                            }
                        }
                );
        builder.create().show();
    }
}
