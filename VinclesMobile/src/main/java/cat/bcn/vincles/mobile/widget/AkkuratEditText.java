/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.mobile.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.EditText;

import cat.bcn.vincles.lib.util.FontManager;
import cat.bcn.vincles.lib.util.VinclesConstants;
import cat.bcn.vincles.mobile.R;

public class AkkuratEditText extends EditText {

    public AkkuratEditText(Context context) {
        super(context);
        init();
    }

    public AkkuratEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AkkuratEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    // cat.bcn.vincles.mobile.widget.Akkurat
    public void init() {
        int style = Typeface.NORMAL;
        if (this.getTypeface() != null) style = this.getTypeface().getStyle();
        FontManager.setCustomFont(this, getContext(), style == Typeface.BOLD ? VinclesConstants.TYPEFACE.BOLD : VinclesConstants.TYPEFACE.REGULAR);

        this.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if ((AkkuratEditText.this.getText()).toString().length() <= 0) {
//                    AkkuratEditText.this.setError("Enter FirstName");
                } else {
                    AkkuratEditText.this.setError(null);
                    setBackgroundResource(R.drawable.edittext_background_def);
                }
            }
        });
    }

    @Override
    public void setError(CharSequence error, Drawable icon) {
        setCompoundDrawables(null, null, null, null);
        setBackgroundResource(R.drawable.edittext_background_red);
    }


}
