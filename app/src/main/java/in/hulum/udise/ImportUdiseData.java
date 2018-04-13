package in.hulum.udise;

import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import in.hulum.udise.database.UdiseContract;
import in.hulum.udise.database.UdiseDbHelper;
import in.hulum.udise.models.UserDataModel;
import in.hulum.udise.utils.NotificationHelper;
import in.hulum.udise.utils.SchoolReportsConstants;

/**
 * Created by Irshad on 20-03-2018.
 * This class contains methods for importing excel file into SQLite database,
 * determining user type and determining whether the database is empty or not.
 * All these methods are invoked through {@link ImportJobIntentService} only
 * in order to execute this code on a background thread.
 *
 * <p>
 * So these methods should be called only through the corresponding
 * startAction methods in the {@link android.support.v4.app.JobIntentService}
 * </p>
 *
 */

public class ImportUdiseData {

    public static final String ACTION_IMPORT_RAW_DATA = "in.hulum.udise.action.IMPORT_RAW_DATA";
    public static final String ACTION_DOES_RAW_DATA_EXIST_IN_DATABASE = "in.hulum.udise.action.DOES_RAW_DATA_EXIST_IN_DATABASE";
    public static final String ACTION_DETERMINE_USER_TYPE = "in.hulum.udise.action.DETERMINE_USER_TYPE";

    public static final String PARAM_DOES_RAW_DATA_EXIST = "in.hulum.udise.extra.PARAM_DOES_RAW_DATA_EXIST";

    public static final String PARAM_ERROR = "in.hulum.udise.extra.PARAM_ERROR";
    public static final String PARAM_MESSAGE = "in.hulum.udise.extra.PARAM_ERROR_MESSAGE";
    public static final String PARAM_IS_ANALYSING = "in.hulum.udise.extra.PARAM_IS_ANALYSING";
    public static final String PARAM_IS_IMPORTING = "in.hulum.udise.extra.PARAM_IS_IMPORTING";
    public static final String PARAM_PERCENTAGE_COMPLETED = "in.hulum.udise.extra.PARAM_PERCENTAGE_COMPLETED";
    public static final String PARAM_USER_IDENTIFIED = "in.hulum.udise.extra.PARAM_USER_IDENTIFIED";
    public static final String PARAM_USER_TYPE = "in.hulum.udise.extra.PARAM_USER_TYPE";
    public static final String PARAM_IMPORT_ENDED_SUCCESSFULLY = "in.hulum.udise.extra.PARAM_IMPORT_ENDED_SUCCESSFULLY";

    public static final String TAG = ImportUdiseData.class.getSimpleName();

    private static NotificationHelper notificationHelper;
    private static int NOTIFICATION_ID = 17;

    private static final String SHARED_PREFERENCES_KEY_IS_IMPORTING = "in.hulum.udise.sharedpreferences.keys.isimporting";
    private static final String SHARED_PREFERENCES_KEY_PROGRESS = "in.hulum.udise.sharedpreferences.keys.progress";



    /**
     * This method acts as an entry point into this class. It acts as a bridge
     * between the JobIntentService and this class, as such it is casted as static.
     *
     * @param context context of the calling class (JobIntentService)
     * @param intent The intent which contains the Uri of the Excel file
     */
    public static void executeAction(Context context, Intent intent){
        if (intent != null) {
            final String action = intent.getAction();

            if(ACTION_IMPORT_RAW_DATA.equals(action)){

                final Uri importFileUri = intent.getData();
                handleActionImportRawData(context,importFileUri);
            }

            else if(ACTION_DOES_RAW_DATA_EXIST_IN_DATABASE.equals(action)){
                handleActionDoesRawDataExistInDatabase(context);
            }

            else if(ACTION_DETERMINE_USER_TYPE.equals(action)){
                handleActionDetermineUserType(context);
            }
        }
    }

    /**
     * This method determines the usertype on a background thread and stores
     * it in the shared preferences so that main activity can read it and show the
     * user type in the navigation drawer
     * @param context
     */

