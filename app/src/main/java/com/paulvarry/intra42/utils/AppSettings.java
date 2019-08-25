package com.paulvarry.intra42.utils;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;

import com.paulvarry.intra42.AppClass;
import com.paulvarry.intra42.R;
import com.paulvarry.intra42.api.model.Coalitions;
import com.paulvarry.intra42.api.model.Users;

/**
 * This class is a interface for app Settings set with {@link com.paulvarry.intra42.activities.SettingsActivity SettingsActivity}.
 * <p>
 * Default value for list settings:
 * <p>
 * -1 : Nothing selected or error.
 * <p>
 * 0 : No specific element selected, all.
 */
public class AppSettings {

    final static String PREFERENCE_INTRODUCTION_FINISHED = "introduction_finished";

    public static SharedPreferences getSharedPreferences(Context context) {
        if (context == null)
            return null;
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    /**
     * Return the forced Campus if is set or the main Campus of the user logged.
     *
     * @param app AppClass.
     * @return The id of a Campus.
     * @see Users#getCampusUsersToDisplay(Context)
     */
    public static int getAppCampus(AppClass app) {
        int forced = Advanced.getContentForceCampus(app);
        if (forced > 0)
            return forced;
        return getUserCampus(app);
    }

    /**
     * Return the main Campus of the user logged.
     *
     * @param app AppClass.
     * @return The id of a Campus.
     * @see Users#getCampusUsersToDisplay(Context)
     */
    public static int getUserCampus(AppClass app) {
        if (app.me != null)
            return app.me.getCampusUsersToDisplayID(app);
        return -1;
    }

    /**
     * Return the forced Cursus if is set or the main Cursus of the user logged.
     *
     * @param app AppClass.
     * @return The id of a Cursus.
     * @see Users#getCursusUsersToDisplay(Context)
     */
    public static int getAppCursus(AppClass app) {
        int forced = Advanced.getContentForceCursus(app);
        if (forced > 0)
            return forced;
        return getUserCursus(app);
    }

    /**
     * Return the main Cursus of the user logged.
     *
     * @param app AppClass.
     * @return The id of a Cursus.
     * @see Users#getCursusUsersToDisplay(Context)
     */
    public static int getUserCursus(AppClass app) {
        if (app.me != null)
            return app.me.getCursusUsersToDisplayID(app);
        return -1;
    }

    public static boolean getIntroductionFinished(Context context) {
        if (context == null)
            return true;
        else
            return getIntroductionFinished(getSharedPreferences(context));
    }

    public static boolean getIntroductionFinished(SharedPreferences settings) {
        return settings.getBoolean(PREFERENCE_INTRODUCTION_FINISHED, false);
    }

    public static void setIntroductionFinished(Context context, boolean activated) {
        if (context != null)
            setIntroductionFinished(getSharedPreferences(context), activated);
    }

    public static void setIntroductionFinished(SharedPreferences settings, boolean activated) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(PREFERENCE_INTRODUCTION_FINISHED, activated);
        editor.apply();
    }

    public static class Advanced {

        public static final String PREFERENCE_ADVANCED_ALLOW_ADVANCED = "switch_preference_advanced_allow_beta";
        public static final String PREFERENCE_ADVANCED_ALLOW_DATA = "switch_preference_advanced_allow_advanced_data";
        public static final String PREFERENCE_ADVANCED_ALLOW_MARKDOWN = "switch_preference_advanced_allow_markdown_renderer";
        public static final String PREFERENCE_ADVANCED_ALLOW_PAST_EVENTS = "switch_preference_advanced_allow_past_events";
        public static final String PREFERENCE_ADVANCED_ALLOW_SAVE_LOGS = "switch_preference_advanced_allow_save_logs_on_file";
        public static final String PREFERENCE_ADVANCED_FORCE_CURSUS = "list_preference_advanced_force_cursus";
        public static final String PREFERENCE_ADVANCED_FORCE_CAMPUS = "list_preference_advanced_force_campus";

