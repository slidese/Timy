
package se.slide.timy;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;

import java.util.Date;

public class HoursDialog extends DialogFragment {

    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_PROJECT_ID = "project_id";

    private String mTitle = "About";

    private EditText mInput;
    private TimePicker mTimePicker;
    private EditText mComment;

    private int mProjectId = 0;

    public interface HoursDialogListener {
        void onAddHoursDialog(int projectId, int hours, int minutes, Date date, String comment);
    }

    public HoursDialog() {

    }

    public static final HoursDialog newInstance(String title, int projectId)
    {
        HoursDialog fragment = new HoursDialog();
        Bundle bdl = new Bundle(2);
        bdl.putString(EXTRA_TITLE, title);
        bdl.putInt(EXTRA_PROJECT_ID, projectId);
        fragment.setArguments(bdl);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.hours_dialog, container);

        Button btnOk = (Button) view.findViewById(R.id.btnOk);
        btnOk.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                HoursDialogListener activity = (HoursDialogListener) getActivity();
                activity.onAddHoursDialog(mProjectId, mTimePicker.getCurrentHour(),
                        mTimePicker.getCurrentMinute(), new Date(), mComment.getText().toString());
                dismiss();
            }
        });

        Button btnCancel = (Button) view.findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                dismiss();

            }
        });

        mTitle = getArguments().getString(EXTRA_TITLE);
        mProjectId = getArguments().getInt(EXTRA_PROJECT_ID);

        getDialog().setTitle(getString(R.string.add_time_for) + " " + mTitle);

        /*
         * mInput = (EditText) view.findViewById(R.id.input);
         * mInput.setHint(getString(R.string.hint_comment));
         * mInput.setOnEditorActionListener(this);
         */

        mComment = (EditText) view.findViewById(R.id.comment);

        mTimePicker = (TimePicker) view.findViewById(R.id.timePicker);
        mTimePicker.setIs24HourView(true);
        mTimePicker.setCurrentHour(0);
        mTimePicker.setCurrentMinute(0);

        return view;
    }

}
