/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.mobile.activity.diary.fragments;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import cat.bcn.vincles.lib.util.AsyncResponse;
import cat.bcn.vincles.lib.util.VinclesConstants;
import cat.bcn.vincles.lib.vo.PushMessage;
import cat.bcn.vincles.lib.vo.Task;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.activity.diary.adapter.DiaryDayAdapter;
import cat.bcn.vincles.mobile.component.swipetoremove.SwipeToRemoveTouchCallback;
import cat.bcn.vincles.mobile.model.MainModel;
import cat.bcn.vincles.mobile.model.TaskModel;

public class DiaryDayFragment extends Fragment implements IDiaryFragment {
    private static final String ARG_SECTION_NUMBER = "diary_section_number";
    RecyclerView mRecyclerView;
    TaskModel taskModel;
    private DiaryDayAdapter adapter;
    private List<Task> items;
    private View rootView;
    private Date date;
    private boolean viewNewDateButton;

    public DiaryDayFragment() {
    }

    public static DiaryDayFragment newInstance() {
        DiaryDayFragment fragment = new DiaryDayFragment();
        fragment.taskModel = TaskModel.getInstance();
        return fragment;
    }

    public void setFragmentDate(Date date) {
        this.date = date;
    }

    @Override
    public Date getDate() {
        return date;
    }

    public void setViewNewDateButton(boolean viewNewDateButton) {
        this.viewNewDateButton = viewNewDateButton;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        taskModel = TaskModel.getInstance();
        if (viewNewDateButton) rootView = inflater.inflate(R.layout.fragment_diary_day_detail, container, false);
        else rootView = inflater.inflate(R.layout.fragment_diary_day, container, false);
        if (date == null) date = Calendar.getInstance().getTime();

        TextView textTitle = (TextView) rootView.findViewById(R.id.diary_title);
        textTitle.setText(VinclesConstants.getDateString(date, getResources().getString(R.string.dateLargeformat),
                new Locale(getResources().getString(R.string.locale_language), getResources().getString(R.string.locale_country)))
        );

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.lisTask);

        SwipeToRemoveTouchCallback simpleItemTouchCallback = new SwipeToRemoveTouchCallback(mRecyclerView, getActivity());
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(mRecyclerView);

        taskModel.getSelectedTaskServerList(new AsyncResponse() {
            @Override
            public void onSuccess(Object result) {
                refreshFragment();
            }

            @Override
            public void onFailure(Object error) {
                if (rootView != null && getActivity() != null) {
                    MainModel.getInstance().showSimpleError(rootView, getString(R.string.error_task_load_list), Snackbar.LENGTH_LONG);
                }
            }
        }, date);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (MainModel.getInstance().currentNetwork != null)
            refreshFragment();
    }

    @Override
    public void refreshFragment (Task temp, PushMessage pushMessage) {
        if (mRecyclerView == null) return;
        if (temp == null && pushMessage.getIdData() != null) refreshFragment(); // NEED REFRESH ON REMOVE ITEM
        if (temp == null) return;
        else {
            int position = 0;
            for (position = 0; position < items.size(); position++) {
                if (items.get(position).getId().compareTo(temp.getId()) == 0) break;
            }
            if (position < items.size()) {
                items.set(position, temp);
                final int finalPosition = position;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mRecyclerView.getAdapter().notifyItemChanged(finalPosition);
                    }
                });
            } else refreshFragment();
        }
    }

    @Override
    public void refreshFragment() {
        if (mRecyclerView == null) return;
        items = taskModel.getSelectedTaskList(date);
        adapter = new DiaryDayAdapter(items, getActivity());
        if (getActivity() != null)
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mRecyclerView.setAdapter(adapter);
                }
            });
        else mRecyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        if (items != null && items.size() > 0) {
            rootView.findViewById(R.id.task_no_items_text).setVisibility(View.GONE);
        }
    }
}