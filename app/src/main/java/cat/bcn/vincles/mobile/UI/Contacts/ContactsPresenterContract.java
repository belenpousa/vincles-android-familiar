package cat.bcn.vincles.mobile.UI.Contacts;

import android.os.Bundle;

import java.util.ArrayList;

public interface ContactsPresenterContract  {

    static final int FILTER_NOT_INIT = -1;
    static final int FILTER_ALL_CONTACTS = 0;
    static final int FILTER_FAMILY = 1;
    static final int FILTER_GROUPS = 2;
    static final int FILTER_DYNAM = 3;
    static final int FILTER_ALL_CONTACTS_BUT_GROUPS = 4;

    void getContacts();
    int getFilterKind();
    void onFilterClicked(int whichFilter);
    void getContactPicture(int contactId, int contactType);
    void deleteCircle(int idUserToUnlink);
    void refreshContactList();
    void shareMedia(ArrayList<Integer> mediaIdArrayList);
    ArrayList<Integer> getSelectedContacts();
    ArrayList<Integer> getSelectedIdsChat();
    void itemSelected(Contact contact, int index);
    void notificationToProcess(Bundle data);
}
