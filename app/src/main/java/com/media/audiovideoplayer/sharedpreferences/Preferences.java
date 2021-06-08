package com.media.audiovideoplayer.sharedpreferences;

import android.content.Context;
import android.content.SharedPreferences;

import com.media.audiovideoplayer.constants.AudioVideoConstants;

public class Preferences {
    public static SharedPreferences sharedPreferences;
    public static SharedPreferences getSharedPreferences(Context context) {
        return (sharedPreferences == null) ? context.getSharedPreferences(AudioVideoConstants.FILE_NAME_SHARED_PREFERENCES, Context.MODE_PRIVATE) : sharedPreferences;
    }
}
