package in.hulum.udise.utils;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import in.hulum.udise.database.UdiseContract;
import in.hulum.udise.models.ManagementWiseSchoolSummaryModel;

/**
 * Created by Irshad on 03-04-2018.
 * This class contains several helper methods for generating reports related
 * to "Number of Schools"
 */

public class  SchoolReportsHelper {

    private static final String TAG = "SchoolReportsHelper";

    /**
     * This method generates summary of a district in a list of type {@link ManagementWiseSchoolSummaryModel}
     * The first item in the list contains the total number of Primary, Middle, High, and Higher Secondary
     * Schools in the whole country.
     * Subsequent items in the list contain number of Primary, Middle, High, and Higher Secondary Schools
     * for each management category. There is one item in the list per management category.
     *
     * So if a country contains 3 management categories, this list will contain 4 items,
     * one for the national summary and one for each management category
     * @param cursor Cursor containing data of the desired country for the desired academic year
     * @return a list of type {@link ManagementWiseSchoolSummaryModel}, the first entry in the list contains
     * the summary of the whole country and the subsequent items in the list contain management wise
     * number of schools, one per each management type.
     */
    public static List<ManagementWiseSchoolSummaryModel> nationalManagementWiseSummary(Cursor cursor){

        if(cursor==null){
            return null;
        }

        List<ManagementWiseSchoolSummaryModel> nationalSummaryList = new ArrayList<>();

        Set<String> schoolManagementSet = new HashSet<String>();
        ArrayList<ContentValues> managementInfoList = new ArrayList<ContentValues>();

        int indexColumnSchoolManagementCode = cursor.getColumnIndex(UdiseContract.RawData.COLUMN_SCHOOL_MANAGEMENT);
        int indexColumnSchoolManagementDescription = cursor.getColumnIndex(UdiseContract.RawData.COLUMN_SCHOOL_MANAGEMENT_DESCRIPTION);
        int indexColumnSchoolCategoryCode = cursor.getColumnIndex(UdiseContract.RawData.COLUMN_SCHOOL_CATEGORY);

        ManagementWiseSchoolSummaryModel nationalSummary = new ManagementWiseSchoolSummaryModel();
        nationalSummary.setModelType(SchoolReportsConstants.MODEL_TYPE_NATIONAL);
        nationalSummary.setFlagIsSummaryOrManagementDetail(SchoolReportsConstants.FLAG_ITEM_IS_NATIONAL_SUMMARY);
        nationalSummary.setManagementNameOrSummaryHeading("National Summary");

            /*
             * Iterate over all the rows of the cursor and
             * Create a list of distinct value of school managements
             */
        for(int index=0;index<cursor.getCount();index++) {
            cursor.moveToPosition(index);
            /*
             * Try adding school management values to the Set and
             * check if it succeeds. If the set accepts it (returns true) then it
             * is a distinct value. So store it in a Content Value List for later use.
             */
            if(schoolManagementSet.add(cursor.getString(indexColumnSchoolManagementDescription))){
                ContentValues managementInfo = new ContentValues();
                managementInfo.put(UdiseContract.RawData.COLUMN_SCHOOL_MANAGEMENT,cursor.getInt(indexColumnSchoolManagementCode));
                managementInfo.put(UdiseContract.RawData.COLUMN_SCHOOL_MANAGEMENT_DESCRIPTION,cursor.getString(indexColumnSchoolManagementDescription));
                /*Add the above value to an array list*/
                managementInfoList.add(managementInfo);
            }

            /*
             * Count the number of each category of schools for the
             * whole district to create a summary of the district
             */

            switch (determineSchoolType(cursor.getInt(indexColumnSchoolCategoryCode))){
                case SchoolReportsConstants.PRIMARY_SCHOOL:
                    nationalSummary.incrementPrimarySchools();
                    break;
                case SchoolReportsConstants.MIDDLE_SCHOOL:
                    nationalSummary.incrementMiddleSchools();
                    break;
                case SchoolReportsConstants.HIGH_SCHOOL:
                    nationalSummary.incrementHighSchools();
                    break;
                case SchoolReportsConstants.HIGHER_SECONDARY_SCHOOL:
                    nationalSummary.incrementHigherSecondarySchools();
                    break;
            }
        }

        /*
         * National summary is created. Make it the first item in the list
         */
        nationalSummaryList.add(nationalSummary);

        /*
         * Here we will count the number of primary, middle, high and higher secondary schools
         * for each management
         */
        for(int mgmtCounter=0;mgmtCounter<managementInfoList.size();mgmtCounter++){
            ManagementWiseSchoolSummaryModel managementWiseSchoolSummaryModel = new ManagementWiseSchoolSummaryModel();
            managementWiseSchoolSummaryModel.setModelType(SchoolReportsConstants.MODEL_TYPE_NATIONAL);
            managementWiseSchoolSummaryModel.setFlagIsSummaryOrManagementDetail(SchoolReportsConstants.FLAG_ITEM_IS_MANAGEMENT_DETAIL);

            managementWiseSchoolSummaryModel.setManagementNameOrSummaryHeading(managementInfoList.get(mgmtCounter).getAsString(UdiseContract.RawData.COLUMN_SCHOOL_MANAGEMENT_DESCRIPTION));

            for(int cursorRow=0;cursorRow<cursor.getCount();cursorRow++){

                cursor.moveToPosition(cursorRow);
                if(cursor.getInt(indexColumnSchoolManagementCode)==managementInfoList.get(mgmtCounter).getAsInteger(UdiseContract.RawData.COLUMN_SCHOOL_MANAGEMENT)){
                    int schoolType = determineSchoolType(cursor.getInt(indexColumnSchoolCategoryCode));
                    switch (schoolType){
                        case SchoolReportsConstants.PRIMARY_SCHOOL:
                            managementWiseSchoolSummaryModel.incrementPrimarySchools();
                            break;
                        case SchoolReportsConstants.MIDDLE_SCHOOL:
                            managementWiseSchoolSummaryModel.incrementMiddleSchools();
                            break;
                        case SchoolReportsConstants.HIGH_SCHOOL:
                            managementWiseSchoolSummaryModel.incrementHighSchools();
                            break;
                        case SchoolReportsConstants.HIGHER_SECONDARY_SCHOOL:
                            managementWiseSchoolSummaryModel.incrementHigherSecondarySchools();
                            break;
                    }
                }
            }
            /*
             * Add this management to list if and only if there are any schools of this management type
             * Otherwise just ignore this management type
             */
            if(managementWiseSchoolSummaryModel.getPrimarySchools()>0 || managementWiseSchoolSummaryModel.getMiddleSchools()>0 || managementWiseSchoolSummaryModel.getHighSchools()>0 || managementWiseSchoolSummaryModel.getHigherSecondarySchools()>0){
                nationalSummaryList.add(managementWiseSchoolSummaryModel);
                Log.d(TAG,"Entry Added!!");
            }
        }
        return nationalSummaryList;
    }



