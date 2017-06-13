/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.mobile.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.model.TaskModel;

public class TaskActivityNew extends MainActivity {
    private static final String TAG = "TaskActivity";
    protected TaskModel taskModel;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        taskModel = TaskModel.getInstance();
        setContentView(R.layout.activity_task);

        TextView texBack = (TextView) findViewById(R.id.texBack);
        texBack.setText("(*)new Task");

        super.createEnvironment(4);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        return true;
    }

    public void goBack(View view) {
        back();
    }

    private void back() {
        taskModel.view = "";
        startActivity(new Intent(this, TaskActivity.class));
    }
}
