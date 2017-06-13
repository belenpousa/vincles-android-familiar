/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.mobile.model;

import android.text.TextUtils;
import android.util.Log;

import cat.bcn.vincles.lib.dao.NoteDAO;
import cat.bcn.vincles.lib.dao.NoteDAOImpl;
import cat.bcn.vincles.lib.vo.Note;
import cat.bcn.vincles.mobile.R;

import java.util.List;

public class NoteModel {
    private static final String TAG = "NoteModel";
    private MainModel mainModel;
    private NoteDAO noteDAO;
    public Note note;

    public NoteModel() {
        mainModel = MainModel.getInstance();
        noteDAO = new NoteDAOImpl();

        setNetworkNote();
    }

    public void setNetworkNote() {
        List<Note> items = noteDAO.getAll();
        if (items != null && !items.isEmpty())
            note = items.get(0);
        else
            note = new Note();
    }

    public String getNotes() {
        Log.i(TAG, "getNoteList()");
        setNetworkNote();

        return note.description;
    }

    public void saveNote(String description) {
        note.description = description;
        noteDAO.save(note);
    }
}