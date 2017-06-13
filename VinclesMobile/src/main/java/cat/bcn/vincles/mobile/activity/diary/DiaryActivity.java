/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.mobile.activity.diary;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;
import java.util.Date;
import cat.bcn.vincles.lib.dao.TaskDAOImpl;
import cat.bcn.vincles.lib.push.CommonVinclesGcmHelper;
import cat.bcn.vincles.lib.vo.PushMessage;
import cat.bcn.vincles.lib.vo.Task;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.activity.MainActivity;
import cat.bcn.vincles.mobile.activity.diary.adapter.DiaryPagerAdapter;
import cat.bcn.vincles.mobile.activity.diary.fragments.IDiaryFragment;
import cat.bcn.vincles.mobile.activity.message.MessageListActivity;
import cat.bcn.vincles.mobile.activity.network.NetworkActivity;
import cat.bcn.vincles.mobile.component.CustomViewPager;
import cat.bcn.vincles.mobile.model.MainModel;
import cat.bcn.vincles.mobile.model.TaskModel;
import cat.bcn.vincles.mobile.push.AppFCMDefaultListenerImpl;

public class DiaryActivity extends MainActivity {
    public static final int REQUEST_CREATE_NEW_TASK = 8539;
    public static final int RESPONSE_UPDATE_TASK = 8762;
    private DiaryPagerAdapter mSectionsPagerAdapter;
    protected TaskModel taskModel;
    private  TabLayout tabLayout;
    private CustomViewPager mViewPager;
    private TabLayout.Tab selectedTab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary);
        setTitle(getString(R.string.title_activity_diary));

        super.createEnvironment(3);

        mSectionsPagerAdapter = new DiaryPagerAdapter(getSupportFragmentManager(), this);
        mViewPager = (CustomViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        mViewPager.setPagingEnabled(false);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                selectedTab = tabLayout.getTabAt(position);
                ((IDiaryFragment)mSectionsPagerAdapter.getItem(selectedTab.getPosition()))
                        .refreshFragment();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        selectedTab = tabLayout.getTabAt(0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        taskModel  = TaskModel.getInstance();
        if (selectedTab != null) selectedTab.select();

        // REACTIVE LIST
        CommonVinclesGcmHelper.setPushListener(new AppFCMDefaultListenerImpl(this) {
            @Override
            public void onPushMessageReceived(PushMessage pushMessage) {
                super.onPushMessageReceived(pushMessage);
                try {
                    Task temp = new TaskDAOImpl().get(pushMessage.getIdData());
                    ((IDiaryFragment)mSectionsPagerAdapter.getItem(selectedTab.getPosition()))
                            .refreshFragment(temp, pushMessage);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onPushMessageError(long idPush, Throwable t) {
                super.onPushMessageError(idPush, t);
                ((IDiaryFragment)mSectionsPagerAdapter.getItem(selectedTab.getPosition()))
                        .refreshFragment();
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        CommonVinclesGcmHelper.setPushListener(MainModel.getInstance().getPushListener());
    }

    public void addTask(View v) {
        if (mainModel.currentUser != null && mainModel.currentNetwork != null) {
            taskModel.currentTask = new Task();
            Date newDate = ((IDiaryFragment)mSectionsPagerAdapter.getItem(selectedTab.getPosition())).getDate();
            taskModel.currentTask.setDate(newDate);
            taskModel.view = TaskModel.TASK_DETAIL;
            startActivityForResult(new Intent(this, DiaryActivityNew.class), REQUEST_CREATE_NEW_TASK);
        }
        else {
            mainModel.showCustomError(findViewById(R.id.main_content),
                    getString(R.string.error_no_vincles_user), Snackbar.LENGTH_LONG,
                    R.drawable.icon_user_block, getString(R.string.add_new_user),
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(DiaryActivity.this, NetworkActivity.class);
                            startActivity(intent);
                        }
                    });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CREATE_NEW_TASK) {
            if (resultCode == RESULT_OK) {
                Snackbar snackbar = Snackbar.make(findViewById(R.id.main_content), getString(R.string.task_sent), Snackbar.LENGTH_LONG);
                ((ImageView)snackbar.getView().findViewById(R.id.snackbar_icon)).setImageResource(R.drawable.icon_calendar_white);
                snackbar.getView().findViewById(R.id.snackbar_icon).setVisibility(View.VISIBLE);
                snackbar.show();
            } else if (resultCode == RESPONSE_UPDATE_TASK) {
                Snackbar snackbar = Snackbar.make(findViewById(R.id.main_content), getString(R.string.task_updated), Snackbar.LENGTH_LONG);
                ((ImageView)snackbar.getView().findViewById(R.id.snackbar_icon)).setImageResource(R.drawable.icon_calendar_white);
                snackbar.getView().findViewById(R.id.snackbar_icon).setVisibility(View.VISIBLE);
                snackbar.show();
            } if (resultCode == RESULT_CANCELED) {

            }
        }
    }
}
