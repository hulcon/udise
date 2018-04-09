package in.hulum.udise.utils;

/**
 * Created by Irshad on 03-04-2018.
 */

public class SchoolReportsConstants {
    public static final String SHARED_PREFERENCES_FILE = "in.hulum.udise.sharedprefs";
    public static final String SHARED_PREFERENCES_NAVIGATION_DRAWER_USER_TYPE_STRING = "in.hulum.udise.sharedprefs.keys.USER_TYPE_STRING";
    public static final String SHARED_PREFERENCES_NAVIGATION_DRAWER_SUBTITLE = "in.hulum.udise.sharedprefs.keys.DRAWER_SUBTITLE";

    public static final int MODEL_TYPE_NATIONAL = 100;
    public static final int MODEL_TYPE_STATE = 200;
    public static final int MODEL_TYPE_DISTRICT = 300;
    public static final int MODEL_TYPE_ZONE = 400;
    public static final int MODEL_TYPE_CLUSTER = 500;
    public static final int MODEL_TYPE_ASSEMBLY_CONSTITUENCY = 600;

    public static final int ASSEMBLY_CONSTITUENCY_PARENT_STATE = 700;
    public static final int ASSEMBLY_CONSTITUENCY_PARENT_DISTRICT = 800;
    public static final int ASSEMBLY_CONSTITUENCY_PARENT_ZONE = 900;

    public static final int MODEL_TYPE_STATE_WISE_LIST = 10;
    public static final int MODEL_TYPE_DISTRICT_WISE_LIST = 20;
    public static final int MODEL_TYPE_ZONE_WISE_LIST = 30;
    public static final int MODEL_TYPE_CLUSTER_WISE_LIST = 40;
    public static final int MODEL_TYPE_ASSEMBLY_CONSTITUENCY_WISE_LIST_FOR_STATE = 50;
    public static final int MODEL_TYPE_ASSEMBLY_CONSTITUENCY_WISE_LIST_FOR_DISTRICT = 60;
    public static final int MODEL_TYPE_ASSEMBLY_CONSTITUENCY_WISE_LIST_FOR_ZONE = 70;



    public static final int PRIMARY_SCHOOL = 1000;
    public static final int MIDDLE_SCHOOL = 2000;
    public static final int HIGH_SCHOOL = 3000;
    public static final int HIGHER_SECONDARY_SCHOOL = 4000;

    public static final int FLAG_ITEM_IS_NATIONAL_SUMMARY = 10;
    public static final int FLAG_ITEM_IS_STATE_SUMMARY = 20;
    public static final int FLAG_ITEM_IS_DISTRICT_SUMMARY = 30;
    public static final int FLAG_ITEM_IS_ZONE_SUMMARY = 40;
    public static final int FLAG_ITEM_IS_CLUSTER_SUMMARY = 50;
    public static final int FLAG_ITEM_IS_ASSEMBLY_CONSTITUENCY_SUMMARY = 60;

    public static final int FLAG_ITEM_IS_MANAGEMENT_DETAIL = 90;

    public static final int REPORT_DISPLAY_LEVEL_STATEWISE = 110;
    public static final int REPORT_DISPLAY_LEVEL_DISTRICTWISE = 120;
    public static final int REPORT_DISPLAY_LEVEL_ZONEWISE = 130;
    public static final int REPORT_DISPLAY_LEVEL_CLUSTERWISE = 140;
    public static final int REPORT_DISPLAY_LEVEL_TAKE_NO_ACTION = 150;
    public static final int REPORT_DISPLAY_LEVEL_ASSEMBLY_CONSTITUENCY_WISE = 160;

    public static final int REPORT_DISPLAY_NATIONAL_SUMMARY = 210;
    public static final int REPORT_DISPLAY_STATE_SUMMARY =220;
    public static final int REPORT_DISPLAY_DISTRICT_SUMMARY = 230;
    public static final int REPORT_DISPLAY_ZONE_SUMMARY = 240;
    public static final int REPORT_DISPLAY_CLUSTER_SUMMARY = 250;
    public static final int REPORT_DISPLAY_ASSEMBLY_CONSTITUENCY_SUMMARY_WITH_PARENT_STATE = 260;
    public static final int REPORT_DISPLAY_ASSEMBLY_CONSTITUENCY_SUMMARY_WITH_PARENT_DISTRICT = 270;
    public static final int REPORT_DISPLAY_ASSEMBLY_CONSTITUENCY_SUMMARY_WITH_PARENT_ZONE = 280;
    public static final int REPORT_DISPLAY_INVALID = -1;

    public static final String EXTRA_PARAM_KEY_REPORT_DISPLAY_SUMMARY_TYPE = "in.hulum.udise.extra.REPORT_DISPLAY_SUMMARY";
    public static final String EXTRA_PARAM_KEY_REPORT_DISPLAY_LEVEL_WISE_TYPE = "in.hulum.udise.extra.REPORT_DISPLAY_LEVEL_WISE_TYPE";

    public static final String EXTRA_KEY_CODE_STATE_DISTRICT_ZONE_CLUSTER = "in.hulum.udise.extra.DISTRICT_ZONE_CLUSTER_CODE";
    public static final String EXTRA_KEY_NAME_STATE_DISTRICT_ZONE_CLUSTER = "in.hulum.udise.extra.STATE_DISTRICT_ZONE_CLUSTER_NAME";
    public static final String EXTRA_KEY_PARENT_STATE_DISTRICT_OR_ZONE_CODE = "in.hulum.udise.extra.PARENT_CODE";
    public static final String EXTRA_KEY_ACADEMIC_YEAR = "in.hulum.udise.extra.ACADEMIC_YEAR";
}
