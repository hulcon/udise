package in.hulum.udise;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import in.hulum.udise.fragments.EnrolmentReportsFragment;
import in.hulum.udise.fragments.ImportDialogFragment;
import in.hulum.udise.fragments.SchoolReportsFragment;
import in.hulum.udise.fragments.TeacherReportsFragment;
import in.hulum.udise.utils.SchoolReportsConstants;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private ResponseReceiver responseReceiver = new ResponseReceiver();
    private static final int PERMISSION_READ_EXTERNAL_STORAGE_REQUEST_CODE = 200;
    private int clickedButtonId;

    private static final String TAG = MainActivity.class.getSimpleName();

    private SharedPreferences mPreferences;
    private String sharedPrefFile = SchoolReportsConstants.SHARED_PREFERENCES_FILE;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ViewPager viewPager = findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        TabLayout tabs = findViewById(R.id.tab_layout);
        tabs.setupWithViewPager(viewPager);

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();



        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        /*
         * Determine the user type on background thread so that appropriate
         * title and subtitle can be set on the navigation drawer.
         * But do it only the first time the activity is created, not
         * every time the device is rotated. onCreate method is called
         * every time the device is rotated. In order to overcome this
         * we use the bundle savedInstanceState to determine if this is
         * the first time onCreate is being called.
         */
        if(savedInstanceState == null){
            ImportJobIntentService.startActionDetermineUserType(this);
        }
    }




    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }



    public class ResponseReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {


            if(ImportUdiseData.ACTION_DOES_RAW_DATA_EXIST_IN_DATABASE.equals(intent.getAction())){
                /*
                 * This broadcast is received when a report button is pressed.
                 * Before generating any report, we first check whether there
                 * is any data in the database or it is empty.
                 */
                boolean rawDataExistsInDatabase = intent.getBooleanExtra(ImportUdiseData.PARAM_DOES_RAW_DATA_EXIST,false);
                if(rawDataExistsInDatabase){
                    /**
                     * If the database contains data, check which button was pressed
                     * to generate and display the corresponding report.
                     * The value of the variable clickedButtonId is set in the
                     * {@link MainActivity#schoolReportsClickHandler(View)} method.
                     * Here we just compare the value of this variable against the
                     * id of each button to determine which button was pressed.
                     */
                    switch (clickedButtonId){
                        case R.id.button_number_of_schools:
                            Intent tempIntent = new Intent(MainActivity.this,NumberOfSchools.class);
                            startActivity(tempIntent);
                            break;
                    }
                } else {
                    /*
                     * This case is activated only if the database is empty.
                     * If the database is empty, we display the import excel file
                     * dialog.
                     */
                    ImportDialogFragment importDialogFragment = new ImportDialogFragment();
                    importDialogFragment.setCancelable(false);
                    importDialogFragment.show(getSupportFragmentManager(),"Import Excel File");
                }
            }

            else if(intent.getAction().equals(ImportUdiseData.ACTION_DETERMINE_USER_TYPE)){
                /*
                 * This broadcast is received either when an excel file is imported
                 * or this app is started. This broadcast means that the user type
                 * has been determined on the basis of data found in the database
                 * and the user type has been stored in shared preferences.
                 * We just need to read the shared preferences and update the
                 * title and subtitle of navigation drawer header
                 */
                mPreferences = getSharedPreferences(sharedPrefFile,Context.MODE_PRIVATE);
                String navigationDrawerUserTypeString = mPreferences.getString(SchoolReportsConstants.SHARED_PREFERENCES_NAVIGATION_DRAWER_USER_TYPE_STRING,"Unknown User");
                String navigationDrawerSubtitle = mPreferences.getString(SchoolReportsConstants.SHARED_PREFERENCES_NAVIGATION_DRAWER_SUBTITLE,"Unknown Office");
                NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
                TextView textViewNavigationDrawerUserType = navigationView.getHeaderView(0).findViewById(R.id.textview_drawer_usertype);
                textViewNavigationDrawerUserType.setText(navigationDrawerUserTypeString);
                TextView textViewNavigationDrawerSubtitle = navigationView.getHeaderView(0).findViewById(R.id.textview_drawer_subtitle);
                textViewNavigationDrawerSubtitle.setText(navigationDrawerSubtitle);
            }
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter myFilter = new IntentFilter();
        myFilter.addAction(ImportUdiseData.ACTION_DOES_RAW_DATA_EXIST_IN_DATABASE);
        myFilter.addAction(ImportUdiseData.ACTION_DETERMINE_USER_TYPE);
        LocalBroadcastManager.getInstance(this).registerReceiver(responseReceiver,myFilter);
    }


    @Override
    protected void onPause() {
        super.onPause();
        /*
         * Unregister the broadcast receiver. If this is omitted,
         * it will cause the broadcast receiver to register multiple
         * times resulting in firing of response multiple times to
         * a single broadcast.
         */
        LocalBroadcastManager.getInstance(this).unregisterReceiver(responseReceiver);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        //TODO: Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_import_raw_data) {
            /*
             * Display import dialog for importing excel file.
             * The dialog is displayed even if the SQLite database
             * already contains data.
             */
            ImportDialogFragment importDialogFragment = new ImportDialogFragment();
            importDialogFragment.setCancelable(false);
            /*
             * Set a custom message to be displayed on the dialog fragment.
             * Since Google recommends to pass arguments to fragments through bundles
             * instead of custom constructors, we are using the recommended mechanism
             * to pass the custom message to the dialog fragment through the bundle.
             */
            Bundle args = new Bundle();
            args.putString(ImportDialogFragment.KEY_DIALOG_FRAGMENT_MESSAGE,"Please choose the School Raw Data Excel File to import");
            importDialogFragment.setArguments(args);
            /*
             * Show the actual dialog fragment
             */
            importDialogFragment.show(getSupportFragmentManager(),"Import Excel File");

        } else if (id == R.id.nav_import_teacher_data) {

        } else if (id == R.id.nav_import_facilities_data) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        /*
         * Close the navigation drawer automatically if any menu item is clicked
         */
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    /*
     * This method sets up the viewpager.
     * We simply add all of our fragments that we want
     * to appear in the viewpager of this activity.
     * Currently, there are only three fragments
     */
    private void setupViewPager(ViewPager viewPager){
        Adapter adapter = new Adapter(getSupportFragmentManager());
        adapter.addFragment(new SchoolReportsFragment(),"School");
        adapter.addFragment(new EnrolmentReportsFragment(),"Enrolment");
        adapter.addFragment(new TeacherReportsFragment(),"Teacher");
        viewPager.setAdapter(adapter);

    }

    /**
     * This method is the onClick handler of School Reports Layout
     * This method handles clicks on the buttons of school reports fragment.
     * It simply starts the background action "Does Raw Data Exist in Database"
     * and stores the id of the clicked button in the clickedButtonId variable.
     * Rest is handled by the {@link ResponseReceiver#onReceive(Context, Intent)}
     * nested class method. The actual code for launching appropriate action
     * corresponding to the button clicked is launched  from the broadcast receiver
     * {@link ResponseReceiver#onReceive(Context, Intent)}. If the database is empty,
     * the broadcast receiver displays an import dialog instead of the report.
     * @param view the view that was clicked
     */

    public void schoolReportsClickHandler(View view) {
        ImportJobIntentService.startActionDoesRawDataExistInDatabase(this);
        clickedButtonId = view.getId();
    }


    /*
     * This adapter class is a fragment pager adapter and
     * is used to manage the fragments.
     */
    static class Adapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public Adapter(FragmentManager manager){
            super(manager);
        }

        @Override
        public Fragment getItem(int position){
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title){
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

   /* private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE);

        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_READ_EXTERNAL_STORAGE_REQUEST_CODE);

    }*/

}
