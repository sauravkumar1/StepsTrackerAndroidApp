package com.example.android.stepstracker.Data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import static com.example.android.stepstracker.Data.TaskContract.TaskEntry.TABLE_NAME;

import static com.example.android.stepstracker.Data.TaskContract.TaskEntry.COLUMN_DATE;

public class TaskContentProvider extends ContentProvider {

    // Define final integer constants for the directory of tasks and a single item.
    // It's convention to use 100, 200, 300, etc for directories,
    // and related ints (101, 102, ..) for items in that directory.
    public static final int STEPS = 100;
    public static final int STEPS_WITH_DATE = 101;

    // CDeclare a static variable for the Uri matcher that you construct
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    // Define a static buildUriMatcher method that associates URI's with their int match
    /**
     Initialize a new matcher object without any matches,
     then use .addURI(String authority, String path, int match) to add matches
     */
    public static UriMatcher buildUriMatcher() {

        // Initialize a UriMatcher with no matches by passing in NO_MATCH to the constructor
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        /*
          All paths added to the UriMatcher have a corresponding int.
          For each kind of uri you may want to access, add the corresponding match with addURI.
          The two calls below add matches for the task directory and a single item by ID.
         */
        uriMatcher.addURI(TaskContract.AUTHORITY, TaskContract.PATH_STEPS, STEPS);
        uriMatcher.addURI(TaskContract.AUTHORITY, TaskContract.PATH_STEPS + "/#", STEPS_WITH_DATE);

        return uriMatcher;
    }

    // Member variable for a TaskDbHelper that's initialized in the onCreate() method
    private TaskDbHelper mTaskDbHelper;

    /* onCreate() is where you should initialize anything you’ll need to setup
    your underlying data source.
    In this case, you’re working with a SQLite database, so you’ll need to
    initialize a DbHelper to gain access to it.
     */
    @Override
    public boolean onCreate() {
        // Complete onCreate() and initialize a TaskDbhelper on startup
        // [Hint] Declare the DbHelper as a global variable

        Context context = getContext();
        mTaskDbHelper = new TaskDbHelper(context);
        return true;
    }


    // Implement insert to handle requests to insert a single new row of data
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        // Get access to the task database (to write new data to)
        final SQLiteDatabase db = mTaskDbHelper.getWritableDatabase();

        // Write URI matching code to identify the match for the tasks directory
        int match = sUriMatcher.match(uri);
        int date = values.getAsInteger(COLUMN_DATE);
        Uri returnUri; // URI to be returned

        switch (match) {
            case STEPS_WITH_DATE:
                // Insert new values into the database
                // Inserting values into tasks table
                long id = db.insert(TABLE_NAME, null, values);
                if ( id > 0 ) {
                    Uri _uri = ContentUris.withAppendedId(TaskContract.TaskEntry.CONTENT_URI, id);
                    getContext().getContentResolver().notifyChange(_uri, null);
                    return _uri;
                  //  returnUri = ContentUris.withAppendedId(TaskContract.TaskEntry.CONTENT_URI, date);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }

            // Set the value for the returnedUri and write the default case for unknown URI's
            // Default case throws an UnsupportedOperationException
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }



    }


    // Implement query to handle requests for data by URI
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        // Get access to underlying database (read-only for query)
        final SQLiteDatabase db = mTaskDbHelper.getReadableDatabase();

        // Write URI match code and set a variable to return a Cursor
        int match = sUriMatcher.match(uri);
        Cursor retCursor;

        // Query for the tasks directory and write a default case
        switch (match) {
            // Query for the tasks directory
            case STEPS:
                retCursor =  db.query(TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                return  retCursor;
                //break;
            case STEPS_WITH_DATE :
                String date = uri.getPathSegments().get(1);
                String mSelection = COLUMN_DATE + " = ?";
                String[] mSelectionArgs = new String[]{date};
                retCursor =  db.query(TABLE_NAME,
                        projection,
                        mSelection,
                        mSelectionArgs,
                        null,
                        null,
                        sortOrder);
                return  retCursor;
             //   break;
            // Default exception
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Set a notification URI on the Cursor and return that Cursor
      //  retCursor.setNotificationUri(getContext().getContentResolver(), uri);

        // Return the desired Cursor
     //   return retCursor;
    }


    // Implement delete to delete a single row of data
    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {

        // COMPLETED (1) Get access to the database and write URI matching code to recognize a single item
        final SQLiteDatabase db = mTaskDbHelper.getWritableDatabase();

        int match = sUriMatcher.match(uri);
        // Keep track of the number of deleted tasks
        int tasksDeleted; // starts as 0

        // COMPLETED (2) Write the code to delete a single row of data
        // [Hint] Use selections to delete an item by its row ID
        switch (match) {
            case STEPS :
                tasksDeleted = db.delete(TABLE_NAME, null, null);
                return tasksDeleted;
             //   break;
            // Handle the single item case, recognized by the ID included in the URI path
            case STEPS_WITH_DATE:
                // Get the task ID from the URI path
                String date = uri.getPathSegments().get(1);
                // Use selections/selectionArgs to filter for this ID
                tasksDeleted = db.delete(TABLE_NAME, "date=?", new String[]{date});
                return tasksDeleted;
               // break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // COMPLETED (3) Notify the resolver of a change and return the number of items deleted
    //    if (tasksDeleted != 0) {
            // A task was deleted, set notification
         //   getContext().getContentResolver().notifyChange(uri, null);
     //   }

        // Return the number of tasks deleted
      //  return tasksDeleted;
    }


    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {

        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public String getType(Uri uri) {
        switch(sUriMatcher.match(uri)){
            case STEPS:
                return TaskContract.TaskEntry.CONTENT_URI.toString();
            case STEPS_WITH_DATE:
                return TaskContract.TaskEntry.CONTENT_URI.toString();

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

}