    private static void handleActionDetermineUserType(Context context){
        String userTypeString = "Unknown User";
        String userTypeSubtitle = "Unknown Office";
        /*
         * Create a new instance of UdiseDbHelper
         * This is required because the determineUserTypeAndDataModel method
         * that is used to determine the type of data and user is not static.
         */
        UdiseDbHelper udiseDbHelper = new UdiseDbHelper(context);

        /*
         * Create a new instance of UserDataModel
         * to hold the userType and and other data
         * returned by the determineUserTypeAndDataModel method of
         * UdiseDbHelper class
         */
        UserDataModel userDataModel;
        userDataModel = udiseDbHelper.determineUserTypeAndDataModel(context);

        /**
         * Generate an appropriate title and subtitle for Navigation Drawer
         * of the {@link MainActivity} based on the user type.
         */
        switch(userDataModel.getUserType()){
            case UserDataModel.USER_TYPE_NATIONAL:
                userTypeString = "National User";
                userTypeSubtitle = "Total States: " + userDataModel.getStatesList().size();
                break;

            case UserDataModel.USER_TYPE_STATE:
                userTypeString = userDataModel.getStatesList().get(0).getStateName();
                userTypeSubtitle = "State User with " + userDataModel.getDistrictsList().size() + " Districts";
                break;

            case UserDataModel.USER_TYPE_DISTRICT:
                userTypeString = "CEO " + userDataModel.getDistrictsList().get(0).getDistrictName();
                userTypeSubtitle = "District User with " + userDataModel.getZoneList().size() + " Zones";
                break;
            case UserDataModel.USER_TYPE_ZONE:
                userTypeString = "ZEO " + userDataModel.getZoneList().get(0).getZoneName();
                userTypeSubtitle = "Zone Code: " + userDataModel.getZoneList().get(0).getZoneCode();
                break;
        }

        /*
         * Save the title and subtitle in the shared preferences for later retrieval
         */
        SharedPreferences mPreferences;
        String sharedPrefFile = SchoolReportsConstants.SHARED_PREFERENCES_FILE;
        mPreferences = context.getSharedPreferences(sharedPrefFile,Context.MODE_PRIVATE);
        SharedPreferences.Editor preferenceEditor = mPreferences.edit();
        preferenceEditor.putString(SchoolReportsConstants.SHARED_PREFERENCES_NAVIGATION_DRAWER_USER_TYPE_STRING,userTypeString);
        preferenceEditor.putString(SchoolReportsConstants.SHARED_PREFERENCES_NAVIGATION_DRAWER_SUBTITLE,userTypeSubtitle);
        preferenceEditor.apply();
        /*
         * Send a local broadcast confirming that the user type  has been determined
         * and saved in the shared preferences
         */
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(ACTION_DETERMINE_USER_TYPE);
        LocalBroadcastManager.getInstance(context).sendBroadcast(broadcastIntent);
    }


    /**
     * This method checks whether raw data exists in database or not
     * If the database is empty then the reports cannot be
     * displayed and user will be prompted to import excel file first.
     *
     * Since this method is run through {@link ImportJobIntentService},
     * it runs on a background (JobIntentService) thread and hence cannot
     * return any values to the main UI. So, the boolean value (true or false)
     * is conveyed to the calling activity through a local broadcast
     * @param context
     */
    private static void handleActionDoesRawDataExistInDatabase(Context context){
        UdiseDbHelper udiseDbHelper = new UdiseDbHelper(context);
        boolean result = udiseDbHelper.doesRawDataExistInDatabase(context);
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(ACTION_DOES_RAW_DATA_EXIST_IN_DATABASE);
        broadcastIntent.putExtra(PARAM_DOES_RAW_DATA_EXIST,result);
        LocalBroadcastManager.getInstance(context).sendBroadcast(broadcastIntent);
    }




    /**
     * Import an Excel file (.XLS) into SQLite
     * This method does check the validity of the excel file
     * It looks at markers (excel headers) to validate the file before importing
     * @param uriExcelFile Content Uri to the excel file
     */

