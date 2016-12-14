package de.dala.simplenews.ui;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import de.dala.simplenews.R;
import de.dala.simplenews.utilities.PrefUtilities;

/**
 * Created by Daniel on 20.01.14.
 * based on http://blog.zeezonline.com/2012/07/android-rate-my-app-dialog/
 */
class RateMyApp {
    private final static String APP_TITLE = "SimpleNews - Feed Reader (Pro)";
    private final static String APP_PACKAGE_NAME = "de.dala.simplenews.smarties";
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

    private static void showRateDialog(final Context mContext) {
        new MaterialDialog.Builder(mContext)
                .content(String.format(mContext.getString(R.string.rate_my_app_desc), APP_TITLE))
                .title(String.format(mContext.getString(R.string.rate_my_app_title), APP_TITLE))
                .iconRes(mContext.getApplicationInfo().icon)
                .cancelable(false)
                .positiveText(R.string.rate_my_app_now)
                .neutralText(R.string.rate_my_app_later)
                .negativeText(R.string.rate_my_app_never)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        PrefUtilities.getInstance().shouldNotAskForRatingAnymore();
                        try {
                            mContext.startActivity(new Intent(
                                    Intent.ACTION_VIEW, Uri
                                    .parse("market://details?id="
                                            + APP_PACKAGE_NAME)
                            ));
                        } catch (ActivityNotFoundException e) {
                            Toast.makeText(mContext, mContext.getString(R.string.playstore_not_found), Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        PrefUtilities.getInstance().shouldNotAskForRatingAnymore();
                    }
                })
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        PrefUtilities.getInstance().shouldAskForRatingAgain();
                    }
                }).show();
    }
}
