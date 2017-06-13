/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.mobile.activity.home.adapter;

import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.List;
import cat.bcn.vincles.lib.vo.FeedItem;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.component.swipetoremove.SwipeToRemoveAdapter;
import cat.bcn.vincles.mobile.model.FeedModel;

public class FeedRVAdapter extends RecyclerView.Adapter<FeedViewHolder> implements SwipeToRemoveAdapter {
    private static final int PENDING_REMOVAL_TIMEOUT = 3000; // 3sec
    private List<FeedItem> itemsPendingRemoval;
    private List<FeedItem> mDataset;
    private Context context;

    public FeedRVAdapter(Context context, List<FeedItem> dataSet) {
        this.context = context;
        itemsPendingRemoval = new ArrayList<>();
        mDataset = dataSet;
        if (mDataset == null) mDataset = new ArrayList<>();
    }

    @Override
    public FeedViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_list_feed, parent, false);
        FeedViewHolder vh = new FeedViewHolder(v, mDataset, context);
        return vh;
    }

    @Override
    public void onBindViewHolder(FeedViewHolder holder, int position) {
        FeedItem item = mDataset.get(position);
        holder.setFeedItem(item);
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    @Override
    public void pendingRemoval(final int position) {
        final FeedItem item = mDataset.get(position);
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
        FeedItem item = mDataset.get(position);
        if (itemsPendingRemoval.contains(item)) {
            itemsPendingRemoval.remove(item);

            if (mDataset.contains(item)) {
                mDataset.remove(position);
                FeedModel.getInstance().remove(item);
                notifyItemRemoved(position);
            }
        }
    }

    @Override
    public boolean isPendingRemoval(int position) {
        if (mDataset.size() <= position || position == -1) return false;
        FeedItem item = mDataset.get(position);
        return itemsPendingRemoval.contains(item);
    }

    @Override
    public boolean isRemovable(int position) {
        // ALL ITEMS ARE REMOVABLE
        return true;
    }
}
