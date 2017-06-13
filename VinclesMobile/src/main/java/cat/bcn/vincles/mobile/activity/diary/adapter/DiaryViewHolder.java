/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.mobile.activity.diary.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;
import cat.bcn.vincles.lib.vo.Task;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.activity.VinclesActivity;
import cat.bcn.vincles.mobile.activity.diary.DiaryActivity;
import cat.bcn.vincles.mobile.activity.diary.DiaryActivityNew;
import cat.bcn.vincles.mobile.model.TaskModel;

public class DiaryViewHolder extends RecyclerView.ViewHolder {
    public TextView dateText, dateStatus, startTime, endTime;
    public ImageView clock;
    public Button rememberButton, removeButton;
    private Context ctx;

    public DiaryViewHolder(View v, final List<Task> tasklist, final DiaryDayAdapter adapter) {
        super(v);
        dateText = (TextView)v.findViewById(R.id.dateText);
        dateStatus = (TextView)v.findViewById(R.id.dateStatus);
        startTime = (TextView)v.findViewById(R.id.startTime);
        endTime = (TextView)v.findViewById(R.id.endTime);
        clock = (ImageView) v.findViewById(R.id.diaryClock);
        rememberButton = (Button) v.findViewById(R.id.buttonRememberTask);
        removeButton = (Button) v.findViewById(R.id.buttonRemoveTask);

        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TaskModel taskModel = TaskModel.getInstance();
                if (TextUtils.isEmpty(tasklist.get(getAdapterPosition()).description)
                        && tasklist.get(getAdapterPosition()).owner == null) return;    // OCCUPIED ITEMS AREN'T EDITABLE
                taskModel.currentTask = tasklist.get(getAdapterPosition());
                taskModel.view = TaskModel.TASK_DETAIL;

                // I NEED STARTACTIVITYFORRESULT
                // VinclesActivity.instance SHOULD BE ALWAYS THE VISIBLE ACTIVITY
                VinclesActivity.instance.startActivityForResult(
                        new Intent(ctx, DiaryActivityNew.class), DiaryActivity.REQUEST_CREATE_NEW_TASK);
            }
        });

        rememberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TaskModel taskModel = TaskModel.getInstance();
                taskModel.rememberTask(tasklist.get(getAdapterPosition()));
                rememberButton.setEnabled(false);
                rememberButton.setAlpha(0.4f);
            }
        });

        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.pendingRemoval(getAdapterPosition());
                adapter.remove(getAdapterPosition());
            }
        });
    }

    public void setContext(Context ctx) {
        this.ctx = ctx;
    }

    public void setStatus(int taskStatus) {
        switch (taskStatus) {
            case Task.STATE_ACCEPTED:
                itemView.setBackgroundResource(R.color.white);
                clock.setColorFilter(ContextCompat.getColor(ctx, R.color.red));
                dateText.setTextColor(ContextCompat.getColor(ctx, R.color.black));
                startTime.setTextColor(ContextCompat.getColor(ctx, R.color.black));
                endTime.setTextColor(ContextCompat.getColor(ctx, R.color.black));
                dateStatus.setText(ctx.getString(R.string.task_accepted));
                dateStatus.setVisibility(View.VISIBLE);

                rememberButton.setVisibility(View.GONE);
                removeButton.setVisibility(View.GONE);
                break;
            case Task.STATE_PENDING:
                itemView.setBackgroundResource(R.color.white);
                clock.setColorFilter(ContextCompat.getColor(ctx, R.color.red));
                dateText.setTextColor(ContextCompat.getColor(ctx, R.color.black));
                startTime.setTextColor(ContextCompat.getColor(ctx, R.color.black));
                endTime.setTextColor(ContextCompat.getColor(ctx, R.color.black));
                dateStatus.setText(ctx.getString(R.string.task_pending));
                dateStatus.setVisibility(View.VISIBLE);

                rememberButton.setText(ctx.getString(R.string.task_button_remember));
                rememberButton.setVisibility(View.VISIBLE);
                removeButton.setVisibility(View.GONE);
                break;
            case Task.STATE_REJECTED:
                itemView.setBackgroundResource(R.color.white);
                clock.setColorFilter(ContextCompat.getColor(ctx, R.color.red));
                dateText.setTextColor(ContextCompat.getColor(ctx, R.color.black));
                startTime.setTextColor(ContextCompat.getColor(ctx, R.color.black));
                endTime.setTextColor(ContextCompat.getColor(ctx, R.color.black));
                dateStatus.setText(ctx.getString(R.string.task_rejected));
                dateStatus.setVisibility(View.VISIBLE);

                removeButton.setText(ctx.getString(R.string.task_button_remove));
                rememberButton.setVisibility(View.GONE);
                removeButton.setVisibility(View.VISIBLE);
                break;
            default: // OCCUPIED
                itemView.setBackgroundResource(R.color.gray1);
                clock.setColorFilter(ContextCompat.getColor(ctx, R.color.white));
                dateText.setTextColor(ContextCompat.getColor(ctx, R.color.white));
                startTime.setTextColor(ContextCompat.getColor(ctx, R.color.white));
                endTime.setTextColor(ContextCompat.getColor(ctx, R.color.white));
                dateStatus.setVisibility(View.INVISIBLE);
                dateText.setText(ctx.getString(R.string.task_date_occupied));

                rememberButton.setVisibility(View.GONE);
                removeButton.setVisibility(View.GONE);
                break;
        }

    }
}
