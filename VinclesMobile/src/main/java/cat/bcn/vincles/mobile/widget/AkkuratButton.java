/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.mobile.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.Button;

import cat.bcn.vincles.lib.util.FontManager;
import cat.bcn.vincles.lib.util.VinclesConstants;

public class AkkuratButton extends Button {

    public AkkuratButton(Context context) {
        super(context);
        init();
    }

    public AkkuratButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AkkuratButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    // cat.bcn.vincles.mobile.widget.Akkurat
    public void init() {
        int style = Typeface.NORMAL;
        if (this.getTypeface() != null) style = this.getTypeface().getStyle();
        FontManager.setCustomFont(this, getContext(), style == Typeface.BOLD ? VinclesConstants.TYPEFACE.BOLD : VinclesConstants.TYPEFACE.REGULAR);

    }


}
