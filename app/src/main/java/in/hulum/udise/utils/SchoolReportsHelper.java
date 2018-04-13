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
     * This method generates management-wise summary of a single assembly constituency.
     * It is important to note that the summary generated is not of the whole assembly constituency.
     * The summary is generated only for portion of the assembly constituency which is under the
     * selected State, District or Zone
     * @param cursor cursor containing data of the assembly constituency under the desired
     *               State, District or Zone
     * @param entityCode udise code of the assembly constituency
     * @param entityName name of the assembly constituency
     * @param parentCode udise code of the parent entity
     * @param parentEntityType type of parent entity for which the assembly constituency summary is to
     *                         be created (State, District or Zone)
     * @return list of type {@link ManagementWiseSchoolSummaryModel}, the first entry of which
     *         contains the summary of all managements and the rest of the items in the list
     *         contain details of each management separately
     */
    public static List<ManagementWiseSchoolSummaryModel> assemblyConstituencyManagementWiseSummary(Cursor cursor,String entityCode,String entityName,String parentCode,int parentEntityType){

        if(cursor==null){
            return null;
        }

        List<ManagementWiseSchoolSummaryModel> assemblyConstituencySummaryList = new ArrayList<>();

        Set<String> schoolManagementSet = new HashSet<String>();
        ArrayList<ContentValues> managementInfoList = new ArrayList<ContentValues>();

        String parentEntityName = null;

        /*
         * Move cursor to the first row of data
         */
        cursor.moveToFirst();

        int indexColumnSchoolManagementCode = cursor.getColumnIndex(UdiseContract.RawData.COLUMN_SCHOOL_MANAGEMENT);
        int indexColumnSchoolManagementDescription = cursor.getColumnIndex(UdiseContract.RawData.COLUMN_SCHOOL_MANAGEMENT_DESCRIPTION);
        int indexColumnSchoolCategoryCode = cursor.getColumnIndex(UdiseContract.RawData.COLUMN_SCHOOL_CATEGORY);

        /*
         * Determine parent of this assembly constituency for displaying
         * appropriate label
         */
        int indexParentEntityName;
        switch (parentEntityType){
            case SchoolReportsConstants.ASSEMBLY_CONSTITUENCY_PARENT_STATE:
                indexParentEntityName = cursor.getColumnIndex(UdiseContract.RawData.COLUMN_STATE_NAME);
                parentEntityName = cursor.getString(indexParentEntityName) + " State";
                break;

            case SchoolReportsConstants.ASSEMBLY_CONSTITUENCY_PARENT_DISTRICT:
                indexParentEntityName = cursor.getColumnIndex(UdiseContract.RawData.COLUMN_DISTRICT_NAME);
                parentEntityName = cursor.getString(indexParentEntityName) + " District";
                break;

            case SchoolReportsConstants.ASSEMBLY_CONSTITUENCY_PARENT_ZONE:
                indexParentEntityName = cursor.getColumnIndex(UdiseContract.RawData.COLUMN_ZONE_NAME);
                parentEntityName = cursor.getString(indexParentEntityName) + " Zone";
                break;

            default:
                Log.e(TAG,"parentEntityType " + parentEntityType + " not implemented yet in assembly constitution management-wise summary");
        }


        ManagementWiseSchoolSummaryModel assemblyConstituencySummary = new ManagementWiseSchoolSummaryModel();
        assemblyConstituencySummary.setModelType(SchoolReportsConstants.MODEL_TYPE_ASSEMBLY_CONSTITUENCY);
        assemblyConstituencySummary.setFlagIsSummaryOrManagementDetail(SchoolReportsConstants.FLAG_ITEM_IS_ASSEMBLY_CONSTITUENCY_SUMMARY);
        assemblyConstituencySummary.setManagementNameOrSummaryHeading("Assembly Constituency " + entityName + " [Area Under " + parentEntityName + "]");
        assemblyConstituencySummary.setName(entityName);
        assemblyConstituencySummary.setCode(entityCode);
        assemblyConstituencySummary.setExtraPayLoad(parentCode);


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
             * Count the number of each category of schools for
             * assembly constituency
             */

            switch (determineSchoolType(cursor.getInt(indexColumnSchoolCategoryCode))){
                case SchoolReportsConstants.PRIMARY_SCHOOL:
                    assemblyConstituencySummary.incrementPrimarySchools();
                    break;
                case SchoolReportsConstants.MIDDLE_SCHOOL:
                    assemblyConstituencySummary.incrementMiddleSchools();
                    break;
                case SchoolReportsConstants.HIGH_SCHOOL:
                    assemblyConstituencySummary.incrementHighSchools();
                    break;
                case SchoolReportsConstants.HIGHER_SECONDARY_SCHOOL:
                    assemblyConstituencySummary.incrementHigherSecondarySchools();
                    break;
            }
        }

        /*
         * Assembly Constituency summary is created. Make it the first item in the list
         * This summary contains the number of schools for all managements in aggregate.
         */
        assemblyConstituencySummaryList.add(assemblyConstituencySummary);

        /*
         * Now we will count the number of primary, middle, high and higher secondary schools
         * for each management separately
         */
        for(int mgmtCounter=0;mgmtCounter<managementInfoList.size();mgmtCounter++){
            ManagementWiseSchoolSummaryModel managementWiseSchoolSummaryModel = new ManagementWiseSchoolSummaryModel();
            managementWiseSchoolSummaryModel.setModelType(SchoolReportsConstants.MODEL_TYPE_ASSEMBLY_CONSTITUENCY);
            managementWiseSchoolSummaryModel.setFlagIsSummaryOrManagementDetail(SchoolReportsConstants.FLAG_ITEM_IS_MANAGEMENT_DETAIL);
            assemblyConstituencySummary.setName(entityName);
            assemblyConstituencySummary.setCode(entityCode);
            assemblyConstituencySummary.setExtraPayLoad(parentCode);

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
                assemblyConstituencySummaryList.add(managementWiseSchoolSummaryModel);
            }
        }
        return assemblyConstituencySummaryList;
    }



    /**
     * This method generates assembly constituency wise list containing number of schools.
     * It should be noted that the cursor contains data of the desired state, district or
     * zone only and not the whole database.
     * @param cursor cursor containing schools of the desired constituency under desired
     *               state, district or zone
     * @param parentEntityStateDistrictOrZone type of the parent entity i.e., one of the
     *                                        types:
     *                                        - SchoolReportsConstants.ASSEMBLY_CONSTITUENCY_PARENT_STATE
     *                                        - SchoolReportsConstants.ASSEMBLY_CONSTITUENCY_PARENT_DISTRICT
     *                                        - SchoolReportsConstants.ASSEMBLY_CONSTITUENCY_PARENT_ZONE
     *
     * @param parentCode Udise code of the parent State, District or
     *                   Zone for the assembly constituency
     * @return a list of type {@link ManagementWiseSchoolSummaryModel}, one per each assembly constituency
     *
     * NOTE: Although 'management' has nothing to do with this method as we are just generating
     * a list of assembly constituencies along with the number of schools in each state, i am using the same model
     * {@link ManagementWiseSchoolSummaryModel} just to keep things simple without creating
     * too many models.
     */
    public static List<ManagementWiseSchoolSummaryModel> assemblyConstituencyWiseSummary(Cursor cursor,int parentEntityStateDistrictOrZone,String parentCode){

        if(cursor==null){
            return null;
        }

        List<ManagementWiseSchoolSummaryModel> assemblyConstituencyWiseSummaryList = new ArrayList<>();

        Set<String> uniqueAssemblyConstituencyCodesSet = new HashSet<String>();
        ArrayList<ContentValues> assemblyConstituencyInfoList = new ArrayList<ContentValues>();

        int indexColumnAssemblyConstituencyName = cursor.getColumnIndex(UdiseContract.RawData.COLUMN_ASSEMBLY_CONSTITUENCY_NAME);
        int indexColumnAssemblyConstituencyCode = cursor.getColumnIndex(UdiseContract.RawData.COLUMN_ASSEMBLY_CONSTITUENCY_CODE);
        int indexColumnSchoolCategoryCode = cursor.getColumnIndex(UdiseContract.RawData.COLUMN_SCHOOL_CATEGORY);

            /*
             * Iterate over all the rows of the cursor and
             * Create a list of distinct value of assembly
             * constituency codes
             */
        for(int index=0;index<cursor.getCount();index++) {
            cursor.moveToPosition(index);
            /*
             * Create a list of unique assembly constituencies found in the cursor
             */
            if(uniqueAssemblyConstituencyCodesSet.add(cursor.getString(indexColumnAssemblyConstituencyCode))){
                ContentValues assemblyConstituencyInfo = new ContentValues();
                assemblyConstituencyInfo.put(UdiseContract.RawData.COLUMN_ASSEMBLY_CONSTITUENCY_CODE,cursor.getString(indexColumnAssemblyConstituencyCode));
                assemblyConstituencyInfo.put(UdiseContract.RawData.COLUMN_ASSEMBLY_CONSTITUENCY_NAME,cursor.getString(indexColumnAssemblyConstituencyName));
                assemblyConstituencyInfoList.add(assemblyConstituencyInfo);
            }
        }

        /*
         * Here we will count the number of primary, middle, high and higher secondary schools
         * for each Assembly Constituency
         */
        for(int assemblyConstituencyCounter=0;assemblyConstituencyCounter<assemblyConstituencyInfoList.size();assemblyConstituencyCounter++){
            ManagementWiseSchoolSummaryModel assemblyConstituencySummaryModel = new ManagementWiseSchoolSummaryModel();

            switch(parentEntityStateDistrictOrZone){
                case SchoolReportsConstants.ASSEMBLY_CONSTITUENCY_PARENT_STATE:
                    assemblyConstituencySummaryModel.setModelType(SchoolReportsConstants.MODEL_TYPE_ASSEMBLY_CONSTITUENCY_WISE_LIST_FOR_STATE);
                    assemblyConstituencySummaryModel.setExtraPayLoad(parentCode);
                    break;

                case SchoolReportsConstants.ASSEMBLY_CONSTITUENCY_PARENT_DISTRICT:
                    assemblyConstituencySummaryModel.setModelType(SchoolReportsConstants.MODEL_TYPE_ASSEMBLY_CONSTITUENCY_WISE_LIST_FOR_DISTRICT);
                    assemblyConstituencySummaryModel.setExtraPayLoad(parentCode);
                    break;

                case SchoolReportsConstants.ASSEMBLY_CONSTITUENCY_PARENT_ZONE:
                    assemblyConstituencySummaryModel.setModelType(SchoolReportsConstants.MODEL_TYPE_ASSEMBLY_CONSTITUENCY_WISE_LIST_FOR_ZONE);
                    assemblyConstituencySummaryModel.setExtraPayLoad(parentCode);
                    break;

                default:
                    Log.e(TAG,"Unknown Assembly Constituency-wise report with report code " + parentEntityStateDistrictOrZone);
                    throw new IllegalArgumentException("Unknown Assembly Constituency-wise report with report code " + parentEntityStateDistrictOrZone);
            }

            assemblyConstituencySummaryModel.setFlagIsSummaryOrManagementDetail(SchoolReportsConstants.FLAG_ITEM_IS_ASSEMBLY_CONSTITUENCY_SUMMARY);

            String currentAssemblyConstituencyCode = assemblyConstituencyInfoList.get(assemblyConstituencyCounter).getAsString(UdiseContract.RawData.COLUMN_ASSEMBLY_CONSTITUENCY_CODE);
            String currentAssemblyConstituencyName = assemblyConstituencyInfoList.get(assemblyConstituencyCounter).getAsString(UdiseContract.RawData.COLUMN_ASSEMBLY_CONSTITUENCY_NAME);
            assemblyConstituencySummaryModel.setCode(currentAssemblyConstituencyCode);
            assemblyConstituencySummaryModel.setName(currentAssemblyConstituencyName);

            assemblyConstituencySummaryModel.setManagementNameOrSummaryHeading("Assembly Constituency " + currentAssemblyConstituencyName);

            for(int cursorRow=0;cursorRow<cursor.getCount();cursorRow++){

                cursor.moveToPosition(cursorRow);
                if(cursor.getString(indexColumnAssemblyConstituencyCode).equals(currentAssemblyConstituencyCode)){
                    int schoolType = determineSchoolType(cursor.getInt(indexColumnSchoolCategoryCode));
                    switch (schoolType){
                        case SchoolReportsConstants.PRIMARY_SCHOOL:
                            assemblyConstituencySummaryModel.incrementPrimarySchools();
                            break;
                        case SchoolReportsConstants.MIDDLE_SCHOOL:
                            assemblyConstituencySummaryModel.incrementMiddleSchools();
                            break;
                        case SchoolReportsConstants.HIGH_SCHOOL:
                            assemblyConstituencySummaryModel.incrementHighSchools();
                            break;
                        case SchoolReportsConstants.HIGHER_SECONDARY_SCHOOL:
                            assemblyConstituencySummaryModel.incrementHigherSecondarySchools();
                            break;
                    }
                }
            }
            /*
             * Add this assembly constituency to the list if and only if there are any schools
             * in it. Otherwise just ignore it
             */
            if(assemblyConstituencySummaryModel.getPrimarySchools()>0 || assemblyConstituencySummaryModel.getMiddleSchools()>0 || assemblyConstituencySummaryModel.getHighSchools()>0 || assemblyConstituencySummaryModel.getHigherSecondarySchools()>0){
                assemblyConstituencyWiseSummaryList.add(assemblyConstituencySummaryModel);
            }
        }
        return assemblyConstituencyWiseSummaryList;
    }





    /**
     * This method generates a list of states along with the number of schools in each state
     * in a list of type {@link ManagementWiseSchoolSummaryModel}
     *
     * @param cursor Cursor containing data of the desired country for the desired academic year
     * @return a list of type {@link ManagementWiseSchoolSummaryModel} one entry per each state
     *
     * NOTE: Although 'management' has nothing to do with this method as we are just generating
     * a list of states along with the number of schools in each state, i am using the same model
     * {@link ManagementWiseSchoolSummaryModel} just to keep things simple without creating
     * too many models.
     */
    public static List<ManagementWiseSchoolSummaryModel> stateWiseSummary(Cursor cursor){

        if(cursor==null){
            return null;
        }

        List<ManagementWiseSchoolSummaryModel> stateWiseSummaryList = new ArrayList<>();

        Set<String> uniqueStatesSet = new HashSet<String>();
        ArrayList<ContentValues> stateInfoList = new ArrayList<ContentValues>();

        int indexColumnStateName = cursor.getColumnIndex(UdiseContract.RawData.COLUMN_STATE_NAME);
        int indexColumnSchoolCategoryCode = cursor.getColumnIndex(UdiseContract.RawData.COLUMN_SCHOOL_CATEGORY);

            /*
             * Iterate over all the rows of the cursor and
             * Create a list of distinct value of state names
             */
        for(int index=0;index<cursor.getCount();index++) {
            cursor.moveToPosition(index);
            /*
             * Create a list of unique states found in the database
             * Try adding state name values to the Set and
             * check if it succeeds. If the set accepts it (returns true) then it
             * is a distinct value. So store it in a Content Value List for later use.
             */
            if(uniqueStatesSet.add(cursor.getString(indexColumnStateName))){
                ContentValues stateInfo = new ContentValues();
                stateInfo.put(UdiseContract.RawData.COLUMN_STATE_NAME,cursor.getString(indexColumnStateName));
                stateInfoList.add(stateInfo);
            }
        }


        /*
         * Here we will count the number of primary, middle, high and higher secondary schools
         * for each State
         */
        for(int stateCounter=0;stateCounter<stateInfoList.size();stateCounter++){
            ManagementWiseSchoolSummaryModel stateSummaryModel = new ManagementWiseSchoolSummaryModel();
            stateSummaryModel.setModelType(SchoolReportsConstants.MODEL_TYPE_STATE_WISE_LIST);
            stateSummaryModel.setFlagIsSummaryOrManagementDetail(SchoolReportsConstants.FLAG_ITEM_IS_STATE_SUMMARY);

            String currentState = stateInfoList.get(stateCounter).getAsString(UdiseContract.RawData.COLUMN_STATE_NAME);
            stateSummaryModel.setName(currentState);
            stateSummaryModel.setManagementNameOrSummaryHeading(currentState);

            for(int cursorRow=0;cursorRow<cursor.getCount();cursorRow++){

                cursor.moveToPosition(cursorRow);
                if(cursor.getString(indexColumnStateName).equals(currentState)){
                    int schoolType = determineSchoolType(cursor.getInt(indexColumnSchoolCategoryCode));
                    switch (schoolType){
                        case SchoolReportsConstants.PRIMARY_SCHOOL:
                            stateSummaryModel.incrementPrimarySchools();
                            break;
                        case SchoolReportsConstants.MIDDLE_SCHOOL:
                            stateSummaryModel.incrementMiddleSchools();
                            break;
                        case SchoolReportsConstants.HIGH_SCHOOL:
                            stateSummaryModel.incrementHighSchools();
                            break;
                        case SchoolReportsConstants.HIGHER_SECONDARY_SCHOOL:
                            stateSummaryModel.incrementHigherSecondarySchools();
                            break;
                    }
                }
            }
            /*
             * Add this state to the list if and only if there are any schools in this state
             * Otherwise just ignore it
             */
            if(stateSummaryModel.getPrimarySchools()>0 || stateSummaryModel.getMiddleSchools()>0 || stateSummaryModel.getHighSchools()>0 || stateSummaryModel.getHigherSecondarySchools()>0){
                stateWiseSummaryList.add(stateSummaryModel);
            }
        }
        return stateWiseSummaryList;
    }



    /**
     * This method generates a list of districts along with the number of schools in each district
     * in a list of type {@link ManagementWiseSchoolSummaryModel}
     *
     * @param cursor Cursor containing data of the desired state for the desired academic year
     * @return a list of type {@link ManagementWiseSchoolSummaryModel} one entry per each district
     *
     * NOTE: Although 'management' has nothing to do with this method, i am using the same model
     * {@link ManagementWiseSchoolSummaryModel} just to keep things simple without creating
     * too many models.
     */
    public static List<ManagementWiseSchoolSummaryModel> districtWiseSummary(Cursor cursor){

        if(cursor==null){
            return null;
        }

        List<ManagementWiseSchoolSummaryModel> districtWiseSummaryList = new ArrayList<>();

        Set<String> uniqueDistrictCodesSet = new HashSet<String>();
        ArrayList<ContentValues> districtInfoList = new ArrayList<ContentValues>();

        int indexColumnDistrictName = cursor.getColumnIndex(UdiseContract.RawData.COLUMN_DISTRICT_NAME);
        int indexColumnDistrictCode = cursor.getColumnIndex(UdiseContract.RawData.COLUMN_DISTRICT_CODE);
        int indexColumnSchoolCategoryCode = cursor.getColumnIndex(UdiseContract.RawData.COLUMN_SCHOOL_CATEGORY);


            /*
             * Iterate over all the rows of the cursor and
             * Create a list of distinct value of district codes
             */
        for(int index=0;index<cursor.getCount();index++) {
            cursor.moveToPosition(index);
            /*
             * Create a list of unique districts found in the database
             * Try adding district code values to the Set and
             * check if it succeeds. If the set accepts it (returns true) then it
             * is a distinct value. So store it in a Content Value List for later use.
             */
            if(uniqueDistrictCodesSet.add(cursor.getString(indexColumnDistrictCode))){
                ContentValues districtInfo = new ContentValues();
                districtInfo.put(UdiseContract.RawData.COLUMN_DISTRICT_CODE,cursor.getString(indexColumnDistrictCode));
                districtInfo.put(UdiseContract.RawData.COLUMN_DISTRICT_NAME,cursor.getString(indexColumnDistrictName));
                districtInfoList.add(districtInfo);
            }
        }


        /*
         * Here we will count the number of primary, middle, high and higher secondary schools
         * for each District
         */
        for(int districtCounter=0;districtCounter<districtInfoList.size();districtCounter++){
            ManagementWiseSchoolSummaryModel districtSummaryModel = new ManagementWiseSchoolSummaryModel();
            districtSummaryModel.setModelType(SchoolReportsConstants.MODEL_TYPE_DISTRICT_WISE_LIST);
            districtSummaryModel.setFlagIsSummaryOrManagementDetail(SchoolReportsConstants.FLAG_ITEM_IS_DISTRICT_SUMMARY);

            String currentDistrictCode = districtInfoList.get(districtCounter).getAsString(UdiseContract.RawData.COLUMN_DISTRICT_CODE);
            String currentDistrictName = districtInfoList.get(districtCounter).getAsString(UdiseContract.RawData.COLUMN_DISTRICT_NAME);
            districtSummaryModel.setCode(currentDistrictCode);
            districtSummaryModel.setName(currentDistrictName);
            districtSummaryModel.setManagementNameOrSummaryHeading("District " + currentDistrictName);

            for(int cursorRow=0;cursorRow<cursor.getCount();cursorRow++){

                cursor.moveToPosition(cursorRow);
                if(cursor.getString(indexColumnDistrictCode).equals(currentDistrictCode)){
                    int schoolType = determineSchoolType(cursor.getInt(indexColumnSchoolCategoryCode));
                    switch (schoolType){
                        case SchoolReportsConstants.PRIMARY_SCHOOL:
                            districtSummaryModel.incrementPrimarySchools();
                            break;
                        case SchoolReportsConstants.MIDDLE_SCHOOL:
                            districtSummaryModel.incrementMiddleSchools();
                            break;
                        case SchoolReportsConstants.HIGH_SCHOOL:
                            districtSummaryModel.incrementHighSchools();
                            break;
                        case SchoolReportsConstants.HIGHER_SECONDARY_SCHOOL:
                            districtSummaryModel.incrementHigherSecondarySchools();
                            break;
                    }
                }
            }
            /*
             * Add this district to the list if and only if there are any schools in this district
             * Otherwise just ignore it
             */
            if(districtSummaryModel.getPrimarySchools()>0 || districtSummaryModel.getMiddleSchools()>0 || districtSummaryModel.getHighSchools()>0 || districtSummaryModel.getHigherSecondarySchools()>0){
                districtWiseSummaryList.add(districtSummaryModel);
            }
        }
        return districtWiseSummaryList;
    }



    /**
     * This method generates a list of zones along with the number of schools in each zone
     * in a list of type {@link ManagementWiseSchoolSummaryModel}
     *
     * @param cursor Cursor containing data of the desired district for the desired academic year
     * @return a list of type {@link ManagementWiseSchoolSummaryModel} one entry per each zone
     *
     * NOTE: Although 'management' has nothing to do with this method, i am using the same model
     * {@link ManagementWiseSchoolSummaryModel} just to keep things simple without creating
     * too many models.
     */
    public static List<ManagementWiseSchoolSummaryModel> zoneWiseSummary(Cursor cursor){

        if(cursor==null){
            return null;
        }

        List<ManagementWiseSchoolSummaryModel> zoneWiseSummaryList = new ArrayList<>();

        Set<String> uniqueZoneCodesSet = new HashSet<String>();
        ArrayList<ContentValues> zoneInfoList = new ArrayList<ContentValues>();

        int indexColumnZoneName = cursor.getColumnIndex(UdiseContract.RawData.COLUMN_ZONE_NAME);
        int indexColumnZoneCode = cursor.getColumnIndex(UdiseContract.RawData.COLUMN_ZONE_CODE);
        int indexColumnSchoolCategoryCode = cursor.getColumnIndex(UdiseContract.RawData.COLUMN_SCHOOL_CATEGORY);


            /*
             * Iterate over all the rows of the cursor and
             * Create a list of distinct value of zone codes
             */
        for(int index=0;index<cursor.getCount();index++) {
            cursor.moveToPosition(index);
            /*
             * Create a list of unique zones found in the cursor
             * Try adding zone code values to the Set and
             * check if it succeeds. If the set accepts it (returns true) then it
             * is a distinct value. So store it in a Content Value List for later use.
             */
            if(uniqueZoneCodesSet.add(cursor.getString(indexColumnZoneCode))){
                ContentValues zoneInfo = new ContentValues();
                zoneInfo.put(UdiseContract.RawData.COLUMN_ZONE_CODE,cursor.getString(indexColumnZoneCode));
                zoneInfo.put(UdiseContract.RawData.COLUMN_ZONE_NAME,cursor.getString(indexColumnZoneName));
                zoneInfoList.add(zoneInfo);
            }
        }


        /*
         * Here we will count the number of primary, middle, high and higher secondary schools
         * for each Zone
         */
        for(int zoneCounter=0;zoneCounter<zoneInfoList.size();zoneCounter++){
            ManagementWiseSchoolSummaryModel zoneSummaryModel = new ManagementWiseSchoolSummaryModel();
            zoneSummaryModel.setModelType(SchoolReportsConstants.MODEL_TYPE_ZONE_WISE_LIST);
            zoneSummaryModel.setFlagIsSummaryOrManagementDetail(SchoolReportsConstants.FLAG_ITEM_IS_ZONE_SUMMARY);

            String currentZoneCode = zoneInfoList.get(zoneCounter).getAsString(UdiseContract.RawData.COLUMN_ZONE_CODE);
            String currentZoneName = zoneInfoList.get(zoneCounter).getAsString(UdiseContract.RawData.COLUMN_ZONE_NAME);
            zoneSummaryModel.setCode(currentZoneCode);
            zoneSummaryModel.setName(currentZoneName);
            zoneSummaryModel.setManagementNameOrSummaryHeading("Zone " + currentZoneName);

            for(int cursorRow=0;cursorRow<cursor.getCount();cursorRow++){

                cursor.moveToPosition(cursorRow);
                if(cursor.getString(indexColumnZoneCode).equals(currentZoneCode)){
                    int schoolType = determineSchoolType(cursor.getInt(indexColumnSchoolCategoryCode));
                    switch (schoolType){
                        case SchoolReportsConstants.PRIMARY_SCHOOL:
                            zoneSummaryModel.incrementPrimarySchools();
                            break;
                        case SchoolReportsConstants.MIDDLE_SCHOOL:
                            zoneSummaryModel.incrementMiddleSchools();
                            break;
                        case SchoolReportsConstants.HIGH_SCHOOL:
                            zoneSummaryModel.incrementHighSchools();
                            break;
                        case SchoolReportsConstants.HIGHER_SECONDARY_SCHOOL:
                            zoneSummaryModel.incrementHigherSecondarySchools();
                            break;
                    }
                }
            }
            /*
             * Add this zone to the list if and only if there are any schools in this zone
             * Otherwise just ignore it
             */
            if(zoneSummaryModel.getPrimarySchools()>0 || zoneSummaryModel.getMiddleSchools()>0 || zoneSummaryModel.getHighSchools()>0 || zoneSummaryModel.getHigherSecondarySchools()>0){
                zoneWiseSummaryList.add(zoneSummaryModel);
            }
        }
        return zoneWiseSummaryList;
    }



    /**
     * This method generates a list of clusters along with the number of schools in each cluster
     * in a list of type {@link ManagementWiseSchoolSummaryModel}
     *
     * @param cursor Cursor containing data of the desired zone for the desired academic year
     * @return a list of type {@link ManagementWiseSchoolSummaryModel} one entry per each cluster
     *
     * NOTE: Although 'management' has nothing to do with this method, i am using the same model
     * {@link ManagementWiseSchoolSummaryModel} just to keep things simple without creating
     * too many models.
     */
    public static List<ManagementWiseSchoolSummaryModel> clusterWiseSummary(Cursor cursor){

        if(cursor==null){
            return null;
        }

        List<ManagementWiseSchoolSummaryModel> clusterWiseSummaryList = new ArrayList<>();

        Set<String> uniqueClusterCodesSet = new HashSet<String>();
        ArrayList<ContentValues> clusterInfoList = new ArrayList<ContentValues>();

        int indexColumnClusterName = cursor.getColumnIndex(UdiseContract.RawData.COLUMN_CLUSTER_NAME);
        int indexColumnClusterCode = cursor.getColumnIndex(UdiseContract.RawData.COLUMN_CLUSTER_CODE);
        int indexColumnSchoolCategoryCode = cursor.getColumnIndex(UdiseContract.RawData.COLUMN_SCHOOL_CATEGORY);


            /*
             * Iterate over all the rows of the cursor and
             * Create a list of distinct value of cluster codes
             */
        for(int index=0;index<cursor.getCount();index++) {
            cursor.moveToPosition(index);
            /*
             * Create a list of unique clusters found in the cursor
             * Try adding cluster code values to the Set and
             * check if it succeeds. If the set accepts it (returns true) then it
             * is a distinct value. So store it in a Content Value List for later use.
             */
            if(uniqueClusterCodesSet.add(cursor.getString(indexColumnClusterCode))){
                ContentValues clusterInfo = new ContentValues();
                clusterInfo.put(UdiseContract.RawData.COLUMN_CLUSTER_CODE,cursor.getString(indexColumnClusterCode));
                clusterInfo.put(UdiseContract.RawData.COLUMN_CLUSTER_NAME,cursor.getString(indexColumnClusterName));
                clusterInfoList.add(clusterInfo);
            }
        }


        /*
         * Here we will count the number of primary, middle, high and higher secondary schools
         * for each Cluster
         */
        for(int clusterCounter=0;clusterCounter<clusterInfoList.size();clusterCounter++){
            ManagementWiseSchoolSummaryModel clusterSummaryModel = new ManagementWiseSchoolSummaryModel();
            clusterSummaryModel.setModelType(SchoolReportsConstants.MODEL_TYPE_CLUSTER_WISE_LIST);
            clusterSummaryModel.setFlagIsSummaryOrManagementDetail(SchoolReportsConstants.FLAG_ITEM_IS_CLUSTER_SUMMARY);

            String currentClusterCode = clusterInfoList.get(clusterCounter).getAsString(UdiseContract.RawData.COLUMN_CLUSTER_CODE);
            String currentClusterName = clusterInfoList.get(clusterCounter).getAsString(UdiseContract.RawData.COLUMN_CLUSTER_NAME);
            clusterSummaryModel.setCode(currentClusterCode);
            clusterSummaryModel.setName(currentClusterName);
            clusterSummaryModel.setManagementNameOrSummaryHeading("Cluster " + currentClusterName);

            for(int cursorRow=0;cursorRow<cursor.getCount();cursorRow++){

                cursor.moveToPosition(cursorRow);
                if(cursor.getString(indexColumnClusterCode).equals(currentClusterCode)){
                    int schoolType = determineSchoolType(cursor.getInt(indexColumnSchoolCategoryCode));
                    switch (schoolType){
                        case SchoolReportsConstants.PRIMARY_SCHOOL:
                            clusterSummaryModel.incrementPrimarySchools();
                            break;
                        case SchoolReportsConstants.MIDDLE_SCHOOL:
                            clusterSummaryModel.incrementMiddleSchools();
                            break;
                        case SchoolReportsConstants.HIGH_SCHOOL:
                            clusterSummaryModel.incrementHighSchools();
                            break;
                        case SchoolReportsConstants.HIGHER_SECONDARY_SCHOOL:
                            clusterSummaryModel.incrementHigherSecondarySchools();
                            break;
                    }
                }
            }
            /*
             * Add this cluster to the list if and only if there are any schools in this cluster
             * Otherwise just ignore it
             */
            if(clusterSummaryModel.getPrimarySchools()>0 || clusterSummaryModel.getMiddleSchools()>0 || clusterSummaryModel.getHighSchools()>0 || clusterSummaryModel.getHigherSecondarySchools()>0){
                clusterWiseSummaryList.add(clusterSummaryModel);
            }
        }
        return clusterWiseSummaryList;
    }


    /**********************************
     * Summary Methods Start From Here
     * *******************************
     */



    /**
     * This method generates summary of the whole nation in a list of type {@link ManagementWiseSchoolSummaryModel}
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
        stateSummary.setName(stateName);
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
            managementWiseSchoolSummaryModel.setName(stateName);

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
        districtSummary.setCode(districtCode);
        districtSummary.setName(districtName);
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
            managementWiseSchoolSummaryModel.setCode(districtCode);
            managementWiseSchoolSummaryModel.setName(districtName);

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
        zoneSummary.setCode(zoneCode);
        zoneSummary.setName(zoneName);
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
            managementWiseSchoolSummaryModel.setCode(zoneCode);
            managementWiseSchoolSummaryModel.setName(zoneName);

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
        clusterSummary.setCode(clusterCode);
        clusterSummary.setName(clusterName);
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
            managementWiseSchoolSummaryModel.setCode(clusterCode);
            managementWiseSchoolSummaryModel.setName(clusterName);

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
