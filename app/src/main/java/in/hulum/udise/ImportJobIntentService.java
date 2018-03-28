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
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class ImportJobIntentService extends JobIntentService {
    public static final String TAG = ImportJobIntentService.class.getSimpleName();

    /*
     * Unique Job ID for this service
     * This remains same for every action
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
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see JobIntentService
     */
    // TODO: Customize helper method
    public static void startActionFoo(Context context, String param1, String param2) {
        Intent intent = new Intent(context, ImportJobIntentService.class);
        intent.setAction(ImportUdiseData.ACTION_FOO);
        intent.putExtra(ImportUdiseData.EXTRA_PARAM1, param1);
        intent.putExtra(ImportUdiseData.EXTRA_PARAM2, param2);
        enqueueWork(context,ImportJobIntentService.class,IMPORT_JOB_ID,intent);
    }


    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see JobIntentService
     */
    public static void startActionBaz(Context context, String param1, String param2) {
        Intent intent = new Intent(context, ImportJobIntentService.class);
        intent.setAction(ImportUdiseData.ACTION_BAZ);
        intent.putExtra(ImportUdiseData.EXTRA_PARAM1, param1);
        intent.putExtra(ImportUdiseData.EXTRA_PARAM2, param2);
        enqueueWork(context,ImportJobIntentService.class,IMPORT_JOB_ID,intent);
    }



    /**
     * Starts this service to perform action Baz with the given parameters. If
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
        intent.setAction(ImportUdiseData.ACTION_DOES_RAW_DATA_EXISTS_IN_DATABASE);
        enqueueWork(context,ImportJobIntentService.class,IMPORT_JOB_ID,intent);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        ImportUdiseData.executeAction(this,intent);
    }

}
