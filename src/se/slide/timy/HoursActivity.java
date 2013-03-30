
package se.slide.timy;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import se.slide.timy.db.DatabaseManager;
import se.slide.timy.model.Project;
import se.slide.timy.model.Report;
import se.slide.timy.service.SyncService;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class HoursActivity extends FragmentActivity implements OnDateSetListener {

    public static final String EXTRA_PROJECT_ID = "project_id";

    private Project mProject;
    private Calendar mCalendar;

    private TimePicker mTimePicker;
    private EditText mComment;
    private Button mDateButton;

    public interface HoursDialogListener {
        void onAddHoursDialog(int projectId, int hours, int minutes, Date date, String comment);
    }

    /*
     * (non-Javadoc)
     * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);

        DatabaseManager.init(this);

        setContentView(R.layout.hours_dialog);

        Intent i = getIntent();

        if (i != null && i.hasExtra(EXTRA_PROJECT_ID)) {
            int id = i.getIntExtra(EXTRA_PROJECT_ID, -1);
            List<Project> projects = DatabaseManager.getInstance().getProject(id);

            if (projects.size() > 0)
                mProject = projects.get(0);

            if (mProject != null)
                setTitle(getString(R.string.hint_add_hours) + " " + mProject.getName());
        }

        mCalendar = Calendar.getInstance();
        mCalendar.setTime(new Date());

        mComment = (EditText) findViewById(R.id.comment);

        mTimePicker = (TimePicker) findViewById(R.id.timePicker);
        mTimePicker.setIs24HourView(true);
        mTimePicker.setCurrentHour(0);
        mTimePicker.setCurrentMinute(0);

        mDateButton = (Button) findViewById(R.id.dateButton);
        mDateButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                int year = mCalendar.get(Calendar.YEAR);
                int month = mCalendar.get(Calendar.MONTH);
                int day = mCalendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dateDialog = new DatePickerDialog(v.getContext(),
                        HoursActivity.this, year, month, day);
                dateDialog.show();
            }
        });
    }

    /*
     * (non-Javadoc)
     * @see
     * android.app.DatePickerDialog.OnDateSetListener#onDateSet(android.widget
     * .DatePicker, int, int, int)
     */
    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        mCalendar.set(Calendar.YEAR, year);
        mCalendar.set(Calendar.MONTH, monthOfYear);
        mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        DateFormat dateFormat = android.text.format.DateFormat
                .getDateFormat(getApplicationContext());

        mDateButton.setText(dateFormat.format(mCalendar.getTime()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.project_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // This ID represents the Home or Up button. In the case of this
                // activity, the Up button is shown. Use NavUtils to allow users
                // to navigate up one level in the application structure. For
                // more details, see the Navigation pattern on Android Design:
                //
                // http://developer.android.com/design/patterns/navigation.html#up-vs-back
                //
                // TODO: If Settings has multiple levels, Up should navigate up
                // that hierarchy.
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.menu_cancel:
                // finishActivity(RESULT_CANCELED);
                finish();
                return true;
            case R.id.menu_done:

                onAddHoursDialog(mProject.getId(), mTimePicker.getCurrentHour(),
                        mTimePicker.getCurrentMinute(), mCalendar.getTime(), mComment.getText()
                                .toString());
                finish();

                /*
                 * if (mName.getText().toString().trim().length() < 1) return
                 * true; Intent result = new Intent();
                 * result.putExtra(EXTRA_PROJECT_NAME,
                 * mName.getText().toString());
                 * result.putExtra(EXTRA_PROJECT_COLOR_ID,
                 * mAdapter.getColor(mPosition).getId()); if (mProject != null)
                 * result.putExtra(EXTRA_PROJECT_ID, mProject.getId());
                 * setResult(RESULT_OK, result); finishActivity(RESULT_OK);
                 * finish(); return true;
                 */
        }
        return super.onOptionsItemSelected(item);
    }

    public void onAddHoursDialog(int projectId, int hours, int minutes, Date date, String comment) {
        Report report = new Report();

        List<Report> reports = DatabaseManager.getInstance().getReport(projectId, date);
        if (reports.size() > 0)
            report = reports.get(0);

        report.setProjectId(projectId);
        report.setHours(hours);
        report.setMinutes(minutes);
        report.setDate(date);
        report.setComment(comment);
        report.setGoogleCalendarSync(false);

        DatabaseManager.getInstance().addOrUpdateReport(report);

        startService(new Intent(this, SyncService.class));
    }
}
