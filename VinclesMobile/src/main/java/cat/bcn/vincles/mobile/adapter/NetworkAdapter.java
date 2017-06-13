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
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import cat.bcn.vincles.lib.util.ImageUtils;
import cat.bcn.vincles.lib.util.VinclesConstants;
import cat.bcn.vincles.lib.vo.Network;
import cat.bcn.vincles.lib.vo.User;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.activity.MainActivity;
import cat.bcn.vincles.mobile.model.MainModel;

public class NetworkAdapter extends ArrayAdapter<Network> {
    public NetworkAdapter(Context context, int resource, List<Network> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Network item = getItem(position);
        View rowView = convertView;
        if (rowView == null) {
            ViewHolder viewHolder = new ViewHolder();
            rowView = LayoutInflater.from(getContext()).inflate(R.layout.item_list_network, parent, false);
            viewHolder.imgPhoto         = (ImageView) rowView.findViewById(R.id.item_message_photo);
            viewHolder.texFullName      = (TextView) rowView.findViewById(R.id.item_network_fullname);
            viewHolder.imgCheck         = (ImageView) rowView.findViewById(R.id.item_network_check);
            rowView.setTag(viewHolder);
        }

        // fill data
        ViewHolder holder = (ViewHolder) rowView.getTag();

        if (item.userVincles == null) {
            item.userVincles = new User();
        }

        Glide.with(getContext())
                .load(MainModel.getInstance().getUserPhotoUrlFromUser(item.userVincles))
                .error(R.drawable.user).placeholder(R.color.superlightgray)
                .dontAnimate()      // GLIDE BUG WITH PLACEHOLDERS
                .into(holder.imgPhoto);

        holder.texFullName.setText(item.userVincles.alias);
        if (item.selected) {
            holder.imgCheck.setVisibility(View.VISIBLE);
        } else {
            holder.imgCheck.setVisibility(View.INVISIBLE);
        }

        // Return the completed view to render on screen
        return rowView;
    }
    static class ViewHolder {
        ImageView imgPhoto;
        TextView texFullName;
        ImageView imgCheck;
    }
}
