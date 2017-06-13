/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.mobile.model;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.util.Log;

import java.util.Calendar;
import java.util.TimeZone;

import cat.bcn.vincles.lib.dao.TaskDAOImpl;
import cat.bcn.vincles.lib.vo.Task;

public class AndroidCalendarModel {
    private static final String TAG = "AndroidCalendarModel";
    private static AndroidCalendarModel instance;

    private final String VINCLES_CALENDAR_NAME = "VinclesCalendar";

    // Projection array. Creating indices for this array instead of doing
    // dynamic lookups improves performance.
    public static final String[] EVENT_PROJECTION = new String[] {
            CalendarContract.Calendars._ID,                           // 0
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,         // 1
            CalendarContract.Calendars.ACCOUNT_NAME,                  // 2
            CalendarContract.Calendars.OWNER_ACCOUNT                  // 3
    };

    // The indices for the projection array above.
    private static final int PROJECTION_ID_INDEX = 0;
    private static final int PROJECTION_DISPLAY_NAME_INDEX = 1;
    private static final int PROJECTION_ACCOUNT_NAME_INDEX = 2;
    private static final int PROJECTION_OWNER_ACCOUNT_INDEX = 3;

    private long calID = 0;
    private Context context;

    public static AndroidCalendarModel getInstance() {
        if (instance == null) {
            instance = new AndroidCalendarModel();
        }
        return instance;
    }

    public void selectDefaultCalendar(Context ctx) {
        if (ctx != null) this.context = ctx;
        // Run query
        Cursor cur = null;
        ContentResolver cr = context.getContentResolver();
        Uri uri = CalendarContract.Calendars.CONTENT_URI;
        String selection = "("
                + CalendarContract.Calendars.NAME + " = ? " +
//                "(" + CalendarContract.Calendars.ACCOUNT_NAME + " = ?) AND " +
//                "(" + CalendarContract.Calendars.ACCOUNT_TYPE + " = ?) AND " +
//                "(" + CalendarContract.Calendars.OWNER_ACCOUNT + " = ?)" +
                ")";
        // Submit the query and get a Cursor object back.
        cur = cr.query(uri, EVENT_PROJECTION, selection, new String[] {VINCLES_CALENDAR_NAME}, null);
//        cur = cr.query(uri, EVENT_PROJECTION, null, null, null);

        // Use the cursor to step through the returned records
        if (cur.moveToNext()) {
            String displayName = null;
            String accountName = null;
            String ownerName = null;

            // Get the field values
            calID = cur.getLong(PROJECTION_ID_INDEX);
            displayName = cur.getString(PROJECTION_DISPLAY_NAME_INDEX);
            accountName = cur.getString(PROJECTION_ACCOUNT_NAME_INDEX);
            ownerName = cur.getString(PROJECTION_OWNER_ACCOUNT_INDEX);

            Log.d(TAG, "Calendar detected: ID=" + calID + ", name=" + displayName + ", account=" + accountName + ", owner=" + ownerName );
        }
        else calID = createCalendar(context);
    }

    public long createCalendar(Context ctx) {
        if (ctx != null) this.context = ctx;
        AccountManager manager = AccountManager.get(context);
        Account[] accounts = manager.getAccountsByType("com.google");

        String accountName = "";
        String accountType = "";

        for (Account account : accounts) {
            accountName = account.name;
            accountType = account.type;
            break;
        }

        ContentResolver cr = context.getContentResolver();
        ContentValues values = new ContentValues();

        values.put(CalendarContract.Calendars.ACCOUNT_NAME, accountName);
        //values.put(CalendarContract.Calendars.ACCOUNT_TYPE, accountType);
        values.put(CalendarContract.Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL);
        values.put(CalendarContract.Calendars.NAME, VINCLES_CALENDAR_NAME);
        values.put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, VINCLES_CALENDAR_NAME);
        values.put(CalendarContract.Calendars.SYNC_EVENTS, 1);
        values.put(CalendarContract.Calendars.VISIBLE, 1);
        values.put(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL, CalendarContract.Calendars.CAL_ACCESS_OWNER);
        values.put(CalendarContract.Calendars.OWNER_ACCOUNT, accountName);
        values.put(CalendarContract.Calendars.DIRTY, 1);
        values.put(CalendarContract.Calendars.CALENDAR_TIME_ZONE, TimeZone.getDefault().getID());

        String ret =  null;
        try {
            Uri calUri = CalendarContract.Calendars.CONTENT_URI;

            calUri = calUri.buildUpon()
                    .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                    .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, accountName)
                    .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, accountType)
                    .build();

            Uri result = cr.insert(calUri, values);
            ret = result.getLastPathSegment();

            Log.d(TAG, VINCLES_CALENDAR_NAME + " INSERTED WITH ID: " + result.getLastPathSegment());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Long.parseLong(ret);
    }

    public void addOrUpdateAndroidCalendarEvent(Task task, Context ctx) {
        if (ctx != null) this.context = ctx;
        long startMillis = 0;
        long endMillis = 0;
        Calendar beginTime = Calendar.getInstance();
        beginTime.setTime(task.getDate());
        startMillis = beginTime.getTimeInMillis();
        Calendar endTime = Calendar.getInstance();
        endTime.setTime(task.getDate());
        endTime.add(Calendar.MINUTE, task.duration);
        endMillis = endTime.getTimeInMillis();

        ContentResolver cr = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.DTSTART, startMillis);
        values.put(CalendarContract.Events.DTEND, endMillis);
        values.put(CalendarContract.Events.TITLE, task.description);
        values.put(CalendarContract.Events.DESCRIPTION, task.description);
        values.put(CalendarContract.Events.CALENDAR_ID, calID);
        values.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());
        values.put(CalendarContract.Events.HAS_ALARM, 1);

        int rows = 0;
        if (task.androidCalendarId != null) {
            Uri updateUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, task.androidCalendarId);
            rows = cr.update(updateUri, values, null, null);
            Log.i(TAG, "Rows updated: " + rows);
        }

        // EVENT HAD NOT BEEN CREATED OR UPDATE FAILED (SO.. DELETED)
        if (rows == 0) {
            Uri result = cr.insert(CalendarContract.Events.CONTENT_URI, values);

            // get the event ID that is the last element in the Uri
            long eventID = Long.parseLong(result.getLastPathSegment());
            Log.d(TAG, "EVENT INSERTED RESULT = " + eventID + ", result=" + result);

            // SAVE EVENT ID TO UPDATE
            task.androidCalendarId = eventID;
            new TaskDAOImpl().save(task);
        }
    }

    public void deleteAndroidCalendarEvent(Task task, Context ctx) {
        if (ctx != null) this.context = ctx;
        ContentResolver cr = context.getContentResolver();
        if (task.androidCalendarId != null) {
            Uri deleteUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, task.androidCalendarId);
            int rows = cr.delete(deleteUri, null, null);
            Log.i(TAG, "Rows deleted: " + rows);
        }
        else Log.i(TAG, "Event have no associated android calendar ID");

    }
}
