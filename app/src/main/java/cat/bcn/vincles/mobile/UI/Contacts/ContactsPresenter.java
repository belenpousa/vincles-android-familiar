package cat.bcn.vincles.mobile.UI.Contacts;


import android.os.Bundle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cat.bcn.vincles.mobile.Client.Model.ChatMessageRepositoryModel;
import cat.bcn.vincles.mobile.Client.Requests.BaseRequest;
import cat.bcn.vincles.mobile.UI.FragmentManager.ContactsRepository;

public class ContactsPresenter implements ContactsPresenterContract, ContactsRepository.ContactsCallback {

    List<Contact> contactList;
    private ContactsRepository contactsRepository;
    private ContactsFragment contactsFragment;
    private int filterKind = ContactsPresenterContract.FILTER_ALL_CONTACTS;
    private ArrayList<Integer> contactSelected;
    private ArrayList<Integer> idChatSelected;

    public ContactsPresenter(BaseRequest.RenewTokenFailed listener, ContactsFragment contactsFragment, int filterKind) {
        contactsRepository = new ContactsRepository(this, listener);
        this.contactsFragment = contactsFragment;
        if (filterKind != ContactsPresenterContract.FILTER_NOT_INIT) {
            this.filterKind = filterKind;
            contactsRepository.setFilterKind(filterKind);
        }
    }

    @Override
    public void getContacts() {
        contactsRepository.loadLocalContacts();
        //contactsRepository.loadCircleUsers();
    }

    @Override
    public int getFilterKind() {
        return filterKind;
    }

    @Override
    public void onFilterClicked(int whichFilter) {
        filterKind = whichFilter;
        contactsRepository.onFilterChanged(filterKind);
    }


    @Override
    public void getContactPicture(int contactId, int contactType) {
        contactsRepository.loadContactPicture(contactId, contactType);
    }

    @Override
    public void deleteCircle(int idUserToUnlink) {
        contactsFragment.showWaitDialog();
        contactsRepository.deleteCircle(idUserToUnlink);
    }

    @Override
    public void refreshContactList() {
        contactsRepository.onFinishedLoadingContacts(false);
    }

    @Override
    public void shareMedia(ArrayList<Integer> mediaIdArrayList) {
        if ((contactSelected != null && contactSelected.size() > 0) || (idChatSelected != null
                && idChatSelected.size() > 0)) {
            contactsFragment.showWaitDialog();
            if (contactSelected !=null && contactSelected.size() == 1 && (idChatSelected == null
                    || idChatSelected.size() == 0)) {
                contactsRepository.shareMediaToOneContact(mediaIdArrayList, contactSelected.get(0));
            } else {
                contactsRepository.shareMediaToManyContacts(mediaIdArrayList, contactSelected,
                        idChatSelected);
            }
        }
    }

    @Override
    public ArrayList<Integer> getSelectedContacts() {
        return contactSelected;
    }

    @Override
    public ArrayList<Integer> getSelectedIdsChat() {
        return idChatSelected;
    }

    @Override
    public void onCirclesLoaded(List<Contact> contactList) {
        this.contactList = contactList;
        if (contactList.size() == 0) {
            contactsFragment.showNoContactsError();
        } else {
            contactsFragment.hideNoContactsError();
            contactsFragment.loadContacts(contactList);
        }
    }

    @Override
    public void onRemoveContact(boolean ok) {
        contactsFragment.hideWaitDialog();
        contactsFragment.showContactDeleteAlert(ok);
    }

    @Override
    public void onUserPictureLoaded() {
        contactsFragment.reloadContactAdapter();
    }

    @Override
    public void onUserPictureFail() {
    }

    @Override
    public void onMediaShared(boolean ok) {
        contactsFragment.hideWaitDialog();
        contactsFragment.showSharedMediaAlert(ok);
    }

    @Override
    public void itemSelected(Contact contact, int index) {
        if (contact.getType() <= Contact.TYPE_USER_CIRCLE) {
            if (contactSelected == null) contactSelected = new ArrayList<>();

            if (contactSelected.contains(contact.getId())) {
                contactSelected.removeAll(Arrays.asList(contact.getId()));
            } else {
                contactSelected.add(contact.getId());
            }
        } else {
            if (idChatSelected == null) idChatSelected = new ArrayList<>();

            if (idChatSelected.contains(contact.getIdChat())) {
                idChatSelected.removeAll(Arrays.asList(contact.getIdChat()));
            } else {
                idChatSelected.add(contact.getIdChat());
            }
        }

    }

    @Override
    public void notificationToProcess(Bundle data) {
        String type = data.getString("type");
        switch (type) {
            case "USER_UPDATED":
            case "USER_LINKED":
            case "ADDED_TO_GROUP":
            case "GROUP_UPDATED":
            case "REMOVED_FROM_GROUP":
            case "NEW_MESSAGE":
            case "NEW_CHAT_MESSAGE":
                contactsRepository.onReloadList();
                break;
            case "USER_UNLINKED":
            case "USER_LEFT_CIRCLE":
                int idUser = data.getInt("idUser");
                contactsRepository.onUserRemoved(idUser);
                break;
        }
    }
}
