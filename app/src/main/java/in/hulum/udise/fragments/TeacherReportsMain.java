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
 */

public class TeacherReportsMain extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView;
        return inflater.inflate(R.layout.teacher_reports_main,container,false);
    }
}