        public static boolean getAllowAdvanced(Context context) {
            return context != null && getAllowAdvanced(getSharedPreferences(context));
        }

        public static boolean getAllowAdvanced(SharedPreferences settings) {
            return settings.getBoolean(PREFERENCE_ADVANCED_ALLOW_ADVANCED, false);
        }

        public static void setAllowAdvanced(Context context, boolean activated) {
            if (context != null)
                setAllowAdvanced(getSharedPreferences(context), activated);
        }

        public static void setAllowAdvanced(SharedPreferences settings, boolean activated) {
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(PREFERENCE_ADVANCED_ALLOW_ADVANCED, activated);
            editor.apply();
        }

        // advanced data
        public static boolean getAllowAdvancedData(Context context) {
            return context != null && getAllowAdvancedData(getSharedPreferences(context));
        }

        public static boolean getAllowAdvancedData(SharedPreferences settings) {
            return getAllowAdvanced(settings) && settings.getBoolean(PREFERENCE_ADVANCED_ALLOW_DATA, false);
        }

        // markdown renderer
        public static boolean getAllowMarkdownRenderer(Context context) {
            if (context == null)
                return false;
            return getAllowMarkdownRenderer(getSharedPreferences(context));
        }

        public static boolean getAllowMarkdownRenderer(SharedPreferences settings) {
            if (getAllowAdvanced(settings))
                return settings.getBoolean(PREFERENCE_ADVANCED_ALLOW_MARKDOWN, true);
            else
                return true;
        }

        // friends
        public static boolean getAllowPastEvents(Context context) {
            return context != null && getAllowPastEvents(getSharedPreferences(context));
        }

        public static boolean getAllowPastEvents(SharedPreferences settings) {
            if (getAllowAdvanced(settings))
                return settings.getBoolean(PREFERENCE_ADVANCED_ALLOW_PAST_EVENTS, false);
            else
                return false;
        }

        // save logs
        public static boolean getAllowSaveLogs(Context context) {
            return context != null && getAllowSaveLogs(getSharedPreferences(context));
        }


        public static boolean getAllowSaveLogs(SharedPreferences settings) {
            if (getAllowAdvanced(settings))
                return settings.getBoolean(PREFERENCE_ADVANCED_ALLOW_SAVE_LOGS, true);
            else
                return false;
        }

        public static void setAllowSaveLogs(Context context, boolean activated) {
            if (context != null)
                setAllowSaveLogs(getSharedPreferences(context), activated);
        }

        public static void setAllowSaveLogs(SharedPreferences settings, boolean activated) {
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(PREFERENCE_ADVANCED_ALLOW_SAVE_LOGS, activated);
            editor.apply();
        }

        // force campus
        public static int getContentForceCampus(Context context) {
            if (context == null)
                return -1;
            else
                return getContentForceCampus(getSharedPreferences(context));
        }

        public static int getContentForceCampus(SharedPreferences settings) {
            if (getAllowAdvanced(settings))
                return Integer.parseInt(settings.getString(PREFERENCE_ADVANCED_FORCE_CAMPUS, "-1"));
            else
                return -1;
        }

        // force cursus
        public static int getContentForceCursus(Context context) {
            if (context == null)
                return -1;
            else
                return getContentForceCursus(getSharedPreferences(context));
        }

        public static int getContentForceCursus(SharedPreferences settings) {
            if (getAllowAdvanced(settings))
                return Integer.parseInt(settings.getString(PREFERENCE_ADVANCED_FORCE_CURSUS, "-1"));
            else
                return -1;
        }
    }

    static public class Notifications {

        public static final String ENABLE_NOTIFICATIONS = "notifications_allow";
        public static final String FREQUENCY = "notifications_frequency";
        public static final String CHECKBOX_EVENTS = "check_box_preference_notifications_events";
        public static final String CHECKBOX_SCALES = "check_box_preference_notifications_scales";
        public static final String CHECKBOX_ANNOUNCEMENTS = "check_box_preference_notifications_announcements";

