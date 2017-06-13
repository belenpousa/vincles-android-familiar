/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.mobile.activity.message.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.activity.message.newfragments.MessageNewPhotoFragment;
import cat.bcn.vincles.mobile.activity.message.newfragments.MessageNewTextFragment;
import cat.bcn.vincles.mobile.activity.message.newfragments.MessageNewVideoFragment;

public class MessageNewPagerAdapter extends FragmentPagerAdapter {
    public static final int titleResources[] = {R.drawable.icon_video, R.drawable.icon_fotos, R.drawable.icon_texto};
    Context ctx;

    public MessageNewPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        ctx = context;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment ret;
        switch (position) {
            case 2:
                ret = MessageNewTextFragment.newInstance();
                break;
            case 1:
                ret = MessageNewPhotoFragment.newInstance();
                break;
            case 0:
            default:
                ret = MessageNewVideoFragment.newInstance();
                break;
        }
        return ret;
    }

    @Override
    public int getCount() {
        return titleResources.length;
    }

    public void clearFragments() {
        MessageNewVideoFragment.instance = null;
        MessageNewPhotoFragment.instance = null;
        MessageNewVideoFragment.instance = null;
    }
}