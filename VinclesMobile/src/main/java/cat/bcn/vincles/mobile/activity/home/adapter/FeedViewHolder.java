/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.mobile.activity.home.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import cat.bcn.vincles.lib.dao.MessageDAOImpl;
import cat.bcn.vincles.lib.dao.NetworkDAOImpl;
import cat.bcn.vincles.lib.dao.TaskDAOImpl;
import cat.bcn.vincles.lib.dao.UserDAOImpl;
import cat.bcn.vincles.lib.util.AsyncResponse;
import cat.bcn.vincles.lib.util.VinclesConstants;
import cat.bcn.vincles.lib.vo.FeedItem;
import cat.bcn.vincles.lib.vo.User;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.activity.diary.DiaryDayDetailActivity;
import cat.bcn.vincles.mobile.activity.message.MessageListActivity;
import cat.bcn.vincles.mobile.activity.message.details.MessageDetailAudioActivity;
import cat.bcn.vincles.mobile.activity.message.details.MessageDetailImageActivity;
import cat.bcn.vincles.mobile.activity.message.details.MessageDetailVideoActivity;
import cat.bcn.vincles.mobile.activity.network.NetworkActivity;
import cat.bcn.vincles.mobile.activity.videocall.VideoCallIntroActivity;
import cat.bcn.vincles.mobile.model.FeedModel;
import cat.bcn.vincles.mobile.model.MainModel;
import cat.bcn.vincles.mobile.model.NetworkModel;

public class FeedViewHolder extends RecyclerView.ViewHolder {
    public TextView text, subtext, date, time;
    private ImageView icon, userImage;
    private View clock;
    private Context context;

    public FeedViewHolder(View v, final List<FeedItem> FeedItemList, Context context) {
        super(v);
        this.context = context;
        text = (TextView) v.findViewById(R.id.item_text);
        subtext = (TextView) v.findViewById(R.id.item_subtext);
        clock = v.findViewById(R.id.item_clock);
        time = (TextView) v.findViewById(R.id.item_time);
        date = (TextView) v.findViewById(R.id.item_date);
        icon = (ImageView) v.findViewById(R.id.item_icon);
        userImage = (ImageView) v.findViewById(R.id.item_user_icon);
    }


    public void setFeedItem(FeedItem item) {
        if (item != null) {
            text.setText(item.getType());
            time.setVisibility(View.VISIBLE);
            clock.setVisibility(View.GONE);
            subtext.setVisibility(View.GONE);

            setTimes(item);
            setType(item);
            setStatus(item);
        }
    }

    private void setTimes(FeedItem item) {
        if (item != null) {

            String title = "";
            String subtitle = "";
            String duration = "";

            if (item.getCreated() != null) {
                // Populate the data into the template view using the data object
                title = VinclesConstants.getDateString(item.getCreated(), context.getResources().getString(R.string.dateSmallformat),
                        new Locale(context.getResources().getString(R.string.locale_language), context.getResources().getString(R.string.locale_country)));

                if (DateUtils.isToday(item.getCreated().getTime()))
                    title = context.getString(R.string.task_today);

                subtitle = VinclesConstants.getDateString(item.getCreated(), context.getResources().getString(R.string.timeformat),
                        new Locale(context.getResources().getString(R.string.locale_language), context.getResources().getString(R.string.locale_country)));

                int min = VinclesConstants.getMinutesInterval(item.getCreated());

                duration = context.getString(R.string.message_ago) + " ";
                if (min < 60) duration += min + " " + context.getString(R.string.minutes);
                else if (min < 60 * 24)
                    duration += ((int) min / 60) + " " + context.getString(R.string.hours);
                else if (min < 60 * 24 * 30)
                    duration += ((int) min / (60 * 24)) + " " + context.getString(R.string.days);
                else duration += context.getString(R.string.message_long_ago);

                date.setText(title.substring(0, 1).toUpperCase() + title.substring(1) + "    " + subtitle);
                time.setText(duration);
            }

        }
    }