        public static final String ENABLE_CALENDAR = "switch_preference_enable_calendar";
        public static final String SELECTED_CALENDAR = "sync_events_calendars";

        public static boolean permissionCalendarEnable(Context context) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED;
        }

        public static boolean getNotificationsAllow(Context context) {
            return context != null && getNotificationsAllow(getSharedPreferences(context));
        }

        public static boolean getNotificationsAllow(SharedPreferences settings) {
            return settings.getBoolean(ENABLE_NOTIFICATIONS, true);
        }

        public static void setNotificationsAllow(Context context, boolean allow) {
            SharedPreferences sharedPreferences = getSharedPreferences(context);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(ENABLE_NOTIFICATIONS, allow);
            editor.apply();
        }

        public static int getNotificationsFrequency(Context context) {
            if (context != null)
                return getNotificationsFrequency(getSharedPreferences(context));
            return -1;
        }

        public static int getNotificationsFrequency(SharedPreferences settings) {
            return Integer.parseInt(settings.getString(FREQUENCY, "60"));
        }

        public static boolean getNotificationsEvents(Context context) {
            return context != null && getNotificationsEvents(getSharedPreferences(context));
        }

        public static boolean getNotificationsEvents(SharedPreferences settings) {
            return getNotificationsAllow(settings) && settings.getBoolean(CHECKBOX_EVENTS, true);
        }

        public static boolean getNotificationsScales(Context context) {
            return context != null && getNotificationsScales(getSharedPreferences(context));
        }

        public static boolean getNotificationsScales(SharedPreferences settings) {
            return getNotificationsAllow(settings) && settings.getBoolean(CHECKBOX_SCALES, true);
        }

        /**
         * Return true when calendar sync is enable. This will check if calendar primary option is
         * enable in prefs and if this app have permissions for the calendar.
         *
         * @param context THe context.
         * @return Boolean
         */
        public static boolean getCalendarSyncEnable(Context context) {
            return getCalendarSyncEnableNoPermissionCheck(context)
                    && permissionCalendarEnable(context);
        }

        /**
         * Return true when calendar sync is enable. This will check if calendar primary option is
         * enable in prefs.
         *
         * @param context THe context.
         * @return Boolean
         */
        public static boolean getCalendarSyncEnableNoPermissionCheck(Context context) {
            return context != null
                    && getSharedPreferences(context).getBoolean(ENABLE_CALENDAR, false);
        }

        /**
         * Set calendar primary param without check permission requirements. For better usability :
         * {@link Calendar#setEnableCalendarWithAutoSelect(Context, boolean)}
         *
         * @param context Context.
         * @param enable  value to set
         */
        public static void setCalendarSyncEnable(Context context, boolean enable) {
            SharedPreferences sharedPreferences = getSharedPreferences(context);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(ENABLE_CALENDAR, enable);
            editor.apply();
        }

        /**
         * Get the id of the selected calendar of the phone to puts events.
         *
         * @param context The context.
         * @return Id of the selected calendar, -1 otherwise.
         */
        public static int getSelectedCalendar(Context context) {
            if (context == null)
                return -1;
            return getSelectedCalendar(getSharedPreferences(context));
        }

        /**
         * Get the id of the selected calendar of the phone to puts events.
         *
         * @param settings Prefs.
         * @return Id of the selected calendar, -1 otherwise.
         */
        public static int getSelectedCalendar(SharedPreferences settings) {
            return Integer.decode(settings.getString(SELECTED_CALENDAR, "-1"));
        }

        /**
         * Set the selected calendar.
         *
         * @param context  Context.
         * @param calendar Id of the selected calendar of the phone.
         */
        public static void setCalendarSelected(Context context, int calendar) {
            if (calendar == -1)
                return;
            SharedPreferences sharedPreferences = getSharedPreferences(context);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(SELECTED_CALENDAR, String.valueOf(calendar));
            editor.apply();
        }

