package in.hulum.udise;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

import in.hulum.udise.adapters.NumberOfSchoolsSummaryAdapter;
import in.hulum.udise.database.UdiseContract;
import in.hulum.udise.database.UdiseDbHelper;
import in.hulum.udise.models.ManagementWiseSchoolSummaryModel;
import in.hulum.udise.models.UserDataModel;
import in.hulum.udise.utils.SchoolReportsConstants;
import in.hulum.udise.utils.SchoolReportsHelper;

public class NumberOfSchools extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>,
        NumberOfSchoolsSummaryAdapter.NumberOfSchoolsSummaryAdapterOnClickHandler{

    private static final String TAG = "NumberOfSchools";

    Button buttonShowReport;
    RadioButton radioButtonZoneWise;

    private String stateName;
    private String districtCode;
    private String districtName;
    private String zoneCode;
    private String zoneName;
    private String clusterCode;
    private String clusterName;

    private String academicYear;

    private int userType;
    private int displaySummaryFor;

    int loaderId;
    Bundle arguments;

    private boolean showTitleInsteadOfSpinner = false;

    private static final int ID_MANAGEMENT_WISE_NUMBER_OF_SCHOOLS_NATIONAL_SUMMARY_LOADER = 1000;
    private static final int ID_MANAGEMENT_WISE_NUMBER_OF_SCHOOLS_STATE_SUMMARY_LOADER = 2000;
    private static final int ID_MANAGEMENT_WISE_NUMBER_OF_SCHOOLS_DISTRICT_SUMMARY_LOADER = 3000;
    private static final int ID_MANAGEMENT_WISE_NUMBER_OF_SCHOOLS_ZONE_SUMMARY_LOADER = 4000;
    private static final int ID_MANAGEMENT_WISE_NUMBER_OF_SCHOOLS_CLUSTER_SUMMARY_LOADER = 5000;

    private NumberOfSchoolsSummaryAdapter numberOfSchoolsSummaryAdapter;
    private RecyclerView mRecyclerView;
    private int mPosition = RecyclerView.NO_POSITION;
    UserDataModel userDataModel = new UserDataModel();
    List<String> academicYearsList = new ArrayList<>();

    private ProgressBar mLoadingIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_number_of_schools);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        Spinner spinnerAcademicYears = (Spinner)findViewById(R.id.spinner_academic_years);
        arguments = new Bundle();

        /*
         * We have to check if this is the root level report or if it is a
         * child level report. If this is a root level report then the intent
         * will not contain any data and we will take have to calculate the academic year
         * as well as the user type from the database. If this is a child level report
         * (report triggered from other report) then these parameters will be passed from
         * the calling activity along with the intent.
         */
        academicYear = getIntent().getStringExtra(SchoolReportsConstants.EXTRA_KEY_ACADEMIC_YEAR);
        if(academicYear==null){
            Log.e(TAG,"This is a root level report");
            UdiseDbHelper udiseDbHelper = new UdiseDbHelper(this);
            userDataModel = udiseDbHelper.determineUserTypeAndDataModel(this);


            for(int i=0;i<userDataModel.getAcademicYearsList().size();i++){
                academicYearsList.add(userDataModel.getAcademicYearsList().get(i).getAc_year());

                academicYear = academicYearsList.get(0);
                userType = userDataModel.getUserType();
                switch(userType){
                    case UserDataModel.USER_TYPE_NATIONAL:
                        /*
                         * No code required for national user
                         */
                        displaySummaryFor = SchoolReportsConstants.REPORT_DISPLAY_NATIONAL_SUMMARY;
                        break;
                    case UserDataModel.USER_TYPE_STATE:
                        /*
                         *
                         */
                        stateName = userDataModel.getStatesList().get(0).getStateName();
                        displaySummaryFor = SchoolReportsConstants.REPORT_DISPLAY_STATE_SUMMARY;
                        break;
                    case UserDataModel.USER_TYPE_DISTRICT:
                        districtCode = userDataModel.getDistrictsList().get(0).getDistrictCode();
                        districtName = userDataModel.getDistrictsList().get(0).getDistrictName();
                        displaySummaryFor = SchoolReportsConstants.REPORT_DISPLAY_DISTRICT_SUMMARY;
                        break;
                    case UserDataModel.USER_TYPE_ZONE:
                        zoneCode = userDataModel.getZoneList().get(0).getZoneCode();
                        zoneName = userDataModel.getZoneList().get(0).getZoneName();
                        displaySummaryFor = SchoolReportsConstants.REPORT_DISPLAY_ZONE_SUMMARY;
                        break;
                    /*
                     * Cluster-level summary is not possible by directly invoking this
                     * activity from the mainactivity. Cluster-level summary will be
                     * possible only when this activity is called with proper extra
                     * data in its intent. The intent must contain code of
                     * REPORT_DISPLAY_CLUSTER_SUMMARY to display cluster level summary
                     */
                    default:
                        throw new RuntimeException("Unknown userType discovered in " + getLocalClassName());

                }
            }

            ArrayAdapter<String> adapterAcademicYears =
                    new ArrayAdapter<String>(this,R.layout.spinner_item_custom,academicYearsList);
            adapterAcademicYears.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerAcademicYears.setAdapter(adapterAcademicYears);
            spinnerAcademicYears.setOnItemSelectedListener(new
                  AdapterView.OnItemSelectedListener() {
                      @Override
                      public void onItemSelected(AdapterView<?> parentView,
                                                 View selectedItemView, int position, long id) {
                          // selected item in the list
                          academicYear = parentView.getItemAtPosition(position).toString();
                          arguments.putString(SchoolReportsConstants.EXTRA_KEY_ACADEMIC_YEAR,academicYear);
                          getSupportLoaderManager().restartLoader(loaderId,arguments,NumberOfSchools.this);
                      }
                      @Override
                      public void onNothingSelected(AdapterView<?> parentView) {
                          // your code here
                      }
                  });
            academicYear = spinnerAcademicYears.getSelectedItem().toString();
        } else {
            /*
             * TODO: Handle this case if this is a child level report
             * We need to get the data from the intent like ac_year and
             * district code (or similar things). Also the spinner needs
             * to be disabled in that case
             */
            spinnerAcademicYears.setVisibility(View.GONE);
            showTitleInsteadOfSpinner = true;
            getSupportActionBar().setDisplayShowTitleEnabled(true);

            displaySummaryFor = getIntent().getIntExtra(SchoolReportsConstants.EXTRA_PARAM_KEY_REPORT_DISPLAY_SUMMARY_TYPE,SchoolReportsConstants.REPORT_DISPLAY_INVALID);
            academicYear = getIntent().getStringExtra(SchoolReportsConstants.EXTRA_KEY_ACADEMIC_YEAR);
            /*
             * The intent will contain ONLY ONE of the following:
             * - State name
             * - District name
             * - Zone name
             * - Cluster name
             * So we will copy it to all
             * Later we can determine the type by the variable displaySummaryFor
             */
            stateName = getIntent().getStringExtra(SchoolReportsConstants.EXTRA_KEY_NAME_STATE_DISTRICT_ZONE_CLUSTER);
            districtName = stateName;
            zoneName = stateName;
            clusterName = stateName;
            /*
             * The intent will contain ONLY ONE of the following:
             * - District code
             * - Zone code
             * - Cluster code
             * So we will copy it to all
             * Later we can determine the type by the variable displaySummaryFor
             */
            districtCode = getIntent().getStringExtra(SchoolReportsConstants.EXTRA_KEY_CODE_STATE_DISTRICT_ZONE_CLUSTER);
            zoneCode = districtCode;
            clusterCode = districtCode;
            Log.e(TAG,"OMG!! This is a child level report");
        }

        mRecyclerView = findViewById(R.id.recyclerview_summary_managementwise_number_of_schools);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
        numberOfSchoolsSummaryAdapter = new NumberOfSchoolsSummaryAdapter(this,this);
        mRecyclerView.setAdapter(numberOfSchoolsSummaryAdapter);


        arguments.putString(SchoolReportsConstants.EXTRA_KEY_ACADEMIC_YEAR,academicYear);
        switch(displaySummaryFor){
            case SchoolReportsConstants.REPORT_DISPLAY_NATIONAL_SUMMARY:
                /*
                 * In case of national summary, we do not require any code
                 * We simply create a summary of all the schools present
                 * in the database for the selected academic year
                 *
                 */
                loaderId = ID_MANAGEMENT_WISE_NUMBER_OF_SCHOOLS_NATIONAL_SUMMARY_LOADER;
                if(showTitleInsteadOfSpinner){
                    getSupportActionBar().setTitle("National Summary");
                    getSupportActionBar().setSubtitle(academicYear);
                }
                break;

            case SchoolReportsConstants.REPORT_DISPLAY_STATE_SUMMARY:
                /*
                 * State code is not present in the database
                 * So we only need the state name
                 */
                arguments.putString(SchoolReportsConstants.EXTRA_KEY_NAME_STATE_DISTRICT_ZONE_CLUSTER,stateName);
                loaderId = ID_MANAGEMENT_WISE_NUMBER_OF_SCHOOLS_STATE_SUMMARY_LOADER;
                if(showTitleInsteadOfSpinner){
                    getSupportActionBar().setTitle(stateName);
                    getSupportActionBar().setSubtitle(academicYear);
                }
                break;

            case SchoolReportsConstants.REPORT_DISPLAY_DISTRICT_SUMMARY:
                arguments.putString(SchoolReportsConstants.EXTRA_KEY_CODE_STATE_DISTRICT_ZONE_CLUSTER,districtCode);
                arguments.putString(SchoolReportsConstants.EXTRA_KEY_NAME_STATE_DISTRICT_ZONE_CLUSTER,districtName);
                loaderId = ID_MANAGEMENT_WISE_NUMBER_OF_SCHOOLS_DISTRICT_SUMMARY_LOADER;
                if(showTitleInsteadOfSpinner){
                    getSupportActionBar().setTitle(districtName + " - " + districtCode);
                    getSupportActionBar().setSubtitle(academicYear);
                }
                break;

            case SchoolReportsConstants.REPORT_DISPLAY_ZONE_SUMMARY:
                arguments.putString(SchoolReportsConstants.EXTRA_KEY_CODE_STATE_DISTRICT_ZONE_CLUSTER,zoneCode);
                arguments.putString(SchoolReportsConstants.EXTRA_KEY_NAME_STATE_DISTRICT_ZONE_CLUSTER,zoneName);
                loaderId = ID_MANAGEMENT_WISE_NUMBER_OF_SCHOOLS_ZONE_SUMMARY_LOADER;
                if(showTitleInsteadOfSpinner){
                    getSupportActionBar().setTitle(zoneName + " - " + zoneCode);
                    getSupportActionBar().setSubtitle(academicYear);
                }
                break;

            case SchoolReportsConstants.REPORT_DISPLAY_CLUSTER_SUMMARY:
                arguments.putString(SchoolReportsConstants.EXTRA_KEY_CODE_STATE_DISTRICT_ZONE_CLUSTER,clusterCode);
                arguments.putString(SchoolReportsConstants.EXTRA_KEY_NAME_STATE_DISTRICT_ZONE_CLUSTER,clusterName);
                loaderId = ID_MANAGEMENT_WISE_NUMBER_OF_SCHOOLS_CLUSTER_SUMMARY_LOADER;
                if(showTitleInsteadOfSpinner){
                    getSupportActionBar().setTitle(clusterName + " - " + clusterCode);
                    getSupportActionBar().setSubtitle(academicYear);
                }
                break;
        }

        getSupportLoaderManager().initLoader(loaderId,arguments,this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String acYear = args.getString(SchoolReportsConstants.EXTRA_KEY_ACADEMIC_YEAR);
        String code = args.getString(SchoolReportsConstants.EXTRA_KEY_CODE_STATE_DISTRICT_ZONE_CLUSTER);
        String name = args.getString(SchoolReportsConstants.EXTRA_KEY_NAME_STATE_DISTRICT_ZONE_CLUSTER);


        switch (id){
            case ID_MANAGEMENT_WISE_NUMBER_OF_SCHOOLS_NATIONAL_SUMMARY_LOADER:
                Uri udiseSchoolsNationalUri = UdiseContract.RawData.CONTENT_URI;
                String[] projectionNational = {
                        UdiseContract.RawData.COLUMN_UDISE_SCHOOL_CODE,
                        UdiseContract.RawData.COLUMN_SCHOOL_MANAGEMENT,
                        UdiseContract.RawData.COLUMN_SCHOOL_MANAGEMENT_DESCRIPTION,
                        UdiseContract.RawData.COLUMN_SCHOOL_CATEGORY
                };

                String selectionStringNational = UdiseContract.RawData.COLUMN_AC_YEAR + " = ?";

                String[] selectionArgumentsNational = {acYear};
                return new CursorLoader(this,udiseSchoolsNationalUri,projectionNational,selectionStringNational,selectionArgumentsNational,null);

            case ID_MANAGEMENT_WISE_NUMBER_OF_SCHOOLS_STATE_SUMMARY_LOADER:
                Uri udiseSchoolsStateUri = UdiseContract.RawData.CONTENT_URI;
                String[] projectionState = {
                        UdiseContract.RawData.COLUMN_UDISE_SCHOOL_CODE,
                        UdiseContract.RawData.COLUMN_SCHOOL_MANAGEMENT,
                        UdiseContract.RawData.COLUMN_SCHOOL_MANAGEMENT_DESCRIPTION,
                        UdiseContract.RawData.COLUMN_SCHOOL_CATEGORY
                };

                String selectionStringState = UdiseContract.RawData.COLUMN_STATE_NAME + " = ? and " + UdiseContract.RawData.COLUMN_AC_YEAR + " = ?";

                String[] selectionArgumentsState = {name, acYear};
                return new CursorLoader(this,udiseSchoolsStateUri,projectionState,selectionStringState,selectionArgumentsState,null);


            case ID_MANAGEMENT_WISE_NUMBER_OF_SCHOOLS_DISTRICT_SUMMARY_LOADER:
                Uri udiseSchoolsDistrictUri = UdiseContract.RawData.CONTENT_URI;
                //TODO: Need to clean this up. Perhaps the Zonecode and Zonename columns are unnecessary
                //Need to check it!!!
                String[] projectionDistrict = {
                        UdiseContract.RawData.COLUMN_UDISE_SCHOOL_CODE,
                        //UdiseContract.RawData.COLUMN_ZONE_CODE,
                        //UdiseContract.RawData.COLUMN_ZONE_NAME,
                        UdiseContract.RawData.COLUMN_SCHOOL_MANAGEMENT,
                        UdiseContract.RawData.COLUMN_SCHOOL_MANAGEMENT_DESCRIPTION,
                        UdiseContract.RawData.COLUMN_SCHOOL_CATEGORY
                };

                String selectionStringDistrict = UdiseContract.RawData.COLUMN_DISTRICT_CODE + " = ? and " + UdiseContract.RawData.COLUMN_AC_YEAR + " = ?";

                String[] selectionArgumentsDistrict = {code, acYear};
                return new CursorLoader(this,udiseSchoolsDistrictUri,projectionDistrict,selectionStringDistrict,selectionArgumentsDistrict,null);

            case ID_MANAGEMENT_WISE_NUMBER_OF_SCHOOLS_ZONE_SUMMARY_LOADER:
                Uri udiseSchoolsZoneUri = UdiseContract.RawData.CONTENT_URI;
                String[] projectionZone = {
                        UdiseContract.RawData.COLUMN_UDISE_SCHOOL_CODE,
                        UdiseContract.RawData.COLUMN_SCHOOL_MANAGEMENT,
                        UdiseContract.RawData.COLUMN_SCHOOL_MANAGEMENT_DESCRIPTION,
                        UdiseContract.RawData.COLUMN_SCHOOL_CATEGORY
                };

                String selectionStringZone = UdiseContract.RawData.COLUMN_ZONE_CODE + " = ? and " + UdiseContract.RawData.COLUMN_AC_YEAR + " = ?";

                String[] selectionArgumentsZone = {code, acYear};
                return new CursorLoader(this,udiseSchoolsZoneUri,projectionZone,selectionStringZone,selectionArgumentsZone,null);

            case ID_MANAGEMENT_WISE_NUMBER_OF_SCHOOLS_CLUSTER_SUMMARY_LOADER:
                Uri udiseSchoolsClusterUri = UdiseContract.RawData.CONTENT_URI;
                String[] projectionCluster = {
                        UdiseContract.RawData.COLUMN_UDISE_SCHOOL_CODE,
                        UdiseContract.RawData.COLUMN_SCHOOL_MANAGEMENT,
                        UdiseContract.RawData.COLUMN_SCHOOL_MANAGEMENT_DESCRIPTION,
                        UdiseContract.RawData.COLUMN_SCHOOL_CATEGORY
                };

                String selectionStringCluster = UdiseContract.RawData.COLUMN_CLUSTER_CODE + " = ? and " + UdiseContract.RawData.COLUMN_AC_YEAR + " = ?";

                String[] selectionArgumentsCluster = {code, acYear};
                return new CursorLoader(this,udiseSchoolsClusterUri,projectionCluster,selectionStringCluster,selectionArgumentsCluster,null);

            default:
                throw new RuntimeException("Loader not Implemented: " + id);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if(data==null){
            Log.e(TAG,"Cursor Loader Error: OnLoadFinished received null Cursor in class " + getLocalClassName());
            return;
        }

        switch(loader.getId()){
            case ID_MANAGEMENT_WISE_NUMBER_OF_SCHOOLS_NATIONAL_SUMMARY_LOADER:
                List<ManagementWiseSchoolSummaryModel> nationalSummaryList = SchoolReportsHelper.nationalManagementWiseSummary(data);
                numberOfSchoolsSummaryAdapter.swapDataList(nationalSummaryList);
                break;

            case ID_MANAGEMENT_WISE_NUMBER_OF_SCHOOLS_STATE_SUMMARY_LOADER:
                List<ManagementWiseSchoolSummaryModel> stateSummaryList = SchoolReportsHelper.stateManagementWiseSummary(data,stateName);
                numberOfSchoolsSummaryAdapter.swapDataList(stateSummaryList);
                break;

            case ID_MANAGEMENT_WISE_NUMBER_OF_SCHOOLS_DISTRICT_SUMMARY_LOADER:
                List<ManagementWiseSchoolSummaryModel> districtSummaryList = SchoolReportsHelper.districtManagementWiseSummary(data,districtCode,districtName);
                Log.d(TAG,"Loader finished!! Items are " + districtSummaryList.size());
                numberOfSchoolsSummaryAdapter.swapDataList(districtSummaryList);
                break;

            case ID_MANAGEMENT_WISE_NUMBER_OF_SCHOOLS_ZONE_SUMMARY_LOADER:
                List<ManagementWiseSchoolSummaryModel> zoneSummaryList = SchoolReportsHelper.zoneManagementWiseSummary(data,zoneCode,zoneName);
                numberOfSchoolsSummaryAdapter.swapDataList(zoneSummaryList);
                break;

            case ID_MANAGEMENT_WISE_NUMBER_OF_SCHOOLS_CLUSTER_SUMMARY_LOADER:
                List<ManagementWiseSchoolSummaryModel> clusterSummaryList = SchoolReportsHelper.clusterManagementWiseSummary(data,clusterCode,clusterName);
                numberOfSchoolsSummaryAdapter.swapDataList(clusterSummaryList);
                break;

            default:
                throw new RuntimeException("Loader not implemented yet in onLoadFinished with id " + loader.getId());
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        numberOfSchoolsSummaryAdapter.swapDataList(null);
    }

    @Override
    public void onClick(int reportDisplayLevel, String zoneDistrictOrStateCode, String zoneDistrictOrStateName) {
        Intent intent;
        Log.d(TAG,"Report Request received with level " + reportDisplayLevel);
        switch(reportDisplayLevel){

            case SchoolReportsConstants.REPORT_DISPLAY_LEVEL_STATEWISE:
                /*
                 * Use REPORT_DISPLAY_LEVEL_WISE_TYPE for passing the type of report
                 */
                intent = new Intent(this,NumberOfSchoolsLevelWise.class);
                intent.putExtra(SchoolReportsConstants.EXTRA_KEY_ACADEMIC_YEAR,academicYear);
                //intent.putExtra(SchoolReportsConstants.EXTRA_KEY_CODE_STATE_DISTRICT_ZONE_CLUSTER,zoneDistrictOrStateCode);
                //intent.putExtra(SchoolReportsConstants.EXTRA_KEY_NAME_STATE_DISTRICT_ZONE_CLUSTER,zoneDistrictOrStateName);
                intent.putExtra(SchoolReportsConstants.EXTRA_PARAM_KEY_REPORT_DISPLAY_LEVEL_WISE_TYPE,SchoolReportsConstants.REPORT_DISPLAY_LEVEL_STATEWISE);
                startActivity(intent);
                break;

            case SchoolReportsConstants.REPORT_DISPLAY_LEVEL_DISTRICTWISE:
                intent = new Intent(this,NumberOfSchoolsLevelWise.class);
                intent.putExtra(SchoolReportsConstants.EXTRA_KEY_ACADEMIC_YEAR,academicYear);
                intent.putExtra(SchoolReportsConstants.EXTRA_KEY_CODE_STATE_DISTRICT_ZONE_CLUSTER,zoneDistrictOrStateCode);
                intent.putExtra(SchoolReportsConstants.EXTRA_KEY_NAME_STATE_DISTRICT_ZONE_CLUSTER,zoneDistrictOrStateName);
                intent.putExtra(SchoolReportsConstants.EXTRA_PARAM_KEY_REPORT_DISPLAY_LEVEL_WISE_TYPE,SchoolReportsConstants.REPORT_DISPLAY_LEVEL_DISTRICTWISE);
                startActivity(intent);
                break;

            case SchoolReportsConstants.REPORT_DISPLAY_LEVEL_ZONEWISE:
                intent = new Intent(this,NumberOfSchoolsLevelWise.class);
                intent.putExtra(SchoolReportsConstants.EXTRA_KEY_ACADEMIC_YEAR,academicYear);
                intent.putExtra(SchoolReportsConstants.EXTRA_KEY_CODE_STATE_DISTRICT_ZONE_CLUSTER,zoneDistrictOrStateCode);
                intent.putExtra(SchoolReportsConstants.EXTRA_KEY_NAME_STATE_DISTRICT_ZONE_CLUSTER,zoneDistrictOrStateName);
                intent.putExtra(SchoolReportsConstants.EXTRA_PARAM_KEY_REPORT_DISPLAY_LEVEL_WISE_TYPE,SchoolReportsConstants.REPORT_DISPLAY_LEVEL_ZONEWISE);
                startActivity(intent);
                break;

            case SchoolReportsConstants.REPORT_DISPLAY_LEVEL_CLUSTERWISE:
                intent = new Intent(this,NumberOfSchoolsLevelWise.class);
                intent.putExtra(SchoolReportsConstants.EXTRA_KEY_ACADEMIC_YEAR,academicYear);
                intent.putExtra(SchoolReportsConstants.EXTRA_KEY_CODE_STATE_DISTRICT_ZONE_CLUSTER,zoneDistrictOrStateCode);
                intent.putExtra(SchoolReportsConstants.EXTRA_KEY_NAME_STATE_DISTRICT_ZONE_CLUSTER,zoneDistrictOrStateName);
                intent.putExtra(SchoolReportsConstants.EXTRA_PARAM_KEY_REPORT_DISPLAY_LEVEL_WISE_TYPE,SchoolReportsConstants.REPORT_DISPLAY_LEVEL_CLUSTERWISE);
                startActivity(intent);
                break;

            case SchoolReportsConstants.REPORT_DISPLAY_LEVEL_TAKE_NO_ACTION:
                break;

            case SchoolReportsConstants.REPORT_DISPLAY_INVALID:
                break;
        }
        Log.d(TAG,"You clicked it!!!!");
    }

    /*
     * This code provides "Up" navigation for the back button in appbar
     * of this activity. We simply call the finish method to end this
     * activity.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
