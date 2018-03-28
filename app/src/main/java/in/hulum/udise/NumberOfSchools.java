package in.hulum.udise;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

import in.hulum.udise.database.UdiseDbHelper;
import in.hulum.udise.models.UserDataModel;

public class NumberOfSchools extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_number_of_schools);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        Spinner spinnerAcademicYears = (Spinner)findViewById(R.id.spinner_academic_years);

        UserDataModel userDataModel = new UserDataModel();
        UdiseDbHelper udiseDbHelper = new UdiseDbHelper(this);
        userDataModel = udiseDbHelper.determineUserTypeAndDataModel(this);

        List<String> academicYearsList = new ArrayList<>();
        for(int i=0;i<userDataModel.getAcademicYearsList().size();i++){
            academicYearsList.add(userDataModel.getAcademicYearsList().get(i).getAc_year());
        }

        ArrayAdapter<String> adapterAcademicYears =
                new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,academicYearsList);
        adapterAcademicYears.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spinnerAcademicYears.setAdapter(adapterAcademicYears);
    }

}