        public static boolean getNotificationsAnnouncements(Context context) {
            return context != null && getNotificationsAnnouncements(getSharedPreferences(context));
        }

        public static boolean getNotificationsAnnouncements(SharedPreferences settings) {
            return settings.getBoolean(CHECKBOX_SCALES, false);
        }
    }

    static public class Theme {

        public static final String THEME = "list_preference_theme";
        public static final String ACTIONBAR_BACKGROUND = "switch_theme_actionbar_enable_background";
        public static final String BRIGHTNESS = "list_preference_theme_brightness";

        public static boolean getActionBarBackgroundEnable(Context context) {
            SharedPreferences preferences = getSharedPreferences(context);
            return preferences.getBoolean(ACTIONBAR_BACKGROUND, true);
        }

        public static EnumBrightness getBrightness(Context context) {
            if (context == null)
                return EnumBrightness.SYSTEM;
            return getBrightness(getSharedPreferences(context));
        }

        public static EnumBrightness getBrightness(SharedPreferences preferences) {
            try {
                return EnumBrightness.valueOf(preferences.getString(BRIGHTNESS, EnumBrightness.SYSTEM.name()));
            } catch (java.lang.IllegalArgumentException e) {
                return EnumBrightness.SYSTEM;
            }
        }

        public static void setBrightness(Context context, EnumBrightness selection) {
            SharedPreferences.Editor preferences = getSharedPreferences(context).edit();
            preferences.putString(BRIGHTNESS, selection.name());
            preferences.apply();
        }

        public static EnumTheme getEnumTheme(Context context) {
            if (context != null)
                return getEnumTheme(getSharedPreferences(context));
            return EnumTheme.DEFAULT;
        }

        public static EnumTheme getEnumTheme(SharedPreferences preferences) {
            String string = preferences.getString(THEME, "default");

            EnumTheme enumTheme;
            switch (string) {
                case "red":
                    enumTheme = EnumTheme.RED;
                    break;
                case "purple":
                    enumTheme = EnumTheme.PURPLE;
                    break;
                case "blue":
                    enumTheme = EnumTheme.BLUE;
                    break;
                case "green":
                    enumTheme = EnumTheme.GREEN;
                    break;
                default:
                    enumTheme = EnumTheme.DEFAULT;
            }

            return enumTheme;
        }

        public static EnumTheme getEnumTheme(Context context, @Nullable Users user) {
            EnumTheme theme = getEnumTheme(context);

            if (theme == EnumTheme.DEFAULT &&
                    user != null && user.coalitions != null && user.coalitions.size() > 0) {

                EnumTheme themeTmp = null;
                Coalitions coalitions = user.coalitions.get(0);
                switch (coalitions.id) {
                    case 1:
                        themeTmp = EnumTheme.BLUE;
                        break;
                    case 2:
                        themeTmp = EnumTheme.GREEN;
                        break;
                    case 3:
                        themeTmp = EnumTheme.PURPLE;
                        break;
                    case 4:
                        themeTmp = EnumTheme.RED;
                        break;
                }
                if (themeTmp != null) {
                    theme = themeTmp;
                }
            }

            return theme;
        }

        public enum EnumTheme {
            DEFAULT("default", R.string.pref_theme_list_titles_default),
            RED("red", R.string.pref_theme_list_titles_red_the_order),
            PURPLE("purple", R.string.pref_theme_list_titles_purple_the_assembly),
            BLUE("blue", R.string.pref_theme_list_titles_blue_the_federation),
            GREEN("green", R.string.pref_theme_list_titles_green_the_alliance);

            private String key;
            private int name;

            EnumTheme(String key, @StringRes int name) {

                this.key = key;
                this.name = name;
            }

            public int getName() {
                return name;
            }
        }

        public enum EnumBrightness {
            SYSTEM,
            DARK,
            LIGHT
        }
    }

}