    /**
     * This method generates summary of a state in a list of type {@link ManagementWiseSchoolSummaryModel}
     * The first item in the list contains the total number of Primary, Middle, High, and Higher Secondary
     * Schools in the whole state.
     * Subsequent items in the list contain number of Primary, Middle, High, and Higher Secondary Schools
     * for each management category. There is one item in the list per management category.
     *
     * So if a district contains 3 management categories, this list will contain 4 items,
     * one for the district summary and one for each management category
     * @param cursor Cursor containing data of the desired state for the desired academic year
     * @param stateName name of the state
     * @return a list of type {@link ManagementWiseSchoolSummaryModel}, the first entry in the list contains
     * the summary of the whole state and the subsequent items in the list contain management wise
     * number of schools, one per each management type.
     */
    public static List<ManagementWiseSchoolSummaryModel> stateManagementWiseSummary(Cursor cursor, String stateName){

        if(cursor==null){
            return null;
        }

        List<ManagementWiseSchoolSummaryModel> stateSummaryList = new ArrayList<>();

        Set<String> schoolManagementSet = new HashSet<String>();
        ArrayList<ContentValues> managementInfoList = new ArrayList<ContentValues>();

        int indexColumnSchoolManagementCode = cursor.getColumnIndex(UdiseContract.RawData.COLUMN_SCHOOL_MANAGEMENT);
        int indexColumnSchoolManagementDescription = cursor.getColumnIndex(UdiseContract.RawData.COLUMN_SCHOOL_MANAGEMENT_DESCRIPTION);
        int indexColumnSchoolCategoryCode = cursor.getColumnIndex(UdiseContract.RawData.COLUMN_SCHOOL_CATEGORY);

        ManagementWiseSchoolSummaryModel stateSummary = new ManagementWiseSchoolSummaryModel();
        stateSummary.setModelType(SchoolReportsConstants.MODEL_TYPE_STATE);
        stateSummary.setFlagIsSummaryOrManagementDetail(SchoolReportsConstants.FLAG_ITEM_IS_STATE_SUMMARY);
        stateSummary.setStateName(stateName);
        stateSummary.setManagementNameOrSummaryHeading(stateName + " Summary");

            /*
             * Iterate over all the rows of the cursor and
             * Create a list of distinct value of school managements
             */
        for(int index=0;index<cursor.getCount();index++) {
            cursor.moveToPosition(index);
            /*
             * Try adding school management values to the Set and
             * check if it succeeds. If the set accepts it (returns true) then it
             * is a distinct value. So store it in a Content Value List for later use.
             */
            if(schoolManagementSet.add(cursor.getString(indexColumnSchoolManagementDescription))){
                ContentValues managementInfo = new ContentValues();
                managementInfo.put(UdiseContract.RawData.COLUMN_SCHOOL_MANAGEMENT,cursor.getInt(indexColumnSchoolManagementCode));
                managementInfo.put(UdiseContract.RawData.COLUMN_SCHOOL_MANAGEMENT_DESCRIPTION,cursor.getString(indexColumnSchoolManagementDescription));
                /*Add the above value to an array list*/
                managementInfoList.add(managementInfo);
            }

            /*
             * Count the number of each category of schools for the
             * whole state to create a summary of the state
             */

            switch (determineSchoolType(cursor.getInt(indexColumnSchoolCategoryCode))){
                case SchoolReportsConstants.PRIMARY_SCHOOL:
                    stateSummary.incrementPrimarySchools();
                    break;
                case SchoolReportsConstants.MIDDLE_SCHOOL:
                    stateSummary.incrementMiddleSchools();
                    break;
                case SchoolReportsConstants.HIGH_SCHOOL:
                    stateSummary.incrementHighSchools();
                    break;
                case SchoolReportsConstants.HIGHER_SECONDARY_SCHOOL:
                    stateSummary.incrementHigherSecondarySchools();
                    break;
            }
        }

        /*
         * Summary of the whole state is created. Make it the first item in the list
         */
        stateSummaryList.add(stateSummary);

        /*
         * Here we will count the number of primary, middle, high and higher secondary schools
         * for each management for the whole state
         */
        for(int mgmtCounter=0;mgmtCounter<managementInfoList.size();mgmtCounter++){
            ManagementWiseSchoolSummaryModel managementWiseSchoolSummaryModel = new ManagementWiseSchoolSummaryModel();
            managementWiseSchoolSummaryModel.setModelType(SchoolReportsConstants.MODEL_TYPE_STATE);
            managementWiseSchoolSummaryModel.setFlagIsSummaryOrManagementDetail(SchoolReportsConstants.FLAG_ITEM_IS_MANAGEMENT_DETAIL);
            managementWiseSchoolSummaryModel.setStateName(stateName);

            managementWiseSchoolSummaryModel.setManagementNameOrSummaryHeading(managementInfoList.get(mgmtCounter).getAsString(UdiseContract.RawData.COLUMN_SCHOOL_MANAGEMENT_DESCRIPTION));

            for(int cursorRow=0;cursorRow<cursor.getCount();cursorRow++){

                cursor.moveToPosition(cursorRow);
                if(cursor.getInt(indexColumnSchoolManagementCode)==managementInfoList.get(mgmtCounter).getAsInteger(UdiseContract.RawData.COLUMN_SCHOOL_MANAGEMENT)){
                    int schoolType = determineSchoolType(cursor.getInt(indexColumnSchoolCategoryCode));
                    switch (schoolType){
                        case SchoolReportsConstants.PRIMARY_SCHOOL:
                            managementWiseSchoolSummaryModel.incrementPrimarySchools();
                            break;
                        case SchoolReportsConstants.MIDDLE_SCHOOL:
                            managementWiseSchoolSummaryModel.incrementMiddleSchools();
                            break;
                        case SchoolReportsConstants.HIGH_SCHOOL:
                            managementWiseSchoolSummaryModel.incrementHighSchools();
                            break;
                        case SchoolReportsConstants.HIGHER_SECONDARY_SCHOOL:
                            managementWiseSchoolSummaryModel.incrementHigherSecondarySchools();
                            break;
                    }
                }
            }
            /*
             * Add this management to list if and only if there are any schools of this management type
             * Otherwise just ignore this management type
             */
            if(managementWiseSchoolSummaryModel.getPrimarySchools()>0 || managementWiseSchoolSummaryModel.getMiddleSchools()>0 || managementWiseSchoolSummaryModel.getHighSchools()>0 || managementWiseSchoolSummaryModel.getHigherSecondarySchools()>0){
                stateSummaryList.add(managementWiseSchoolSummaryModel);
                Log.d(TAG,"Entry Added!!");
            }
        }
        return stateSummaryList;
    }