    private static void handleActionImportRawData(Context context,Uri uriExcelFile){

        notificationHelper = new NotificationHelper(context);

        Workbook workbook = null;

        String notificationTitle = "Import Excel File";

        /*
         * Declare and initialise shared preferences
         * Shared preferences are used for storing the state
         * of the import progress. This is required to decide
         * if the dialogfragment needs to be dismissed when the
         * app comes to foreground after being interrupted by
         * the user or the operating system. If the import process
         * was completed while the UI was not visible, once the
         * UI becomes visible the dialogfragment will still be
         * frozen at an incorrect progress position. In order to
         * overcome this, we will keep updating the progress in a
         * shared preference variable even if the UI is not visible.
         * Once the UI becomes visible, we will check the shared preference
         * variable and check if the import has been completed already.
         * If the import has been completed, we will dismiss the dialogfragment.
         */

        SharedPreferences mPreferences;
        String sharedPrefFile = SchoolReportsConstants.SHARED_PREFERENCES_FILE;
        mPreferences = context.getSharedPreferences(sharedPrefFile,Context.MODE_PRIVATE);

        sendProgressBroadcast(context,ACTION_IMPORT_RAW_DATA,false,false,false,false,"Loading file, Please Wait...",0,UserDataModel.USER_TYPE_UNKNOWN);
        NotificationCompat.Builder notificationBuilder = notificationHelper.getNotificationWithoutAlerts(notificationTitle,"Loading File, Please Wait...");
        notificationHelper.getManager().notify(NOTIFICATION_ID,notificationBuilder.build());



        try {
            workbook = WorkbookFactory.create(context.getContentResolver().openInputStream(uriExcelFile));
            Sheet firstSheet = null;

            if(workbook!=null){
                /* Read the first worksheet from the excel file */
                firstSheet = workbook.getSheetAt(0);
            }



            int firstRowNum,lastRowNum,firstColNum,lastColNum;

            firstRowNum = firstSheet.getFirstRowNum();
            lastRowNum = firstSheet.getLastRowNum();

            DataFormatter dataFormatter = new DataFormatter();

            /*
              If it is an empty file, return
             */
            if(lastRowNum<2){
                sendProgressBroadcast(context,ACTION_IMPORT_RAW_DATA,true,false,false,false,"The imported excel file does not contain any data. Please choose a valid file.",0,UserDataModel.USER_TYPE_UNKNOWN);
                notificationBuilder = notificationHelper.getNotificationWithAlerts(notificationTitle,"The imported excel file does not contain any data. Please choose a valid file.");
                notificationHelper.getManager().notify(NOTIFICATION_ID,notificationBuilder.build());
                Log.e(TAG,"Imported excel file contains less than two rows");
                if (workbook != null) {
                    workbook.close();
                }
                return;
            }

            /**
             * First check if it is a valid udise data excel file
             * A file may be considered valid if it contains certain header names in the first row
             */
            Row firstRow = firstSheet.getRow(0);
            firstColNum = firstRow.getFirstCellNum();
            lastColNum = firstRow.getLastCellNum();

            Resources res = context.getResources();
            String[] excelHeaders = res.getStringArray(R.array.excel_headers);

            if(lastColNum<excelHeaders.length){
                sendProgressBroadcast(context,ACTION_IMPORT_RAW_DATA,true,false,false,false,"The imported excel file does not contain valid UDISE data. Please choose another file containing valid UDISE data.",0,UserDataModel.USER_TYPE_UNKNOWN);
                notificationBuilder = notificationHelper.getNotificationWithAlerts(notificationTitle,"The imported excel file does not contain valid UDISE data. Please choose another file containing valid UDISE data.");
                notificationHelper.getManager().notify(NOTIFICATION_ID,notificationBuilder.build());
                Log.e(TAG,"Imported excel file contains only " + lastColNum + " columns which is lesser than standard " + excelHeaders.length + " columns.");
                if (workbook != null) {
                    workbook.close();
                }
                return;
            }

            /*
             * Send a broadcast that the imported file is being analysed for a valid data format
             */
            sendProgressBroadcast(context,ACTION_IMPORT_RAW_DATA,false,true,false,false,"Analyzing headers, please wait...",0,UserDataModel.USER_TYPE_UNKNOWN);


            /*
             * Check if all the headers present in the string array 'excelHeader' are also present in the excel file
             */
            for(int i=firstColNum;i<excelHeaders.length;i++){
                Cell cell = firstRow.getCell(i);
                String cellString = dataFormatter.formatCellValue(cell);
                if(!excelHeaders[i].equals(cellString)){
                    sendProgressBroadcast(context,ACTION_IMPORT_RAW_DATA,true,false,false,false,"The imported excel file does not contain UDISE data in a valid format. Please export the raw data to excel with headers and then save the file as xls file using MS-Excel software",0,UserDataModel.USER_TYPE_UNKNOWN);
                    Log.e(TAG,"Imported excel file contains unknown header label " + excelHeaders[i] + " instead of expected label " + cellString + " at position " + i);
                    notificationBuilder = notificationHelper.getNotificationWithAlerts(notificationTitle,"The imported excel file does not contain UDISE data in a valid format. Please export the raw data to excel with headers and then save the file as xls file using MS-Excel software");
                    notificationHelper.getManager().notify(NOTIFICATION_ID,notificationBuilder.build());
                    if (workbook != null) {
                        workbook.close();
                    }
                    return;
                }
            }


            /**
             * Determine the number of Academic Years, States, Districts, Zones and Schools
             */
            String[] acYears;
            String[] stateNames;
            String[] districtNames;
            String[] zoneNames;
            String[] schoolCodes;
            Set<String> acYearsSet = new HashSet<String>();
            Set<String> stateNamesSet = new HashSet<String>();
            Set<String> districtNamesSet = new HashSet<String>();
            Set<String> zoneNamesSet = new HashSet<String>();
            Set<String> schoolCodesSet = new HashSet<String>();
            String cellString;
            Cell cell;
            Row row;
            int numberOfStates=0,numberOfDistricts=0,numberOfAcademicYears=0,numberOfZones=0,numberOfSchools=0;
            int percentageCompleted=0;

            for(int rowIndex=firstRowNum+1;rowIndex<lastRowNum+1;rowIndex++){
                row = firstSheet.getRow(rowIndex);
                firstColNum = row.getFirstCellNum();
                /*
                 * Iterate from column 0 to column 10 since ac_years, states, districts,
                 * zones and schools are all within the first 11 columns
                 */
                //TODO:
                //Need to use constants instead of hard coding numerals in
                //the if-else structure
                //Typically there should be column index constants for
                //all columns
                //Also currently this logic currently also adds all the blank
                //rows if present in the excel file. Need to check it!

                for(int colIndex=firstColNum;colIndex<11;colIndex++){
                    cell = row.getCell(colIndex);
                    cellString = dataFormatter.formatCellValue(cell);
                    cellString = cellString.trim();
                    /*
                     * Add this value to the list if and only if it
                     * is not empty
                     */
                    if(cellString.length()!=0){
                        if(colIndex==0){
                            acYearsSet.add(cellString);
                        }
                        else if(colIndex==1){
                            stateNamesSet.add(cellString);
                        }
                        else if(colIndex==3){
                            districtNamesSet.add(cellString);
                        }
                        else if(colIndex==5){
                            zoneNamesSet.add(cellString);
                        }
                        else if(colIndex==10){
                            schoolCodesSet.add(cellString);
                        }
                    }
                }
                percentageCompleted = (rowIndex*100/lastRowNum);
                String progressMessage = "Analysing imported file, " + percentageCompleted+ " % complete ...";
                sendProgressBroadcast(context,ACTION_IMPORT_RAW_DATA,false,true,false,false,progressMessage,percentageCompleted,UserDataModel.USER_TYPE_UNKNOWN);
            }


            acYears = acYearsSet.toArray(new String[acYearsSet.size()]);
            stateNames = stateNamesSet.toArray(new String[stateNamesSet.size()]);
            districtNames = districtNamesSet.toArray(new String[districtNamesSet.size()]);
            zoneNames = zoneNamesSet.toArray(new String[zoneNamesSet.size()]);
            schoolCodes = schoolCodesSet.toArray(new String[schoolCodesSet.size()]);
            numberOfAcademicYears = acYears.length;
            numberOfStates = stateNames.length;
            numberOfDistricts = districtNames.length;
            numberOfZones = zoneNames.length;
            numberOfSchools = schoolCodes.length;

            int userType;
            if(numberOfStates>1){
                userType = UserDataModel.USER_TYPE_NATIONAL;
            }
            else if(numberOfStates==1) {
                if(numberOfDistricts>1){
                    userType = UserDataModel.USER_TYPE_STATE;
                }
                else if(numberOfDistricts==1){
                    if(numberOfZones>1){
                        userType = UserDataModel.USER_TYPE_DISTRICT;
                    }
                    else if(numberOfZones==1){
                        userType = UserDataModel.USER_TYPE_ZONE;
                    }
                    else {
                        userType = UserDataModel.USER_TYPE_UNKNOWN;
                    }
                }
                else{
                    userType = UserDataModel.USER_TYPE_UNKNOWN;
                }
            }
            else{
                /*
                 * This case is only possible if numberOfStates is equal to zero
                 * If that's the case this issue will need to be tackled afterwards
                 */
                userType = UserDataModel.USER_TYPE_UNKNOWN;
            }

            //TODO: Clean-up this broadcast
            String analysisMessage = "Analysis Complete";
            sendProgressBroadcast(context,ACTION_IMPORT_RAW_DATA,false,false,false,true,analysisMessage,100,userType);

            /*
             * Check if it is an invalid excel file containing only headers
             */
            if(numberOfAcademicYears<1 || numberOfStates<1 || numberOfDistricts<1 || numberOfZones<1 || numberOfSchools<1 || userType==UserDataModel.USER_TYPE_UNKNOWN){
                sendProgressBroadcast(context,ACTION_IMPORT_RAW_DATA,true,false,false,false,"Selected Excel file does not contain valid UDISE data. Please choose a valid file.",100,userType);
                notificationBuilder = notificationHelper.getNotificationWithAlerts(notificationTitle,"The imported excel file does not contain valid UDISE data. Please choose a valid file.");
                notificationHelper.getManager().notify(NOTIFICATION_ID,notificationBuilder.build());
                if (workbook != null) {
                    workbook.close();
                }
                return;
            }


            /*
             * Start actual data insertion into SQLite database using content provider
             */
            ArrayList<String> oneExcelRowAllCells = new ArrayList<>();
            ContentValues contentValuesForInsertion = new ContentValues();
            UdiseDbHelper udiseDbHelper = new UdiseDbHelper(context);
            for(int rowIndex=firstRowNum+1;rowIndex<lastRowNum+1;rowIndex++) {
                row = firstSheet.getRow(rowIndex);

                /*
                 * Check if the first cell of the row is blank
                 * If it is blank skip this row, otherwise read
                 * data from the row
                 */
                cell = row.getCell(0);
                cellString = dataFormatter.formatCellValue(cell);
                if(cellString.isEmpty()){
                    continue;
                }


                /*
                 * Loop through all cells of a row and store it in a string array list
                 */
                for(int colIndex=0;colIndex<excelHeaders.length;colIndex++){
                    cell = row.getCell(colIndex);
                    cellString = dataFormatter.formatCellValue(cell);
                    /* Trim the cell value read from excel and add it to ArrayList */
                    oneExcelRowAllCells.add(colIndex,cellString.trim());
                }
                /*
                 * Convert the ArrayList into Content Values for insertion
                 */
                contentValuesForInsertion = udiseDbHelper.convertExcelRowToContentValues(oneExcelRowAllCells);

                /*
                 * Insert the values into RawData table using Content Provider
                 */
                Uri returnedUri;
                returnedUri = context.getContentResolver().insert(UdiseContract.RawData.CONTENT_URI,contentValuesForInsertion);
                /*
                 * Clear values from ArrayList and Content Values variables
                 */
                oneExcelRowAllCells.clear();
                contentValuesForInsertion.clear();
                /*
                 * Send progress update broadcast
                 */
                percentageCompleted = (rowIndex*100/lastRowNum);
                String progressMessage = "Importing Row " + rowIndex + " out of " + lastRowNum + " [ " + percentageCompleted+ " % complete ]";
                sendProgressBroadcast(context,ACTION_IMPORT_RAW_DATA,false,false,true,false,progressMessage,percentageCompleted,userType);
                notificationBuilder = notificationHelper.getNotificationWithProgress(notificationTitle,progressMessage,percentageCompleted);
                notificationHelper.getManager().notify(NOTIFICATION_ID,notificationBuilder.build());

                SharedPreferences.Editor preferenceEditor = mPreferences.edit();
                preferenceEditor.putBoolean(SHARED_PREFERENCES_KEY_IS_IMPORTING,true);
                preferenceEditor.putInt(SHARED_PREFERENCES_KEY_PROGRESS,percentageCompleted);
                preferenceEditor.apply();
            }



            /*
             * Close the workbook to prevent memory leaks!
             */
            if (workbook != null) {
                workbook.close();
            }

            /*
             * Send final broadcast to mark the successful end of import process
             * This broadcast will help in dismissing the import dialog once
             * the import process is completed.
             */
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(ACTION_IMPORT_RAW_DATA);
            broadcastIntent.putExtra(PARAM_IMPORT_ENDED_SUCCESSFULLY,true);
            LocalBroadcastManager.getInstance(context).sendBroadcast(broadcastIntent);

            /*
             * Send the final notification to indicate the end of import process
             * Also, create a pending intent so that if the user clicks the
             * notification, the mainactivity of the app is opened
             *
             * Before showing any new notification, cancel all the previous notifications
             */
            notificationHelper.getManager().cancelAll();

            Intent pIntent = new Intent( context,MainActivity.class);
            pIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(context,0,pIntent,PendingIntent.FLAG_CANCEL_CURRENT);

            notificationBuilder = notificationHelper.getFinalNotificationWithAlerts(notificationTitle,"Excel file imported successfully!",pendingIntent);
            notificationHelper.getManager().notify(NOTIFICATION_ID,notificationBuilder.build());


            /*
             * Update shared preferences to indicate that import process has been
             * completed successfully
             */
            percentageCompleted = 100;
            SharedPreferences.Editor preferenceEditor = mPreferences.edit();
            preferenceEditor.putBoolean(SHARED_PREFERENCES_KEY_IS_IMPORTING,true);
            preferenceEditor.putInt(SHARED_PREFERENCES_KEY_PROGRESS,percentageCompleted);
            preferenceEditor.apply();


        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidFormatException e) {
            Log.e(TAG,"Invalid Excel File Format Exception!");
            sendProgressBroadcast(context,ACTION_IMPORT_RAW_DATA,true,false,false,false,"The imported excel file does not contain UDISE data in a valid format. Please export the raw data to excel with headers and then save the file as XLS file using MS-Excel software",0,UserDataModel.USER_TYPE_UNKNOWN);
            notificationBuilder = notificationHelper.getNotificationWithAlerts(notificationTitle,"The imported excel file does not contain UDISE data in a valid format. Please export the raw data to excel with headers and then save the file as xls file using MS-Excel software");
            notificationHelper.getManager().notify(NOTIFICATION_ID,notificationBuilder.build());
            e.printStackTrace();
        } catch (NullPointerException e){
            e.printStackTrace();
        }

    }

