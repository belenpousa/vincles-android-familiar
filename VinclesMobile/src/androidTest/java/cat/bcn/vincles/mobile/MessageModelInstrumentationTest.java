/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.mobile;

import android.app.Instrumentation;
import android.support.test.runner.AndroidJUnit4;
import android.test.InstrumentationTestCase;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.List;

import cat.bcn.vincles.lib.vo.Message;
import cat.bcn.vincles.mobile.model.MainModel;
import cat.bcn.vincles.mobile.model.MessageModel;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
//@RunWith(AndroidJUnit4.class)
public class MessageModelInstrumentationTest extends InstrumentationTestCase {
    private static final String TAG = "MessageModelInstrumentationTest";
    private Instrumentation instrumentation;
    private MainModel mainModel;
    private MessageModel messageModel;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        instrumentation = getInstrumentation();
        mainModel = MainModel.getInstance();
        mainModel.initialize(instrumentation.getTargetContext());
        messageModel = MessageModel.getInstance();
    }

    @Test
    public void testGetMessageList() throws IOException {
        List<Message> result = messageModel.getMessageList();
        assertNotNull(result);

        // Check last message time
        if (result.size() > 1) {
            assertTrue(result.get(0).sendTime.getTime() > result.get(1).sendTime.getTime());
        }
    }
}