    /**
     * This method generates summary of a district in a list of type {@link ManagementWiseSchoolSummaryModel}
     * The first item in the list contains the total number of Primary, Middle, High, and Higher Secondary
     * Schools in the whole district.
     * Subsequent items in the list contain number of Primary, Middle, High, and Higher Secondary Schools
     * for each management category. There is one item in the list per management category.
     *
     * So if a district contains 3 management categories, this list will contain 4 items,
     * one for the district summary and one for each management category
     * @param cursor Cursor containing data of the desired district for the desired academic year
     * @param districtCode code of the district for which summary needs to be generated
     * @param districtName name of the district
     * @return a list of type {@link ManagementWiseSchoolSummaryModel}, the first entry in the list contains
     * the summary of the whole district and the subsequent items in the list contain management wise
     * number of schools, one per each management type.
     */
    public static List<ManagementWiseSchoolSummaryModel> districtManagementWiseSummary(Cursor cursor,String districtCode, String districtName){

        if(cursor==null){
            return null;
        }

        List<ManagementWiseSchoolSummaryModel> districtSummaryList = new ArrayList<>();

        Set<String> schoolManagementSet = new HashSet<String>();
        ArrayList<ContentValues> managementInfoList = new ArrayList<ContentValues>();

        int indexColumnSchoolManagementCode = cursor.getColumnIndex(UdiseContract.RawData.COLUMN_SCHOOL_MANAGEMENT);
        int indexColumnSchoolManagementDescription = cursor.getColumnIndex(UdiseContract.RawData.COLUMN_SCHOOL_MANAGEMENT_DESCRIPTION);
        int indexColumnSchoolCategoryCode = cursor.getColumnIndex(UdiseContract.RawData.COLUMN_SCHOOL_CATEGORY);

        ManagementWiseSchoolSummaryModel districtSummary = new ManagementWiseSchoolSummaryModel();
        districtSummary.setModelType(SchoolReportsConstants.MODEL_TYPE_DISTRICT);
        districtSummary.setFlagIsSummaryOrManagementDetail(SchoolReportsConstants.FLAG_ITEM_IS_DISTRICT_SUMMARY);
        districtSummary.setDistrictCode(districtCode);
        districtSummary.setDistrictName(districtName);
        districtSummary.setManagementNameOrSummaryHeading("District " + districtName + " Summary");

            /*
             * Iterate over all the rows of the cursor and
             * Create a list of distinct value of school managements
             */
        for(int index=0;index<cursor.getCount();index++) {
            cursor.moveToPosition(index);
            /*
             * Try adding school management values to the Set and
             * check if it succeeds. If the set accepts it (returns true) then it
             * is a distinct value. So store it in a Content Value List for later use.
             */
            if(schoolManagementSet.add(cursor.getString(indexColumnSchoolManagementDescription))){
                ContentValues managementInfo = new ContentValues();
                managementInfo.put(UdiseContract.RawData.COLUMN_SCHOOL_MANAGEMENT,cursor.getInt(indexColumnSchoolManagementCode));
                managementInfo.put(UdiseContract.RawData.COLUMN_SCHOOL_MANAGEMENT_DESCRIPTION,cursor.getString(indexColumnSchoolManagementDescription));
                /*Add the above value to an array list*/
                managementInfoList.add(managementInfo);
            }

            /*
             * Count the number of each category of schools for the
             * whole district to create a summary of the district
             */

            switch (determineSchoolType(cursor.getInt(indexColumnSchoolCategoryCode))){
                case SchoolReportsConstants.PRIMARY_SCHOOL:
                    districtSummary.incrementPrimarySchools();
                    break;
                case SchoolReportsConstants.MIDDLE_SCHOOL:
                    districtSummary.incrementMiddleSchools();
                    break;
                case SchoolReportsConstants.HIGH_SCHOOL:
                    districtSummary.incrementHighSchools();
                    break;
                case SchoolReportsConstants.HIGHER_SECONDARY_SCHOOL:
                    districtSummary.incrementHigherSecondarySchools();
                    break;
            }
        }

        /*
         * Summary of the whole district is created. Make it the first item in the list
         */
        districtSummaryList.add(districtSummary);

        /*
         * Here we will count the number of primary, middle, high and higher secondary schools
         * for each management for the whole district
         */
        for(int mgmtCounter=0;mgmtCounter<managementInfoList.size();mgmtCounter++){
            ManagementWiseSchoolSummaryModel managementWiseSchoolSummaryModel = new ManagementWiseSchoolSummaryModel();
            managementWiseSchoolSummaryModel.setModelType(SchoolReportsConstants.MODEL_TYPE_DISTRICT);
            managementWiseSchoolSummaryModel.setFlagIsSummaryOrManagementDetail(SchoolReportsConstants.FLAG_ITEM_IS_MANAGEMENT_DETAIL);
            managementWiseSchoolSummaryModel.setDistrictCode(districtCode);
            managementWiseSchoolSummaryModel.setDistrictName(districtName);

            managementWiseSchoolSummaryModel.setManagementNameOrSummaryHeading(managementInfoList.get(mgmtCounter).getAsString(UdiseContract.RawData.COLUMN_SCHOOL_MANAGEMENT_DESCRIPTION));

            for(int cursorRow=0;cursorRow<cursor.getCount();cursorRow++){

                cursor.moveToPosition(cursorRow);
                if(cursor.getInt(indexColumnSchoolManagementCode)==managementInfoList.get(mgmtCounter).getAsInteger(UdiseContract.RawData.COLUMN_SCHOOL_MANAGEMENT)){
                    int schoolType = determineSchoolType(cursor.getInt(indexColumnSchoolCategoryCode));
                    switch (schoolType){
                        case SchoolReportsConstants.PRIMARY_SCHOOL:
                            managementWiseSchoolSummaryModel.incrementPrimarySchools();
                            break;
                        case SchoolReportsConstants.MIDDLE_SCHOOL:
                            managementWiseSchoolSummaryModel.incrementMiddleSchools();
                            break;
                        case SchoolReportsConstants.HIGH_SCHOOL:
                            managementWiseSchoolSummaryModel.incrementHighSchools();
                            break;
                        case SchoolReportsConstants.HIGHER_SECONDARY_SCHOOL:
                            managementWiseSchoolSummaryModel.incrementHigherSecondarySchools();
                            break;
                    }
                }
            }
            /*
             * Add this management to list if and only if there are any schools of this management type
             * Otherwise just ignore this management type
             */
            if(managementWiseSchoolSummaryModel.getPrimarySchools()>0 || managementWiseSchoolSummaryModel.getMiddleSchools()>0 || managementWiseSchoolSummaryModel.getHighSchools()>0 || managementWiseSchoolSummaryModel.getHigherSecondarySchools()>0){
                districtSummaryList.add(managementWiseSchoolSummaryModel);
                Log.d(TAG,"Entry Added!!");
            }
        }
        return districtSummaryList;
    }




