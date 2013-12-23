package de.dala.simplenews;

import android.content.Context;

import java.util.Date;

/**
 * Created by Daniel on 23.12.13.
 */
public class Helper {
    public static String formatDate(Context context, Long date) {
        String trimmer = " -  ";
        if  (date != null){
            long currentTime = new Date().getTime();
            long diff = currentTime - date;
            long seconds = diff/1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;
            long days = hours / 24;
            String unit = "";
            long value = 0;
            if (days > 0 ){
                if (days == 1){
                    unit = context.getString(R.string.day);
                }else{
                    unit = context.getString(R.string.days);
                }
                value = days;
            }else  if (hours > 0){
                if (hours == 1){
                    unit = context.getString(R.string.hour);
                }else{
                    unit = context.getString(R.string.hours);
                }
                value = hours;
            }else if (minutes > 0) {
                if (hours == 1){
                    unit = context.getString(R.string.minute);
                }else{
                    unit = context.getString(R.string.minutes);
                }
                value = minutes;
            }else {
                if (seconds == 0){
                    return "";
                }
                return context.getString(R.string.just_now);
            }
            return String.format("%s vor %s %s", trimmer, value+"", unit);
        }else{
            return "";
        }
    }
}
