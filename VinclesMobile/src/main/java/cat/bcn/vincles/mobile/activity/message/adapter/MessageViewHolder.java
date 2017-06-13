/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.mobile.activity.message.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;
import java.util.Locale;
import cat.bcn.vincles.lib.util.VinclesConstants;
import cat.bcn.vincles.lib.vo.Message;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.activity.message.details.MessageDetailAudioActivity;
import cat.bcn.vincles.mobile.activity.message.details.MessageDetailImageActivity;
import cat.bcn.vincles.mobile.activity.message.details.MessageDetailTextActivity;
import cat.bcn.vincles.mobile.activity.message.details.MessageDetailVideoActivity;
import cat.bcn.vincles.mobile.model.MessageModel;

public class MessageViewHolder extends RecyclerView.ViewHolder {
    public TextView title, subtitle, time;
    private ImageView icon;
    private Context context;

    public MessageViewHolder(View v, final List<Message> messageList) {
        super(v);
        title = (TextView) v.findViewById(R.id.item_message_title);
        subtitle = (TextView) v.findViewById(R.id.item_message_subtitle);
        time = (TextView) v.findViewById(R.id.item_message_time);
        icon = (ImageView) v.findViewById(R.id.item_message_icon);

        // SET ON CLICK LISTENER TO EDIT
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MessageModel messageModel = MessageModel.getInstance();
                messageModel.currentMessage = messageList.get(getAdapterPosition());
                messageModel.view = MessageModel.MESSAGE_DETAIL;
                switch (messageModel.currentMessage.metadataTipus) {
                    case VinclesConstants.RESOURCE_TYPE.TEXT_MESSAGE:
                        // THIS MESSAGE TYPE IS NOT POSSIBLE
                        context.startActivity(new Intent(context, MessageDetailTextActivity.class));
                    case VinclesConstants.RESOURCE_TYPE.IMAGES_MESSAGE:
                        // THIS MESSAGE TYPE DOES NOT EXIST YET
                        context.startActivity(new Intent(context, MessageDetailImageActivity.class));
                        break;
                    case VinclesConstants.RESOURCE_TYPE.AUDIO_MESSAGE:
                        context.startActivity(new Intent(context, MessageDetailAudioActivity.class));
                        break;
                    case VinclesConstants.RESOURCE_TYPE.VIDEO_MESSAGE:
                    default:
                        context.startActivity(new Intent(context, MessageDetailVideoActivity.class));
                        break;
                }
            }
        });
    }

    public void setContext(Context ctx) {
        this.context = ctx;
    }

    public void setMessage(Message item) {
        if (item != null) {

            String title = "";
            String subtitle = "";
            String duration = "";

            if (item.sendTime != null) {
                // Populate the data into the template view using the data object
                title = VinclesConstants.getDateString(item.sendTime, context.getResources().getString(R.string.dateSmallformat),
                        new Locale(context.getResources().getString(R.string.locale_language), context.getResources().getString(R.string.locale_country)));

                if (DateUtils.isToday(item.sendTime.getTime()))
                    title = context.getString(R.string.task_today);

                subtitle = VinclesConstants.getDateString(item.sendTime, context.getResources().getString(R.string.timeformat),
                        new Locale(context.getResources().getString(R.string.locale_language), context.getResources().getString(R.string.locale_country)));

                int min = VinclesConstants.getMinutesInterval(item.sendTime);

                duration = context.getString(R.string.message_ago) + " ";
                if (min < 60) duration += min + " " + context.getString(R.string.minutes);
                else if (min < 60 * 24)
                    duration += ((int) min / 60) + " " + context.getString(R.string.hours);
                else if (min < 60 * 24 * 30)
                    duration += ((int) min / (60 * 24)) + " " + context.getString(R.string.days);
                else duration += context.getString(R.string.message_long_ago);

                setData(title.substring(0, 1).toUpperCase() + title.substring(1), subtitle, duration);
            } else {
                setData(title, subtitle, duration);
            }

        }
    }

    public void setData(String titleText, String subtitleText, String durationText) {
        title.setText(titleText);
        subtitle.setText(subtitleText);
        time.setText(durationText);
    }

    public void setStatus(boolean readed) {
        if (readed) {
            itemView.setBackgroundResource(R.color.viewed_background);
            icon.setColorFilter(ContextCompat.getColor(context, R.color.white));
            title.setTextColor(ContextCompat.getColor(context, R.color.white));
            time.setTextColor(ContextCompat.getColor(context, R.color.white));
        } else {
            itemView.setBackgroundResource(R.color.white);
            icon.setColorFilter(ContextCompat.getColor(context, R.color.red));
            title.setTextColor(ContextCompat.getColor(context, R.color.black));
            time.setTextColor(ContextCompat.getColor(context, R.color.red));
        }
    }

    public void setType(String taskStatus) {
        switch (taskStatus) {
            case VinclesConstants.RESOURCE_TYPE.TEXT_MESSAGE:
                icon.setImageResource(R.drawable.icon_texto);
                break;
            case VinclesConstants.RESOURCE_TYPE.IMAGES_MESSAGE:
                icon.setImageResource(R.drawable.icon_fotos);
                break;
            case VinclesConstants.RESOURCE_TYPE.AUDIO_MESSAGE:
                icon.setImageResource(R.drawable.icon_micro);
                break;
            case VinclesConstants.RESOURCE_TYPE.VIDEO_MESSAGE:
                icon.setImageResource(R.drawable.icon_video);
                break;
            default:
                icon.setImageResource(R.drawable.icon_texto);
                break;
        }
    }
}
