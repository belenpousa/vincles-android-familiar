/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.mobile.activity.diary;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import java.util.Date;
import cat.bcn.vincles.lib.dao.TaskDAOImpl;
import cat.bcn.vincles.lib.push.CommonVinclesGcmHelper;
import cat.bcn.vincles.lib.vo.PushMessage;
import cat.bcn.vincles.lib.vo.Task;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.activity.MainActivity;
import cat.bcn.vincles.mobile.activity.diary.fragments.DiaryDayFragment;
import cat.bcn.vincles.mobile.activity.network.NetworkActivity;
import cat.bcn.vincles.mobile.model.MainModel;
import cat.bcn.vincles.mobile.model.TaskModel;
import cat.bcn.vincles.mobile.push.AppFCMDefaultListenerImpl;

public class DiaryDayDetailActivity extends MainActivity {
    private Date date;
    protected TaskModel taskModel;
    private DiaryDayFragment dayDetailFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_detail);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (getIntent().hasExtra("date")) {
            date = new Date();
            date.setTime(getIntent().getLongExtra("date", -1));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        taskModel  = TaskModel.getInstance();

        dayDetailFragment = DiaryDayFragment.newInstance();
        dayDetailFragment.setViewNewDateButton(true);
        if (date != null) dayDetailFragment.setFragmentDate(date);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, dayDetailFragment).commit();

        CommonVinclesGcmHelper.setPushListener(new AppFCMDefaultListenerImpl(this) {
            @Override
            public void onPushMessageReceived(PushMessage pushMessage) {
                super.onPushMessageReceived(pushMessage);
                try {
                    Task temp = new TaskDAOImpl().get(pushMessage.getIdData());
                    dayDetailFragment.refreshFragment(temp, pushMessage);
                } catch (Exception e) {
                    dayDetailFragment.refreshFragment();
                }
            }

            @Override
            public void onPushMessageError(long idPush, Throwable t) {
                super.onPushMessageError(idPush, t);
                dayDetailFragment.refreshFragment();
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        CommonVinclesGcmHelper.setPushListener(MainModel.getInstance().getPushListener());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void addTask(View v) {
        if (mainModel.currentUser != null && mainModel.currentNetwork != null) {
            taskModel.currentTask = new Task();
            taskModel.currentTask.setDate(dayDetailFragment.getDate());
            taskModel.view = TaskModel.TASK_DETAIL;
            startActivityForResult(new Intent(this, DiaryActivityNew.class), DiaryActivity.REQUEST_CREATE_NEW_TASK);
        }
        else {
            mainModel.showCustomError(findViewById(R.id.main_content),
                    getString(R.string.error_no_vincles_user), Snackbar.LENGTH_LONG,
                    R.drawable.icon_user_block, getString(R.string.add_new_user),
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(DiaryDayDetailActivity.this, NetworkActivity.class);
                            startActivity(intent);
                        }
                    });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == DiaryActivity.REQUEST_CREATE_NEW_TASK) {
            if (resultCode == RESULT_OK) {
                Snackbar snackbar = Snackbar.make(findViewById(R.id.main_content), getString(R.string.task_sent), Snackbar.LENGTH_LONG);
                ((ImageView)snackbar.getView().findViewById(R.id.snackbar_icon)).setImageResource(R.drawable.icon_calendar_white);
                snackbar.getView().findViewById(R.id.snackbar_icon).setVisibility(View.VISIBLE);
                snackbar.show();
            } else if (resultCode == RESULT_CANCELED) {

            }
        }
    }
}
