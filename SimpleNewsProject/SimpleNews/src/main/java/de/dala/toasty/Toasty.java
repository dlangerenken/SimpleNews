package de.dala.toasty;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

/**
 * Logging based on Google:
 *
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
 *
 * Created by Daniel on 10.01.14.
 */
public class Toasty {

    private static Toasty _instance;
    private Context context;

    private static String LOG_PREFIX = "prefix";
    private static int LOG_PREFIX_LENGTH = LOG_PREFIX.length();
    private static final int MAX_LOG_TAG_LENGTH = 23;

    private enum LogType {TOAST, LOG, BOTH};

    private Toast currentToast;

    private LogType type = LogType.LOG;
    private boolean shouldInterrupt = true;
    private int toastLength = Toast.LENGTH_LONG;

    private Toasty(Context context){
        this.context = context;
    }

    private boolean isLogType(){
        return type == LogType.LOG || type== LogType.BOTH;
    }
    public void showToast(Boolean interrupt, Integer length, Integer viewId, String text){
        int myLength = length != null ? length : toastLength;
        if (shouldInterrupt || interrupt){
            if (currentToast != null){
                currentToast.cancel();
            }
        }
        if (viewId != null){
            currentToast = Toast.makeText(context, viewId, myLength);
            currentToast.setText(text);
        }else{
            currentToast = Toast.makeText(context, text, myLength);
        }
        currentToast.show();
    }

    public void showOrLogToast(final String tag, String message, Throwable cause, int logLevel){
        int result = -1;
        if (isLogType()){
         if (Log.isLoggable(tag, logLevel)) {
            switch (logLevel){
                case Log.DEBUG:
                    result = (cause != null) ? Log.d(tag, message, cause) :  Log.d(tag, message);
                    break;
                case Log.ERROR:
                    result = (cause != null) ? Log.e(tag, message, cause) :  Log.e(tag, message);
                    break;
                case Log.INFO:
                    result = (cause != null) ? Log.i(tag, message, cause) :  Log.i(tag, message);
                    break;
                case Log.WARN:
                    result = (cause != null) ? Log.w(tag, message, cause) :  Log.w(tag, message);
                    break;
                case Log.VERBOSE:
                    result = (cause != null) ? Log.v(tag, message, cause) :  Log.v(tag, message);
                    break;
            }
         }
        }else{
            showToast(null, null, null, tag + ": " + message);
        }
    }

    public static void init(Context context, String logPrefix){
        _instance = new Toasty(context);
    }

    public static void toastI(String text){
        _instance.showToast(true, null, null, text);
    }

    public static void toastI(String text, int duration){
        _instance.showToast(true, duration, null, text);
    }

    public static void toastI(String text, int duration, int view){
        _instance.showToast(true, duration, view, text);
    }

    public static void toast(String text){
        _instance.showToast(null, null, null, text);
    }

    public static void toast(String text, int duration){
        _instance.showToast(null, duration, null, text);
    }

    public static void toast(String text, int duration, int view){
        _instance.showToast(null, duration, view, text);
    }


    public static String makeLogTag(String str) {
        if (str.length() > MAX_LOG_TAG_LENGTH - LOG_PREFIX_LENGTH) {
            return LOG_PREFIX + str.substring(0, MAX_LOG_TAG_LENGTH - LOG_PREFIX_LENGTH - 1);
        }
        return LOG_PREFIX + str;
    }

    /**
     * Don't use this when obfuscating class names!
     */
    public static String makeLogTag(Class cls) {
        return makeLogTag(cls.getSimpleName());
    }

    public static void LOGD(final String tag, String message) {
        _instance.showOrLogToast(tag, message, null, Log.DEBUG);
    }

    public static void LOGD(final String tag, String message, Throwable cause) {
        _instance.showOrLogToast(tag, message, cause, Log.DEBUG);
    }

    public static void LOGV(final String tag, String message) {
        _instance.showOrLogToast(tag, message, null, Log.VERBOSE);
    }

    public static void LOGV(final String tag, String message, Throwable cause) {
        _instance.showOrLogToast(tag, message, cause, Log.VERBOSE);
    }

    public static void LOGI(final String tag, String message) {
        _instance.showOrLogToast(tag, message, null, Log.INFO);
    }

    public static void LOGI(final String tag, String message, Throwable cause) {
        _instance.showOrLogToast(tag, message, cause, Log.INFO);
    }

    public static void LOGW(final String tag, String message) {
        _instance.showOrLogToast(tag, message, null, Log.WARN);
    }

    public static void LOGW(final String tag, String message, Throwable cause) {
        _instance.showOrLogToast(tag, message, cause, Log.WARN);
    }

    public static void LOGE(final String tag, String message) {
        _instance.showOrLogToast(tag, message, null, Log.ERROR);
    }

    public static void LOGE(final String tag, String message, Throwable cause) {
        _instance.showOrLogToast(tag, message, cause, Log.ERROR);
    }
}
