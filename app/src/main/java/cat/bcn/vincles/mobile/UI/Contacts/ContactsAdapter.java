package cat.bcn.vincles.mobile.UI.Contacts;


import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;

import cat.bcn.vincles.mobile.Client.Preferences.UserPreferences;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.Utils.OtherUtils;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> {

    private Context context;
    private List<Contact> contactList;
    private ContactsAdapterListener listener;
    private boolean selectionEnabled;
    private List<OnItemClicked> onItemClickedListeners = new ArrayList<>();
    private boolean deleteVisibility;
    private boolean deleteIsInvite;
    private boolean groupDetailIcons;
    private ArrayList<Integer> selectedItems = new ArrayList<>();

    private boolean showNotificationsNumber = true;

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView contactsIcon;
        TextView contactsText;
        TextView stateText;
        TextView notificationsNumber;
        ProgressBar progressBar;
        LinearLayout deleteLayout;
        RelativeLayout itemLayout;
        ImageView selected;
        ImageView deleteIcon;
        TextView deleteText;

        public ViewHolder(View itemView) {
            super(itemView);
            contactsIcon = itemView.findViewById(R.id.contactsIcon);
            contactsText = itemView.findViewById(R.id.contactsText);
            stateText = itemView.findViewById(R.id.state_text);
            progressBar = itemView.findViewById(R.id.progressbar);
            deleteLayout = itemView.findViewById(R.id.deleteLayout);
            deleteIcon = deleteLayout.findViewById(R.id.delete_iv);
            deleteText = deleteLayout.findViewById(R.id.delete_tv);
            itemLayout = itemView.findViewById(R.id.itemLayout);
            selected = itemView.findViewById(R.id.selected);
            notificationsNumber = itemView.findViewById(R.id.notifications_number);
            if (!selectionEnabled) {
                selected.setVisibility(View.GONE);
            }
        }

        public void onSelectItem() {
            for (int i = 0; i < onItemClickedListeners.size(); i++) {
                int position = getAdapterPosition();
                Contact contact = contactList.get(position);
                onItemClickedListeners.get(i).onSelectItem(contact, position);
                if (selectedItems.contains(position)) {
                    selectedItems.remove((Integer)position);
                } else {
                    selectedItems.add(position);
                }
            }
            Drawable.ConstantState selectedImageState = context.getResources().getDrawable(R.drawable.imatge_selected).getConstantState();
            Drawable.ConstantState actualState = selected.getDrawable().getConstantState();
            Drawable unselectedImage = context.getResources().getDrawable(R.drawable.image_unselected);
            Drawable selectedImage = context.getResources().getDrawable(R.drawable.imatge_selected);
            Drawable stateBackground = actualState.equals(selectedImageState) ? unselectedImage : selectedImage;
            selected.setImageDrawable(stateBackground);
        }

    }

    private void setSelectionDrawable(ImageView selectIndicator, boolean isSelected) {
        Drawable unselectedImage = context.getResources().getDrawable(R.drawable.image_unselected);
        Drawable selectedImage = context.getResources().getDrawable(R.drawable.imatge_selected);
        selectIndicator.setImageDrawable(isSelected ? selectedImage : unselectedImage);
    }

    public ContactsAdapter(Context context,List<Contact> contactList, ContactsAdapterListener listener){
        this.context = context;
        this.contactList = contactList;
        this.listener = listener;
    }

    @Override
    public ContactsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View v = LayoutInflater.from(context).inflate(R.layout.contacts_adapter_item, parent, false);
        ContactsAdapter.ViewHolder vh = new ContactsAdapter.ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ContactsAdapter.ViewHolder holder, final int position) {
        holder.contactsText.setText(contactList.get(position).getName() + " "
                + contactList.get(position).getLastname());
        String state = contactList.get(position).getState();
        if (state != null && state.length() > 0) {
            holder.stateText.setVisibility(View.VISIBLE);
            holder.stateText.setText(OtherUtils.getMeetingInvitationState(state, holder.stateText.getResources()));
        } else {
            holder.stateText.setVisibility(View.GONE);
        }
        String contactPicturePath = contactList.get(position).getPath();
        if (contactPicturePath != null && !"".equals(contactPicturePath)) {
            Glide.with(context)
                    .load(contactPicturePath.equals("placeholder") ?
                            context.getResources().getDrawable(R.drawable.user)
                            : new File(contactPicturePath))
                    .apply(RequestOptions.overrideOf(200, 200))
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, final Target<Drawable> target, boolean isFirstResource) {
                            holder.contactsIcon.setImageResource(R.drawable.user);
                            return true;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            return false;
                        }
                    })

                    .into(holder.contactsIcon);
            holder.contactsIcon.setVisibility(View.VISIBLE);
            holder.progressBar.setVisibility(View.GONE);
        } else {
            if (listener != null) {
                holder.contactsIcon.setVisibility(View.GONE);
                holder.progressBar.setVisibility(View.VISIBLE);
                listener.needContactPicturePath(contactList.get(position).getId(),
                        contactList.get(position).getType());
            }
        }

        if (selectionEnabled) {
            setSelectionDrawable(holder.selected, selectedItems.contains(holder.getAdapterPosition()));
        }


        holder.deleteLayout.setVisibility((deleteVisibility && (contactList.get(position).getType()
                == Contact.TYPE_USER_CIRCLE || contactList.get(position).getType()
                == Contact.TYPE_CIRCLE_USER) || (groupDetailIcons) ? View.VISIBLE : View.GONE));
        if (deleteIsInvite) {
            ((TextView)holder.deleteLayout.findViewById(R.id.delete_tv)).setText(R.string.calendar_stop_inviting);
        }

        if (deleteIsInvite) {
            holder.deleteLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.deleteCircle(contactList.get(position).getId(), contactList.get(position).getName() + " " + contactList.get(position).getLastname());
                }
            });
        } else if (groupDetailIcons) {
            holder.deleteLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.deleteCircle(contactList.get(position).getId(),
                            String.valueOf(contactList.get(position).getType()));
                }
            });
            Log.d("gdp","adapter type:"+contactList.get(position).getType());
            switch (contactList.get(position).getType()) {
                case Contact.TYPE_DYNAMIZER:
                    Drawable drawable = holder.deleteIcon.getResources().getDrawable(
                            R.drawable.ic_notification_small);
                    Drawable wrappedDrawable = DrawableCompat.wrap(drawable);
                    wrappedDrawable = wrappedDrawable.mutate();
                    DrawableCompat.setTint(wrappedDrawable, holder.deleteIcon.getResources()
                            .getColor(R.color.colorPrimary));
                    holder.deleteIcon.setImageDrawable(wrappedDrawable);
                    holder.deleteText.setText(holder.deleteIcon.getResources().getString(R.string.chat_button_dinamizer));
                    holder.deleteText.setTextColor(holder.deleteText.getResources().getColor(R.color.darkGray));
                    break;
                case Contact.TYPE_GROUP:
                    holder.deleteIcon.setImageDrawable(holder.deleteIcon.getResources().getDrawable(R.drawable.add_contact));
                    holder.deleteIcon.setBackground(holder.deleteIcon.getResources().getDrawable(R.drawable.red_circle_white_background));
                    holder.deleteText.setText(holder.deleteIcon.getResources().getString(R.string.group_detail_send_invite));
                    holder.deleteText.setTextColor(holder.deleteText.getResources().getColor(R.color.colorPrimary));
                    int padding = (int) holder.deleteIcon.getResources().getDimension(R.dimen.group_detail_invite_icon_padding);
                    holder.deleteIcon.setPadding(padding,padding,padding,padding);
                    break;
                default:
                    holder.deleteLayout.setVisibility(View.INVISIBLE);
                    break;
            }
            if (contactList.get(position).getId() == new UserPreferences().getUserID()) {
                holder.deleteLayout.setVisibility(View.INVISIBLE);
                holder.contactsText.setText(R.string.chat_username_you);
            }
        } else {
            holder.itemLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (holder.deleteLayout.getVisibility() == View.VISIBLE) {
                        listener.deleteCircle(contactList.get(position).getId(), contactList.get(position).getName() + " " + contactList.get(position).getLastname());
                    } else if (selectionEnabled){
                        if (holder.progressBar.getVisibility() == View.GONE) {
                            holder.onSelectItem();
                        }
                    } else {
                        boolean isGroupChat = contactList.get(position).getType() == Contact.TYPE_GROUP;
                        boolean isDynamizer = contactList.get(position).getType() == Contact.TYPE_DYNAMIZER;
                        listener.clickedCircle(String.valueOf(contactList.get(position).getIdChat()), isGroupChat,isDynamizer);
                    }
                }
            });
        }

        if (showNotificationsNumber && contactList.get(position).getNumberNotifications() > 0) {
            holder.notificationsNumber.setVisibility(View.VISIBLE);
            holder.notificationsNumber.setText(String.valueOf(contactList.get(position).getNumberNotifications()));
            holder.contactsIcon.setBackground(context.getResources().getDrawable(R.drawable.red_circle_contact));
        } else {
            holder.notificationsNumber.setVisibility(View.GONE);
            holder.contactsIcon.setBackground(null);
        }

        //ContactsRepository.fillContacts(context, holder.contactsIcon, contactList.get(position).getPath());
    }

    public void setDeleteVisibility(boolean deleteVisibility) {
        this.deleteVisibility = deleteVisibility;
    }

    public void setDeleteIsInvite(boolean deleteIsInvite) {
        this.deleteIsInvite = deleteIsInvite;
    }

    public void setContactSelectionEnabled(boolean enabled) {
        this.selectionEnabled = enabled;
    }

    public void setGroupDetailIcons(boolean groupDetailIcons) {
        this.groupDetailIcons = groupDetailIcons;
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    public void removeContact(int id) {
        for (Contact contact : contactList) {
            if (contact.getId() == id) {
                contactList.remove(contact);
                break;
            }
        }
        notifyDataSetChanged();
    }

    public interface ContactsAdapterListener {
        void needContactPicturePath(int contactId, int contactType);
        void deleteCircle(int idUserToUnlink, String contactName);
        void clickedCircle(String idUserSender, boolean isGroupChat, boolean isDynamizer);
    }

    public void addItemClickedListeners(OnItemClicked onItemClicked) {
        onItemClickedListeners.add(onItemClicked);
    }

    public interface OnItemClicked {
        void onSelectItem(Contact contact, int index);
    }

    public void setShowNotificationsNumber(boolean showNotificationsNumber) {
        this.showNotificationsNumber = showNotificationsNumber;
    }
}
