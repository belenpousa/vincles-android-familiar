/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.mobile.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import cat.bcn.vincles.lib.util.VinclesConstants;
import cat.bcn.vincles.lib.vo.Task;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.activity.TaskActivity;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends ArrayAdapter<Task> {
    private Context context;
    private ImageView imgDelete;

    public TaskAdapter(Context context, int resource, List<Task> objects) {
        super(context, resource, objects);
        this.context = context;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        Task item = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_list_task, parent, false);
        }

        // Lookup view for data population
        TextView txTimeFrom = (TextView) convertView.findViewById(R.id.item_task_timeFrom);
        TextView txTimeTo = (TextView) convertView.findViewById(R.id.item_task_timeTo);
        TextView txDescription = (TextView) convertView.findViewById(R.id.item_task_description);
        TextView txState = (TextView) convertView.findViewById(R.id.item_task_state);
        imgDelete = (ImageView) convertView.findViewById(R.id.item_task_delete);
        imgDelete.setVisibility(View.GONE);
        // Populate the data into the template view using the data object
        txTimeFrom.setText(VinclesConstants.getDateString(item.getDate(), getContext().getResources().getString(R.string.timeformat),
                new Locale(getContext().getResources().getString(R.string.locale_language), getContext().getResources().getString(R.string.locale_country)))
        );

        Calendar cal = Calendar.getInstance();
        cal.setTime(item.getDate());
        cal.add(Calendar.HOUR, (int) item.duration);
        cal.add(Calendar.MINUTE, (int) ((item.duration - ((int) item.duration)) * 60));
        txTimeTo.setText(VinclesConstants.getDateString(cal.getTime(), getContext().getResources().getString(R.string.timeformat),
                new Locale(getContext().getResources().getString(R.string.locale_language), getContext().getResources().getString(R.string.locale_country)))
        );
        txDescription.setText(item.description);

        if (item.state == Task.STATE_PENDING) {
            txState.setText("(*)Cita pendiente");
        } else if (item.state == Task.STATE_ACCEPTED) {
            txState.setText("(*)Cita aceptada");
        } else if (item.state == Task.STATE_REJECTED) {
            txState.setText("(*)Cita rechazada");
        }

        imgDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((TaskActivity) context).deleteTask(position);
            }
        });

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //CAUTION: Nothing here!!!
            }
        });

        convertView.setOnTouchListener(new View.OnTouchListener() {
            private int initialx = 0;
            private int currentx = 0;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    initialx = (int) event.getX();
                    currentx = (int) event.getX();
                }

                if (event.getAction() == MotionEvent.ACTION_UP ||
                        event.getAction() == MotionEvent.ACTION_CANCEL) {
                    currentx = (int) event.getX();

                    if (initialx - currentx > 100) {
                        imgDelete.setVisibility(View.VISIBLE);
                    } else if (initialx - currentx < -100) {
                        imgDelete.setVisibility(View.GONE);
                    } else {
                        ((TaskActivity) context).selectTask(position);
                    }
                }

                return false;
            }

        });

        // Return the completed view to render on screen
        return convertView;
    }
}
