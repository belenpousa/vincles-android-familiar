/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.mobile.component.swipetoremove;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import cat.bcn.vincles.mobile.R;

public class SwipeToRemoveTouchCallback extends ItemTouchHelper.SimpleCallback {
    Context mCtx;
    RecyclerView mRecyclerView;
    Drawable background;
    Drawable xMark;
    int xMarkMargin;
    boolean initiated;

    public SwipeToRemoveTouchCallback(RecyclerView recyclerView, Context ctx) {
        super(0, ItemTouchHelper.LEFT);
        mRecyclerView = recyclerView;
        mCtx = ctx;

        if (!(recyclerView.getAdapter() instanceof SwipeToRemoveAdapter))
            Log.e(null, "SwipeToRemoveTouchCallback needs SwipeToRemoveAdapter");
    }

    private void init() {
        background = new ColorDrawable(Color.BLACK);
        xMark = ContextCompat.getDrawable(mCtx, R.drawable.icon_borrar);
        xMark.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        xMarkMargin = (int) mCtx.getResources().getDimension(R.dimen.padding_large);

        mRecyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {
                int position = 0; View child = null;
                // Looks like translated Child only is active where his view is so X = 0
                child = recyclerView.findChildViewUnder(0, motionEvent.getY());
                if (child != null) {
                    position = recyclerView.getChildAdapterPosition(child);
                }
                if (child != null) {
                    int widthOffset = child.getWidth() - child.getWidth() / 4;
                    if (((SwipeToRemoveAdapter) recyclerView.getAdapter()).isPendingRemoval(position)
                            && motionEvent.getX() >= widthOffset) {
                        ((SwipeToRemoveAdapter) recyclerView.getAdapter()).remove(position);
                        return true;
                    }
                }

                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {}

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean b) {}
        });
        initiated = true;
    }

    // not important, we don't want drag & drop
    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(final RecyclerView.ViewHolder viewHolder, int swipeDir) {
        if (((SwipeToRemoveAdapter)mRecyclerView.getAdapter()).isRemovable(viewHolder.getAdapterPosition()))
            ((SwipeToRemoveAdapter)mRecyclerView.getAdapter()).pendingRemoval(viewHolder.getAdapterPosition());
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        View itemView = viewHolder.itemView;
        int position = viewHolder.getAdapterPosition();

        // not sure why, but this method get's called for viewholder that are already swiped away
        if (position == -1) {
            // not interested in those
            return;
        }

        if (!initiated) {
            init();
        }

        if (((SwipeToRemoveAdapter)mRecyclerView.getAdapter()).isRemovable(position)) {
            int itemHeight = itemView.getBottom() - itemView.getTop();
            int intrinsicWidth = xMark.getIntrinsicWidth();
            int intrinsicHeight = xMark.getIntrinsicWidth();

            int xMarkLeft = itemView.getRight() - xMarkMargin - intrinsicWidth;
            int xMarkRight = itemView.getRight() - xMarkMargin;
            int xMarkTop = itemView.getTop() + (itemHeight - intrinsicHeight) / 2;
            int xMarkBottom = xMarkTop + intrinsicHeight;
            xMark.setBounds(xMarkLeft, xMarkTop, xMarkRight, xMarkBottom);

            if (dX < -intrinsicWidth * 3) dX = -intrinsicWidth * 3;
            background.setBounds(itemView.getRight() + (int) dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());

            background.draw(c);
            xMark.draw(c);
            ViewCompat.setTranslationX(itemView, dX);

        }
    }

}