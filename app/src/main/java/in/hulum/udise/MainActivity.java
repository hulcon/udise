package in.hulum.udise;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import in.hulum.udise.fragments.EnrolmentReportsFragment;
import in.hulum.udise.fragments.ImportDialogFragment;
import in.hulum.udise.fragments.SchoolReportsFragment;
import in.hulum.udise.fragments.TeacherReportsFragment;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private ResponseReceiver responseReceiver = new ResponseReceiver();
    private static final int PERMISSION_READ_EXTERNAL_STORAGE_REQUEST_CODE = 200;
    private int clickedButtonId;

    private static final String TAG = MainActivity.class.getSimpleName();
    static final int REQUEST_CODE_IMPORT_FILE_PICKER_ACTIVITY_FOR_RESULT = 7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        TabLayout tabs = (TabLayout) findViewById(R.id.tab_layout);
        tabs.setupWithViewPager(viewPager);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        /*ActionBar supportActionBar = getSupportActionBar();
        if(supportActionBar != null){
            VectorDrawableCompat indicator = VectorDrawableCompat.create(getResources(),R.drawable.ic_menu_camera,getTheme());
            supportActionBar.setHomeAsUpIndicator(indicator);
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }*/
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
            Log.d(TAG,"Receiver in main activity fired!!!!");
            if(intent.getAction().equals(ImportUdiseData.ACTION_DOES_RAW_DATA_EXISTS_IN_DATABASE)){
                Log.d(TAG,"Broadcast Received");
                boolean rawDataExistsInDatabase = intent.getBooleanExtra(ImportUdiseData.PARAM_DOES_RAW_DATA_EXIST,false);
                if(rawDataExistsInDatabase){
                    Toast.makeText(getApplicationContext(),"Database Exists!!! YaaY",Toast.LENGTH_SHORT).show();
                    Intent tempIntent = new Intent(MainActivity.this,NumberOfSchools.class);
                    startActivity(tempIntent);
                    switch (clickedButtonId){
                        case R.id.button_number_of_schools:
                            break;
                    }
                } else {
                    Toast.makeText(getApplicationContext(),"Oops! No data found!!",Toast.LENGTH_SHORT).show();
                    ImportDialogFragment importDialogFragment = new ImportDialogFragment();
                    importDialogFragment.setCancelable(false);
                    importDialogFragment.show(getSupportFragmentManager(),"Import Excel File");


                }
                //progressBar.setProgress(value);
                //textviewPercentage.setText(message);
                //buttonNumberOfSchools.setEnabled(true);
            }

            /*else if(intent.getAction().equals(ImportData.ACTION_IMPORT_RAW_DATA)){
                boolean isError = intent.getBooleanExtra(ImportData.PARAM_ERROR,false);
                boolean isAnalyzing = intent.getBooleanExtra(ImportData.PARAM_IS_ANALYSING,false);
                boolean isImporting = intent.getBooleanExtra(ImportData.PARAM_IS_IMPORTING,false);
                boolean isUserIdentified = intent.getBooleanExtra(ImportData.PARAM_USER_IDENTIFIED,false);
                //Log.d(TAG,"error is " + isError + " analysing " + isAnalyzing + " importing " + isImporting + " user identified " + isUserIdentified);
                if(isError){
                    String msg = intent.getStringExtra(ImportData.PARAM_MESSAGE);
                    textviewPercentage.setText(msg);
                }
                else if(isAnalyzing){
                    String msg = intent.getStringExtra(ImportData.PARAM_MESSAGE);
                    int value = intent.getIntExtra(ImportData.PARAM_PERCENTAGE_COMPLETED,0);
                    textviewPercentage.setText(msg);
                    progressBar.setProgress(value);
                    //Log.d(TAG,"Analysing message " + msg + " and percent " + value);
                }
                else if(isImporting){
                    String msg = intent.getStringExtra(ImportData.PARAM_MESSAGE);
                    int value = intent.getIntExtra(ImportData.PARAM_PERCENTAGE_COMPLETED,0);
                    textviewPercentage.setText(msg);
                    progressBar.setProgress(value);
                }
                else if(isUserIdentified){
                    String msg = intent.getStringExtra(ImportData.PARAM_MESSAGE);
                    textviewPercentage.setText(msg);
                    Log.d(TAG,"User identified is " + intent.getStringExtra(ImportData.PARAM_USER_TYPE));
                }
                else{
                    String msg = intent.getStringExtra(ImportData.PARAM_MESSAGE);
                    textviewPercentage.setText(msg);
                    //Log.d(TAG,"Percent completed is  " + intent.getIntExtra(ImportData.PARAM_PERCENTAGE_COMPLETED,-1));
                    //Log.d(TAG,"Message is " + msg);
                }
            }*/
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter myFilter = new IntentFilter();
        myFilter.addAction(ImportUdiseData.ACTION_DOES_RAW_DATA_EXISTS_IN_DATABASE);
        LocalBroadcastManager.getInstance(this).registerReceiver(responseReceiver,myFilter);
    }

    @Override
    protected void onStop() {
        /*
         * Unregister the broadcast receiver. If this is omitted,
         * it will cause the broadcast receiver to register multiple
         * times resulting in firing of response multiple times to
         * a single broadcast.
         */
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(responseReceiver);
        Log.d(TAG,"onStop called");
    }

    @Override
    protected void onPause() {
        super.onPause();
        //LocalBroadcastManager.getInstance(this).unregisterReceiver(responseReceiver);
        Log.d(TAG,"onPause called");
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_import_raw_data) {
           /* Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("application/vnd.ms-excel");
            startActivityForResult(intent, REQUEST_CODE_IMPORT_FILE_PICKER_ACTIVITY_FOR_RESULT);*/
            Log.d(TAG,"Import item in drawer clicked");
            ImportDialogFragment importDialogFragment = new ImportDialogFragment();
            importDialogFragment.setCancelable(false);
            importDialogFragment.show(getSupportFragmentManager(),"Import Excel File");

        } else if (id == R.id.nav_import_teacher_data) {

        } else if (id == R.id.nav_import_facilities_data) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setupViewPager(ViewPager viewPager){
        Adapter adapter = new Adapter(getSupportFragmentManager());
        adapter.addFragment(new SchoolReportsFragment(),"School");
        adapter.addFragment(new EnrolmentReportsFragment(),"Enrolment");
        adapter.addFragment(new TeacherReportsFragment(),"Teacher");
        viewPager.setAdapter(adapter);

    }

    public void schoolReportsClickHandler(View view) {
        ImportJobIntentService.startActionDoesRawDataExistInDatabase(this);
        clickedButtonId = view.getId();
        Log.d(TAG,"Button Clicked!!!");
        /*switch(view.getId()){
            case R.id.button_number_of_schools:

        }*/

        //buttonNumberOfSchools = (Button) findViewById(R.id.button_number_of_schools);
        //buttonNumberOfSchools.setEnabled(false);
    }

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

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE);

        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_READ_EXTERNAL_STORAGE_REQUEST_CODE);

    }

}
