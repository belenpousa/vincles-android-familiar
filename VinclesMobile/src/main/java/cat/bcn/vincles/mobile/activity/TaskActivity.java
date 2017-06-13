/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.mobile.activity;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;

import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cat.bcn.vincles.lib.util.AsyncResponse;
import cat.bcn.vincles.lib.util.ValidationResponse;
import cat.bcn.vincles.lib.vo.Task;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.adapter.TaskAdapter;
import cat.bcn.vincles.mobile.model.TaskModel;

public class TaskActivity extends MainActivity {
    private static final String TAG = "TaskActivity";
    private static final int GOSHT_TAB_INDEX = 3;
    protected TaskModel taskModel;

    private CaldroidFragment caldroidFragment;
    private Spinner spiYear;
    private Spinner spiMonth;
    private Spinner spiDay;
    private Spinner spiHour;
    private Spinner spiMinute;
    private Spinner spiDuration;

    @NotEmpty(messageResId = R.string.error_empty_field)
    EditText ediDescriptionTask;
    TextView txTaskResul;

    private ListView lisTask;
    private LinearLayout calContainer;
    private List<Task> items;
    private TaskAdapter adapter;
    private Calendar calendar;
    private Date currentDateTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        taskModel = TaskModel.getInstance();
        setContentView(R.layout.activity_task);

        LinearLayout linTaskList = (LinearLayout) findViewById(R.id.linTaskList);
        LinearLayout linTaskDetail = (LinearLayout) findViewById(R.id.linTaskDetail);
        calContainer = (LinearLayout) findViewById(R.id.calContainer);
        if (taskModel.view.equals(TaskModel.TASK_DETAIL)) {
            // show detail
            linTaskList.setVisibility(View.GONE);
            linTaskDetail.setVisibility(View.VISIBLE);

            spiYear = (Spinner) findViewById(R.id.spiYear);
            spiMonth = (Spinner) findViewById(R.id.spiMonth);
            spiDay = (Spinner) findViewById(R.id.spiDay);
            spiHour = (Spinner) findViewById(R.id.spiHour);
            spiMinute = (Spinner) findViewById(R.id.spiMinute);
            spiDuration = (Spinner) findViewById(R.id.spiDuration);
            calendar = Calendar.getInstance();

            // Year
            ArrayList<String> yearList = new ArrayList<String>();
            yearList.add(getResources().getString(R.string.year));
            int year = calendar.get(Calendar.YEAR);
            int interval = 5;
            for (int i = year; i <= year + interval; i++) {
                yearList.add(Integer.toString(i));
            }
            ArrayAdapter<String> yearAdapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_item, yearList);
            // Apply the adapter to the spinner
            spiYear.setAdapter(yearAdapter);

            // Month
            ArrayAdapter<CharSequence> monthAdapter = ArrayAdapter.createFromResource(this,
                    R.array.monthList, android.R.layout.simple_spinner_item);
            monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spiMonth.setAdapter(monthAdapter);

