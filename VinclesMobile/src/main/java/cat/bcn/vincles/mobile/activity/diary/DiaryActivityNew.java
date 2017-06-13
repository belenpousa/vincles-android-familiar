/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.mobile.activity.diary;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import cat.bcn.vincles.lib.util.AsyncResponse;
import cat.bcn.vincles.lib.util.VinclesConstants;
import cat.bcn.vincles.lib.vo.FeedItem;
import cat.bcn.vincles.lib.vo.Task;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.activity.MainActivity;
import cat.bcn.vincles.mobile.model.AndroidCalendarModel;
import cat.bcn.vincles.mobile.model.FeedModel;
import cat.bcn.vincles.mobile.model.TaskModel;

public class DiaryActivityNew extends MainActivity {
    private static final String TAG = "TaskActivity";
    protected TaskModel taskModel;
    protected AndroidCalendarModel androidCalendarModel;

    private Spinner spiYear;
    private Spinner spiMonth;
    private Spinner spiDay;
    private Spinner spiHour;
    private Spinner spiMinute;
    private Spinner spiDuration;
    private Calendar calendar;
    private ArrayAdapter<String> dayAdapter;
    private Date currentDateTask;

    EditText ediDescriptionTask;
    View buttonAction;
    ViewGroup layoutOccupied, canvasOccupied;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_new);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        spiYear = (Spinner) findViewById(R.id.spiYear);
        spiMonth = (Spinner) findViewById(R.id.spiMonth);
        spiDay = (Spinner) findViewById(R.id.spiDay);
        spiHour = (Spinner) findViewById(R.id.spiHour);
        spiMinute = (Spinner) findViewById(R.id.spiMinute);
        spiDuration = (Spinner) findViewById(R.id.spiDuration);
        buttonAction = findViewById(R.id.buttonAction);

        layoutOccupied = (ViewGroup) findViewById(R.id.layout_occupied);
        canvasOccupied = (ViewGroup) findViewById(R.id.canvas_occupied);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        taskModel = TaskModel.getInstance();
        calendar = Calendar.getInstance();
        calendar.setTime(taskModel.currentTask.getDate());
        androidCalendarModel = AndroidCalendarModel.getInstance();
        androidCalendarModel.selectDefaultCalendar(this);

        // Year
        ArrayList<String> yearList = new ArrayList<String>();
        yearList.add(getResources().getString(R.string.year));
        int year = calendar.get(Calendar.YEAR);
        int interval = 5;
        for (int i = year; i <= year + interval; i++) {
            yearList.add(Integer.toString(i));
        }
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<String>(this,
                R.layout.simple_spinner_item, yearList);
        // Apply the adapter to the spinner
        spiYear.setAdapter(yearAdapter);

        // Month
        ArrayAdapter<CharSequence> monthAdapter = ArrayAdapter.createFromResource(this,
                R.array.monthList, R.layout.simple_spinner_item);
        spiMonth.setAdapter(monthAdapter);

        // Day
        ArrayList<String> dayList = new ArrayList<String>();
        dayList.add(getResources().getString(R.string.day));
        for (int i = 1; i <= 31; i++) {
            dayList.add(Integer.toString(i));
        }
        dayAdapter = new ArrayAdapter<String>(this,
                R.layout.simple_spinner_item, dayList);
        spiDay.setAdapter(dayAdapter);

        // Hour
        ArrayList<String> hourList = new ArrayList<String>();
        hourList.add(getResources().getString(R.string.hour));
        for (int i = 0; i <= 23; i++) {
            hourList.add(Integer.toString(i));
        }
        ArrayAdapter<String> hourAdapter = new ArrayAdapter<String>(this,
                R.layout.simple_spinner_item, hourList);
        spiHour.setAdapter(hourAdapter);

        // Minute
        ArrayList<String> minuteList = new ArrayList<String>();
        minuteList.add(getResources().getString(R.string.minute));
        for (int i = 0; i <= 59; i+=5) {
            minuteList.add(Integer.toString(i));
        }
        ArrayAdapter<String> minuteAdapter = new ArrayAdapter<String>(this,
                R.layout.simple_spinner_item, minuteList);
        spiMinute.setAdapter(minuteAdapter);

        // Duration
        String durationList[] = {"0:30h.", "1:00h.", "1:30h.", "2:00h.", "2:30h.", "3:00h.", "3:30h.", "4:00h.", "4:30h.", "5:00h."};
        ArrayAdapter<String> durationAdapter = new ArrayAdapter<String>(this,
                R.layout.simple_spinner_item, durationList);
        spiDuration.setAdapter(durationAdapter);

        // Update view
        TextView texBack = (TextView) findViewById(R.id.texBack);
        ediDescriptionTask = (EditText) findViewById(R.id.ediDescriptionTask);
        TextView btnUpdate = (TextView) findViewById(R.id.task_button_text);
        if (taskModel.currentTask.getId() == null) {
            texBack.setText(getResources().getString(R.string.task_new));
            btnUpdate.setText(getResources().getString(R.string.task_date_send));
            spiYear.setSelection(1);
            spiMonth.setSelection(calendar.get(Calendar.MONTH) + 1);
            spiDay.setSelection(calendar.get(Calendar.DAY_OF_MONTH));
            spiHour.setSelection(calendar.get(Calendar.HOUR_OF_DAY) + 1);
            spiMinute.setSelection(calendar.get(Calendar.MINUTE)/5 + 1);
        } else {
            texBack.setText(getResources().getString(R.string.task_change));
            btnUpdate.setText(getResources().getString(R.string.task_update));
            Calendar cal = Calendar.getInstance();
            cal.setTime(taskModel.currentTask.getDate());
            ediDescriptionTask.setText(taskModel.currentTask.description);

            spiYear.setSelection(cal.get(Calendar.YEAR) - year + 1);
            spiMonth.setSelection(cal.get(Calendar.MONTH) + 1);
            spiDay.setSelection(cal.get(Calendar.DAY_OF_MONTH));
            spiHour.setSelection(cal.get(Calendar.HOUR_OF_DAY) + 1);
            spiMinute.setSelection(calendar.get(Calendar.MINUTE)/5 + 1);

            spiDuration.setSelection((int) (taskModel.currentTask.duration / 30)-1);
        }

        AdapterView.OnItemSelectedListener spinnerListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!isValidDate(spiDay.getSelectedItem() + "/" + spiMonth.getSelectedItemPosition() + "/" + spiYear.getSelectedItem()))
                    spiDay.setSelection(spiDay.getSelectedItemPosition()-1);

                else {
                    fillDaysSpinnerInMonth();
                    refreshOccupiedRows();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        };
        spiDay.setOnItemSelectedListener(spinnerListener);
        spiMonth.setOnItemSelectedListener(spinnerListener);
        spiYear.setOnItemSelectedListener(spinnerListener);
    }

    public void sendTask(View view) {
        currentDateTask = getDateFromSpinners();
        calendar = Calendar.getInstance();
        int result = currentDateTask.compareTo(calendar.getTime());
        if (result < 0) {
            mainModel.showSimpleError(findViewById(R.id.main_content), getString(R.string.error_invalid_date), Snackbar.LENGTH_LONG);
        } else if (TextUtils.isEmpty(ediDescriptionTask.getText())) {
            mainModel.showSimpleError(findViewById(R.id.main_content), getString(R.string.error_empty_desc), Snackbar.LENGTH_LONG);
        } else {
            onSendTask();
        }

        refreshOccupiedRows();
    }

    private Date getDateFromSpinners() {
        int year = 1950;
        int month = 0;
        int day = 1;
        int hour = 0;
        int minute = 0;
        float duration = 0f;
        try {
            day = Integer.parseInt(spiDay.getSelectedItem().toString());
            month = spiMonth.getSelectedItemPosition() - 1;
            year = Integer.parseInt(spiYear.getSelectedItem().toString());
            hour = Integer.parseInt(spiHour.getSelectedItem().toString());
            minute = Integer.parseInt(spiMinute.getSelectedItem().toString());
        } catch (NumberFormatException e) {
            Log.e(TAG, e.toString());
        }

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private void refreshOccupiedRows() {
        canvasOccupied.removeAllViews();
        List<Task> tasklist =  taskModel.getSelectedTaskList(getDateFromSpinners());
        if (tasklist.size() > 0) {
            LayoutInflater inflater = getLayoutInflater();
            layoutOccupied.setVisibility(View.VISIBLE);

            View currentView; TextView textView1, textView2;
            for (int i = 0; i < tasklist.size(); i += 2) {
                Log.d(TAG, "VUELTA " + i + " DE "  + tasklist.size());

                currentView = inflater.inflate(R.layout.item_task_busy_hours, null);
                textView1 = (TextView)currentView.findViewById(R.id.column1);
                textView2 = (TextView)currentView.findViewById(R.id.column2);

                textView1.setText(getTimeString(tasklist.get(i)));
                if (i+1 < tasklist.size()) textView2.setText(getTimeString(tasklist.get(i+1)));
                else textView2.setVisibility(View.INVISIBLE);

                canvasOccupied.addView(currentView);
            }

        } else
            layoutOccupied.setVisibility(View.GONE);
    }

    private String getTimeString(Task task) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(task.getDate());
        cal.add(Calendar.MINUTE, (int)task.duration);
        return
            VinclesConstants.getDateString(task.getDate(),
                    getResources().getString(R.string.timeformat),
                    new Locale(getResources().getString(R.string.locale_language),
                            getResources().getString(R.string.locale_country)))
            + " - " +
            VinclesConstants.getDateString(cal.getTime(),
                    getResources().getString(R.string.timeformat),
                    new Locale(getResources().getString(R.string.locale_language),
                            getResources().getString(R.string.locale_country)));
    }

    public void onSendTask() {
        Log.i(TAG, "sendTask()");

        buttonAction.setEnabled(false);

        Task task = new Task();
        if (taskModel.currentTask != null) {
            task = taskModel.currentTask;
        }

        task.setDate(currentDateTask);

        int duration = 0;
        try {
            duration = ((spiDuration.getSelectedItemPosition() + 1) * 30);
        } catch (NumberFormatException e) {
            Log.e(TAG, e.toString());
        }

        task.description = ediDescriptionTask.getText().toString();
        task.duration = duration;

        task.network = mainModel.currentNetwork;
        task.owner = mainModel.currentUser;
        task.state = Task.STATE_PENDING;
        task.calendarId = mainModel.currentNetwork.userVincles.idCalendar;

        final Task finalTask = task;
        if (taskModel.currentTask.getId() != null) {
            taskModel.updateTask(new AsyncResponse() {
                @Override
                public void onSuccess(Object result) {
                    setResult(DiaryActivity.RESPONSE_UPDATE_TASK);
                    FeedModel.getInstance().addItem(new FeedItem()
                            .setType(FeedItem.FEED_TYPE_EVENT_UPDATED)
                            .setIdData(taskModel.currentTask.getId())
                            .setFixedData(null, taskModel.currentTask.description, taskModel.currentTask.getDate().getTime())
                            .setExtraId(mainModel.currentNetwork.userVincles.getId()));

                    if (mainModel.synchronizations)
                        androidCalendarModel.addOrUpdateAndroidCalendarEvent(finalTask, DiaryActivityNew.this);
                    finish();
                }

                @Override
                public void onFailure(Object error) {
                    Log.i(TAG, "error: " + error);
                    String errorMessage = mainModel.getErrorByCode(error);
                    mainModel.showSimpleError(findViewById(R.id.main_content), errorMessage, Snackbar.LENGTH_LONG);
                    buttonAction.setEnabled(true);
                }
            }, task);
        } else {
            taskModel.sendTask(new AsyncResponse() {
                @Override
                public void onSuccess(Object result) {
                    setResult(RESULT_OK);
                    FeedModel.getInstance().addItem(new FeedItem()
                            .setType(FeedItem.FEED_TYPE_NEW_EVENT)
                            .setIdData(taskModel.currentTask.getId())
                            .setFixedData(null, taskModel.currentTask.description, taskModel.currentTask.getDate().getTime())
                            .setExtraId(mainModel.currentNetwork.userVincles.getId()));

                    if (mainModel.synchronizations)
                        androidCalendarModel.addOrUpdateAndroidCalendarEvent(finalTask, DiaryActivityNew.this);
                    finish();
                }

                @Override
                public void onFailure(Object error) {
                    Log.i(TAG, "error: " + error);
                    String errorMessage = mainModel.getErrorByCode(error);
                    buttonAction.setEnabled(true);
                    mainModel.showSimpleError(findViewById(R.id.main_content), errorMessage, Snackbar.LENGTH_LONG);
                }
            }, task);
        }
    }

    private void fillDaysSpinnerInMonth() {
        Calendar mycal = new GregorianCalendar(
                Integer.parseInt(spiYear.getSelectedItem().toString()),
                spiMonth.getSelectedItemPosition()-1, 1);
        int daysInMonth = mycal.getActualMaximum(Calendar.DAY_OF_MONTH);
        ArrayList<String> dayList = new ArrayList<String>();
        dayList.add(getResources().getString(R.string.day));
        for (int i = 1; i <= daysInMonth; i++) {
            dayList.add(Integer.toString(i));
        }
        dayAdapter.clear();
        dayAdapter.addAll(dayList);
        dayAdapter.notifyDataSetChanged();
    }

    private static boolean isValidDate(String input) {
        String formatString = "dd/MM/yyyy";

        try {
            SimpleDateFormat format = new SimpleDateFormat(formatString);
            format.setLenient(false);
            format.parse(input);
        } catch (ParseException e) {
            return false;
        } catch (IllegalArgumentException e) {
            return false;
        }

        return true;
    }
}