    /**
     * This method creates a local broadcast to send progress updates to the UI
     * Since almost all the methods of this class are called through ImportJobIntentService
     * those methods cannot update the UI directly. Hence they require the service of this
     * custom broadcaster. This custom broadcaster internally calls the standard local broadcaster
     * method provided by Android. This method is customised to take a few extra parameters for
     * correctly updating the UI like progressbar etc.
     *
     * @param context context of the caller
     * @param action which action does the broadcast belong to e.g, ACTION_IMPORT_RAW_DATA
     * @param errorFlag boolean flag indicating whether this broadcast is conveying an error
     *                  message or a normal message
     * @param isAnalysing boolean indicating whether the excel file is currently under analysis
     *                    or not
     * @param isImporting boolean indicating whether the data is being currently imported
     *                    This flag is set to true while the excel data is actually being
     *                    inserted into the sQLite database
     * @param isUserIdentified boolen indicating whether the user has been identified from
     *                         the excel data file or not
     * @param message string containing the broadcast message. It may be either an error
     *                message or a normal one depending upon other flags
     * @param percentageCompleted integer containing the percentage of the progress completed
     *                            This field should contain a percentage value if the file is
     *                            being analysed or imported
     * @param userType integer containing user type definition from UserDataModel
     */
    private static void sendProgressBroadcast(Context context, String action,boolean errorFlag,boolean isAnalysing,boolean isImporting,boolean isUserIdentified,String message,int percentageCompleted,int userType){
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(action);
        broadcastIntent.putExtra(PARAM_ERROR,errorFlag);
        broadcastIntent.putExtra(PARAM_IS_ANALYSING,isAnalysing);
        broadcastIntent.putExtra(PARAM_IS_IMPORTING,isImporting);
        broadcastIntent.putExtra(PARAM_USER_IDENTIFIED,isUserIdentified);
        broadcastIntent.putExtra(PARAM_MESSAGE,message);
        broadcastIntent.putExtra(PARAM_PERCENTAGE_COMPLETED,percentageCompleted);
        broadcastIntent.putExtra(PARAM_USER_TYPE,userType);
        LocalBroadcastManager.getInstance(context).sendBroadcast(broadcastIntent);
    }
}
