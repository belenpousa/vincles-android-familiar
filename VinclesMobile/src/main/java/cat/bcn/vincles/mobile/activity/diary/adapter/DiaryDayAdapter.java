/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.mobile.activity.diary.adapter;

import android.content.Context;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import cat.bcn.vincles.lib.util.AsyncResponse;
import cat.bcn.vincles.lib.util.VinclesConstants;
import cat.bcn.vincles.lib.vo.FeedItem;
import cat.bcn.vincles.lib.vo.Task;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.component.swipetoremove.SwipeToRemoveAdapter;
import cat.bcn.vincles.mobile.model.AndroidCalendarModel;
import cat.bcn.vincles.mobile.model.FeedModel;
import cat.bcn.vincles.mobile.model.MainModel;
import cat.bcn.vincles.mobile.model.TaskModel;

public class DiaryDayAdapter extends RecyclerView.Adapter<DiaryViewHolder> implements SwipeToRemoveAdapter {
    private static final int PENDING_REMOVAL_TIMEOUT = 3000; // 3sec
    private List<Task> itemsPendingRemoval;
    private List<Task> mDataset;
    private Context ctx;

    public DiaryDayAdapter(List<Task> myDataset, Context pCtx) {
        mDataset = myDataset;
        itemsPendingRemoval = new ArrayList<>();
        this.ctx = pCtx;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public DiaryViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_list_diary, parent, false);

        DiaryViewHolder vh = new DiaryViewHolder(v, mDataset, this);
        vh.setContext(ctx);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(DiaryViewHolder holder, int position) {
        holder.dateText.setText(mDataset.get(position).description);
        holder.startTime.setText(VinclesConstants.getDateString(mDataset.get(position).getDate(),
                ctx.getResources().getString(R.string.timeformat),
                new Locale(ctx.getResources().getString(R.string.locale_language),
                ctx.getResources().getString(R.string.locale_country))));

        Calendar cal = Calendar.getInstance();
        cal.setTime(mDataset.get(position).getDate());
        cal.add(Calendar.MINUTE, (int)mDataset.get(position).duration);
        holder.endTime.setText(VinclesConstants.getDateString(cal.getTime(), ctx.getResources().getString(R.string.timeformat),
                new Locale(ctx.getResources().getString(R.string.locale_language), ctx.getResources().getString(R.string.locale_country)))
        );

        Task item = mDataset.get(position);
        if (isOccupied(item))
            holder.setStatus(Task.STATE_OCCUPIED);
        else holder.setStatus(mDataset.get(position).state);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    @Override
    public void pendingRemoval(final int position) {
        final Task item = mDataset.get(position);
        if (!itemsPendingRemoval.contains(item)) {
            itemsPendingRemoval.add(item);

            // let's create, store and post a runnable to remove the item
            Runnable pendingRemovalRunnable = new Runnable() {
                @Override
                public void run() {
                    itemsPendingRemoval.remove(item);
                    notifyItemChanged(position);
                }
            };
            new Handler().postDelayed(pendingRemovalRunnable, PENDING_REMOVAL_TIMEOUT);
        }
    }

    @Override
    public void remove(int position) {
        Task item = mDataset.get(position);
        if (itemsPendingRemoval.contains(item)) {
            itemsPendingRemoval.remove(item);

            if (mDataset.contains(item)) {
                mDataset.remove(position);
                notifyItemRemoved(position);
            }
        }

        FeedModel.getInstance().addItem(new FeedItem()
                .setType(FeedItem.FEED_TYPE_DELETED_EVENT)
                .setIdData(item.getId())
                .setFixedData(null, item.description, item.getDate().getTime())
                .setExtraId(item.network.userVincles.getId()));

        TaskModel taskModel = TaskModel.getInstance();
        taskModel.deleteTaskServer(item);
    }

    @Override
    public boolean isPendingRemoval(int position) {
        if (mDataset.size() <= position || position == -1) return false;
        Task item = mDataset.get(position);
        return itemsPendingRemoval.contains(item);
    }

    @Override
    public boolean isRemovable(int position) {
        return !isOccupied(mDataset.get(position));
    }

    private boolean isOccupied(Task item) {
        return TextUtils.isEmpty(item.description) && item.owner == null;
    }
}

