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
        districtSummary.setManagementNameOrSummaryHeading("DISTRICT " + districtName.toUpperCase() + " SUMMARY");

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
