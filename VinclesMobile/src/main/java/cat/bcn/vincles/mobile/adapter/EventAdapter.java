/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.mobile.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import cat.bcn.vincles.lib.vo.Task;
import cat.bcn.vincles.mobile.R;

import java.util.Date;
import java.util.List;

public class EventAdapter extends ArrayAdapter<Task> {
    public EventAdapter(Context context, int resource, List<Task> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Task item = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_list_event, parent, false);
        }

        // Lookup view for data population
        TextView texTitle = (TextView) convertView.findViewById(R.id.texTitle);
        TextView texMessage = (TextView) convertView.findViewById(R.id.texMessage);
        TextView texTime = (TextView) convertView.findViewById(R.id.texTime);

        // Populate the data into the template view using the data object
        texTitle.setText(item.title);
        texMessage.setText(item.description);
        texTime.setText(new Date().toString());

        // Return the completed view to render on screen
        return convertView;
    }
}
