package com.paulvarry.intra42;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.util.AndroidRuntimeException;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.paulvarry.intra42.Tools.AppSettings;
import com.paulvarry.intra42.Tools.Token;
import com.paulvarry.intra42.activity.MainActivity;
import com.paulvarry.intra42.api.ApiService;
import com.paulvarry.intra42.api.ApiServiceCantina;
import com.paulvarry.intra42.api.ServiceGenerator;
import com.paulvarry.intra42.api.model.AccessToken;
import com.paulvarry.intra42.api.model.CursusUsers;
import com.paulvarry.intra42.api.model.Users;
import com.paulvarry.intra42.cache.CacheCampus;
import com.paulvarry.intra42.cache.CacheCursus;
import com.paulvarry.intra42.cache.CacheSQLiteHelper;
import com.paulvarry.intra42.cache.CacheTags;
import com.paulvarry.intra42.cache.CacheUsers;
import com.paulvarry.intra42.notifications.AlarmReceiverNotifications;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AppClass extends Application {

    public static final String PREFS_APP_VERSION = "prefs_app_version";
    public static final String PREFS_NAME = "intra_prefs";
    public static final String PREFS_API_TOKEN = "api_token";
    public static final String API_ME_LOGIN = "me_login";

    private static AppClass sInstance;
    public List<CursusUsers> cursus;
    public Users me;

    public AccessToken accessToken;
    public CacheSQLiteHelper cacheSQLiteHelper;
    @Nullable
    public DatabaseReference firebaseRefFriends;

    public static AppClass instance() {
        return sInstance;
    }

    public static void scheduleAlarm(Context context) {
        // Construct an intent that will execute the AlarmReceiver
        Intent intent = new Intent(context, AlarmReceiverNotifications.class);

        // Create a PendingIntent to be triggered when the alarm goes off
        final PendingIntent pIntent = PendingIntent.getBroadcast(context, AlarmReceiverNotifications.REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        long firstMillis = System.currentTimeMillis(); // alarm is set right away
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        SharedPreferences settings = AppSettings.getSharedPreferences(context);
        int notificationsFrequency = AppSettings.Notifications.getNotificationsFrequency(settings);

        if (AppSettings.Notifications.getNotificationsAllow(settings) && notificationsFrequency != -1)
            alarm.setRepeating(AlarmManager.RTC_WAKEUP, firstMillis + 100, 60000 * notificationsFrequency, pIntent);

        Log.d("Notification", "schedule");
    }

    public static void unscheduleAlarm(Context context) {
        // Construct an intent that will execute the AlarmReceiver
        Intent intent = new Intent(context, AlarmReceiverNotifications.class);

        // Create a PendingIntent to be triggered when the alarm goes off
        final PendingIntent pIntent = PendingIntent.getBroadcast(context, AlarmReceiverNotifications.REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pIntent);

        Log.d("Notification", "unschedule");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        accessToken = Token.getTokenFromShared(this);
        cacheSQLiteHelper = new CacheSQLiteHelper(this);
        sInstance = this;

        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String login = sharedPreferences.getString(API_ME_LOGIN, "");
        if (login.isEmpty()) {
//            logoutAndRedirect();
            return;
        }

        if (CacheUsers.isCached(cacheSQLiteHelper, login))
            me = CacheUsers.get(cacheSQLiteHelper, login);

        initFirebase();

        if (isExternalStorageWritable() && (AppSettings.Advanced.getAllowSaveLogs(this) | BuildConfig.DEBUG)) {

            File appDirectory = getExternalFilesDir(null);
            File logDirectory = new File(appDirectory + "/logs");
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSX", Locale.getDefault());
            Date now = new Date();
            File logFile = new File(logDirectory, "logcat_" + formatter.format(now) + ".txt");

            // create log folder
            if (!logDirectory.exists()) {
                logDirectory.mkdir();
            }

            // clear the previous logcat and then write the new one to the file
            try {
                logFile.createNewFile();
                Process process = Runtime.getRuntime().exec("logcat -c");
                process = Runtime.getRuntime().exec("logcat -f " + logFile);

            } catch (IOException e) {
                e.printStackTrace();
            }

        } else if (isExternalStorageReadable()) {
            // only readable
        } else {
            // not accessible
        }
    }

    public ApiService getApiService() {
        return ServiceGenerator.createService(ApiService.class, accessToken, this, this, true);
    }

    public ApiService getApiServiceDisableRedirectActivity() {
        return ServiceGenerator.createService(ApiService.class, accessToken, this, this, false);
    }

    public ApiServiceCantina getApiServiceCantina() {
        return ServiceGenerator.createService(ApiServiceCantina.class, null, this, this, false);
    }

    /**
     * Method to init some data for user (/me, cursus, campus).
     *
     * @return status
     */
    public boolean initCache(boolean forceAPI) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        ApiService api = getApiService();

        String login = sharedPreferences.getString(API_ME_LOGIN, "");

        if (login.isEmpty() || !CacheUsers.isCached(cacheSQLiteHelper, login) || forceAPI) {
            me = Users.me(api);
            if (me != null) {
                CacheUsers.put(cacheSQLiteHelper, me);
                editor.putString(API_ME_LOGIN, me.login);
            }
        } else
            me = CacheUsers.get(cacheSQLiteHelper, login);

        if (me == null)
            return false;
        else {
            cursus = me.cursusUsers;
            initFirebase();

            CacheCursus.getAllowInternet(cacheSQLiteHelper, this);
            CacheCampus.getAllowInternet(cacheSQLiteHelper, this);
            CacheTags.getAllowInternet(cacheSQLiteHelper, this);
            //TODO: add integration to force use API with a cache manager in the UI !!
            editor.apply();
            return true;
        }
    }

    void initFirebase() {
        try {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            if (me != null)
                firebaseRefFriends = database.getReference("users").child(me.login).child("friends");
        } catch (IllegalStateException e) {
            Log.e("Firebase", "Fail to init friends with firebase");
        }
    }

    public void logout() {
        me = null;
        accessToken = null;
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(API_ME_LOGIN);
        editor.apply();
        Token.removeToken(this);
    }

    public void logoutAndRedirect() {
        logout();

        Intent i = new Intent(this, MainActivity.class);
        try {
            startActivity(i);
        } catch (AndroidRuntimeException e) {
            e.printStackTrace();
        }

    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

}
