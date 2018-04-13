package in.hulum.udise.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.JobIntentService;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import in.hulum.udise.ImportJobIntentService;
import in.hulum.udise.ImportUdiseData;
import in.hulum.udise.MainActivity;
import in.hulum.udise.R;
import in.hulum.udise.utils.SchoolReportsConstants;

import static android.app.Activity.RESULT_OK;

/**
 * Created by Irshad on 23-03-2018.
 * This fragment displays a dialog for importing excel file
 */

public class ImportDialogFragment extends DialogFragment {

    private TextView mActionCancel;
    private TextView mActionChooseExcelFile;
    private TextView mMessageDisplay;
    private ProgressBar mImportDialogProgressBar;

    private ResponseReceiver responseReceiver = new ResponseReceiver();

    private SharedPreferences mPreferences;
    private String sharedPrefFile = SchoolReportsConstants.SHARED_PREFERENCES_FILE;
    private static final String SHARED_PREFERENCES_KEY_IS_IMPORTING = "in.hulum.udise.sharedpreferences.keys.isimporting";
    private static final String SHARED_PREFERENCES_KEY_PROGRESS = "in.hulum.udise.sharedpreferences.keys.progress";

    private static final int REQUEST_CODE_IMPORT_FILE_PICKER_ACTIVITY_FOR_RESULT = 7;
    private static final String KEY_ACTION_BUTTONS_STATE = "in.hulum.udise.dialogfragment.key.ACTION_BUTTON_STATE";
    public static final String KEY_DIALOG_FRAGMENT_MESSAGE = "in.hulum.udise.dialogfragment.key.DISPLAY_MESSAGE";

    private static final String TAG = "ImportDialogFragment";

    private boolean isImporting;
    private int progressPercentage;


    public ImportDialogFragment() {
        super();
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.dialog_fragment_import,container,false);

        mMessageDisplay = view.findViewById(R.id.textview_import_dialog_message);
        mImportDialogProgressBar = view.findViewById(R.id.progressBar_dialog_import);
        mActionCancel = view.findViewById(R.id.action_cancel);
        mActionChooseExcelFile = view.findViewById(R.id.action_import_data);

        mActionCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });


        mActionChooseExcelFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("application/vnd.ms-excel");
                startActivityForResult(intent, REQUEST_CODE_IMPORT_FILE_PICKER_ACTIVITY_FOR_RESULT);
            }
        });

        /*
         * Check if we have saved the button state (on device rotation)
         * and restore it
         */

        if(savedInstanceState != null){
            int visibility = savedInstanceState.getInt(KEY_ACTION_BUTTONS_STATE);
            mActionCancel.setVisibility(visibility);
            mActionChooseExcelFile.setVisibility(visibility);
        }


        /*
         * Check if a custom message was set by setArguments method.
         * If a custom message was set, get it and display it in the textview
         * of the dialog fragment.
         *
         * In the absence of the custom message, the default message set in the
         * layout file will be displayed.
         */

        Bundle args = getArguments();
        if(args != null){
            String message = args.getString(KEY_DIALOG_FRAGMENT_MESSAGE);
            if(message != null){
                mMessageDisplay.setText(message);
            }
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        /*
         * Save the visibility state of action buttons.
         * If the visibility state of buttons is not saved,
         * the buttons are enabled even when the import process
         * is going on if the device is rotated.
         */
        outState.putInt(KEY_ACTION_BUTTONS_STATE,mActionCancel.getVisibility());
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter myFilter = new IntentFilter();
        myFilter.addAction(ImportUdiseData.ACTION_IMPORT_RAW_DATA);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(responseReceiver,myFilter);
        mPreferences = getContext().getSharedPreferences(sharedPrefFile,Context.MODE_PRIVATE);
        isImporting = mPreferences.getBoolean(SHARED_PREFERENCES_KEY_IS_IMPORTING,false);
        progressPercentage = mPreferences.getInt(SHARED_PREFERENCES_KEY_PROGRESS,0);
        if(isImporting){
            if(progressPercentage==100){
                mImportDialogProgressBar.setProgress(100);
                getDialog().dismiss();

                /*
                 * After dismissing the dialog, reset the preferences
                 * flag so that dialog appears the next time
                 */
                int percentageCompleted = 0;
                SharedPreferences.Editor preferenceEditor = mPreferences.edit();
                preferenceEditor.putBoolean(SHARED_PREFERENCES_KEY_IS_IMPORTING,false);
                preferenceEditor.putInt(SHARED_PREFERENCES_KEY_PROGRESS,percentageCompleted);
                preferenceEditor.apply();

                /*
                 * Determine the user type on background thread so that appropriate
                 * title and subtitle can be set on the navigation drawer in mainactivity
                 */
                ImportJobIntentService.startActionDetermineUserType(getContext());
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(responseReceiver);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode){
            case REQUEST_CODE_IMPORT_FILE_PICKER_ACTIVITY_FOR_RESULT:
                if(resultCode==RESULT_OK)
                {
                    Toast.makeText(getContext(),"Uri is " + data.getData().toString(),Toast.LENGTH_SHORT).show();
                    ImportJobIntentService.startActionImportRawData(getContext(),data.getData());
                    mActionCancel.setVisibility(View.INVISIBLE);
                    mActionChooseExcelFile.setVisibility(View.INVISIBLE);
                }
                break;

            default:
                Log.e(TAG,"Invalid code passed to onActivityResult method: Request Code " + requestCode);
                throw new IllegalArgumentException("Invalid code passed to onActivityResult method: Request Code " +requestCode);
        }
    }


    public class ResponseReceiver extends BroadcastReceiver {


        @Override
        public void onReceive(Context context, Intent intent) {

            /*
             * Check the type of broadcast received and act accordingly
             */
            if(ImportUdiseData.ACTION_IMPORT_RAW_DATA.equals(intent.getAction())){
                boolean isError = intent.getBooleanExtra(ImportUdiseData.PARAM_ERROR,false);
                boolean isAnalyzing = intent.getBooleanExtra(ImportUdiseData.PARAM_IS_ANALYSING,false);
                boolean isImporting = intent.getBooleanExtra(ImportUdiseData.PARAM_IS_IMPORTING,false);
                boolean isUserIdentified = intent.getBooleanExtra(ImportUdiseData.PARAM_USER_IDENTIFIED,false);
                boolean hasFinishedImportingSuccessfully = intent.getBooleanExtra(ImportUdiseData.PARAM_IMPORT_ENDED_SUCCESSFULLY,false);

                /*
                 * If import process has completed, dismiss the import dialog
                 */
                if(hasFinishedImportingSuccessfully){
                    getDialog().dismiss();
                    /*
                     * If this broadcast is being received by the fragment then
                     * it means that the fragment is in the foreground.
                     * Since the JobIntentService updates the shared preferences
                     * even when the fragment is in the foreground, we need to
                     * reset the preferences here so that the dialog appears the
                     * next time if needed.
                     *
                     */
                    int percentageCompleted = 0;
                    SharedPreferences.Editor preferenceEditor = mPreferences.edit();
                    preferenceEditor.putBoolean(SHARED_PREFERENCES_KEY_IS_IMPORTING,false);
                    preferenceEditor.putInt(SHARED_PREFERENCES_KEY_PROGRESS,percentageCompleted);
                    preferenceEditor.apply();

                    /*
                     * Determine the user type on background thread so that appropriate
                     * title and subtitle can be set on the navigation drawer in mainactivity
                     */
                    ImportJobIntentService.startActionDetermineUserType(context);
                }
                else if(isError){
                    String msg = intent.getStringExtra(ImportUdiseData.PARAM_MESSAGE);
                    mMessageDisplay.setText(msg);
                    mActionCancel.setVisibility(View.VISIBLE);
                    mActionChooseExcelFile.setVisibility(View.VISIBLE);
                }
                else if(isAnalyzing){
                    String msg = intent.getStringExtra(ImportUdiseData.PARAM_MESSAGE);
                    int value = intent.getIntExtra(ImportUdiseData.PARAM_PERCENTAGE_COMPLETED,0);
                    mMessageDisplay.setText(msg);
                    mImportDialogProgressBar.setProgress(value);
                }
                else if(isImporting){
                    String msg = intent.getStringExtra(ImportUdiseData.PARAM_MESSAGE);
                    int value = intent.getIntExtra(ImportUdiseData.PARAM_PERCENTAGE_COMPLETED,0);
                    mMessageDisplay.setText(msg);
                    mImportDialogProgressBar.setProgress(value);
                }
                else if(isUserIdentified){
                    String msg = intent.getStringExtra(ImportUdiseData.PARAM_MESSAGE);
                    mMessageDisplay.setText(msg);
                }
                else{
                    String msg = intent.getStringExtra(ImportUdiseData.PARAM_MESSAGE);
                    mMessageDisplay.setText(msg);
                }
            }
        }
    }
}
