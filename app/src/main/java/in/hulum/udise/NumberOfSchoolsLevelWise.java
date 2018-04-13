package in.hulum.udise;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Spinner;

import java.util.List;

import in.hulum.udise.adapters.NumberOfSchoolsSummaryAdapter;
import in.hulum.udise.database.UdiseContract;
import in.hulum.udise.models.ManagementWiseSchoolSummaryModel;
import in.hulum.udise.utils.SchoolReportsConstants;
import in.hulum.udise.utils.SchoolReportsHelper;

/**
 * Created by Irshad
 *
 * This Activity displays the level wise number of schools summary
 * in a recycler view. The report can be:
 * - State wise
 * - District wise
 * - Zone wise
 * - Cluster wise
 * - Assembly Constituency wise
 * On clicking any item in the recycler view, this activity launches
 * another activity {@link NumberOfSchoolsLevelWise} to display
 * the management-wise summary of the clicked item
 *
 * NOTE: Assembly Constituency-wise report is generated only when the
 * floating action button (FAB) is clicked. The code for generating
 * Assembly Constituency-wise reports is contained in the click handler
 * of FAB as well as onCreateLoader method.
 */

public class NumberOfSchoolsLevelWise extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>,
        NumberOfSchoolsSummaryAdapter.NumberOfSchoolsSummaryAdapterOnClickHandler{

    private static final String TAG = "NumOfSchoolsLevelWise";


    private String entityCode;
    private String entityName;

    private String academicYear;

    private int displayReportLevel;

    int loaderId;
    /*
     * Bundle variable for passing arguments to the loader
     */
    Bundle arguments;

    /*
     * Constants for various loader ids
     */
    private static final int ID_NUMBER_OF_SCHOOLS_STATE_WISE_LOADER = 110;
    private static final int ID_NUMBER_OF_SCHOOLS_DISTRICT_WISE_LOADER = 120;
    private static final int ID_NUMBER_OF_SCHOOLS_ZONE_WISE_LOADER = 130;
    private static final int ID_NUMBER_OF_SCHOOLS_CLUSTER_WISE_LOADER = 140;
    private static final int ID_NUMBER_OF_SCHOOLS_ASSEMBLY_CONSTITUENCY_WISE_FOR_STATE_LOADER = 150;
    private static final int ID_NUMBER_OF_SCHOOLS_ASSEMBLY_CONSTITUENCY_WISE_FOR_DISTRICT_LOADER = 160;
    private static final int ID_NUMBER_OF_SCHOOLS_ASSEMBLY_CONSTITUENCY_WISE_FOR_ZONE_LOADER = 170;



    private NumberOfSchoolsSummaryAdapter numberOfSchoolsSummaryAdapter;
    private RecyclerView mRecyclerView;
    private int mPosition = RecyclerView.NO_POSITION;
    /*
     * Boolean flag indicating whether the current report
     * is Assembly Constituency-wise summary list or not.
     * This flag is used in the floating action button which
     * is used to toggle between Assembly Constituency-wise list
     * and State/District/Zone/Cluster -wise summary list
     */
    private  boolean isAssemblyConstituencyWiseList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_number_of_schools);

        /*
         * Setup toolbar and appbar
         */
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar supportActionBar = getSupportActionBar();
        if(supportActionBar != null){
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setHomeButtonEnabled(true);
        }

        /*
         * Setup floating action button (FAB)
         */
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleAssemblyConstituencyWiseList(view);
            }
        });

        /*
         * Since we do not need spinner (academic year selector) in level-wise
         * reports, we hide the it here. Instead we enable the title.
         */
        Spinner spinnerAcademicYears = (Spinner)findViewById(R.id.spinner_academic_years);
        spinnerAcademicYears.setVisibility(View.GONE);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        /*
         * Fetch the academic year and desired report level from intent
         */
        isAssemblyConstituencyWiseList = false;
        academicYear = getIntent().getStringExtra(SchoolReportsConstants.EXTRA_KEY_ACADEMIC_YEAR);
        displayReportLevel = getIntent().getIntExtra(SchoolReportsConstants.EXTRA_PARAM_KEY_REPORT_DISPLAY_LEVEL_WISE_TYPE,SchoolReportsConstants.REPORT_DISPLAY_INVALID);

        /*
         * The intent will contain ONLY ONE of the following:
         * - State name
         * - District name
         * - Zone name
         * - Cluster name
         * So we will copy it to the variable entityName
         * Later we can determine the type by the variable displaySummaryFor
         */
        entityName = getIntent().getStringExtra(SchoolReportsConstants.EXTRA_KEY_NAME_STATE_DISTRICT_ZONE_CLUSTER);

        /*
         * The intent will contain ONLY ONE of the following:
         * - District code
         * - Zone code
         * - Cluster code
         * So we will copy it to all
         * Later we can determine the type by the variable displaySummaryFor
         */
        entityCode = getIntent().getStringExtra(SchoolReportsConstants.EXTRA_KEY_CODE_STATE_DISTRICT_ZONE_CLUSTER);

        /*
         * Setup recycler view
         */
        mRecyclerView = findViewById(R.id.recyclerview_summary_managementwise_number_of_schools);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
        numberOfSchoolsSummaryAdapter = new NumberOfSchoolsSummaryAdapter(this,this,true);
        mRecyclerView.setAdapter(numberOfSchoolsSummaryAdapter);

        /*
         * Prepare loader arguments in a bundle
         */
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
                fab.setVisibility(View.GONE);
                break;

            case SchoolReportsConstants.REPORT_DISPLAY_LEVEL_DISTRICTWISE:
                arguments.putString(SchoolReportsConstants.EXTRA_KEY_NAME_STATE_DISTRICT_ZONE_CLUSTER,entityName);
                loaderId = ID_NUMBER_OF_SCHOOLS_DISTRICT_WISE_LOADER;
                getSupportActionBar().setTitle(entityName);
                getSupportActionBar().setSubtitle(getString(R.string.district_wise_summary_list));
                break;

            case SchoolReportsConstants.REPORT_DISPLAY_LEVEL_ZONEWISE:
                arguments.putString(SchoolReportsConstants.EXTRA_KEY_CODE_STATE_DISTRICT_ZONE_CLUSTER,entityCode);
                arguments.putString(SchoolReportsConstants.EXTRA_KEY_NAME_STATE_DISTRICT_ZONE_CLUSTER,entityName);
                loaderId = ID_NUMBER_OF_SCHOOLS_ZONE_WISE_LOADER;
                getSupportActionBar().setTitle(entityName + " - " + entityCode);
                getSupportActionBar().setSubtitle(getString(R.string.zone_wise_summary_list));
                break;

            case SchoolReportsConstants.REPORT_DISPLAY_LEVEL_CLUSTERWISE:
                arguments.putString(SchoolReportsConstants.EXTRA_KEY_CODE_STATE_DISTRICT_ZONE_CLUSTER,entityCode);
                arguments.putString(SchoolReportsConstants.EXTRA_KEY_NAME_STATE_DISTRICT_ZONE_CLUSTER,entityName);
                loaderId = ID_NUMBER_OF_SCHOOLS_CLUSTER_WISE_LOADER;
                getSupportActionBar().setTitle(entityName + " - " + entityCode);
                getSupportActionBar().setSubtitle(getString(R.string.cluster_wise_summary_list));
                break;
        }

        /*
         * Initialise the loader with the desired arguments
         */
        getSupportLoaderManager().initLoader(loaderId,arguments,this);

        /*
         * If device is rotated, this activity is restarted. Since we are using
         * the floating action button (FAB) as a toggle, on device rotation the
         * report defaults to State/District/Zone/Cluster wise report even if
         * it was showing Assembly Constituency. To overcome this, we save the
         * value of the boolean flag isAssemblyConstituencyWiseList in the
         * savedInstanceState bundle. Here we check if there is a configuration change
         * and if the current report type was assembly constituency wise report.
         * If so, we simulate button click on the FAB button. Rest is handled by
         * the onClick handler of the FAB itself.
         */
        if(savedInstanceState != null){
            if(savedInstanceState.getBoolean(SchoolReportsConstants.EXTRA_KEY_SHOW_ASSEMBLY_CONSTITUENCY_WISE_REPORT_TOGGLE)){
                fab.performClick();
            }
        }
    }

    /*
     * This method is the clickhandler of floating action button (FAB).
     * This FAB is used to toggle between State/District/Zone/Cluster-wise
     * reports and Assembly Constituency-wise reports. By default,
     * State/District/Zone/Cluster-wise report is shown. On clicking the FAB,
     * the report changes to Assembly Constituency-wise reports.
     * We also toggle the button drawable to indicate the type of report
     * that will be displayed if the FAB is clicked
     */
    public void handleAssemblyConstituencyWiseList(View view){
        Bundle args = new Bundle();
        if(isAssemblyConstituencyWiseList) {
            /*
             * If the current report being displayed is Assembly Constituency-wise report,
             * toggle it change to State/District/Zone/Cluster-wise report.
             */
            isAssemblyConstituencyWiseList = false;
            FloatingActionButton fab = findViewById(R.id.fab);
            fab.setImageResource(R.drawable.ic_assembly_constituency);
            switch(displayReportLevel){
                case SchoolReportsConstants.REPORT_DISPLAY_LEVEL_STATEWISE:
                /*
                 * No code is required for state-wise list
                 * Simply generate a list of all the states
                 * present in the database
                 */
                    loaderId = ID_NUMBER_OF_SCHOOLS_STATE_WISE_LOADER;
                    args.putString(SchoolReportsConstants.EXTRA_KEY_ACADEMIC_YEAR,academicYear);
                    getSupportActionBar().setTitle("State List");
                    getSupportActionBar().setSubtitle(academicYear);
                    break;

                case SchoolReportsConstants.REPORT_DISPLAY_LEVEL_DISTRICTWISE:
                    args.putString(SchoolReportsConstants.EXTRA_KEY_NAME_STATE_DISTRICT_ZONE_CLUSTER,entityName);
                    args.putString(SchoolReportsConstants.EXTRA_KEY_ACADEMIC_YEAR,academicYear);
                    loaderId = ID_NUMBER_OF_SCHOOLS_DISTRICT_WISE_LOADER;
                    getSupportActionBar().setTitle(entityName);
                    getSupportActionBar().setSubtitle(getString(R.string.district_wise_summary_list));
                    break;

                case SchoolReportsConstants.REPORT_DISPLAY_LEVEL_ZONEWISE:
                    args.putString(SchoolReportsConstants.EXTRA_KEY_CODE_STATE_DISTRICT_ZONE_CLUSTER,entityCode);
                    args.putString(SchoolReportsConstants.EXTRA_KEY_NAME_STATE_DISTRICT_ZONE_CLUSTER,entityName);
                    args.putString(SchoolReportsConstants.EXTRA_KEY_ACADEMIC_YEAR,academicYear);
                    loaderId = ID_NUMBER_OF_SCHOOLS_ZONE_WISE_LOADER;
                    getSupportActionBar().setTitle(entityName + " - " + entityCode);
                    getSupportActionBar().setSubtitle(getString(R.string.zone_wise_summary_list));
                    break;

                case SchoolReportsConstants.REPORT_DISPLAY_LEVEL_CLUSTERWISE:
                    args.putString(SchoolReportsConstants.EXTRA_KEY_CODE_STATE_DISTRICT_ZONE_CLUSTER,entityCode);
                    args.putString(SchoolReportsConstants.EXTRA_KEY_NAME_STATE_DISTRICT_ZONE_CLUSTER,entityName);
                    args.putString(SchoolReportsConstants.EXTRA_KEY_ACADEMIC_YEAR,academicYear);
                    loaderId = ID_NUMBER_OF_SCHOOLS_CLUSTER_WISE_LOADER;
                    getSupportActionBar().setTitle(entityName + " - " + entityCode);
                    getSupportActionBar().setSubtitle(getString(R.string.cluster_wise_summary_list));
                    break;
            }

            /*
             * If the FAB is clicked, we need to prepare the necessary arguments
             * to be passed to the loader which we have already done with the
             * help of the above switch-case. Now we just need to restart the
             * loader with those arguments.
             */
            getSupportLoaderManager().restartLoader(loaderId,args,NumberOfSchoolsLevelWise.this);
           /* Snackbar.make(view, "You are now viewing level-wise List", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();*/
        }
        else {
             /*
             * If the current report being displayed is State/District/Zone/Cluster-wise report
             * toggle it change to Assembly Constituency-wise report.
             */
            isAssemblyConstituencyWiseList = true;
            FloatingActionButton fab = findViewById(R.id.fab);
            fab.setImageResource(R.drawable.ic_zone);

            switch (displayReportLevel){
                case SchoolReportsConstants.REPORT_DISPLAY_LEVEL_DISTRICTWISE:
                    args.putString(SchoolReportsConstants.EXTRA_KEY_NAME_STATE_DISTRICT_ZONE_CLUSTER,entityName);
                    args.putString(SchoolReportsConstants.EXTRA_KEY_ACADEMIC_YEAR,academicYear);
                    loaderId = ID_NUMBER_OF_SCHOOLS_ASSEMBLY_CONSTITUENCY_WISE_FOR_STATE_LOADER;
                    getSupportActionBar().setTitle(entityName + " State");
                    getSupportActionBar().setSubtitle(getString(R.string.assembly_constituency_wise_summary_list));
                    break;

                case SchoolReportsConstants.REPORT_DISPLAY_LEVEL_ZONEWISE:
                    args.putString(SchoolReportsConstants.EXTRA_KEY_NAME_STATE_DISTRICT_ZONE_CLUSTER,entityName);
                    args.putString(SchoolReportsConstants.EXTRA_KEY_CODE_STATE_DISTRICT_ZONE_CLUSTER,entityCode);
                    args.putString(SchoolReportsConstants.EXTRA_KEY_ACADEMIC_YEAR,academicYear);
                    loaderId = ID_NUMBER_OF_SCHOOLS_ASSEMBLY_CONSTITUENCY_WISE_FOR_DISTRICT_LOADER;
                    getSupportActionBar().setTitle("District " + entityName);
                    getSupportActionBar().setSubtitle(getString(R.string.assembly_constituency_wise_summary_list));
                    break;

                case SchoolReportsConstants.REPORT_DISPLAY_LEVEL_CLUSTERWISE:
                    args.putString(SchoolReportsConstants.EXTRA_KEY_NAME_STATE_DISTRICT_ZONE_CLUSTER,entityName);
                    args.putString(SchoolReportsConstants.EXTRA_KEY_CODE_STATE_DISTRICT_ZONE_CLUSTER,entityCode);
                    args.putString(SchoolReportsConstants.EXTRA_KEY_ACADEMIC_YEAR,academicYear);
                    loaderId = ID_NUMBER_OF_SCHOOLS_ASSEMBLY_CONSTITUENCY_WISE_FOR_ZONE_LOADER;
                    getSupportActionBar().setTitle("Zone " + entityName);
                    getSupportActionBar().setSubtitle(getString(R.string.assembly_constituency_wise_summary_list));
                    break;

                default:
                    throw new RuntimeException("Unsupported Assembly Constituency-wise Report in " + getLocalClassName());
            }
            getSupportLoaderManager().restartLoader(loaderId,args,NumberOfSchoolsLevelWise.this);
        }
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

                String selectionStringNational = UdiseContract.RawData.COLUMN_SCHOOL_OPERATIONAL_STATUS + " = 0 and " + UdiseContract.RawData.COLUMN_AC_YEAR + " = ?";

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

                String selectionStringState = UdiseContract.RawData.COLUMN_SCHOOL_OPERATIONAL_STATUS + " = 0 and " + UdiseContract.RawData.COLUMN_STATE_NAME + " = ? and " + UdiseContract.RawData.COLUMN_AC_YEAR + " = ?";

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

                String selectionStringDistrict = UdiseContract.RawData.COLUMN_SCHOOL_OPERATIONAL_STATUS + " = 0 and " + UdiseContract.RawData.COLUMN_DISTRICT_CODE + " = ? and " + UdiseContract.RawData.COLUMN_AC_YEAR + " = ?";

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

                String selectionStringZone = UdiseContract.RawData.COLUMN_SCHOOL_OPERATIONAL_STATUS + " = 0 and " + UdiseContract.RawData.COLUMN_ZONE_CODE + " = ? and " + UdiseContract.RawData.COLUMN_AC_YEAR + " = ?";

                String[] selectionArgumentsZone = {code, acYear};
                return new CursorLoader(this,udiseSchoolsZoneUri,projectionZone,selectionStringZone,selectionArgumentsZone,null);

            case ID_NUMBER_OF_SCHOOLS_ASSEMBLY_CONSTITUENCY_WISE_FOR_STATE_LOADER:
                Uri udiseSchoolsStateAssemblyConstituencyUri = UdiseContract.RawData.CONTENT_URI;
                String[] projectionStateAssemblyConstituency = {
                        UdiseContract.RawData.COLUMN_UDISE_SCHOOL_CODE,
                        UdiseContract.RawData.COLUMN_ASSEMBLY_CONSTITUENCY_CODE,
                        UdiseContract.RawData.COLUMN_ASSEMBLY_CONSTITUENCY_NAME,
                        UdiseContract.RawData.COLUMN_SCHOOL_CATEGORY
                };

                String selectionStringStateAssemblyConstituency = UdiseContract.RawData.COLUMN_SCHOOL_OPERATIONAL_STATUS + " = 0 and " + UdiseContract.RawData.COLUMN_STATE_NAME + " = ? and " + UdiseContract.RawData.COLUMN_AC_YEAR + " = ?";

                String[] selectionArgumentsStateAssemblyConstituency = {name, acYear};
                return new CursorLoader(this,udiseSchoolsStateAssemblyConstituencyUri,projectionStateAssemblyConstituency,selectionStringStateAssemblyConstituency,selectionArgumentsStateAssemblyConstituency,null);

            case ID_NUMBER_OF_SCHOOLS_ASSEMBLY_CONSTITUENCY_WISE_FOR_DISTRICT_LOADER:
                Uri udiseSchoolsDistrictAssemblyConstituencyUri = UdiseContract.RawData.CONTENT_URI;
                String[] projectionDistrictAssemblyConstituency = {
                        UdiseContract.RawData.COLUMN_UDISE_SCHOOL_CODE,
                        UdiseContract.RawData.COLUMN_ASSEMBLY_CONSTITUENCY_CODE,
                        UdiseContract.RawData.COLUMN_ASSEMBLY_CONSTITUENCY_NAME,
                        UdiseContract.RawData.COLUMN_SCHOOL_CATEGORY
                };

                String selectionStringDistrictAssemblyConstituency = UdiseContract.RawData.COLUMN_SCHOOL_OPERATIONAL_STATUS + " = 0 and " + UdiseContract.RawData.COLUMN_DISTRICT_CODE + " = ? and " + UdiseContract.RawData.COLUMN_AC_YEAR + " = ?";

                String[] selectionArgumentsDistrictAssemblyConstituency = {code, acYear};
                return new CursorLoader(this,udiseSchoolsDistrictAssemblyConstituencyUri,projectionDistrictAssemblyConstituency,selectionStringDistrictAssemblyConstituency,selectionArgumentsDistrictAssemblyConstituency,null);

            case ID_NUMBER_OF_SCHOOLS_ASSEMBLY_CONSTITUENCY_WISE_FOR_ZONE_LOADER:
                Uri udiseSchoolsZoneAssemblyConstituencyUri = UdiseContract.RawData.CONTENT_URI;
                String[] projectionZoneAssemblyConstituency = {
                        UdiseContract.RawData.COLUMN_UDISE_SCHOOL_CODE,
                        UdiseContract.RawData.COLUMN_ASSEMBLY_CONSTITUENCY_CODE,
                        UdiseContract.RawData.COLUMN_ASSEMBLY_CONSTITUENCY_NAME,
                        UdiseContract.RawData.COLUMN_SCHOOL_CATEGORY
                };

                String selectionStringZoneAssemblyConstituency = UdiseContract.RawData.COLUMN_SCHOOL_OPERATIONAL_STATUS + " = 0 and " + UdiseContract.RawData.COLUMN_ZONE_CODE + " = ? and " + UdiseContract.RawData.COLUMN_AC_YEAR + " = ?";

                String[] selectionArgumentsZoneAssemblyConstituency = {code, acYear};
                return new CursorLoader(this,udiseSchoolsZoneAssemblyConstituencyUri,projectionZoneAssemblyConstituency,selectionStringZoneAssemblyConstituency,selectionArgumentsZoneAssemblyConstituency,null);

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
                break;

            case ID_NUMBER_OF_SCHOOLS_ASSEMBLY_CONSTITUENCY_WISE_FOR_STATE_LOADER:
                List<ManagementWiseSchoolSummaryModel> assemblyConstituencyWiseListForState = SchoolReportsHelper.assemblyConstituencyWiseSummary(data,SchoolReportsConstants.ASSEMBLY_CONSTITUENCY_PARENT_STATE,entityName);
                numberOfSchoolsSummaryAdapter.swapDataList(assemblyConstituencyWiseListForState);
                break;
            case ID_NUMBER_OF_SCHOOLS_ASSEMBLY_CONSTITUENCY_WISE_FOR_DISTRICT_LOADER:
                List<ManagementWiseSchoolSummaryModel> assemblyConstituencyWiseListForDistrict = SchoolReportsHelper.assemblyConstituencyWiseSummary(data,SchoolReportsConstants.ASSEMBLY_CONSTITUENCY_PARENT_DISTRICT,entityCode);
                numberOfSchoolsSummaryAdapter.swapDataList(assemblyConstituencyWiseListForDistrict);
                break;
            case ID_NUMBER_OF_SCHOOLS_ASSEMBLY_CONSTITUENCY_WISE_FOR_ZONE_LOADER:
                List<ManagementWiseSchoolSummaryModel> assemblyConstituencyWiseListForZone = SchoolReportsHelper.assemblyConstituencyWiseSummary(data,SchoolReportsConstants.ASSEMBLY_CONSTITUENCY_PARENT_ZONE,entityCode);
                numberOfSchoolsSummaryAdapter.swapDataList(assemblyConstituencyWiseListForZone);
                break;

            default:
                throw new RuntimeException("Loader not implemented yet in onLoadFinished with id " + loader.getId());
        }
        if(mPosition == RecyclerView.NO_POSITION){
            mPosition = 0;
        }
        mRecyclerView.smoothScrollToPosition(mPosition);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        numberOfSchoolsSummaryAdapter.swapDataList(null);
    }

    @Override
    public void onClick(int reportDisplayLevel, String zoneDistrictOrStateCode, String zoneDistrictOrStateName, String parentCode) {
        Intent intent;

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

            case SchoolReportsConstants.REPORT_DISPLAY_ASSEMBLY_CONSTITUENCY_SUMMARY_WITH_PARENT_STATE:
                intent = new Intent(this,NumberOfSchools.class);
                intent.putExtra(SchoolReportsConstants.EXTRA_KEY_ACADEMIC_YEAR,academicYear);
                intent.putExtra(SchoolReportsConstants.EXTRA_KEY_CODE_STATE_DISTRICT_ZONE_CLUSTER,zoneDistrictOrStateCode);
                intent.putExtra(SchoolReportsConstants.EXTRA_KEY_NAME_STATE_DISTRICT_ZONE_CLUSTER,zoneDistrictOrStateName);
                intent.putExtra(SchoolReportsConstants.EXTRA_KEY_PARENT_STATE_DISTRICT_OR_ZONE_CODE,parentCode);
                intent.putExtra(SchoolReportsConstants.EXTRA_PARAM_KEY_REPORT_DISPLAY_SUMMARY_TYPE,SchoolReportsConstants.REPORT_DISPLAY_ASSEMBLY_CONSTITUENCY_SUMMARY_WITH_PARENT_STATE);
                startActivity(intent);
                break;

            case SchoolReportsConstants.REPORT_DISPLAY_ASSEMBLY_CONSTITUENCY_SUMMARY_WITH_PARENT_DISTRICT:
                intent = new Intent(this,NumberOfSchools.class);
                intent.putExtra(SchoolReportsConstants.EXTRA_KEY_ACADEMIC_YEAR,academicYear);
                intent.putExtra(SchoolReportsConstants.EXTRA_KEY_CODE_STATE_DISTRICT_ZONE_CLUSTER,zoneDistrictOrStateCode);
                intent.putExtra(SchoolReportsConstants.EXTRA_KEY_NAME_STATE_DISTRICT_ZONE_CLUSTER,zoneDistrictOrStateName);
                intent.putExtra(SchoolReportsConstants.EXTRA_KEY_PARENT_STATE_DISTRICT_OR_ZONE_CODE,parentCode);
                intent.putExtra(SchoolReportsConstants.EXTRA_PARAM_KEY_REPORT_DISPLAY_SUMMARY_TYPE,SchoolReportsConstants.REPORT_DISPLAY_ASSEMBLY_CONSTITUENCY_SUMMARY_WITH_PARENT_DISTRICT);
                startActivity(intent);
                break;

            case SchoolReportsConstants.REPORT_DISPLAY_ASSEMBLY_CONSTITUENCY_SUMMARY_WITH_PARENT_ZONE:
                intent = new Intent(this,NumberOfSchools.class);
                intent.putExtra(SchoolReportsConstants.EXTRA_KEY_ACADEMIC_YEAR,academicYear);
                intent.putExtra(SchoolReportsConstants.EXTRA_KEY_CODE_STATE_DISTRICT_ZONE_CLUSTER,zoneDistrictOrStateCode);
                intent.putExtra(SchoolReportsConstants.EXTRA_KEY_NAME_STATE_DISTRICT_ZONE_CLUSTER,zoneDistrictOrStateName);
                intent.putExtra(SchoolReportsConstants.EXTRA_KEY_PARENT_STATE_DISTRICT_OR_ZONE_CODE,parentCode);
                intent.putExtra(SchoolReportsConstants.EXTRA_PARAM_KEY_REPORT_DISPLAY_SUMMARY_TYPE,SchoolReportsConstants.REPORT_DISPLAY_ASSEMBLY_CONSTITUENCY_SUMMARY_WITH_PARENT_ZONE);
                startActivity(intent);
                break;

            case SchoolReportsConstants.REPORT_DISPLAY_INVALID:
                Log.e(TAG,"Invalid Report Request Received with Report Code: " + reportDisplayLevel);
                break;
        }
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


    /*
     * If the device is rotated or any other configuration change
     * event occurs, save the type of report being displayed. We only
     * need to remember whether we are displaying Assembly Constituency-wise
     * list or not so that we can restore the same on device rotation. If this
     * is omitted, the report type automatically defaults to
     * State/District/Zone/Cluster-wise after device rotation even if
     * Assembly Constituency report was being displayed before rotation.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SchoolReportsConstants.EXTRA_KEY_SHOW_ASSEMBLY_CONSTITUENCY_WISE_REPORT_TOGGLE,isAssemblyConstituencyWiseList);
    }
}
