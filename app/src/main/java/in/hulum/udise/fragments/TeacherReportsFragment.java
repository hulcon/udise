package in.hulum.udise.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import in.hulum.udise.R;

/**
 * Created by Irshad on 18-03-2018.
 * This fragment displays the UI for Teacher Reports
 */

public class TeacherReportsFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.teacher_reports_main,container,false);
    }
}
