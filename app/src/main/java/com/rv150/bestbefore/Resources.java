package com.rv150.bestbefore;

/**
 * Created by Rudnev on 09.09.2016.
 */
public final class Resources {
    public static final String PREF_NEED_RATE = "need_rate";
    public static final String PREF_ALARM_SET = "alarm_set_flag";
    public static final String PREF_HOURS_NEEDED = "hours_needed_for_rate";
    public static final String PREF_SHOW_WELCOME_SCREEN = "showWelcomeScreen";
    public static final String PREF_SHOW_HELP_AFTER_FIRST_ADD = "showHelpForNewDesign";
    public static final String PREF_SHOW_SYNC_WARNING = "show_sync_warning";
    public static final String PREF_INSTALL_DAY = "install_day";
    public static final String PREF_INSTALL_MONTH = "install_month";
    public static final String PREF_INSTALL_YEAR = "install_year";
    public static final String PREF_HOW_TO_SORT = "how_to_sort";
    public static final String PREF_FIRST_HOUR = "first_hour";
    public static final String PREF_FIRST_MINUTE = "first_minute";
    public static final String PREF_SECOND_HOUR = "second_hour";
    public static final String PREF_SECOND_MINUTE = "second_minute";
    public static final String PREF_THIRD_HOUR = "third_hour";
    public static final String PREF_THIRD_MINUTE = "third_minute";
    public static final String PREF_FOURTH_HOUR = "fourth_hour";
    public static final String PREF_FOURTH_MINUTE = "fourth_minute";
    public static final String PREF_FIFTH_HOUR = "fifth_hour";
    public static final String PREF_FIFTH_MINUTE = "fifth_minute";
    public static final String PREF_FIRST_NOTIF = "last_day";
    public static final String PREF_SECOND_NOTIF = "day_before";
    public static final String PREF_THIRD_NOTIF = "three_days";
    public static final String PREF_FOURH_NOTIF = "four_days";
    public static final String PREF_FIFTH_NOTIF = "five_days";
    public static final String PREF_DAYS_IN_FIRST_NOTIF = "days_in_first";
    public static final String PREF_DAYS_IN_SECOND_NOTIF = "days_in_second";
    public static final String PREF_DAYS_IN_THIRD_NOTIF = "days_in_third";
    public static final String PREF_DAYS_IN_FOURTH_NOTIF = "days_in_fourth";
    public static final String PREF_DAYS_IN_FIFTH_NOTIF = "days_in_fifth";
    public static final String PREF_USE_GROUPS = "use_groups";
    public static final String WHATS_NEW_34 = "whats_new_in_34";
    public static final String STANDART = "STANDART";
    public static final String SPOILED_TO_FRESH = "SPOILED_TO_FRESH";
    public static final String FRESH_TO_SPOILED = "FRESH_TO_SPOILED";
    public static final String BY_NAME = "BY_NAME";
    public static final String SHOW_OVERDUE_DIALOG = "needShowOverdue";
    public static final String GROUP_ID = "groupName";
    public static final String STATUS = "status";
    public static final String STATUS_ADD = "status_add";
    public static final String STATUS_EDIT = "status_edit";
    public static final int RC_ADD_ACTIVITY = 0;
    public static final int RC_SIGN_IN = 1;
    public static final int RC_DRIVE_API = 2;
    public static final int RC_SETTINGS = 3;
    public static final int RC_CHOOSE_FILE = 4;
    public static final int RC_CAMERA = 5;
    public static final int RC_DIRECTORY_PICKER_FILE = 6;
    public static final int RC_DIRECTORY_PICKER_EXCEL = 7;
    public static final int RESULT_ADD = 1;
    public static final int RESULT_MODIFY = 2;
    public static final String CONGRATULATION = "happy_new_2017_year";
    public static final String WHATS_NEW_25_ADD = "whats_new_25";
    public static final long ID_MAIN_GROUP = 99990;
    public static final String MAIN_GROUP_NAME = "mainGroupName";
    public static final long ID_FOR_SETTINGS = 99991;
    public static final long ID_FOR_FEEDBACK = 99992;
    public static final long ID_FOR_ADD_GROUP = 99993;
    public static final long ID_FOR_OVERDUED = 99994;
    public static final long ID_FOR_TRASH = 99995;
    public static final long ID_FOR_BILLING = 99996;
    public static final String OVERDUED_GROUP_NAME = "overduedGroupName";
    public static final String NEED_MIGRATE = "needMigrate";
    public static final String LAST_RADIO_WAS_OKAY_BEFORE = "lastRadioWasOkayBefore";

    public enum Measures {
        PIECE("шт"),
        KG("кг"),
        G("г"),
        L("л"),
        ML("мл");
        private String text;
        Measures(String text) {
            this.text = text;
        }
        public String getText() {
            return this.text;
        }

        public static Measures fromString(String text) {
            if (text != null) {
                for (Measures b : Measures.values()) {
                    if (text.equalsIgnoreCase(b.text)) {
                        return b;
                    }
                }
            }
            return null;
        }
    }

    public static final String[] popularProducts = {
        "баранина",
                "бекон",
                "брокколи",
                "брынза",
                "буженина",
                "ветчина",
                "говядина",
                "икра",
                "икра красная",
                "икра кабачковая",
                "йогурт",
                "кетчуп",
                "кефир",
                "капуста",
                "капуста квашеная",
                "капуста цветная",
                "колбаса",
                "колбаса вареная",
                "колбаса сырокопченая",
                "колбаса варено-копченая",
                "колбаса докторская",
                "колбаса сервелат",
                "креветки",
                "крылышки",
                "кукуруза",
                "курица",
                "лосось",
                "макароны",
                "майонез",
                "маслины",
                "масло",
                "масло сливочное",
                "масло подсолнечное",
                "масло оливковое",
                "молоко",
                "мороженое",
                "мясо",
                "окунь",
                "окорок",
                "оливки",
                "осетр",
                "паштет",
                "пельмени",
                "печенье",
                "печень",
                "пицца",
                "рис",
                "рыба",
                "ряженка",
                "свинина",
                "селедка",
                "сливки",
                "сметана",
                "сок",
                "сок яблочный",
                "сок вишневый",
                "сок мультифруктовый",
                "сок апельсиновый",
                "сок виноградный",
                "сок томатный",
                "сок ананасовый",
                "сосиски",
                "судак",
                "скумбрия",
                "сыр",
                "сыр плавленый",
                "сырок глазированный",
                "творог",
                "творог обезжиренный",
                "творог полужирный",
                "творог жирный",
                "томатная паста",
                "телятина",
                "фарш",
                "хрен",
                "шампиньоны",
                "яйцо куриное",
                "яйцо перепелиное" };
}
