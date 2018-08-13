package com.paulvarry.intra42.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.paulvarry.intra42.AppClass;
import com.paulvarry.intra42.api.model.AccessToken;
import com.paulvarry.intra42.api.model.EventsUsers;
import com.paulvarry.intra42.api.model.UsersLTE;

import java.io.IOException;

import okhttp3.Request;
import okhttp3.ResponseBody;
import retrofit2.Response;

public class Analytics {

    public static final String EVENT_ERROR_API_42TOOLS = "error_api_42tools";
    public static final String USER_PROPERTY_APP_THEME = "app_theme";
    public static final String USER_PROPERTY_APP_THEME_DARK = "app_theme_dark";

    private static final String EVENT_LOG_USER_ID = "user_id";
    private static final String EVENT_LOG_EVENT_ID = "event_id";
    private static final String EVENT_LOG_SOURCE = "source";
    private static final String EVENT_LOG_FRIEND_ID = "friend_id";
    private static final String EVENT_LOG_SLOTS_COUNT = "slots_count";

    private static final String EVENT_LOG_API_URL = "api_url";
    private static final String EVENT_LOG_API_CODE = "api_status_code";
    private static final String EVENT_LOG_API_MESSAGE = "api_message";
    private static final String EVENT_LOG_API_ERROR_BODY = "api_error_body";

    private static FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(AppClass.instance());

    public static void settingUpdated(Context context) {
        FirebaseAnalytics mFirebaseAnalytics;
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);

        SharedPreferences preferences = AppSettings.getSharedPreferences(context);

        mFirebaseAnalytics.setUserProperty(USER_PROPERTY_APP_THEME, AppSettings.Theme.getEnumTheme(preferences).name());
        if (AppSettings.Theme.getDarkThemeEnable(preferences))
            mFirebaseAnalytics.setUserProperty(USER_PROPERTY_APP_THEME_DARK, "DARK");
        else
            mFirebaseAnalytics.setUserProperty(USER_PROPERTY_APP_THEME_DARK, "LIGHT");
    }

    public static void slotSave(SlotsTools.SlotsGroup slotsGroup) {
        Bundle params = new Bundle();
        if (slotsGroup.group != null)
            params.putInt(EVENT_LOG_SLOTS_COUNT, slotsGroup.group.size());
        firebaseAnalytics.logEvent("slot_save", params);
    }

    public static void slotCreate(SlotsTools.SlotsGroup slotsGroup) {
        Bundle params = new Bundle();
        if (slotsGroup.group != null)
            params.putInt(EVENT_LOG_SLOTS_COUNT, slotsGroup.group.size());
        firebaseAnalytics.logEvent("slot_create", params);
    }

    public static void slotDelete(SlotsTools.SlotsGroup slotsGroup) {
        Bundle params = new Bundle();
        if (slotsGroup.group != null)
            params.putInt(EVENT_LOG_SLOTS_COUNT, slotsGroup.group.size());
        params.putBoolean("is_booked", slotsGroup.isBooked);
        firebaseAnalytics.logEvent("slot_delete", params);
    }

    public static void eventSubscribe(int eventId, int userId, EventSource source) {
        Bundle params = new Bundle();
        params.putString(EVENT_LOG_EVENT_ID, String.valueOf(eventId));
        params.putString(EVENT_LOG_USER_ID, String.valueOf(userId));
        params.putString(EVENT_LOG_SOURCE, source.name());
        firebaseAnalytics.logEvent("event_subscribe", params);
    }

    public static void eventUnsubscribe(EventsUsers event, EventSource source) {
        eventUnsubscribe(event.eventId, event.user_id, source);
    }

    public static void eventUnsubscribe(int eventId, int userId, EventSource source) {
        Bundle params = new Bundle();
        params.putString(EVENT_LOG_EVENT_ID, String.valueOf(eventId));
        params.putString(EVENT_LOG_USER_ID, String.valueOf(userId));
        params.putString(EVENT_LOG_SOURCE, source.name());
        firebaseAnalytics.logEvent("event_unsubscribe", params);
    }

    public static void friendAdd(UsersLTE friend, UsersLTE me) {
        Bundle params = new Bundle();
        params.putString(EVENT_LOG_FRIEND_ID, String.valueOf(friend.id));
        params.putString(EVENT_LOG_USER_ID, String.valueOf(me.id));
        firebaseAnalytics.logEvent("friend_add", params);
    }

    public static void friendRemove(UsersLTE friend, UsersLTE me) {
        Bundle params = new Bundle();
        params.putString(EVENT_LOG_FRIEND_ID, String.valueOf(friend.id));
        params.putString(EVENT_LOG_USER_ID, String.valueOf(me.id));
        firebaseAnalytics.logEvent("friend_remove", params);
    }

    public static void signInAttempt() {
        firebaseAnalytics.logEvent("sign_in_attempt", null);
    }

    public static void signInHaveCode(String referrer) {
        Bundle params = new Bundle();
        params.putString("sign_in_referrer", referrer);
        firebaseAnalytics.logEvent("sign_in_have_code", params);
    }

    public static void signInSuccess() {
        firebaseAnalytics.logEvent("sign_in_success", null);
    }

    public static void signInError(Response<AccessToken> response) {
        Bundle params = new Bundle();
        params.putString(EVENT_LOG_API_CODE, String.valueOf(response.code()));
        params.putString(EVENT_LOG_API_MESSAGE, response.message());
        try {
            if (response.errorBody() != null) {
                params.putString(EVENT_LOG_API_ERROR_BODY, response.errorBody().string());
            }
        } catch (IOException e) {
            params.putString(EVENT_LOG_API_ERROR_BODY, e.getMessage());
        }
        firebaseAnalytics.logEvent("sign_in_error", params);
    }

    public static void signInError(Throwable t) {
        Bundle params = new Bundle();
        params.putString(EVENT_LOG_API_ERROR_BODY, t.getMessage());
        firebaseAnalytics.logEvent("sign_in_error", params);
    }

    public static void shortcutFriends() {
        firebaseAnalytics.logEvent("shortcut_friends", null);
    }

    public static void shortcutGalaxy() {
        firebaseAnalytics.logEvent("shortcut_galaxy", null);
    }

    public static void shortcutClusterMap() {
        firebaseAnalytics.logEvent("shortcut_cluster_map", null);
    }

    public static void setBrightness(boolean isDark) {
        Bundle params = new Bundle();
        params.putString("brightness", isDark ? "DARK" : "LIGHT");
        firebaseAnalytics.logEvent("brightness_switched_menu", params);
    }

    public static void apiCall(Request request, okhttp3.Response response) {
        Bundle params = new Bundle();
        params.putString(EVENT_LOG_API_URL, request.method() + " " + request.url().host() + request.url().encodedPath());
        params.putString(EVENT_LOG_API_CODE, String.valueOf(response.code()));
        params.putString(EVENT_LOG_API_MESSAGE, response.message());

        ResponseBody body = response.body();
        if (!response.isSuccessful() && body != null) {
            try {
                params.putString(EVENT_LOG_API_ERROR_BODY, body.string());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        firebaseAnalytics.logEvent("api_call", params);
    }

    public static void search(String kind, String text) {
        Bundle params = new Bundle();
        if (kind != null)
            params.putString("kind", kind);
        params.putString("query", text);
        firebaseAnalytics.logEvent("search", params);
    }

    public enum EventSource {
        NOTIFICATION, APPLICATION
    }
}
