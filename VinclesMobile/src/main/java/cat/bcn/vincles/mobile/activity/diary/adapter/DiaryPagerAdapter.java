/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.mobile.activity.diary.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.activity.diary.fragments.DiaryDayFragment;
import cat.bcn.vincles.mobile.activity.diary.fragments.DiaryMonthFragment;

public class DiaryPagerAdapter extends FragmentPagerAdapter {
    public static final int titleResources[] = {R.string.task_today, R.string.task_tomorrow, R.string.task_month};
    Fragment[] cacheFragment;
    Context ctx;

    public DiaryPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        ctx = context;
        cacheFragment = new Fragment[titleResources.length];
    }

    @Override
    public Fragment getItem(int position) {
        Calendar cal = Calendar.getInstance();

        if (cacheFragment[position] == null) {
            switch (position) {
                case 2:
                    cacheFragment[position] = DiaryMonthFragment.newInstance();
                    break;
                case 1:
                    cal.add(Calendar.DATE, 1);
                case 0:
                default:
                    cacheFragment[position] = DiaryDayFragment.newInstance();
                    ((DiaryDayFragment)cacheFragment[position]).setFragmentDate(cal.getTime());
                    break;
            }
        }
        return cacheFragment[position];
    }

    @Override
    public int getCount() {
        return titleResources.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return ctx.getString(titleResources[position]);
    }
}