package in.hulum.udise;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;

import static android.support.v4.app.JobIntentService.enqueueWork;

/**
 * An {@link JobIntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * This class has been upgraded and made Oreo compatible.
 * This class now extends new {@link JobIntentService} found in Oreo.
 *
 */
public class ImportJobIntentService extends JobIntentService {
    public static final String TAG = ImportJobIntentService.class.getSimpleName();

    /*
     * Unique Job ID for this service
     * It can be anything but should
     * remain same for every action
     */
    static final int IMPORT_JOB_ID = 1000;


    /**
     * The static method is used only as a bridge for outside classes
     * to start this JobIntentService. Since we are using other static
     * methods like startActionImportRawData(), we do not need this
     * Convenience method for enqueuing work in to this service.
     *
     *  static void enqueueWork(Context context, Intent work) {
     *       enqueueWork(context, SimpleJobIntentService.class, JOB_ID, work);
     *  }
     */



    /**
     * Starts this service to perform action DetermineUserType
     * This helps to display proper user type in the navigation drawer
     *
     * @see JobIntentService
     */
    public static void startActionDetermineUserType(Context context) {
        Intent intent = new Intent(context, ImportJobIntentService.class);
        intent.setAction(ImportUdiseData.ACTION_DETERMINE_USER_TYPE);
        enqueueWork(context,ImportJobIntentService.class,IMPORT_JOB_ID,intent);
    }



    /**
     * Starts this service to perform action import raw data with the given parameters. If
     * the service is already performing a task this action will be queued.
     * @param context
     * @param uriParam Uri of the excel file to be imported
     * @see JobIntentService
     */
    public static void startActionImportRawData(Context context, Uri uriParam){
        Intent intent = new Intent(context,ImportJobIntentService.class);
        intent.setAction(ImportUdiseData.ACTION_IMPORT_RAW_DATA);
        intent.setDataAndType(uriParam,"application/vnd.ms-excel");
        enqueueWork(context,ImportJobIntentService.class,IMPORT_JOB_ID,intent);
    }

    /**
     * Starts this JobIntentService to check whether the database contains
     * raw data or not. If the database is empty then the reports cannot be
     * displayed and user will be prompted to import excel file first.
     * @param context context of the caller class
     */
    public static void startActionDoesRawDataExistInDatabase(Context context){
        Intent intent = new Intent(context,ImportJobIntentService.class);
        intent.setAction(ImportUdiseData.ACTION_DOES_RAW_DATA_EXIST_IN_DATABASE);
        enqueueWork(context,ImportJobIntentService.class,IMPORT_JOB_ID,intent);
    }

    /**
     * Inherited method of this JObIntentService. This method is automatically invoked
     * when the {@link JobIntentService#enqueueWork(Context, Class, int, Intent)} is
     * called.
     */

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        ImportUdiseData.executeAction(this,intent);
    }

}
