/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.mobile.activity.notes;

import android.os.Bundle;
import android.widget.EditText;

import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.activity.MainActivity;
import cat.bcn.vincles.mobile.model.NoteModel;

public class NotesActivity extends MainActivity {
    private static final String TAG = "NotesActivity";
    private NoteModel noteModel;
    EditText ediNotes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);
        setTitle(getString(R.string.title_activity_notes));
        noteModel = new NoteModel();

        ediNotes = (EditText) findViewById(R.id.ediNotes);
        ediNotes.setText(noteModel.getNotes());

        super.createEnvironment(4);
    }

    @Override
    protected void onPause() {
        super.onPause();
        noteModel.saveNote(ediNotes.getText().toString());
    }
}
