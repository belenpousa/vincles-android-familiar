/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.mobile.model;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import cat.bcn.vincles.lib.dao.FeedItemDAO;
import cat.bcn.vincles.lib.dao.FeedItemDAOImpl;
import cat.bcn.vincles.lib.dao.UserDAOImpl;
import cat.bcn.vincles.lib.vo.FeedItem;
import cat.bcn.vincles.lib.vo.Task;
import cat.bcn.vincles.lib.vo.User;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.activity.MainActivity;
import cat.bcn.vincles.mobile.activity.videocall.VideoCallIntroActivity;

import static android.content.Context.NOTIFICATION_SERVICE;

public class FeedModel {
    private static FeedModel instance;
    private List<FeedItem> feedList;
    private int messageLimit = 500;
    private FeedItemDAO feedItemDAO;

    public static FeedModel getInstance() {
        if (instance == null) {
            instance = new FeedModel();
        }
        return instance;
    }

    public FeedModel() {
        feedItemDAO = new FeedItemDAOImpl();
    }

    public FeedModel load() {
        refresh();
        return this;
    }

    public List<FeedItem> refresh() {
        return refresh(messageLimit);
    }

    public List<FeedItem> refresh(int limit) {
        feedList = feedItemDAO.getAll(limit);
        addEventItemsToList();
        return feedList;
    }

    private void addEventItemsToList() {
        int offsetTime = 7200000; // 2 hours in milliseconds
        if (MainModel.getInstance().currentNetwork == null) return;
        List<Task> tmpList = TaskModel.getInstance().getTodayTaskListAllNetworks();
        for (Task tmp: tmpList) {
            if (tmp.getDate().before(new Date(new Date().getTime()+offsetTime)) && tmp.getDate().after(new Date())) {
                if (tmp.owner == null || tmp.network == null || tmp.network.userVincles == null) continue;
                FeedItem item = new FeedItem()
                        .setType(FeedItem.FEED_TYPE_EVENT_FROM_AGENDA)
                        .setIdData(tmp.getId())
                        .setCreationDate(tmp.getDate())
                        .setFixedData(null, tmp.description, tmp.getDate().getTime())
                        .setExtraId(tmp.network.userVincles.getId());

                if (!feedList.contains(item)) {
                    addItemPrivate(item);
                    feedList.add(item);
                }
            }
        }
        orderFeedList();
    }

    private void orderFeedList() {
        Collections.sort(feedList, new Comparator<FeedItem>() {
            public int compare(FeedItem o1, FeedItem o2) {
                return o2.getCreated().compareTo(o1.getCreated());
            }
        });
    }

    public int count() {
        return feedList.size();
    }

    public List<FeedItem> getList(boolean refresh) {
        if (refresh) refresh();
        return feedList;
    }

    public List<FeedItem> addItem(FeedItem item) {
        addItemPrivate(item);
        refresh();
        return feedList;
    }

    public List<FeedItem> remove(FeedItem item) {
        item.delete();
        return refresh();
    }

    public void setWatched(FeedItem item) {
        item.setWatched(true);
        item.save();
    }

    private void addItemPrivate(FeedItem item) {
        item.save();
    }

    public static void addLostCall(Context ctx, Long callerId) {
        FeedModel.getInstance().addItem(
                new FeedItem().setType(FeedItem.FEED_TYPE_LOST_CALL)
                        .setExtraId(callerId));

        // ADD NOTIFICATION
        Intent resultIntent = new Intent(ctx, VideoCallIntroActivity.class);
        resultIntent.putExtra(MainActivity.CHANGE_APP_NETWORK, callerId);   // NETWORKID == USERID

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        ctx,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        User u = new UserDAOImpl().get(callerId);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(ctx)
                        .setSmallIcon(R.drawable.icon_llamar)
                        .setContentTitle(ctx.getString(R.string.feed_lost_call))
                        .setContentText(ctx.getString(R.string.notification_videoconference_lostcall, u.alias))
                        .setAutoCancel(true)
                        .setContentIntent(resultPendingIntent);

        NotificationManager mNotifyMgr =
                (NotificationManager) ctx.getSystemService(NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        mNotifyMgr.notify(1, mBuilder.build());
    }
}
