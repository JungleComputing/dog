/*
 * Copyright (C) 2007 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ibis.dog.gui;

import ibis.dog.gui.LearnedObjects.LearnedObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import android.util.Log;

/**
 * Provides access to a database of notes. Each note has a title, the note
 * itself, a creation date and a modified data.
 */
public class LearnedObjectsProvider extends ContentProvider {

    private static final String TAG = "LearnedObjectsProvider";

    private static final String DATABASE_NAME = "learned_objects.db";

    private static final int DATABASE_VERSION = 2;

    private static final String LEARNED_OBJECTS_TABLE_NAME = "learnedobjects";

    private static HashMap<String, String> sLearnedObjectsProjectionMap;

    private static final int LEARNED_OBJECTS_ALL = 1;

    private static final int LEARNED_OBJECTS_SINGLE_ITEM = 2;

    private static final UriMatcher sUriMatcher;

    private static final String CONTENT_PATH = "/data/data/ibis.dog.gui/";

    /**
     * This class helps open, create, and upgrade the database file.
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + LEARNED_OBJECTS_TABLE_NAME + " ("
                    + LearnedObject._ID + " INTEGER PRIMARY KEY,"
                    + LearnedObject.OBJECT_NAME + " TEXT,"
                    + LearnedObject.AUTHOR + " TEXT,"
                    + LearnedObject.CREATED_DATE + " INTEGER,"
                    + LearnedObject.MODIFIED_DATE + " INTEGER" + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS notes");
            onCreate(db);
        }
    }

    private DatabaseHelper mOpenHelper;

    @Override
    public boolean onCreate() {
        mOpenHelper = new DatabaseHelper(getContext());
        File thumbsDir = new File(CONTENT_PATH + "thumbs");
        File featureVectorsDir = new File(CONTENT_PATH + "featurevectors");
        if (thumbsDir.exists() == false) {
            System.out.println("made thumbs dir: " + thumbsDir.mkdirs());
        }
        if (featureVectorsDir.exists() == false) {
            System.out.println("made featurevectors dir: "
                    + featureVectorsDir.mkdirs());
        }
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        switch (sUriMatcher.match(uri)) {
        case LEARNED_OBJECTS_ALL:
            qb.setTables(LEARNED_OBJECTS_TABLE_NAME);
            qb.setProjectionMap(sLearnedObjectsProjectionMap);
            break;

        case LEARNED_OBJECTS_SINGLE_ITEM:
            qb.setTables(LEARNED_OBJECTS_TABLE_NAME);
            qb.setProjectionMap(sLearnedObjectsProjectionMap);
            qb.appendWhere(LearnedObject._ID + "="
                    + uri.getPathSegments().get(1));
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // If no sort order is specified use the default
        String orderBy;
        if (TextUtils.isEmpty(sortOrder)) {
            orderBy = LearnedObjects.LearnedObject.DEFAULT_SORT_ORDER;
        } else {
            orderBy = sortOrder;
        }

        // Get the database and run the query
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor c = qb.query(db, projection, selection, selectionArgs, null,
                null, orderBy);

        // Tell the cursor what uri to watch, so it knows when its source data
        // changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
        case LEARNED_OBJECTS_ALL:
            return LearnedObject.CONTENT_TYPE;

        case LEARNED_OBJECTS_SINGLE_ITEM:
            return LearnedObject.CONTENT_ITEM_TYPE;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        // Validate the requested uri
        if (sUriMatcher.match(uri) != LEARNED_OBJECTS_ALL) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        ContentValues values;
        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }

        Long now = Long.valueOf(System.currentTimeMillis());

        // Make sure that the fields are all set
        if (values.containsKey(LearnedObjects.LearnedObject.CREATED_DATE) == false) {
            values.put(LearnedObjects.LearnedObject.CREATED_DATE, now);
        }

        if (values.containsKey(LearnedObjects.LearnedObject.MODIFIED_DATE) == false) {
            values.put(LearnedObjects.LearnedObject.MODIFIED_DATE, now);
        }

        if (values.containsKey(LearnedObjects.LearnedObject.OBJECT_NAME) == false) {
            Resources r = Resources.getSystem();
            values.put(LearnedObjects.LearnedObject.OBJECT_NAME, r
                    .getString(android.R.string.untitled));
        }

        if (values.containsKey(LearnedObjects.LearnedObject.AUTHOR) == false) {
            values.put(LearnedObjects.LearnedObject.AUTHOR, "");
        }

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        long rowId = db.insert(LEARNED_OBJECTS_TABLE_NAME,
                LearnedObject.AUTHOR, values);
        if (rowId > 0) {
            Uri noteUri = ContentUris.withAppendedId(
                    LearnedObjects.LearnedObject.CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(noteUri, null);
            return noteUri;
        }

        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
        case LEARNED_OBJECTS_ALL:
            count = db.delete(LEARNED_OBJECTS_TABLE_NAME, where, whereArgs);
            // delete files too!
            File thumbsDir = new File(CONTENT_PATH + "/thumbs");
            for (File thumb : thumbsDir.listFiles()) {
                thumb.delete();
            }
            File featurevectorsDir = new File(CONTENT_PATH + "/featurevectors");
            for (File featurevector : featurevectorsDir.listFiles()) {
                featurevector.delete();
            }
            break;

        case LEARNED_OBJECTS_SINGLE_ITEM:
            String noteId = uri.getPathSegments().get(1);
            count = db.delete(LEARNED_OBJECTS_TABLE_NAME,
                    LearnedObject._ID
                            + "="
                            + noteId
                            + (!TextUtils.isEmpty(where) ? " AND (" + where
                                    + ')' : ""), whereArgs);
            // delete files too!
            new File(CONTENT_PATH + "/thumbs/" + noteId).delete();
            new File(CONTENT_PATH + "/featurevectors/" + noteId).delete();
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String where,
            String[] whereArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
        case LEARNED_OBJECTS_ALL:
            count = db.update(LEARNED_OBJECTS_TABLE_NAME, values, where,
                    whereArgs);
            break;

        case LEARNED_OBJECTS_SINGLE_ITEM:
            String noteId = uri.getPathSegments().get(1);
            count = db.update(LEARNED_OBJECTS_TABLE_NAME, values,
                    LearnedObject._ID
                            + "="
                            + noteId
                            + (!TextUtils.isEmpty(where) ? " AND (" + where
                                    + ')' : ""), whereArgs);
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode)
            throws FileNotFoundException {
        int intMode = ParcelFileDescriptor.MODE_READ_WRITE
                | ParcelFileDescriptor.MODE_CREATE;
        File target = new File(CONTENT_PATH + uri.getPathSegments().get(1)
                + "/" + uri.getPathSegments().get(2));
        if (!target.exists()) {
            try {
                target.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return ParcelFileDescriptor.open(target, intMode);
    }

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(LearnedObjects.AUTHORITY, "learnedobjects",
                LEARNED_OBJECTS_ALL);
        sUriMatcher.addURI(LearnedObjects.AUTHORITY, "learnedobjects/#",
                LEARNED_OBJECTS_SINGLE_ITEM);

        sLearnedObjectsProjectionMap = new HashMap<String, String>();
        sLearnedObjectsProjectionMap.put(LearnedObject._ID, LearnedObject._ID);
        sLearnedObjectsProjectionMap.put(LearnedObject.OBJECT_NAME,
                LearnedObject.OBJECT_NAME);
        sLearnedObjectsProjectionMap.put(LearnedObject.AUTHOR,
                LearnedObject.AUTHOR);
        sLearnedObjectsProjectionMap.put(LearnedObject.CREATED_DATE,
                LearnedObject.CREATED_DATE);
        sLearnedObjectsProjectionMap.put(LearnedObject.MODIFIED_DATE,
                LearnedObject.MODIFIED_DATE);
    }

}
