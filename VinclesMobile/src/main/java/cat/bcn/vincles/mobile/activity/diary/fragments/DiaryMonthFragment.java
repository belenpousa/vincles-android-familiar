/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.mobile.activity.diary.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidListener;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import cat.bcn.vincles.lib.util.AsyncResponse;
import cat.bcn.vincles.lib.vo.PushMessage;
import cat.bcn.vincles.lib.vo.Task;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.activity.diary.DiaryDayDetailActivity;
import cat.bcn.vincles.mobile.model.MainModel;
import cat.bcn.vincles.mobile.model.TaskModel;

public class DiaryMonthFragment extends Fragment implements IDiaryFragment {
    private CaldroidFragment caldroidFragment;
    TaskModel taskModel;
    private List<Task> items;
    private Calendar cal;
    int month, year;

    public DiaryMonthFragment() {
    }

    public static DiaryMonthFragment newInstance() {
        DiaryMonthFragment fragment = new DiaryMonthFragment();
        fragment.taskModel = TaskModel.getInstance();
        return fragment;
    }

    @Override
    public Date getDate() {
        return new Date();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        taskModel = TaskModel.getInstance();
        Locale locale = new Locale(getResources().getString(R.string.locale_language));
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getResources().updateConfiguration(config,
                getResources().getDisplayMetrics());

        View rootView = inflater.inflate(R.layout.fragment_diary_month, container, false);
        TextView textTitle = (TextView) rootView.findViewById(R.id.diary_title);
        textTitle.setText(getString(R.string.task_month));

        caldroidFragment = new CaldroidFragment();
        Bundle args = new Bundle();
        cal = Calendar.getInstance();
        args.putInt(CaldroidFragment.MONTH, cal.get(Calendar.MONTH) + 1);
        args.putInt(CaldroidFragment.YEAR, cal.get(Calendar.YEAR));
        args.putInt(CaldroidFragment.START_DAY_OF_WEEK, CaldroidFragment.MONDAY); // Tuesday
        args.putInt(CaldroidFragment.THEME_RESOURCE, R.style.CaldroidVincles);
        caldroidFragment.setArguments(args);

        month = cal.get(Calendar.MONTH);
        year = cal.get(Calendar.YEAR);

        caldroidFragment.setMinDate(cal.getTime());
        caldroidFragment.setTextColorForDate(R.color.white, cal.getTime());

        android.support.v4.app.FragmentTransaction t = getFragmentManager().beginTransaction();
        t.replace(R.id.calContainer, caldroidFragment);
        t.commit();

        final CaldroidListener listener = new CaldroidListener() {
            @Override
            public void onSelectDate(Date date, View view) {
                // Refresh adapter
                Intent i = new Intent(getActivity(), DiaryDayDetailActivity.class);
                i.putExtra("date", date.getTime());
                startActivity(i);
            }

            @Override
            public void onChangeMonth(int month, int year) {
                DiaryMonthFragment.this.month = month -1;
                DiaryMonthFragment.this.year = year;
                fillMonthCalendar();
            }

            @Override
            public void onLongClickDate(Date date, View view) {
            }

            @Override
            public void onCaldroidViewCreated() {
            }

        };
        caldroidFragment.setCaldroidListener(listener);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshFragment();
    }

    @Override
    public void refreshFragment (Task temp, PushMessage pushMessage) {
        if (temp == null && pushMessage.getIdData() != null) refreshFragment(); // NEED REFRESH ON REMOVE ITEM
        if (temp == null) return;
        refreshFragment();
    }

    @Override
    public void refreshFragment() {
        items = taskModel.getMonthTask(month, year);
        for (Task it : items) {
            caldroidFragment.setSelectedDate(it.getDate());
            caldroidFragment.setTextColorForDate(R.color.white, it.getDate());
        }

        if (caldroidFragment != null && getActivity() != null)
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    caldroidFragment.refreshView();
                }
            });
    }

    private void fillMonthCalendar() {
        refreshFragment();

        taskModel.getMonthTaskServerList(new AsyncResponse() {
            @Override
            public void onSuccess(Object result) {
                // GET DB ITEMS INSTEAD OF SERVER RESULTS CAUSE THEY SHOULD BE UPDATED
                items = taskModel.getMonthTask(month, year);
                refreshFragment();
            }

            @Override
            public void onFailure(Object error) {
                if (getView() != null && getActivity() != null)
                    MainModel.getInstance().showSimpleError(getView(), getString(R.string.error_task_load_list), Snackbar.LENGTH_LONG);
            }
        }, month, year);
    }


    public class WeekdayArrayAdapter extends ArrayAdapter<String> {
        public int textColor = Color.LTGRAY;

        public WeekdayArrayAdapter(Context context, int textViewResourceId,
                                   List<String> objects) {
            super(context, textViewResourceId, objects);
        }

        // To prevent cell highlighted when clicked
        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public boolean isEnabled(int position) {
            return false;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // To customize text size and color
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            TextView textView = (TextView) inflater.inflate(R.layout.weekday_textview, null);

            // Set content
            String item = getItem(position);
            textView.setText(item);

            textView.setTextColor(textColor);
            textView.setGravity(Gravity.CENTER);
            return textView;
        }

    }
}