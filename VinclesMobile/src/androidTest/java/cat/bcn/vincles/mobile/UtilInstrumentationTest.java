/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.mobile;

import android.app.Instrumentation;
import android.content.Context;
import android.test.InstrumentationTestCase;
import android.util.Log;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import cat.bcn.vincles.lib.dao.MessageDAO;
import cat.bcn.vincles.lib.dao.MessageDAOImpl;
import cat.bcn.vincles.lib.dao.NetworkDAO;
import cat.bcn.vincles.lib.dao.NetworkDAOImpl;
import cat.bcn.vincles.lib.dao.NoteDAO;
import cat.bcn.vincles.lib.dao.NoteDAOImpl;
import cat.bcn.vincles.lib.dao.UserDAO;
import cat.bcn.vincles.lib.dao.UserDAOImpl;
import cat.bcn.vincles.lib.vo.Message;
import cat.bcn.vincles.lib.vo.Network;
import cat.bcn.vincles.lib.vo.Note;
import cat.bcn.vincles.lib.vo.User;
import cat.bcn.vincles.mobile.model.MainModel;
import cat.bcn.vincles.mobile.model.MessageModel;

//@RunWith(AndroidJUnit4.class)
public class UtilInstrumentationTest extends InstrumentationTestCase {
    private static final String TAG = "UtilInstrumentationTest";
    private Instrumentation instrumentation;
    private MainModel mainModel;
    private MessageModel messageModel;
    private MessageDAO messageDAO;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        instrumentation = getInstrumentation();
        messageModel = MessageModel.getInstance();
        messageDAO = new MessageDAOImpl();
    }

    @Test
    public void testDeleteDatabase() {
        //The Android's default system path of your application database.
        String DB_PATH = "/data/data/cat.bcn.vincles.mobile.debug/databases/";
        String DB_NAME = "vincles-mobile.db";

        boolean b = instrumentation.getTargetContext().deleteDatabase(DB_NAME); // true if deleted
        assertTrue(b);
        Log.i(TAG, "deleteDatabase()");
    }

    @Test
    public void testLoadDataBase() {
        Date today = new Date();

        UserDAO userDAO = new UserDAOImpl();

        for (int i = 0; i < 10; i++) {
            User it = new User();
            it.username = "username" + i;
            it.name = "First Name " + i;
            it.lastname = "Last Name " + i;
            it.email = "email@server" + i + ".com";
            it.phone = String.valueOf(100000000 + i);
            it.liveInBarcelona = true;
            it.setCreated(today);
            it.setUpdated(today);

            userDAO.save(it);
        }

        MessageDAO messageDAO = new MessageDAOImpl();
        Random rn = new Random();
        int maximum = 4;
        int minimum = 1;

        User user = userDAO.get(1L);
        String audioFilename = "audio_sample.3gp";
        String videoFilename = "video_sample.3gp";
        String imageFilename = "image_sample.png";
        for (int i = 0; i < 10; i++) {
            Message it = new Message();
            it.title = "title " + i;
            it.description = "description " + i;
            it.setCreated(today);
            it.setUpdated(today);

            InputStream in;
            try {
                // Copy & Save audio
                in = instrumentation.getTargetContext().getResources().getAssets().open("audio_sample.3gp");
                byte[] data = IOUtils.toByteArray(in);
                saveFile(data, audioFilename);

                // Copy & Save video
                in = instrumentation.getTargetContext().getResources().getAssets().open("video_sample.3gp");
                data = IOUtils.toByteArray(in);
                saveFile(data, videoFilename);

                // Copy & Save image
                in = instrumentation.getTargetContext().getResources().getAssets().open("image_sample.png");
                data = IOUtils.toByteArray(in);
                saveFile(data, imageFilename);
            } catch (IOException e) {
                e.printStackTrace();
            }

            messageDAO.save(it);
        }

        NoteDAO noteDAO = new NoteDAOImpl();
        Note note = new Note();
        note.description = "This is my note.\nPlease, write your own!";
        noteDAO.save(note);
    }

    private void saveFile(byte[] data, String filename) {
        // CAUTION: openFileOutput only accept 'file-names' no 'paths'!!!
        // CAUTION: data store can't create subfolders!!!
        FileOutputStream outputStream;

        try {
            outputStream = instrumentation.getTargetContext().openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(data);
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testDeleteNetworkList() {
        NetworkDAO networkDAO = new NetworkDAOImpl();

        List<Network> items = networkDAO.getAll();
        Iterator<Network> i = items.iterator();
        while (i.hasNext()) {
            Network item = i.next();
            networkDAO.delete(item);
            i.remove();
        }

        items = networkDAO.getAll();
        assertEquals(0, items.size());
    }

    @Test
    public void testDeleteMessages() {
        List<Message> items = messageDAO.getAll();
        for (Message it : items) {
            messageModel.deleteMessage(it);
        }
        Log.i(TAG, "deleteMessages()");
        items = messageDAO.getAll();
        assertTrue(items.size() == 0);
    }
}