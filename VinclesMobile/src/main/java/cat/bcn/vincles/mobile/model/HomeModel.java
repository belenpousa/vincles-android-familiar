/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.mobile.model;

public class HomeModel {
    private static final String TAG = "HomeModel";
    private static HomeModel instance;

    public static HomeModel getInstance() {
        if (instance == null) {
            instance = new HomeModel();
            instance.initialize();
        }
        return instance;
    }

    private HomeModel() {
    }

    private void initialize() {
    }
}