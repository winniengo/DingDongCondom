package edu.swarthmore.cs.thesexbutton;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.content.SharedPreferences.Editor;

/**
 * Created by wngo1 on 11/29/14.
 *
 * When user logs into application, store login status into sharePreference and clear
 * sharePreference when user logs out. Check everytime when user enters application if user
 * status from sharedPreference is true, then skip login otherwise move to register page.
 */
public class SavedSharedPreferences {
    static final String PREF_SESSION_TOKEN = null;
    static final String PREF_SESSION_TOKEN_EXPIRES = null;
    static final String PREF_DEVICE_UUID = null;
    static final String PREF_PASSPHRASE = null;

    static SharedPreferences getSharedPreferences(Context c) {
        return PreferenceManager.getDefaultSharedPreferences(c);
    }

    public static void setSessionToken(Context c, String sessionToken) {
        Editor editor = getSharedPreferences(c).edit();
        editor.putString(PREF_SESSION_TOKEN, sessionToken);
        editor.commit();
    }

    public static void setSessionTokenExpires(Context c, String sessionTokenExpires) {
        Editor editor = getSharedPreferences(c).edit();
        editor.putString(PREF_SESSION_TOKEN_EXPIRES, sessionTokenExpires);
        editor.commit();
    }

    public static void setDeviceUuid(Context c, String deviceUUID) {
        Editor editor = getSharedPreferences(c).edit();
        editor.putString(PREF_DEVICE_UUID, deviceUUID);
        editor.commit();
    }

    public static void setPassphrase(Context c, String passphrase) {
        Editor editor = getSharedPreferences(c).edit();
        editor.putString(PREF_PASSPHRASE, passphrase);
        editor.commit();
    }

    public static String getSessionToken(Context c) {
        return getSharedPreferences(c).getString(PREF_SESSION_TOKEN, null);
    }

    public static String getSessionTokenExpires(Context c) {
        return getSharedPreferences(c).getString(PREF_SESSION_TOKEN_EXPIRES, null);
    }

    public static String getDeviceUUID(Context c)
    {
        return getSharedPreferences(c).getString(PREF_DEVICE_UUID, null);
    }

    public static String getPassphrase(Context c)
    {
    return getSharedPreferences(c).getString(PREF_PASSPHRASE, null);
    }

    public static void logOut(Context c)
    {
        Editor editor = getSharedPreferences(c).edit();
        editor.clear(); // clear all stored data
        editor.commit();
    }
}