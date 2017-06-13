/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.mobile.activity.message.adapter;

import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import cat.bcn.vincles.lib.util.AsyncResponse;
import cat.bcn.vincles.lib.util.VinclesConstants;
import cat.bcn.vincles.lib.vo.Message;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.component.swipetoremove.SwipeToRemoveAdapter;
import cat.bcn.vincles.mobile.model.MessageModel;

public class MessageRVAdapter extends RecyclerView.Adapter<MessageViewHolder> implements SwipeToRemoveAdapter {
    private static final int PENDING_REMOVAL_TIMEOUT = 3000; // 3sec
    private List<Message> itemsPendingRemoval;
    private List<Message> mDataset;
    private Context context;
    private MessageModel messageModel = MessageModel.getInstance();

    public MessageRVAdapter(Context context, List<Message> dataSet) {
        this.context = context;
        itemsPendingRemoval = new ArrayList<>();
        mDataset = dataSet;
        if (mDataset == null) mDataset = new ArrayList<>();
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_list_message, parent, false);
        MessageViewHolder vh = new MessageViewHolder(v, mDataset);
        return vh;
    }

    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {
        Message item = mDataset.get(position);
        holder.setContext(context);
        holder.setMessage(item);
        holder.setType(item.metadataTipus);
        holder.setStatus(item.watched);
//        holder.setStatus(position < 3);
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    @Override
    public void pendingRemoval(final int position) {
        final Message item = mDataset.get(position);
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
        Message item = mDataset.get(position);
        if (itemsPendingRemoval.contains(item)) {
            itemsPendingRemoval.remove(item);

            if (mDataset.contains(item)) {
                mDataset.remove(position);
                notifyItemRemoved(position);
                messageModel.deleteMessage(item);
            }
        }
    }

    @Override
    public boolean isPendingRemoval(int position) {
        if (mDataset.size() <= position || position == -1) return false;
        Message item = mDataset.get(position);
        return itemsPendingRemoval.contains(item);
    }

    @Override
    public boolean isRemovable(int position) {
        // ALL ITEMS ARE REMOVABLE
        return true;
    }
}