            // Day
            ArrayList<String> dayList = new ArrayList<String>();
            dayList.add(getResources().getString(R.string.day));
            for (int i = 1; i <= 31; i++) {
                dayList.add(Integer.toString(i));
            }
            ArrayAdapter<String> dayAdapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_item, dayList);
            spiDay.setAdapter(dayAdapter);

            // Hour
            ArrayList<String> hourList = new ArrayList<String>();
            hourList.add(getResources().getString(R.string.hour));
            for (int i = 0; i <= 23; i++) {
                hourList.add(Integer.toString(i));
            }
            ArrayAdapter<String> hourAdapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_item, hourList);
            spiHour.setAdapter(hourAdapter);

            // Minute
            ArrayList<String> minuteList = new ArrayList<String>();
            minuteList.add(getResources().getString(R.string.minute));
            for (int i = 0; i <= 59; i++) {
                minuteList.add(Integer.toString(i));
            }
            ArrayAdapter<String> minuteAdapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_item, minuteList);
            spiMinute.setAdapter(minuteAdapter);

            // Duration
            String durationList[] = {"0:30h.", "1:00h.", "1:30h.", "2:00h.", "2:30h.", "3:00h.", "3:30h.", "4:00h.", "4:30h.", "5:00h."};
            ArrayAdapter<String> durationAdapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_item, durationList);
            spiDuration.setAdapter(durationAdapter);

            // Update view
            TextView texBack = (TextView) findViewById(R.id.texBack);
            ediDescriptionTask = (EditText) findViewById(R.id.ediDescriptionTask);
            Button btnUpdate = (Button) findViewById(R.id.btnUpdate);
            if (taskModel.currentTask.getId() == null) {
                texBack.setText(getResources().getString(R.string.task_new));
                btnUpdate.setText(getResources().getString(R.string.task_create));
                spiYear.setSelection(1);
                spiMonth.setSelection(calendar.get(Calendar.MONTH) + 1);
                spiDay.setSelection(calendar.get(Calendar.DAY_OF_MONTH));
                spiHour.setSelection(calendar.get(Calendar.HOUR_OF_DAY) + 1);
                spiMinute.setSelection(calendar.get(Calendar.MINUTE) + 1);
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
                spiMinute.setSelection(cal.get(Calendar.MINUTE) + 1);

                spiDuration.setSelection((int) (taskModel.currentTask.duration / 0.5) - 1);
            }
        } else {
            taskModel.currentTask = null;
            calendar = Calendar.getInstance();
            linTaskList.setVisibility(View.VISIBLE);
            linTaskDetail.setVisibility(View.GONE);
            txTaskResul = (TextView) findViewById(R.id.txTaskResul);

            Resources res = getResources();
            final TabHost tabs = (TabHost) findViewById(android.R.id.tabhost);
            tabs.setup();

            TabHost.TabSpec spec = tabs.newTabSpec("tab1");
            spec.setContent(R.id.tab1);
            spec.setIndicator("(*)Hoy");
            tabs.addTab(spec);

            spec = tabs.newTabSpec("tab2");
            spec.setContent(R.id.tab2);
            spec.setIndicator("(*)Mañana");
            tabs.addTab(spec);

            spec = tabs.newTabSpec("tab3");
            spec.setContent(R.id.tab3);
            spec.setIndicator("(*)Mes");
            tabs.addTab(spec);

            spec = tabs.newTabSpec("ghostTab");
            spec.setContent(R.id.ghostTab);
            spec.setIndicator("");
            tabs.addTab(spec);
            tabs.getTabWidget().getChildTabViewAt(3).setVisibility(View.GONE);

            caldroidFragment = new CaldroidFragment();
            Bundle args = new Bundle();
            Calendar cal = Calendar.getInstance();
            args.putInt(CaldroidFragment.MONTH, cal.get(Calendar.MONTH) + 1);
            args.putInt(CaldroidFragment.YEAR, cal.get(Calendar.YEAR));
            args.putInt(CaldroidFragment.START_DAY_OF_WEEK, CaldroidFragment.MONDAY); // Tuesday
            caldroidFragment.setArguments(args);

            android.support.v4.app.FragmentTransaction t = getSupportFragmentManager().beginTransaction();
            t.replace(R.id.calContainer, caldroidFragment);
            t.commit();

            final CaldroidListener listener = new CaldroidListener() {
                @Override
                public void onSelectDate(Date date, View view) {
                    Log.i(TAG, "onSelectDate() - date: " + date);

                    // Refresh adapter
                    items = taskModel.getSelectedTaskList(date);
                    adapter.clear();
                    adapter.addAll(items);
                    adapter.notifyDataSetChanged();

                    lisTask.setVisibility(View.VISIBLE);
                    calContainer.setVisibility(View.GONE);
                    // Deselect all tabs
                    tabs.setCurrentTab(GOSHT_TAB_INDEX);

                    taskModel.getSelectedTaskServerList(new AsyncResponse() {
                        @Override
                        public void onSuccess(Object result) {
                            Log.i(TAG, "getTaskServerList() - result");

                            adapter.clear();
                            adapter.addAll((List<Task>) result);
                            adapter.notifyDataSetChanged();
                            showTextResult(((List<Task>) result).size() == 0);
                        }

                        @Override
                        public void onFailure(Object error) {
                            Log.e(TAG, "getTaskServerList() - error: " + error);
                            mainModel.showSimpleError(findViewById(R.id.main_content), getString(R.string.error_messsage_load_list), Snackbar.LENGTH_LONG);
                            adapter.notifyDataSetChanged();
                        }
                    }, date);
                }

                @Override
                public void onChangeMonth(int month, int year) {
                    Log.i(TAG, "onChangeMonth()");
                    fillMonthCalendar(month - 1, year);
                }

                @Override
                public void onLongClickDate(Date date, View view) {
                    Log.i(TAG, "onLongClickDate()");
                }

                @Override
                public void onCaldroidViewCreated() {
                    Log.i(TAG, "onCaldroidViewCreated()");
                }

            };
            caldroidFragment.setCaldroidListener(listener);

            tabs.setCurrentTab(0);
            adapter = new TaskAdapter(this, 0, items);
            lisTask = (ListView) findViewById(R.id.lisTask);
            lisTask.setAdapter(adapter);

            items = taskModel.getTodayTaskList();
            showTextResult(items.size() == 0);
            getTodayTaskServerList();

            tabs.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
                @Override
                public void onTabChanged(String tabId) {
                    int selectedTab = tabs.getCurrentTab();
                    calContainer.setVisibility(View.GONE);
                    lisTask.setVisibility(View.VISIBLE);
                    if (selectedTab == 0) {
                        // Refresh adapter
                        items = taskModel.getTodayTaskList();
                        getTodayTaskServerList();
                    }
                    if (selectedTab == 1) {
                        // Refresh adapter
                        items = taskModel.getTomorrowTaskList();

                        taskModel.getTomorrowTaskServerList(new AsyncResponse() {
                            @Override
                            public void onSuccess(Object result) {
                                Log.i(TAG, "getTomorrowTaskServerList() - result");

                                adapter.clear();
                                adapter.addAll((List<Task>) result);
                                adapter.notifyDataSetChanged();
                                showTextResult(((List<Task>) result).size() == 0);
                            }

                            @Override
                            public void onFailure(Object error) {
                                Log.e(TAG, "getTomorrowTaskServerList() - error: " + error);
                                mainModel.showSimpleError(findViewById(R.id.main_content), getString(R.string.error_messsage_load_list), Snackbar.LENGTH_LONG);
                                adapter.notifyDataSetChanged();
                            }
                        });
                    }
                    if (selectedTab == 2) {
                        caldroidFragment.refreshView();
                        lisTask.setVisibility(View.GONE);
                        fillMonthCalendar(calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR));
                        calContainer.setVisibility(View.VISIBLE);
                    }

                    showTextResult(items.size() == 0);

                    adapter.clear();
                    adapter.addAll(items);
                    adapter.notifyDataSetChanged();
                    showTextResult(items.size() == 0);
                }
            });
        }

        super.createEnvironment(3);
    }

    private void fillMonthCalendar(int month, int year) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        int monthDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        // Clear backgrounds to update view
        for (int i = 1; i <= monthDays; i++) {
            cal.set(Calendar.DAY_OF_MONTH, i);
            Date dt = cal.getTime();
            caldroidFragment.clearBackgroundDrawableForDate(dt);
        }

        items = taskModel.getMonthTask(month, year);
        refreshCalendar(items);

        taskModel.getMonthTaskServerList(new AsyncResponse() {
            @Override
            public void onSuccess(Object result) {
                Log.i(TAG, "getMonthTaskServerList() - result");
                refreshCalendar((List<Task>) result);
                showTextResult(((List<Task>)result).size() == 0);
            }

            @Override
            public void onFailure(Object error) {
                Log.e(TAG, "getMonthTaskServerList() - error: " + error);
                mainModel.showSimpleError(findViewById(R.id.main_content), getString(R.string.error_messsage_load_list), Snackbar.LENGTH_LONG);
                adapter.notifyDataSetChanged();
            }
        }, month, year);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
    }

    public void addTask(View view) {
        taskModel.currentTask = new Task();
        taskModel.view = TaskModel.TASK_DETAIL;
        startActivity(getIntent());
    }

    public void goBack(View view) {
        back();
    }

    private void back() {
        taskModel.view = "";
        startActivity(new Intent(this, TaskActivity.class));
    }

    public void sendTask(View view) {
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
        currentDateTask = cal.getTime();
        calendar = Calendar.getInstance();
        int result = currentDateTask.compareTo(calendar.getTime());
        if (result < 0) {
            mainModel.showSimpleError(findViewById(R.id.main_content), getString(R.string.error_invalid_date), Snackbar.LENGTH_LONG);
        } else {
            validate(new ValidationResponse() {
                @Override
                public void onSuccess() {
                    onSendTask();
                }

                @Override
                public void onFailure(List<ValidationError> errors) {
                    onValidationFailed(errors);
                }

                @Override
                public void onFailure(String error) {
                    // Nothing to do
                }
            });
        }
    }

    // Send Task to service
    public void onSendTask() {
        Log.i(TAG, "sendTask()");

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

        taskModel.saveTask(task);

        back();

        mainModel.showSimpleError(findViewById(R.id.main_content), getString(R.string.task_sent)+ "\n\n" + task.description, Snackbar.LENGTH_LONG);
    }

    public void selectTask(int position) {
        taskModel.currentTask = (Task) lisTask.getItemAtPosition(position);
        taskModel.view = TaskModel.TASK_DETAIL;
        finish();
        startActivity(getIntent());
    }

    public void deleteTask(int position) {
        Task item = (Task) lisTask.getItemAtPosition(position);
        taskModel.deleteTask(item);
        adapter.remove(item);
        adapter.notifyDataSetChanged();
    }

    private void refreshCalendar(List<Task> items) {
        ColorDrawable green = new ColorDrawable(Color.GREEN);
        Map<Date, Drawable> dateMap = new HashMap<Date, Drawable>();
        // Set backgrounds with dates
        for (Task it : items) {
            dateMap.put(it.getDate(), green);
        }
        caldroidFragment.setBackgroundDrawableForDates(dateMap);

        // Set today background
        ColorDrawable bgToday = new ColorDrawable(Color.RED);
        caldroidFragment.setBackgroundDrawableForDate(bgToday, new Date());

        caldroidFragment.refreshView();
    }

    private void showTextResult(boolean size) {
        if (size == true) {
            txTaskResul.setVisibility(View.VISIBLE);
        } else {
            txTaskResul.setVisibility(View.GONE);
        }
    }

    private void getTodayTaskServerList() {
        taskModel.getTodayTaskServerList(new AsyncResponse() {
            @Override
            public void onSuccess(Object result) {
                Log.i(TAG, "getTodayTaskServerList() - result");

                adapter.clear();
                adapter.addAll((List<Task>) result);
                adapter.notifyDataSetChanged();
                showTextResult(((List<Task>) result).size() == 0);
            }

            @Override
            public void onFailure(Object error) {
                Log.e(TAG, "getTodayTaskServerList() - error: " + error);
                mainModel.showSimpleError(findViewById(R.id.main_content), getString(R.string.error_messsage_load_list), Snackbar.LENGTH_LONG);
                adapter.notifyDataSetChanged();
            }
        });
    }
}
