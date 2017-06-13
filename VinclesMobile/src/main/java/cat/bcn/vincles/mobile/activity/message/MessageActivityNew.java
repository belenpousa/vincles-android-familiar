/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.mobile.activity.message;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import cat.bcn.vincles.lib.util.VinclesError;
import cat.bcn.vincles.lib.vo.Message;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.activity.MainActivity;
import cat.bcn.vincles.mobile.activity.message.adapter.MessageNewPagerAdapter;
import cat.bcn.vincles.mobile.activity.message.newfragments.IFragmentNewMessage;
import cat.bcn.vincles.mobile.model.MessageModel;

public class MessageActivityNew extends MainActivity {
    private static final String TAG = "MessageActivityNew";
    private MessageNewPagerAdapter mSectionsPagerAdapter;
    protected MessageModel messageModel;
    private  TabLayout tabLayout;
    private ViewPager mViewPager;
    private TabLayout.Tab selectedTab;

    public static final int titleResources[] = {R.drawable.icon_video, R.drawable.icon_fotos, R.drawable.icon_texto};


    public static final int RESULT_LOAD_IMG = 2;
    public static final int RESULT_TEXT_LOAD_IMG = 3;
    public static final int REQUEST_TAKE_GALLERY_VIDEO = 723;
    public static final int REQUEST_CREATE_NEW_VINCLES_MESSAGE = 547;

    private Snackbar snackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_new);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mSectionsPagerAdapter = new MessageNewPagerAdapter(getSupportFragmentManager(), this);
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        tabLayout = (TabLayout) findViewById(R.id.tabs);

        tabLayout.setupWithViewPager(mViewPager);
        
        messageModel  = MessageModel.getInstance();
        messageModel.currentMessage = new Message();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (selectedTab != null) selectedTab.select();
        else selectedTab = tabLayout.getTabAt(0);
        refreshTabTitles();
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                selectedTab = tabLayout.getTabAt(position);
                refreshTabTitles();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
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
    protected void onDestroy() {
        super.onDestroy();
        mSectionsPagerAdapter.clearFragments();
    }

    private void refreshTabTitles() {
        // SET TITLES
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            if (selectedTab.getPosition() == i) continue;
            Drawable icon = ContextCompat.getDrawable(this, titleResources[i]);

            // Do something for lollipop previous versions
            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP){
                DrawableCompat.setTint(icon, ContextCompat.getColor(MessageActivityNew.this, R.color.white));
                icon.clearColorFilter();
            }
            tabLayout.getTabAt(i).setIcon(icon);
        }

        if (selectedTab != null) {
            Drawable icon = ContextCompat.getDrawable(this, titleResources[selectedTab.getPosition()]);
            icon.setColorFilter(ContextCompat.getColor(MessageActivityNew.this, R.color.black), PorterDuff.Mode.MULTIPLY);
            selectedTab.setIcon(icon);
        }
    }

    public void showResendDialog(Object error) {
        stopDialog();
        if (error instanceof String) {
            if (error.equals(VinclesError.ERROR_FILE_NOT_FOUND)) {
                snackbar = Snackbar.make(findViewById(R.id.main_content), R.string.error_no_file, Snackbar.LENGTH_INDEFINITE);
            } else {
                snackbar = Snackbar.make(findViewById(R.id.main_content), R.string.error_default, Snackbar.LENGTH_INDEFINITE);
            }
        } else {
            snackbar = Snackbar.make(findViewById(R.id.main_content), R.string.message_snackbar_error, Snackbar.LENGTH_INDEFINITE);
        }
        snackbar.setAction(R.string.message_snackbar_errorbutton1, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopDialog();
                ((IFragmentNewMessage)mSectionsPagerAdapter
                        .getItem(selectedTab.getPosition())).send();
            }
        });
        View rootView = snackbar.getView();
        rootView.findViewById(R.id.snackbar_icon).setVisibility(View.VISIBLE);
        View actionButton2 = rootView.findViewById(R.id.snackbar_action2);
        actionButton2.setVisibility(View.VISIBLE);
        ((Button)actionButton2).setText(R.string.message_snackbar_actionbutton);
        actionButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopDialog();
            }
        });
        snackbar.show();
    }

    public void finishWithOk() {
        setResult(RESULT_OK);
        finish();
    }

    public void stopDialog() {
        // Close previous dialog if exist
        if (snackbar != null && snackbar.isShownOrQueued()) {
            snackbar.dismiss();
        }
    }

    public void showSendingDialog() {
        stopDialog();

        snackbar = Snackbar.make(findViewById(R.id.main_content), R.string.message_snackbar_sending, Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction(R.string.cancel, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messageModel.cancelSendMessage();
            }
        });
        snackbar.show();
    }
}