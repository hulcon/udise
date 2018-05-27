package in.hulum.udise;

import android.content.Context;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
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

/**
 * Created by Irshad
 *
 * This Activity displays the number of schools summary
 * management wise in a recycler view. The first item
 * of the recycler view displays the summary of the whole
 * Nation, State, District or Zone. The rest of the items
 * in the recycler view display the management-wise summary,
 * one item for each management type. On clicking any item,
 * this activity launches another activity
 * {@link NumberOfSchoolsLevelWise} which then displays the
 * level-wise (State, District, Zone or Cluster -wise) details
 */

public class NumberOfSchools extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>,
        NumberOfSchoolsSummaryAdapter.NumberOfSchoolsSummaryAdapterOnClickHandler{

    private static final String TAG = "NumberOfSchools";

    private String entityCode;
    private String entityName;

    /*
     * In case of a Constituency Assembly report, we need
     * to know the udise code of the parent entity
     * (State, District, or Zone) of that Constituency Assembly.
     * This variable holds that.
     */
    private String parentEntityCodeForAssemblyConstituency;

    private String academicYear;

    private int userType;
    private int displaySummaryFor;

    int loaderId;
    /*
     * Bundle for passing arguments to the loader
     */
    Bundle arguments;

    /*
     * When this activity is first launched by clicking the
     * "Number of Schools" button, i.e., if it is displaying
     * the top-level management-wise report, it should display
     * a spinner in the toolbar to select the desired academic
     * year. But for subsequent deeper level reports (child reports),
     * the spinner should be hidden and instead a title and a
     * subtitle must be displayed. This variable is used to keep
     * track of whether the spinner should be displayed or not.
     */
    private boolean showTitleInsteadOfSpinner = false;

    /*
     * Various loader ids for corresponding types of reports required
     */
    private static final int ID_MANAGEMENT_WISE_NUMBER_OF_SCHOOLS_NATIONAL_SUMMARY_LOADER = 1000;
    private static final int ID_MANAGEMENT_WISE_NUMBER_OF_SCHOOLS_STATE_SUMMARY_LOADER = 2000;
    private static final int ID_MANAGEMENT_WISE_NUMBER_OF_SCHOOLS_DISTRICT_SUMMARY_LOADER = 3000;
    private static final int ID_MANAGEMENT_WISE_NUMBER_OF_SCHOOLS_ZONE_SUMMARY_LOADER = 4000;
    private static final int ID_MANAGEMENT_WISE_NUMBER_OF_SCHOOLS_CLUSTER_SUMMARY_LOADER = 5000;
    private static final int ID_MANAGEMENT_WISE_NUMBER_OF_SCHOOLS_ASSEMBLY_CONSTITUENCY_SUMMARY_WITH_PARENT_STATE_LOADER = 6000;
    private static final int ID_MANAGEMENT_WISE_NUMBER_OF_SCHOOLS_ASSEMBLY_CONSTITUENCY_SUMMARY_WITH_PARENT_DISTRICT_LOADER = 7000;
    private static final int ID_MANAGEMENT_WISE_NUMBER_OF_SCHOOLS_ASSEMBLY_CONSTITUENCY_SUMMARY_WITH_PARENT_ZONE_LOADER = 8000;

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

        /*
         * Set up the support action bar.
         * Enable the back button.
         * Initially hide the title to make
         * room for the spinner
         */
        ActionBar supportActionBar = getSupportActionBar();
        if(supportActionBar != null){
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setHomeButtonEnabled(true);
            supportActionBar.setDisplayShowTitleEnabled(false);
        }

        /**
         * This activity and the {@link NumberOfSchoolsLevelWise} use
         * the same layout file. We need a floating action button in the
         * {@link NumberOfSchoolsLevelWise} for toggling Assembly
         * Constituency-wise view but we do not require that FAB in this
         * activity. So we just hide that FAB in this activity.
         */
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.GONE);

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
                         * We only require state name in case of state summary
                         */
                        entityName = userDataModel.getStatesList().get(0).getStateName();
                        displaySummaryFor = SchoolReportsConstants.REPORT_DISPLAY_STATE_SUMMARY;
                        break;
                    case UserDataModel.USER_TYPE_DISTRICT:
                        entityCode = userDataModel.getDistrictsList().get(0).getDistrictCode();
                        entityName = userDataModel.getDistrictsList().get(0).getDistrictName();
                        displaySummaryFor = SchoolReportsConstants.REPORT_DISPLAY_DISTRICT_SUMMARY;
                        break;
                    case UserDataModel.USER_TYPE_ZONE:
                        entityCode = userDataModel.getZoneList().get(0).getZoneCode();
                        entityName = userDataModel.getZoneList().get(0).getZoneName();
                        displaySummaryFor = SchoolReportsConstants.REPORT_DISPLAY_ZONE_SUMMARY;
                        break;
                    /*
                     * Cluster-level summary is not possible by directly invoking this
                     * activity from the mainactivity because we do not currently support
                     * cluster-level users. Cluster-level summary will be
                     * possible only when this activity is called with proper extra
                     * data in its intent. The intent must contain code of
                     * REPORT_DISPLAY_CLUSTER_SUMMARY to display cluster level summary
                     */
                    default:
                        throw new RuntimeException("Unknown userType discovered in " + getLocalClassName());

                }
            }

            /*
             * Setup the spinner for displaying academic years.
             */
            ArrayAdapter<String> adapterAcademicYears =
                    new ArrayAdapter<String>(this,R.layout.spinner_item_custom,academicYearsList);
            adapterAcademicYears.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerAcademicYears.setAdapter(adapterAcademicYears);
            spinnerAcademicYears.setOnItemSelectedListener(new
                  AdapterView.OnItemSelectedListener() {
                      @Override
                      public void onItemSelected(AdapterView<?> parentView,
                                                 View selectedItemView, int position, long id) {
                          /*
                           * On selecting a spinner item, we simply fetch the academic year
                           * from the currently selected item and put that in a bundle as an
                           * argument. We then restart the loader to load data for the
                           * selected academic year.
                           */
                          academicYear = parentView.getItemAtPosition(position).toString();
                          arguments.putString(SchoolReportsConstants.EXTRA_KEY_ACADEMIC_YEAR,academicYear);
                          getSupportLoaderManager().restartLoader(loaderId,arguments,NumberOfSchools.this);
                      }
                      @Override
                      public void onNothingSelected(AdapterView<?> parentView) {
                          // Not implemented yet
                      }
                  });
            academicYear = spinnerAcademicYears.getSelectedItem().toString();
        } else {
            /* This is a child-level report (deep-level report)
             * We need to get the data from the intent like ac_year and
             * district code (or similar things). Also the spinner needs
             * to be disabled in this case. We will display title and
             * subtitle in place of spinner
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
             * So we call it entityName
             * Later we can determine the type by the variable displaySummaryFor
             */
            entityName = getIntent().getStringExtra(SchoolReportsConstants.EXTRA_KEY_NAME_STATE_DISTRICT_ZONE_CLUSTER);

            /*
             * The intent will contain ONLY ONE of the following:
             * - District code
             * - Zone code
             * - Cluster code
             * So we call it entityCode
             * Later we can determine the type by the variable displaySummaryFor
             */
            entityCode = getIntent().getStringExtra(SchoolReportsConstants.EXTRA_KEY_CODE_STATE_DISTRICT_ZONE_CLUSTER);

            /*
             * This extra parameter holds the code of parent state, district or zone in case
             * of assembly constituency
             */
            parentEntityCodeForAssemblyConstituency = getIntent().getStringExtra(SchoolReportsConstants.EXTRA_KEY_PARENT_STATE_DISTRICT_OR_ZONE_CODE);

        }

        /*
         * Setup the recycler view
         */
        mRecyclerView = findViewById(R.id.recyclerview_summary_managementwise_number_of_schools);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
        numberOfSchoolsSummaryAdapter = new NumberOfSchoolsSummaryAdapter(this,this,false);
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
                arguments.putString(SchoolReportsConstants.EXTRA_KEY_NAME_STATE_DISTRICT_ZONE_CLUSTER,entityName);
                loaderId = ID_MANAGEMENT_WISE_NUMBER_OF_SCHOOLS_STATE_SUMMARY_LOADER;
                if(showTitleInsteadOfSpinner){
                    getSupportActionBar().setTitle(entityName);
                    getSupportActionBar().setSubtitle("Management-wise Details " + academicYear);
                }
                break;

            case SchoolReportsConstants.REPORT_DISPLAY_DISTRICT_SUMMARY:
                arguments.putString(SchoolReportsConstants.EXTRA_KEY_CODE_STATE_DISTRICT_ZONE_CLUSTER,entityCode);
                arguments.putString(SchoolReportsConstants.EXTRA_KEY_NAME_STATE_DISTRICT_ZONE_CLUSTER,entityName);
                loaderId = ID_MANAGEMENT_WISE_NUMBER_OF_SCHOOLS_DISTRICT_SUMMARY_LOADER;
                if(showTitleInsteadOfSpinner){
                    getSupportActionBar().setTitle(entityName + " - " + entityCode);
                    getSupportActionBar().setSubtitle("Management-wise Details " + academicYear);
                }
                break;

            case SchoolReportsConstants.REPORT_DISPLAY_ZONE_SUMMARY:
                arguments.putString(SchoolReportsConstants.EXTRA_KEY_CODE_STATE_DISTRICT_ZONE_CLUSTER,entityCode);
                arguments.putString(SchoolReportsConstants.EXTRA_KEY_NAME_STATE_DISTRICT_ZONE_CLUSTER,entityName);
                loaderId = ID_MANAGEMENT_WISE_NUMBER_OF_SCHOOLS_ZONE_SUMMARY_LOADER;
                if(showTitleInsteadOfSpinner){
                    getSupportActionBar().setTitle(entityName + " - " + entityCode);
                    getSupportActionBar().setSubtitle("Management-wise Details " + academicYear);
                }
                break;

            case SchoolReportsConstants.REPORT_DISPLAY_CLUSTER_SUMMARY:
                arguments.putString(SchoolReportsConstants.EXTRA_KEY_CODE_STATE_DISTRICT_ZONE_CLUSTER,entityCode);
                arguments.putString(SchoolReportsConstants.EXTRA_KEY_NAME_STATE_DISTRICT_ZONE_CLUSTER,entityName);
                loaderId = ID_MANAGEMENT_WISE_NUMBER_OF_SCHOOLS_CLUSTER_SUMMARY_LOADER;
                if(showTitleInsteadOfSpinner){
                    getSupportActionBar().setTitle(entityName + " - " + entityCode);
                    getSupportActionBar().setSubtitle("Management-wise Details " + academicYear);
                }
                break;

            case SchoolReportsConstants.REPORT_DISPLAY_ASSEMBLY_CONSTITUENCY_SUMMARY_WITH_PARENT_STATE:
                arguments.putString(SchoolReportsConstants.EXTRA_KEY_CODE_STATE_DISTRICT_ZONE_CLUSTER,entityCode);
                arguments.putString(SchoolReportsConstants.EXTRA_KEY_NAME_STATE_DISTRICT_ZONE_CLUSTER,entityName);
                arguments.putString(SchoolReportsConstants.EXTRA_KEY_PARENT_STATE_DISTRICT_OR_ZONE_CODE,parentEntityCodeForAssemblyConstituency);
                loaderId = ID_MANAGEMENT_WISE_NUMBER_OF_SCHOOLS_ASSEMBLY_CONSTITUENCY_SUMMARY_WITH_PARENT_STATE_LOADER;
                if(showTitleInsteadOfSpinner){
                    getSupportActionBar().setTitle(entityName);
                    getSupportActionBar().setSubtitle("Assembly Constituency Summary ");
                }
                break;

            case SchoolReportsConstants.REPORT_DISPLAY_ASSEMBLY_CONSTITUENCY_SUMMARY_WITH_PARENT_DISTRICT:
                arguments.putString(SchoolReportsConstants.EXTRA_KEY_CODE_STATE_DISTRICT_ZONE_CLUSTER,entityCode);
                arguments.putString(SchoolReportsConstants.EXTRA_KEY_NAME_STATE_DISTRICT_ZONE_CLUSTER,entityName);
                arguments.putString(SchoolReportsConstants.EXTRA_KEY_PARENT_STATE_DISTRICT_OR_ZONE_CODE,parentEntityCodeForAssemblyConstituency);
                loaderId = ID_MANAGEMENT_WISE_NUMBER_OF_SCHOOLS_ASSEMBLY_CONSTITUENCY_SUMMARY_WITH_PARENT_DISTRICT_LOADER;
                if(showTitleInsteadOfSpinner){
                    getSupportActionBar().setTitle(entityName);
                    getSupportActionBar().setSubtitle("Assembly Constituency Summary ");
                }
                break;

            case SchoolReportsConstants.REPORT_DISPLAY_ASSEMBLY_CONSTITUENCY_SUMMARY_WITH_PARENT_ZONE:
                arguments.putString(SchoolReportsConstants.EXTRA_KEY_CODE_STATE_DISTRICT_ZONE_CLUSTER,entityCode);
                arguments.putString(SchoolReportsConstants.EXTRA_KEY_NAME_STATE_DISTRICT_ZONE_CLUSTER,entityName);
                arguments.putString(SchoolReportsConstants.EXTRA_KEY_PARENT_STATE_DISTRICT_OR_ZONE_CODE,parentEntityCodeForAssemblyConstituency);
                loaderId = ID_MANAGEMENT_WISE_NUMBER_OF_SCHOOLS_ASSEMBLY_CONSTITUENCY_SUMMARY_WITH_PARENT_ZONE_LOADER;
                if(showTitleInsteadOfSpinner){
                    getSupportActionBar().setTitle(entityName);
                    getSupportActionBar().setSubtitle("Assembly Constituency Summary ");
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
        String parentCode = args.getString(SchoolReportsConstants.EXTRA_KEY_PARENT_STATE_DISTRICT_OR_ZONE_CODE);

        /*
         * Sort the rows on the basis of school management
         * This way, Department of Education will be the
         * first management in the list
         */
        String sortOrderColumnName = UdiseContract.RawData.COLUMN_SCHOOL_MANAGEMENT;


        switch (id){
            case ID_MANAGEMENT_WISE_NUMBER_OF_SCHOOLS_NATIONAL_SUMMARY_LOADER:
                Uri udiseSchoolsNationalUri = UdiseContract.RawData.CONTENT_URI;
                String[] projectionNational = {
                        UdiseContract.RawData.COLUMN_UDISE_SCHOOL_CODE,
                        UdiseContract.RawData.COLUMN_SCHOOL_MANAGEMENT,
                        UdiseContract.RawData.COLUMN_SCHOOL_MANAGEMENT_DESCRIPTION,
                        UdiseContract.RawData.COLUMN_SCHOOL_CATEGORY
                };

                String selectionStringNational = UdiseContract.RawData.COLUMN_SCHOOL_OPERATIONAL_STATUS + " = 0 and " + UdiseContract.RawData.COLUMN_AC_YEAR + " = ?";

                String[] selectionArgumentsNational = {acYear};

                return new CursorLoader(this,udiseSchoolsNationalUri,projectionNational,selectionStringNational,selectionArgumentsNational,sortOrderColumnName);

            case ID_MANAGEMENT_WISE_NUMBER_OF_SCHOOLS_STATE_SUMMARY_LOADER:
                Uri udiseSchoolsStateUri = UdiseContract.RawData.CONTENT_URI;
                String[] projectionState = {
                        UdiseContract.RawData.COLUMN_UDISE_SCHOOL_CODE,
                        UdiseContract.RawData.COLUMN_SCHOOL_MANAGEMENT,
                        UdiseContract.RawData.COLUMN_SCHOOL_MANAGEMENT_DESCRIPTION,
                        UdiseContract.RawData.COLUMN_SCHOOL_CATEGORY
                };

                String selectionStringState = UdiseContract.RawData.COLUMN_SCHOOL_OPERATIONAL_STATUS + " = 0 and " + UdiseContract.RawData.COLUMN_STATE_NAME + " = ? and " + UdiseContract.RawData.COLUMN_AC_YEAR + " = ?";

                String[] selectionArgumentsState = {name, acYear};

                return new CursorLoader(this,udiseSchoolsStateUri,projectionState,selectionStringState,selectionArgumentsState,sortOrderColumnName);


            case ID_MANAGEMENT_WISE_NUMBER_OF_SCHOOLS_DISTRICT_SUMMARY_LOADER:
                Uri udiseSchoolsDistrictUri = UdiseContract.RawData.CONTENT_URI;
                String[] projectionDistrict = {
                        UdiseContract.RawData.COLUMN_UDISE_SCHOOL_CODE,
                        UdiseContract.RawData.COLUMN_SCHOOL_MANAGEMENT,
                        UdiseContract.RawData.COLUMN_SCHOOL_MANAGEMENT_DESCRIPTION,
                        UdiseContract.RawData.COLUMN_SCHOOL_CATEGORY
                };

                String selectionStringDistrict = UdiseContract.RawData.COLUMN_SCHOOL_OPERATIONAL_STATUS + " = 0 and " + UdiseContract.RawData.COLUMN_DISTRICT_CODE + " = ? and " + UdiseContract.RawData.COLUMN_AC_YEAR + " = ?";

                String[] selectionArgumentsDistrict = {code, acYear};
                return new CursorLoader(this,udiseSchoolsDistrictUri,projectionDistrict,selectionStringDistrict,selectionArgumentsDistrict,sortOrderColumnName);

            case ID_MANAGEMENT_WISE_NUMBER_OF_SCHOOLS_ZONE_SUMMARY_LOADER:
                Uri udiseSchoolsZoneUri = UdiseContract.RawData.CONTENT_URI;
                String[] projectionZone = {
                        UdiseContract.RawData.COLUMN_UDISE_SCHOOL_CODE,
                        UdiseContract.RawData.COLUMN_SCHOOL_MANAGEMENT,
                        UdiseContract.RawData.COLUMN_SCHOOL_MANAGEMENT_DESCRIPTION,
                        UdiseContract.RawData.COLUMN_SCHOOL_CATEGORY
                };

                String selectionStringZone = UdiseContract.RawData.COLUMN_SCHOOL_OPERATIONAL_STATUS + " = 0 and " + UdiseContract.RawData.COLUMN_ZONE_CODE + " = ? and " + UdiseContract.RawData.COLUMN_AC_YEAR + " = ?";

                String[] selectionArgumentsZone = {code, acYear};
                return new CursorLoader(this,udiseSchoolsZoneUri,projectionZone,selectionStringZone,selectionArgumentsZone,sortOrderColumnName);

            case ID_MANAGEMENT_WISE_NUMBER_OF_SCHOOLS_CLUSTER_SUMMARY_LOADER:
                Uri udiseSchoolsClusterUri = UdiseContract.RawData.CONTENT_URI;
                String[] projectionCluster = {
                        UdiseContract.RawData.COLUMN_UDISE_SCHOOL_CODE,
                        UdiseContract.RawData.COLUMN_SCHOOL_MANAGEMENT,
                        UdiseContract.RawData.COLUMN_SCHOOL_MANAGEMENT_DESCRIPTION,
                        UdiseContract.RawData.COLUMN_SCHOOL_CATEGORY
                };

                String selectionStringCluster = UdiseContract.RawData.COLUMN_SCHOOL_OPERATIONAL_STATUS + " = 0 and " + UdiseContract.RawData.COLUMN_CLUSTER_CODE + " = ? and " + UdiseContract.RawData.COLUMN_AC_YEAR + " = ?";

                String[] selectionArgumentsCluster = {code, acYear};
                return new CursorLoader(this,udiseSchoolsClusterUri,projectionCluster,selectionStringCluster,selectionArgumentsCluster,sortOrderColumnName);

            case ID_MANAGEMENT_WISE_NUMBER_OF_SCHOOLS_ASSEMBLY_CONSTITUENCY_SUMMARY_WITH_PARENT_STATE_LOADER:
                Uri udiseSchoolsAssemblyConstituencyForStateUri = UdiseContract.RawData.CONTENT_URI;
                String[] projectionAssemblyConstituencyForState = {
                        UdiseContract.RawData.COLUMN_UDISE_SCHOOL_CODE,
                        UdiseContract.RawData.COLUMN_SCHOOL_MANAGEMENT,
                        UdiseContract.RawData.COLUMN_SCHOOL_MANAGEMENT_DESCRIPTION,
                        UdiseContract.RawData.COLUMN_STATE_NAME,
                        UdiseContract.RawData.COLUMN_SCHOOL_CATEGORY
                };

                String selectionStringAssemblyConstituencyForState = UdiseContract.RawData.COLUMN_SCHOOL_OPERATIONAL_STATUS + " = 0 and " + UdiseContract.RawData.COLUMN_STATE_NAME + " = ? and " + UdiseContract.RawData.COLUMN_ASSEMBLY_CONSTITUENCY_CODE + " = ? and "+ UdiseContract.RawData.COLUMN_AC_YEAR + " = ?";

                String[] selectionArgumentsAssemblyConstituencyForState = {parentCode, code, acYear};
                return new CursorLoader(this,udiseSchoolsAssemblyConstituencyForStateUri,projectionAssemblyConstituencyForState,selectionStringAssemblyConstituencyForState,selectionArgumentsAssemblyConstituencyForState,sortOrderColumnName);

            case ID_MANAGEMENT_WISE_NUMBER_OF_SCHOOLS_ASSEMBLY_CONSTITUENCY_SUMMARY_WITH_PARENT_DISTRICT_LOADER:
                Uri udiseSchoolsAssemblyConstituencyForDistrictUri = UdiseContract.RawData.CONTENT_URI;
                String[] projectionAssemblyConstituencyForDistrict = {
                        UdiseContract.RawData.COLUMN_UDISE_SCHOOL_CODE,
                        UdiseContract.RawData.COLUMN_SCHOOL_MANAGEMENT,
                        UdiseContract.RawData.COLUMN_SCHOOL_MANAGEMENT_DESCRIPTION,
                        UdiseContract.RawData.COLUMN_DISTRICT_NAME,
                        UdiseContract.RawData.COLUMN_SCHOOL_CATEGORY
                };

                String selectionStringAssemblyConstituencyForDistrict = UdiseContract.RawData.COLUMN_SCHOOL_OPERATIONAL_STATUS + " = 0 and " + UdiseContract.RawData.COLUMN_DISTRICT_CODE + " = ? and " + UdiseContract.RawData.COLUMN_ASSEMBLY_CONSTITUENCY_CODE + " = ? and "+ UdiseContract.RawData.COLUMN_AC_YEAR + " = ?";

                String[] selectionArgumentsAssemblyConstituencyForDistrict = {parentCode, code, acYear};
                return new CursorLoader(this,udiseSchoolsAssemblyConstituencyForDistrictUri,projectionAssemblyConstituencyForDistrict,selectionStringAssemblyConstituencyForDistrict,selectionArgumentsAssemblyConstituencyForDistrict,sortOrderColumnName);

            case ID_MANAGEMENT_WISE_NUMBER_OF_SCHOOLS_ASSEMBLY_CONSTITUENCY_SUMMARY_WITH_PARENT_ZONE_LOADER:
                Uri udiseSchoolsAssemblyConstituencyForZoneUri = UdiseContract.RawData.CONTENT_URI;
                String[] projectionAssemblyConstituencyForZone = {
                        UdiseContract.RawData.COLUMN_UDISE_SCHOOL_CODE,
                        UdiseContract.RawData.COLUMN_SCHOOL_MANAGEMENT,
                        UdiseContract.RawData.COLUMN_SCHOOL_MANAGEMENT_DESCRIPTION,
                        UdiseContract.RawData.COLUMN_ZONE_NAME,
                        UdiseContract.RawData.COLUMN_SCHOOL_CATEGORY
                };

                String selectionStringAssemblyConstituencyForZone = UdiseContract.RawData.COLUMN_SCHOOL_OPERATIONAL_STATUS + " = 0 and " + UdiseContract.RawData.COLUMN_ZONE_CODE + " = ? and " + UdiseContract.RawData.COLUMN_ASSEMBLY_CONSTITUENCY_CODE + " = ? and "+ UdiseContract.RawData.COLUMN_AC_YEAR + " = ?";

                String[] selectionArgumentsAssemblyConstituencyForZone = {parentCode, code, acYear};
                return new CursorLoader(this,udiseSchoolsAssemblyConstituencyForZoneUri,projectionAssemblyConstituencyForZone,selectionStringAssemblyConstituencyForZone,selectionArgumentsAssemblyConstituencyForZone,sortOrderColumnName);


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
                List<ManagementWiseSchoolSummaryModel> stateSummaryList = SchoolReportsHelper.stateManagementWiseSummary(data,entityName);
                numberOfSchoolsSummaryAdapter.swapDataList(stateSummaryList);
                break;

            case ID_MANAGEMENT_WISE_NUMBER_OF_SCHOOLS_DISTRICT_SUMMARY_LOADER:
                List<ManagementWiseSchoolSummaryModel> districtSummaryList = SchoolReportsHelper.districtManagementWiseSummary(data,entityCode,entityName);
                numberOfSchoolsSummaryAdapter.swapDataList(districtSummaryList);
                break;

            case ID_MANAGEMENT_WISE_NUMBER_OF_SCHOOLS_ZONE_SUMMARY_LOADER:
                List<ManagementWiseSchoolSummaryModel> zoneSummaryList = SchoolReportsHelper.zoneManagementWiseSummary(data,entityCode,entityName);
                numberOfSchoolsSummaryAdapter.swapDataList(zoneSummaryList);
                break;

            case ID_MANAGEMENT_WISE_NUMBER_OF_SCHOOLS_CLUSTER_SUMMARY_LOADER:
                List<ManagementWiseSchoolSummaryModel> clusterSummaryList = SchoolReportsHelper.clusterManagementWiseSummary(data,entityCode,entityName);
                numberOfSchoolsSummaryAdapter.swapDataList(clusterSummaryList);
                break;

            case ID_MANAGEMENT_WISE_NUMBER_OF_SCHOOLS_ASSEMBLY_CONSTITUENCY_SUMMARY_WITH_PARENT_STATE_LOADER:
                List<ManagementWiseSchoolSummaryModel> assemblyConstituencyForStateList = SchoolReportsHelper.assemblyConstituencyManagementWiseSummary(data,entityCode,entityName,parentEntityCodeForAssemblyConstituency,SchoolReportsConstants.ASSEMBLY_CONSTITUENCY_PARENT_STATE);
                numberOfSchoolsSummaryAdapter.swapDataList(assemblyConstituencyForStateList);
                break;

            case ID_MANAGEMENT_WISE_NUMBER_OF_SCHOOLS_ASSEMBLY_CONSTITUENCY_SUMMARY_WITH_PARENT_DISTRICT_LOADER:
                List<ManagementWiseSchoolSummaryModel> assemblyConstituencyForDistrictList = SchoolReportsHelper.assemblyConstituencyManagementWiseSummary(data,entityCode,entityName,parentEntityCodeForAssemblyConstituency,SchoolReportsConstants.ASSEMBLY_CONSTITUENCY_PARENT_DISTRICT);
                numberOfSchoolsSummaryAdapter.swapDataList(assemblyConstituencyForDistrictList);
                break;

            case ID_MANAGEMENT_WISE_NUMBER_OF_SCHOOLS_ASSEMBLY_CONSTITUENCY_SUMMARY_WITH_PARENT_ZONE_LOADER:
                List<ManagementWiseSchoolSummaryModel> assemblyConstituencyForZoneList = SchoolReportsHelper.assemblyConstituencyManagementWiseSummary(data,entityCode,entityName,parentEntityCodeForAssemblyConstituency,SchoolReportsConstants.ASSEMBLY_CONSTITUENCY_PARENT_ZONE);
                numberOfSchoolsSummaryAdapter.swapDataList(assemblyConstituencyForZoneList);
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


    /**
     * This method acts as a onClick handler for
     * {@link NumberOfSchoolsSummaryAdapter#NumberOfSchoolsSummaryAdapter(Context, NumberOfSchoolsSummaryAdapter.NumberOfSchoolsSummaryAdapterOnClickHandler, boolean)}
     * This method handles the actual clicks on the recycler view items. All the parameters are
     * passed by the recyclerview adapter to this handler automatically through
     * the interface implemented in that adapter.
     *
     * On click this method calls the {@link NumberOfSchoolsLevelWise} activity
     * to display appropriate level-wise report.
     * @param reportDisplayLevel desired level of report to be displayed on click
     * @param zoneDistrictOrStateCode code of the current entity
     *                                (State, District, Zone, Cluster or Assembly Constituency)
     * @param zoneDistrictOrStateName name of the current entity
     * @param parentCode code of the parent of the current entity
     */

    @Override
    public void onClick(int reportDisplayLevel, String zoneDistrictOrStateCode, String zoneDistrictOrStateName,String parentCode) {
        Intent intent;
        switch(reportDisplayLevel){

            case SchoolReportsConstants.REPORT_DISPLAY_LEVEL_STATEWISE:
                /*
                 * Use REPORT_DISPLAY_LEVEL_WISE_TYPE for passing the type of report
                 */
                intent = new Intent(this,NumberOfSchoolsLevelWise.class);
                intent.putExtra(SchoolReportsConstants.EXTRA_KEY_ACADEMIC_YEAR,academicYear);
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

            case SchoolReportsConstants.REPORT_DISPLAY_ASSEMBLY_CONSTITUENCY_SUMMARY_WITH_PARENT_STATE:
            case SchoolReportsConstants.REPORT_DISPLAY_ASSEMBLY_CONSTITUENCY_SUMMARY_WITH_PARENT_DISTRICT:
            case SchoolReportsConstants.REPORT_DISPLAY_ASSEMBLY_CONSTITUENCY_SUMMARY_WITH_PARENT_ZONE:
                break;


            case SchoolReportsConstants.REPORT_DISPLAY_INVALID:
                Log.e(TAG,"Invalid report request received with report code:" + reportDisplayLevel);
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
}