    public void setStatus(FeedItem item) {
        if (item.getWatched()) {
            icon.setColorFilter(ContextCompat.getColor(context, R.color.white));
            itemView.setBackgroundResource(R.color.viewed_background);
            text.setTextColor(ContextCompat.getColor(context, R.color.white));
            subtext.setTextColor(ContextCompat.getColor(context, R.color.white));
            time.setTextColor(ContextCompat.getColor(context, R.color.white));
        } else {
            icon.setColorFilter(ContextCompat.getColor(context, R.color.red));
            itemView.setBackgroundResource(R.color.white);
            text.setTextColor(ContextCompat.getColor(context, R.color.black));
            subtext.setTextColor(ContextCompat.getColor(context, R.color.black));
            time.setTextColor(ContextCompat.getColor(context, R.color.red));
        }
    }

    public void setType(final FeedItem item) {
        // SHARED WORK:
        switch (item.getType()) {
            case FeedItem.FEED_TYPE_EVENT_FROM_AGENDA:
            case FeedItem.FEED_TYPE_NEW_EVENT:
            case FeedItem.FEED_TYPE_EVENT_ACCEPTED:
            case FeedItem.FEED_TYPE_EVENT_REJECTED:
            case FeedItem.FEED_TYPE_EVENT_UPDATED:
            case FeedItem.FEED_TYPE_DELETED_EVENT:
                String dateString = VinclesConstants.getDateString(new Date(item.getItemDate()), context.getResources().getString(R.string.dateSmallformat),
                        new Locale(context.getResources().getString(R.string.locale_language), context.getResources().getString(R.string.locale_country)));
                String timeString = VinclesConstants.getDateString(new Date(item.getItemDate()), context.getResources().getString(R.string.timeformat),
                        new Locale(context.getResources().getString(R.string.locale_language), context.getResources().getString(R.string.locale_country)));
                checkTaskExistAndAddClick(item);
                icon.setImageResource(R.drawable.icon_agenda);

                if (item.getType().equals(FeedItem.FEED_TYPE_EVENT_FROM_AGENDA))
                    subtext.setText(item.getSubtext());
                else
                    subtext.setText(dateString.substring(0, 1).toUpperCase() + dateString.substring(1) +
                        "\n" + timeString + " " + item.getSubtext());
                subtext.setVisibility(View.VISIBLE);
                break;
            case FeedItem.FEED_TYPE_USER_LINKED:
            case FeedItem.FEED_TYPE_USER_UNLINKED:
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FeedModel.getInstance().setWatched(item);
                        context.startActivity(new Intent(context, NetworkActivity.class));
                    }
                });
                icon.setImageResource(R.drawable.icon_network_white);
                break;
            case FeedItem.FEED_TYPE_INCOMING_CALL:
            case FeedItem.FEED_TYPE_LOST_CALL:
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FeedModel.getInstance().setWatched(item);
                        NetworkModel.getInstance().changeNetwork(
                            NetworkModel.getInstance().getNetwork(((FeedItem) item).getExtraId())
                        );
                        context.startActivity(new Intent(context, VideoCallIntroActivity.class));
                    }
                });
                icon.setImageResource(R.drawable.icon_llamar);
        }


        // PERSONALIZE:
        switch (item.getType()) {
            case FeedItem.FEED_TYPE_NEW_MESSAGE:
                icon.setImageResource(R.drawable.icon_texto);
                text.setText(context.getString(R.string.feed_message));
                checkMessageExistAndAddClick(item, MessageListActivity.class);
                break;
            case FeedItem.FEED_TYPE_NEW_IMAGE_MESSAGE:
                icon.setImageResource(R.drawable.icon_fotos);
                text.setText(context.getString(R.string.feed_message_image));
                checkMessageExistAndAddClick(item, MessageDetailImageActivity.class);
                break;
            case FeedItem.FEED_TYPE_NEW_AUDIO_MESSAGE:
                icon.setImageResource(R.drawable.icon_micro);
                text.setText(context.getString(R.string.feed_message_audio));
                checkMessageExistAndAddClick(item, MessageDetailAudioActivity.class);
                break;
            case FeedItem.FEED_TYPE_NEW_VIDEO_MESSAGE:
                icon.setImageResource(R.drawable.icon_video);
                text.setText(context.getString(R.string.feed_message_video));
                checkMessageExistAndAddClick(item, MessageDetailVideoActivity.class);
                break;
            case FeedItem.FEED_TYPE_EVENT_FROM_AGENDA:
                clock.setVisibility(View.VISIBLE);
                time.setVisibility(View.GONE);

                text.setText(VinclesConstants.getDateString(item.getCreated(), context.getResources().getString(R.string.timeformat),
                        new Locale(context.getResources().getString(R.string.locale_language), context.getResources().getString(R.string.locale_country)))
                        + "  -  " +context.getString(R.string.feed_remember));
                break;
            case FeedItem.FEED_TYPE_NEW_EVENT:
                text.setText(context.getString(R.string.feed_task_sent));
                break;
            case FeedItem.FEED_TYPE_EVENT_ACCEPTED:
                text.setText(context.getString(R.string.feed_task_accepted));
                break;
            case FeedItem.FEED_TYPE_EVENT_REJECTED:
                text.setText(context.getString(R.string.feed_task_rejected));
                break;
            case FeedItem.FEED_TYPE_EVENT_UPDATED:
                text.setText(context.getString(R.string.feed_task_updated));
                break;
            case FeedItem.FEED_TYPE_DELETED_EVENT:
                text.setText(context.getString(R.string.feed_task_deleted));
                break;
            case FeedItem.FEED_TYPE_INCOMING_CALL:
                text.setText(context.getString(R.string.feed_call));
                break;
            case FeedItem.FEED_TYPE_LOST_CALL:
                text.setText(context.getString(R.string.feed_lost_call));
                break;
            case FeedItem.FEED_TYPE_USER_LINKED:
                text.setText(context.getString(R.string.feed_link, item.getInfo()));
                break;
            case FeedItem.FEED_TYPE_USER_UNLINKED:
                text.setText(context.getString(R.string.feed_unlink, item.getInfo()));
                break;
        }

        // EXTRA ID IS USED TO ADD VINCLESUSER, USE USER IMAGE WHEN IT IS PRESENT
        Log.d(getClass().getSimpleName(), "FEED EXTRA ID: " + item.getExtraId());
        icon.setVisibility(View.VISIBLE);
        userImage.setVisibility(View.GONE);
        if (item.getExtraId() > 0) {
            final User temp = new UserDAOImpl().get(item.getExtraId());
            if (temp != null) {
                Log.d(getClass().getSimpleName(), "Add user image");
                icon.setVisibility(View.GONE);
                userImage.setVisibility(View.VISIBLE);
                Glide.with(context)
                        .load(MainModel.getInstance().getUserPhotoUrlFromUserWithAction(temp,
                                new AsyncResponse() {
                                    @Override
                                    public void onSuccess(Object result) {
                                        Glide.with(context)
                                                .load(MainModel.getInstance().getUserPhotoUrlFromUser(temp))
                                                .error(R.drawable.user).placeholder(R.color.superlightgray)
                                                .dontAnimate()      // GLIDE BUG WITH PLACEHOLDERS
                                                .into(userImage);
                                    }

                                    @Override
                                    public void onFailure(Object error) { }
                                }))
                        .error(R.drawable.user).placeholder(R.color.superlightgray)
                        .dontAnimate()      // GLIDE BUG WITH PLACEHOLDERS
                        .into(userImage);
            }
        }
    }

    private void checkTaskExistAndAddClick(final FeedItem item) {
        if (new NetworkDAOImpl().get(item.getExtraId()) != null)
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FeedModel.getInstance().setWatched(item);
                    NetworkModel.getInstance().changeNetwork(
                            NetworkModel.getInstance().getNetwork(item.getExtraId())
                    );
                    Intent i = new Intent(context, DiaryDayDetailActivity.class);
                    i.putExtra("date", item.getItemDate());
                    context.startActivity(i);
                }
            });
        else {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(context)
                            .setMessage(R.string.feed_network_no_exist)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setNegativeButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .show();
                }
            });
        }
    }

    private void checkMessageExistAndAddClick(final FeedItem item, final Class<?> cls) {
        if (new MessageDAOImpl().get(item.getIdData()) != null)
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FeedModel.getInstance().setWatched(item);
                    Intent i = new Intent(context, cls);
                    i.putExtra("GCM_MESSAGE_ID", item.getIdData());
                    NetworkModel.getInstance().changeNetwork(
                            NetworkModel.getInstance().getNetwork(((FeedItem) item).getExtraId())
                    );
                    context.startActivity(i);
                }
            });
        else {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(context)
                            .setMessage(R.string.feed_message_no_exist)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setNegativeButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .show();
                }
            });
        }
    }
}