    /**
     * This method generates summary of a zone in a list of type {@link ManagementWiseSchoolSummaryModel}
     * The first item in the list contains the total number of Primary, Middle, High, and Higher Secondary
     * Schools in the whole zone.
     * Subsequent items in the list contain number of Primary, Middle, High, and Higher Secondary Schools
     * for each management category. There is one item in the list per management category.
     *
     * So if a zone contains 3 management categories, this list will contain 4 items,
     * one for the zone summary and one for each management category
     * @param cursor Cursor containing data of the desired zone for the desired academic year
     * @param zoneCode code of the zone for which summary needs to be generated
     * @param zoneName name of the zone
     * @return a list of type {@link ManagementWiseSchoolSummaryModel}, the first entry in the list contains
     * the summary of the whole zone and the subsequent items in the list contain management wise
     * number of schools, one per each management type.
     */
    public static List<ManagementWiseSchoolSummaryModel> zoneManagementWiseSummary(Cursor cursor,String zoneCode, String zoneName){

        if(cursor==null){
            return null;
        }

        List<ManagementWiseSchoolSummaryModel> zoneSummaryList = new ArrayList<>();

        Set<String> schoolManagementSet = new HashSet<String>();
        ArrayList<ContentValues> managementInfoList = new ArrayList<ContentValues>();

        int indexColumnSchoolManagementCode = cursor.getColumnIndex(UdiseContract.RawData.COLUMN_SCHOOL_MANAGEMENT);
        int indexColumnSchoolManagementDescription = cursor.getColumnIndex(UdiseContract.RawData.COLUMN_SCHOOL_MANAGEMENT_DESCRIPTION);
        int indexColumnSchoolCategoryCode = cursor.getColumnIndex(UdiseContract.RawData.COLUMN_SCHOOL_CATEGORY);

        ManagementWiseSchoolSummaryModel zoneSummary = new ManagementWiseSchoolSummaryModel();
        zoneSummary.setModelType(SchoolReportsConstants.MODEL_TYPE_ZONE);
        zoneSummary.setFlagIsSummaryOrManagementDetail(SchoolReportsConstants.FLAG_ITEM_IS_ZONE_SUMMARY);
        zoneSummary.setDistrictCode(zoneCode);
        zoneSummary.setDistrictName(zoneName);
        zoneSummary.setManagementNameOrSummaryHeading("Zone " + zoneName + " Summary");

            /*
             * Iterate over all the rows of the cursor and
             * Create a list of distinct value of school managements
             */
        for(int index=0;index<cursor.getCount();index++) {
            cursor.moveToPosition(index);
            /*
             * Try adding school management values to the Set and
             * check if it succeeds. If the set accepts it (returns true) then it
             * is a distinct value. So store it in a Content Value List for later use.
             */
            if(schoolManagementSet.add(cursor.getString(indexColumnSchoolManagementDescription))){
                ContentValues managementInfo = new ContentValues();
                managementInfo.put(UdiseContract.RawData.COLUMN_SCHOOL_MANAGEMENT,cursor.getInt(indexColumnSchoolManagementCode));
                managementInfo.put(UdiseContract.RawData.COLUMN_SCHOOL_MANAGEMENT_DESCRIPTION,cursor.getString(indexColumnSchoolManagementDescription));
                /*Add the above value to an array list*/
                managementInfoList.add(managementInfo);
            }

            /*
             * Count the number of each category of schools for the
             * whole district to create a summary of the zone
             */

            switch (determineSchoolType(cursor.getInt(indexColumnSchoolCategoryCode))){
                case SchoolReportsConstants.PRIMARY_SCHOOL:
                    zoneSummary.incrementPrimarySchools();
                    break;
                case SchoolReportsConstants.MIDDLE_SCHOOL:
                    zoneSummary.incrementMiddleSchools();
                    break;
                case SchoolReportsConstants.HIGH_SCHOOL:
                    zoneSummary.incrementHighSchools();
                    break;
                case SchoolReportsConstants.HIGHER_SECONDARY_SCHOOL:
                    zoneSummary.incrementHigherSecondarySchools();
                    break;
            }
        }

        /*
         * Summary of the whole zone is created. Make it the first item in the list
         */
        zoneSummaryList.add(zoneSummary);

        /*
         * Here we will count the number of primary, middle, high and higher secondary schools
         * for each management for the whole zone
         */
        for(int mgmtCounter=0;mgmtCounter<managementInfoList.size();mgmtCounter++){
            ManagementWiseSchoolSummaryModel managementWiseSchoolSummaryModel = new ManagementWiseSchoolSummaryModel();
            managementWiseSchoolSummaryModel.setModelType(SchoolReportsConstants.MODEL_TYPE_ZONE);
            managementWiseSchoolSummaryModel.setFlagIsSummaryOrManagementDetail(SchoolReportsConstants.FLAG_ITEM_IS_MANAGEMENT_DETAIL);
            managementWiseSchoolSummaryModel.setDistrictCode(zoneCode);
            managementWiseSchoolSummaryModel.setDistrictName(zoneName);

            managementWiseSchoolSummaryModel.setManagementNameOrSummaryHeading(managementInfoList.get(mgmtCounter).getAsString(UdiseContract.RawData.COLUMN_SCHOOL_MANAGEMENT_DESCRIPTION));

            for(int cursorRow=0;cursorRow<cursor.getCount();cursorRow++){

                cursor.moveToPosition(cursorRow);
                if(cursor.getInt(indexColumnSchoolManagementCode)==managementInfoList.get(mgmtCounter).getAsInteger(UdiseContract.RawData.COLUMN_SCHOOL_MANAGEMENT)){
                    int schoolType = determineSchoolType(cursor.getInt(indexColumnSchoolCategoryCode));
                    switch (schoolType){
                        case SchoolReportsConstants.PRIMARY_SCHOOL:
                            managementWiseSchoolSummaryModel.incrementPrimarySchools();
                            break;
                        case SchoolReportsConstants.MIDDLE_SCHOOL:
                            managementWiseSchoolSummaryModel.incrementMiddleSchools();
                            break;
                        case SchoolReportsConstants.HIGH_SCHOOL:
                            managementWiseSchoolSummaryModel.incrementHighSchools();
                            break;
                        case SchoolReportsConstants.HIGHER_SECONDARY_SCHOOL:
                            managementWiseSchoolSummaryModel.incrementHigherSecondarySchools();
                            break;
                    }
                }
            }
            /*
             * Add this management to list if and only if there are any schools of this management type
             * Otherwise just ignore this management type
             */
            if(managementWiseSchoolSummaryModel.getPrimarySchools()>0 || managementWiseSchoolSummaryModel.getMiddleSchools()>0 || managementWiseSchoolSummaryModel.getHighSchools()>0 || managementWiseSchoolSummaryModel.getHigherSecondarySchools()>0){
                zoneSummaryList.add(managementWiseSchoolSummaryModel);
                Log.d(TAG,"Entry Added!!");
            }
        }
        return zoneSummaryList;
    }



