/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.mobile.activity.diary.fragments;

import java.util.Date;
import cat.bcn.vincles.lib.vo.PushMessage;
import cat.bcn.vincles.lib.vo.Task;

public interface IDiaryFragment {
    void refreshFragment (Task temp, PushMessage pushMessage);
    void refreshFragment ();
    Date getDate();
}
