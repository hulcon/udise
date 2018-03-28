package in.hulum.udise.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
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
import in.hulum.udise.NumberOfSchools;
import in.hulum.udise.R;

import static android.app.Activity.RESULT_OK;

/**
 * Created by Irshad on 23-03-2018.
 */

public class ImportDialogFragment extends DialogFragment {

    private TextView mActionCancel;
    private TextView mActionChooseExcelFile;
    private TextView mMessageDisplay;
    private ProgressBar mImportDialogProgressBar;

    private ResponseReceiver responseReceiver = new ResponseReceiver();

    static final int REQUEST_CODE_IMPORT_FILE_PICKER_ACTIVITY_FOR_RESULT = 7;

    private static final String TAG = "ImportDialogFragment";


    public ImportDialogFragment() {
        super();
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.dialog_fragment_import2,container,false);

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
        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        IntentFilter myFilter = new IntentFilter();
        myFilter.addAction(ImportUdiseData.ACTION_IMPORT_RAW_DATA);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(responseReceiver,myFilter);
        Log.d(TAG,"Receiver registered!!!!");
    }

    @Override
    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(responseReceiver);
        Log.d(TAG,"Alas Receiver unregistered!!!!");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
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
        }
    }


    public class ResponseReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG,"Entered onReceive in dialog fragment!! with action " + intent.getAction());
            if(intent.getAction().equals(ImportUdiseData.ACTION_IMPORT_RAW_DATA)){
                boolean isError = intent.getBooleanExtra(ImportUdiseData.PARAM_ERROR,false);
                boolean isAnalyzing = intent.getBooleanExtra(ImportUdiseData.PARAM_IS_ANALYSING,false);
                boolean isImporting = intent.getBooleanExtra(ImportUdiseData.PARAM_IS_IMPORTING,false);
                boolean isUserIdentified = intent.getBooleanExtra(ImportUdiseData.PARAM_USER_IDENTIFIED,false);
                boolean hasFinishedImportingSuccessfully = intent.getBooleanExtra(ImportUdiseData.PARAM_IMPORT_ENDED_SUCCESSFULLY,false);

                Log.d(TAG,"error is " + isError + " analysing " + isAnalyzing + " importing " + isImporting + " user identified " + isUserIdentified);
                if(hasFinishedImportingSuccessfully){
                    getDialog().dismiss();
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
                    //Log.d(TAG,"Analysing message " + msg + " and percent " + value);
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
                    //Log.d(TAG,"User identified is " + intent.getStringExtra(ImportData.PARAM_USER_TYPE));
                }
                else{
                    String msg = intent.getStringExtra(ImportUdiseData.PARAM_MESSAGE);
                    mMessageDisplay.setText(msg);
                    //Log.d(TAG,"Percent completed is  " + intent.getIntExtra(ImportData.PARAM_PERCENTAGE_COMPLETED,-1));
                    //Log.d(TAG,"Message is " + msg);
                }
            }
        }
    }
}
