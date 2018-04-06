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

public class NumberOfSchoolsLevelWise extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>,
        NumberOfSchoolsSummaryAdapter.NumberOfSchoolsSummaryAdapterOnClickHandler{

    private static final String TAG = "NumOfSchoolsLevelWise";

    private String stateName;
    private String districtCode;
    private String districtName;
    private String zoneCode;
    private String zoneName;
    private String clusterCode;
    private String clusterName;

    private String academicYear;

    private int userType;
    private int displayReportLevel;

    int loaderId;
    Bundle arguments;

    private static final int ID_NUMBER_OF_SCHOOLS_STATE_WISE_LOADER = 110;
    private static final int ID_NUMBER_OF_SCHOOLS_DISTRICT_WISE_LOADER = 120;
    private static final int ID_NUMBER_OF_SCHOOLS_ZONE_WISE_LOADER = 130;
    private static final int ID_NUMBER_OF_SCHOOLS_CLUSTER_WISE_LOADER = 140;

    private NumberOfSchoolsSummaryAdapter numberOfSchoolsSummaryAdapter;
    private RecyclerView mRecyclerView;
    private int mPosition = RecyclerView.NO_POSITION;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_number_of_schools);//activity_number_of_schools_level_wise);
        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        Spinner spinnerAcademicYears = (Spinner)findViewById(R.id.spinner_academic_years);
        spinnerAcademicYears.setVisibility(View.GONE);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        academicYear = getIntent().getStringExtra(SchoolReportsConstants.EXTRA_KEY_ACADEMIC_YEAR);
        displayReportLevel = getIntent().getIntExtra(SchoolReportsConstants.EXTRA_PARAM_KEY_REPORT_DISPLAY_LEVEL_WISE_TYPE,SchoolReportsConstants.REPORT_DISPLAY_INVALID);

        Log.d(TAG,"Received in levelwise module report request " + displayReportLevel + " ac year " + academicYear);
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
        Log.d(TAG,"Levelwise code is " + districtCode + " and name is " + districtName);

        mRecyclerView = findViewById(R.id.recyclerview_summary_managementwise_number_of_schools);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
        numberOfSchoolsSummaryAdapter = new NumberOfSchoolsSummaryAdapter(this,this);
        mRecyclerView.setAdapter(numberOfSchoolsSummaryAdapter);

        arguments = new Bundle();
        arguments.putString(SchoolReportsConstants.EXTRA_KEY_ACADEMIC_YEAR,academicYear);
        switch(displayReportLevel){
            case SchoolReportsConstants.REPORT_DISPLAY_LEVEL_STATEWISE:
                /*
                 * No code is required for state-wise list
                 * Simply generate a list of all the states
                 * present in the database
                 */
                loaderId = ID_NUMBER_OF_SCHOOLS_STATE_WISE_LOADER;
                getSupportActionBar().setTitle("State List");
                getSupportActionBar().setSubtitle(academicYear);
                break;

            case SchoolReportsConstants.REPORT_DISPLAY_LEVEL_DISTRICTWISE:
                arguments.putString(SchoolReportsConstants.EXTRA_KEY_NAME_STATE_DISTRICT_ZONE_CLUSTER,stateName);
                loaderId = ID_NUMBER_OF_SCHOOLS_DISTRICT_WISE_LOADER;
                getSupportActionBar().setTitle(stateName);
                getSupportActionBar().setSubtitle(academicYear);
                break;

            case SchoolReportsConstants.REPORT_DISPLAY_LEVEL_ZONEWISE:
                arguments.putString(SchoolReportsConstants.EXTRA_KEY_CODE_STATE_DISTRICT_ZONE_CLUSTER,districtCode);
                arguments.putString(SchoolReportsConstants.EXTRA_KEY_NAME_STATE_DISTRICT_ZONE_CLUSTER,districtName);
                loaderId = ID_NUMBER_OF_SCHOOLS_ZONE_WISE_LOADER;
                getSupportActionBar().setTitle(districtName + " - " + districtCode);
                getSupportActionBar().setSubtitle(academicYear);
                break;

            case SchoolReportsConstants.REPORT_DISPLAY_LEVEL_CLUSTERWISE:
                arguments.putString(SchoolReportsConstants.EXTRA_KEY_CODE_STATE_DISTRICT_ZONE_CLUSTER,zoneCode);
                arguments.putString(SchoolReportsConstants.EXTRA_KEY_NAME_STATE_DISTRICT_ZONE_CLUSTER,zoneName);
                loaderId = ID_NUMBER_OF_SCHOOLS_CLUSTER_WISE_LOADER;
                getSupportActionBar().setTitle(zoneName + " - " + zoneCode);
                getSupportActionBar().setSubtitle(academicYear);
                Log.d(TAG,"Switchcase for clusterwise activated with zone name " + zoneName);
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
            case ID_NUMBER_OF_SCHOOLS_STATE_WISE_LOADER:
                Uri udiseSchoolsNationalUri = UdiseContract.RawData.CONTENT_URI;
                String[] projectionNational = {
                        UdiseContract.RawData.COLUMN_UDISE_SCHOOL_CODE,
                        UdiseContract.RawData.COLUMN_STATE_NAME,
                        UdiseContract.RawData.COLUMN_SCHOOL_CATEGORY
                };

                String selectionStringNational = UdiseContract.RawData.COLUMN_AC_YEAR + " = ?";

                String[] selectionArgumentsNational = {acYear};
                return new CursorLoader(this,udiseSchoolsNationalUri,projectionNational,selectionStringNational,selectionArgumentsNational,null);

            case ID_NUMBER_OF_SCHOOLS_DISTRICT_WISE_LOADER:
                Uri udiseSchoolsStateUri = UdiseContract.RawData.CONTENT_URI;
                String[] projectionState = {
                        UdiseContract.RawData.COLUMN_UDISE_SCHOOL_CODE,
                        UdiseContract.RawData.COLUMN_DISTRICT_CODE,
                        UdiseContract.RawData.COLUMN_DISTRICT_NAME,
                        UdiseContract.RawData.COLUMN_SCHOOL_CATEGORY
                };

                String selectionStringState = UdiseContract.RawData.COLUMN_STATE_NAME + " = ? and " + UdiseContract.RawData.COLUMN_AC_YEAR + " = ?";

                String[] selectionArgumentsState = {name, acYear};
                return new CursorLoader(this,udiseSchoolsStateUri,projectionState,selectionStringState,selectionArgumentsState,null);


            case ID_NUMBER_OF_SCHOOLS_ZONE_WISE_LOADER:
                Uri udiseSchoolsDistrictUri = UdiseContract.RawData.CONTENT_URI;
                String[] projectionDistrict = {
                        UdiseContract.RawData.COLUMN_UDISE_SCHOOL_CODE,
                        UdiseContract.RawData.COLUMN_ZONE_CODE,
                        UdiseContract.RawData.COLUMN_ZONE_NAME,
                        UdiseContract.RawData.COLUMN_SCHOOL_CATEGORY
                };

                String selectionStringDistrict = UdiseContract.RawData.COLUMN_DISTRICT_CODE + " = ? and " + UdiseContract.RawData.COLUMN_AC_YEAR + " = ?";

                String[] selectionArgumentsDistrict = {code, acYear};
                return new CursorLoader(this,udiseSchoolsDistrictUri,projectionDistrict,selectionStringDistrict,selectionArgumentsDistrict,null);

            case ID_NUMBER_OF_SCHOOLS_CLUSTER_WISE_LOADER:
                Uri udiseSchoolsZoneUri = UdiseContract.RawData.CONTENT_URI;
                String[] projectionZone = {
                        UdiseContract.RawData.COLUMN_UDISE_SCHOOL_CODE,
                        UdiseContract.RawData.COLUMN_CLUSTER_CODE,
                        UdiseContract.RawData.COLUMN_CLUSTER_NAME,
                        UdiseContract.RawData.COLUMN_SCHOOL_CATEGORY
                };

                String selectionStringZone = UdiseContract.RawData.COLUMN_ZONE_CODE + " = ? and " + UdiseContract.RawData.COLUMN_AC_YEAR + " = ?";

                String[] selectionArgumentsZone = {code, acYear};
                Log.d(TAG, "Case activated for Loader with " + code + " year " + acYear);
                return new CursorLoader(this,udiseSchoolsZoneUri,projectionZone,selectionStringZone,selectionArgumentsZone,null);

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
            case ID_NUMBER_OF_SCHOOLS_STATE_WISE_LOADER:
                List<ManagementWiseSchoolSummaryModel> stateWiseList = SchoolReportsHelper.stateWiseSummary(data);
                numberOfSchoolsSummaryAdapter.swapDataList(stateWiseList);
                break;

            case ID_NUMBER_OF_SCHOOLS_DISTRICT_WISE_LOADER:
                List<ManagementWiseSchoolSummaryModel> districtWiseList = SchoolReportsHelper.districtWiseSummary(data);
                numberOfSchoolsSummaryAdapter.swapDataList(districtWiseList);
                break;

            case ID_NUMBER_OF_SCHOOLS_ZONE_WISE_LOADER:
                List<ManagementWiseSchoolSummaryModel> zoneWiseList = SchoolReportsHelper.zoneWiseSummary(data);
                numberOfSchoolsSummaryAdapter.swapDataList(zoneWiseList);
                break;

            case ID_NUMBER_OF_SCHOOLS_CLUSTER_WISE_LOADER:
                List<ManagementWiseSchoolSummaryModel> clusterWiseList = SchoolReportsHelper.clusterWiseSummary(data);
                numberOfSchoolsSummaryAdapter.swapDataList(clusterWiseList);
                if(clusterWiseList.size()<1){
                    Log.e(TAG,"Loader finished but empty!!!!");
                } else {
                    Log.d(TAG,"Loader finished with clusters " + clusterWiseList.size());
                }
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
        Log.d(TAG,"Report level finalised in level-wise touch is " + reportDisplayLevel + " code is " + zoneDistrictOrStateCode + " and name is " + zoneDistrictOrStateName);

        switch(reportDisplayLevel){

            case SchoolReportsConstants.REPORT_DISPLAY_NATIONAL_SUMMARY:
                intent = new Intent(this,NumberOfSchools.class);
                intent.putExtra(SchoolReportsConstants.EXTRA_KEY_ACADEMIC_YEAR,academicYear);
                intent.putExtra(SchoolReportsConstants.EXTRA_PARAM_KEY_REPORT_DISPLAY_SUMMARY_TYPE,SchoolReportsConstants.REPORT_DISPLAY_NATIONAL_SUMMARY);
                startActivity(intent);
                break;

            case SchoolReportsConstants.REPORT_DISPLAY_STATE_SUMMARY:
                intent = new Intent(this,NumberOfSchools.class);
                intent.putExtra(SchoolReportsConstants.EXTRA_KEY_ACADEMIC_YEAR,academicYear);
                intent.putExtra(SchoolReportsConstants.EXTRA_KEY_NAME_STATE_DISTRICT_ZONE_CLUSTER,zoneDistrictOrStateName);
                intent.putExtra(SchoolReportsConstants.EXTRA_PARAM_KEY_REPORT_DISPLAY_SUMMARY_TYPE,SchoolReportsConstants.REPORT_DISPLAY_STATE_SUMMARY);
                startActivity(intent);
                break;

            case SchoolReportsConstants.REPORT_DISPLAY_DISTRICT_SUMMARY:
                intent = new Intent(this,NumberOfSchools.class);
                intent.putExtra(SchoolReportsConstants.EXTRA_KEY_ACADEMIC_YEAR,academicYear);
                intent.putExtra(SchoolReportsConstants.EXTRA_KEY_CODE_STATE_DISTRICT_ZONE_CLUSTER,zoneDistrictOrStateCode);
                intent.putExtra(SchoolReportsConstants.EXTRA_KEY_NAME_STATE_DISTRICT_ZONE_CLUSTER,zoneDistrictOrStateName);
                intent.putExtra(SchoolReportsConstants.EXTRA_PARAM_KEY_REPORT_DISPLAY_SUMMARY_TYPE,SchoolReportsConstants.REPORT_DISPLAY_DISTRICT_SUMMARY);
                startActivity(intent);
                break;

            case SchoolReportsConstants.REPORT_DISPLAY_ZONE_SUMMARY:
                intent = new Intent(this,NumberOfSchools.class);
                intent.putExtra(SchoolReportsConstants.EXTRA_KEY_ACADEMIC_YEAR,academicYear);
                intent.putExtra(SchoolReportsConstants.EXTRA_KEY_CODE_STATE_DISTRICT_ZONE_CLUSTER,zoneDistrictOrStateCode);
                intent.putExtra(SchoolReportsConstants.EXTRA_KEY_NAME_STATE_DISTRICT_ZONE_CLUSTER,zoneDistrictOrStateName);
                intent.putExtra(SchoolReportsConstants.EXTRA_PARAM_KEY_REPORT_DISPLAY_SUMMARY_TYPE,SchoolReportsConstants.REPORT_DISPLAY_ZONE_SUMMARY);
                startActivity(intent);
                break;

            case SchoolReportsConstants.REPORT_DISPLAY_CLUSTER_SUMMARY:
                intent = new Intent(this,NumberOfSchools.class);
                intent.putExtra(SchoolReportsConstants.EXTRA_KEY_ACADEMIC_YEAR,academicYear);
                intent.putExtra(SchoolReportsConstants.EXTRA_KEY_CODE_STATE_DISTRICT_ZONE_CLUSTER,zoneDistrictOrStateCode);
                intent.putExtra(SchoolReportsConstants.EXTRA_KEY_NAME_STATE_DISTRICT_ZONE_CLUSTER,zoneDistrictOrStateName);
                intent.putExtra(SchoolReportsConstants.EXTRA_PARAM_KEY_REPORT_DISPLAY_SUMMARY_TYPE,SchoolReportsConstants.REPORT_DISPLAY_CLUSTER_SUMMARY);
                startActivity(intent);
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