    /**
     * This method generates summary of a cluster in a list of type {@link ManagementWiseSchoolSummaryModel}
     * The first item in the list contains the total number of Primary, Middle, High, and Higher Secondary
     * Schools in the whole cluster.
     * Subsequent items in the list contain number of Primary, Middle, High, and Higher Secondary Schools
     * for each management category. There is one item in the list per management category.
     *
     * So if a cluster contains 3 management categories, this list will contain 4 items,
     * one for the cluster summary and one for each management category
     * @param cursor Cursor containing data of the desired cluster for the desired academic year
     * @param clusterCode code of the cluster for which summary needs to be generated
     * @param clusterName name of the cluster
     * @return a list of type {@link ManagementWiseSchoolSummaryModel}, the first entry in the list contains
     * the summary of the whole cluster and the subsequent items in the list contain management wise
     * number of schools, one per each management type.
     */
    public static List<ManagementWiseSchoolSummaryModel> clusterManagementWiseSummary(Cursor cursor,String clusterCode, String clusterName){

        if(cursor==null){
            return null;
        }

        List<ManagementWiseSchoolSummaryModel> clusterSummaryList = new ArrayList<>();

        Set<String> schoolManagementSet = new HashSet<String>();
        ArrayList<ContentValues> managementInfoList = new ArrayList<ContentValues>();

        int indexColumnSchoolManagementCode = cursor.getColumnIndex(UdiseContract.RawData.COLUMN_SCHOOL_MANAGEMENT);
        int indexColumnSchoolManagementDescription = cursor.getColumnIndex(UdiseContract.RawData.COLUMN_SCHOOL_MANAGEMENT_DESCRIPTION);
        int indexColumnSchoolCategoryCode = cursor.getColumnIndex(UdiseContract.RawData.COLUMN_SCHOOL_CATEGORY);

        ManagementWiseSchoolSummaryModel clusterSummary = new ManagementWiseSchoolSummaryModel();
        clusterSummary.setModelType(SchoolReportsConstants.MODEL_TYPE_CLUSTER);
        clusterSummary.setFlagIsSummaryOrManagementDetail(SchoolReportsConstants.FLAG_ITEM_IS_CLUSTER_SUMMARY);
        clusterSummary.setClusterCode(clusterCode);
        clusterSummary.setClusterName(clusterName);
        clusterSummary.setManagementNameOrSummaryHeading("Cluster " + clusterName + " Summary");

            /*
             * Iterate over all the rows of the cursor and
             * Create a list of distinct value of school managements
             */
        for(int index=0;index<cursor.getCount();index++) {
            cursor.moveToPosition(index);
            /*
             * Try adding school management values to the Set and
             * check if it succeeds. If the set accepts it (returns true) then it
             * is a distinct value. So store it in a Content Value List for later use.
             */
            if(schoolManagementSet.add(cursor.getString(indexColumnSchoolManagementDescription))){
                ContentValues managementInfo = new ContentValues();
                managementInfo.put(UdiseContract.RawData.COLUMN_SCHOOL_MANAGEMENT,cursor.getInt(indexColumnSchoolManagementCode));
                managementInfo.put(UdiseContract.RawData.COLUMN_SCHOOL_MANAGEMENT_DESCRIPTION,cursor.getString(indexColumnSchoolManagementDescription));
                /*Add the above value to an array list*/
                managementInfoList.add(managementInfo);
            }

            /*
             * Count the number of each category of schools for the
             * whole cluster to create a summary of the cluster
             */

            switch (determineSchoolType(cursor.getInt(indexColumnSchoolCategoryCode))){
                case SchoolReportsConstants.PRIMARY_SCHOOL:
                    clusterSummary.incrementPrimarySchools();
                    break;
                case SchoolReportsConstants.MIDDLE_SCHOOL:
                    clusterSummary.incrementMiddleSchools();
                    break;
                case SchoolReportsConstants.HIGH_SCHOOL:
                    clusterSummary.incrementHighSchools();
                    break;
                case SchoolReportsConstants.HIGHER_SECONDARY_SCHOOL:
                    clusterSummary.incrementHigherSecondarySchools();
                    break;
            }
        }

        /*
         * Summary of the whole cluster is created. Make it the first item in the list
         */
        clusterSummaryList.add(clusterSummary);

        /*
         * Here we will count the number of primary, middle, high and higher secondary schools
         * for each management for the whole cluster
         */
        for(int mgmtCounter=0;mgmtCounter<managementInfoList.size();mgmtCounter++){
            ManagementWiseSchoolSummaryModel managementWiseSchoolSummaryModel = new ManagementWiseSchoolSummaryModel();
            managementWiseSchoolSummaryModel.setModelType(SchoolReportsConstants.MODEL_TYPE_CLUSTER);
            managementWiseSchoolSummaryModel.setFlagIsSummaryOrManagementDetail(SchoolReportsConstants.FLAG_ITEM_IS_MANAGEMENT_DETAIL);
            managementWiseSchoolSummaryModel.setClusterCode(clusterCode);
            managementWiseSchoolSummaryModel.setClusterName(clusterName);

            managementWiseSchoolSummaryModel.setManagementNameOrSummaryHeading(managementInfoList.get(mgmtCounter).getAsString(UdiseContract.RawData.COLUMN_SCHOOL_MANAGEMENT_DESCRIPTION));

            for(int cursorRow=0;cursorRow<cursor.getCount();cursorRow++){

                cursor.moveToPosition(cursorRow);
                if(cursor.getInt(indexColumnSchoolManagementCode)==managementInfoList.get(mgmtCounter).getAsInteger(UdiseContract.RawData.COLUMN_SCHOOL_MANAGEMENT)){
                    int schoolType = determineSchoolType(cursor.getInt(indexColumnSchoolCategoryCode));
                    switch (schoolType){
                        case SchoolReportsConstants.PRIMARY_SCHOOL:
                            managementWiseSchoolSummaryModel.incrementPrimarySchools();
                            break;
                        case SchoolReportsConstants.MIDDLE_SCHOOL:
                            managementWiseSchoolSummaryModel.incrementMiddleSchools();
                            break;
                        case SchoolReportsConstants.HIGH_SCHOOL:
                            managementWiseSchoolSummaryModel.incrementHighSchools();
                            break;
                        case SchoolReportsConstants.HIGHER_SECONDARY_SCHOOL:
                            managementWiseSchoolSummaryModel.incrementHigherSecondarySchools();
                            break;
                    }
                }
            }
            /*
             * Add this management to list if and only if there are any schools of this management type
             * Otherwise just ignore this management type
             */
            if(managementWiseSchoolSummaryModel.getPrimarySchools()>0 || managementWiseSchoolSummaryModel.getMiddleSchools()>0 || managementWiseSchoolSummaryModel.getHighSchools()>0 || managementWiseSchoolSummaryModel.getHigherSecondarySchools()>0){
                clusterSummaryList.add(managementWiseSchoolSummaryModel);
                Log.d(TAG,"Entry Added!!");
            }
        }
        return clusterSummaryList;
    }



    /**
     * This method returns the type of school viz.,
     * Primary, Middle, High or Higher Secondary based on
     * its UDISE school category
     * @param schoolCategory UDISE school category of the school
     * @return an integer constant defined in {@link SchoolReportsConstants} class
     * designating the school as Primary, Middle, High or Higher Secondary
     */
    public static int determineSchoolType(int schoolCategory){
        int category;
        switch (schoolCategory){
            case 1:
                category = SchoolReportsConstants.PRIMARY_SCHOOL;
                break;

            case 2:
            case 4:
                category = SchoolReportsConstants.MIDDLE_SCHOOL;
                break;

            case 6:
            case 7:
            case 8:
                category = SchoolReportsConstants.HIGH_SCHOOL;
                break;

            case 3:
            case 5:
            case 10:
            case 11:
            case 12:
                category = SchoolReportsConstants.HIGHER_SECONDARY_SCHOOL;
                break;

            default:
                category = 0;
                break;
        }

        return category;
    }

}
