/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.mobile.activity.network;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import java.util.List;
import cat.bcn.vincles.lib.util.ImageUtils;
import cat.bcn.vincles.lib.util.VinclesConstants;
import cat.bcn.vincles.lib.vo.Network;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.activity.MainActivity;
import cat.bcn.vincles.mobile.model.NetworkModel;
import cat.bcn.vincles.mobile.util.VinclesMobileConstants;

public class NetworkTemplateActivity extends MainActivity {
    private static final String TAG = "NetworkActivity";
    protected NetworkModel networkModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        networkModel = NetworkModel.getInstance();
        setContentView(R.layout.activity_network);
    }

    protected List<Network> addToNetworkList(List<Network> items) {
        Long networkId = mainModel.preferences.getLong(VinclesMobileConstants.NETWORK_CODE, 0l);
        for (Network it : items) {
            if (it.getId().longValue() == networkId.longValue()) {
                it.selected = true;
            } else {
                it.selected = false;
            }
        }
        return items;
    }

    protected void changeNetwork(Network item) {
        networkModel.changeNetwork(item);
        // Update menu information
        ImageView imgUser = (ImageView) findViewById(R.id.imgUser);
        TextView texUser = (TextView) findViewById(R.id.texUser);
        if (mainModel.currentNetwork != null) {
            Glide.with(this)
                    .load(mainModel.getUserPhotoUrlFromUser(mainModel.currentNetwork.userVincles))
                    .error(R.drawable.user).placeholder(R.color.superlightgray)
                    .dontAnimate()      // GLIDE BUG WITH PLACEHOLDERS
                    .into(imgUser);
            texUser.setText(mainModel.currentNetwork.userVincles.name + " " + mainModel.currentNetwork.userVincles.lastname);
        }

        fillMenu();
    }

    public void addNetwork(View view) {
        networkModel.view = NetworkModel.NETWORK_JOIN;
        startActivity(new Intent(this, NetworkJoinActivity.class));
    }

    public void goBack(View view) {
        back();
    }

    protected void back() {
        networkModel.view = "";
        startActivity(new Intent(this, NetworkActivity.class));
        finish();
    }

    public void enterVincles(View view) {
        back();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
