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
import android.app.ListActivity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

/**
 * Displays a list of notes. Will display notes from the {@link Uri} provided in
 * the intent if there is one, otherwise defaults to displaying the contents of
 * the {@link NotePadProvider}
 */
public class LearnedObjectsList extends ListActivity {
    /**
     * The columns we are interested in from the database
     */
    private static final String[] PROJECTION = new String[] {
            LearnedObject._ID, // 0
            LearnedObject.OBJECT_NAME, // 1
            LearnedObject.AUTHOR, // 2
    };

    private static final int DELETE_ITEM = 1;

    private static final int EDIT_ITEM = 2;

    private static final int SHARE_ITEM = 3;

    private static final int ACTIVITY_EDIT = 1;

    Cursor mCursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.objects_list);

        setDefaultKeyMode(DEFAULT_KEYS_SHORTCUT);

        // If no data was given in the intent (because we were started
        // as a MAIN activity), then use our default content provider.
        Intent intent = getIntent();
        if (intent.getData() == null) {
            intent.setData(LearnedObject.CONTENT_URI);
        }

        // Inform the list we provide context menus for items
        getListView().setOnCreateContextMenuListener(this);

        // Perform a managed query. The Activity will handle closing and
        // requerying the cursor
        // when needed.
        mCursor = managedQuery(getIntent().getData(), PROJECTION, null, null,
                LearnedObject.DEFAULT_SORT_ORDER);

        // Used to map notes entries from the database to views
        PhotoAdapter adapter = new PhotoAdapter(this);
        setListAdapter(adapter);
        registerForContextMenu(getListView());
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        menu.add(0, DELETE_ITEM, 0, "Delete");
        menu.add(0, EDIT_ITEM, 0, "Edit");
        menu.add(0, SHARE_ITEM, 0, "Share this!");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        super.onContextItemSelected(item);
        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item
                .getMenuInfo();
        mCursor.moveToPosition(menuInfo.position);
        int id = mCursor.getInt(mCursor.getColumnIndex(LearnedObject._ID));

        switch (item.getItemId()) {
        case DELETE_ITEM:
            getContentResolver().delete(
                    Uri.withAppendedPath(getIntent().getData(), "" + id), null,
                    null);
            break;
        case EDIT_ITEM:
            Intent editIntent = new Intent(LearnedObjectsList.this,
                    LearnedObjectEditor.class);
            editIntent.putExtra("id", id);
            editIntent.putExtra("name", mCursor.getString(mCursor
                    .getColumnIndex(LearnedObject.OBJECT_NAME)));
            editIntent.putExtra("author", mCursor.getString(mCursor
                    .getColumnIndex(LearnedObject.AUTHOR)));
            startActivityForResult(editIntent, ACTIVITY_EDIT);
            break;
        }

        mCursor.requery();
        return true;
    }

    protected void onActivityResult(int requestCode, int resultCode,
            Intent resultIntent) {
        super.onActivityResult(requestCode, resultCode, resultIntent);
        if (resultIntent == null) {
            return;
        }
        switch (requestCode) {
        case ACTIVITY_EDIT:
            ContentValues values = new ContentValues();
            Bundle extras = resultIntent.getExtras();
            values.put(LearnedObject.OBJECT_NAME, extras.getString("name"));
            values.put(LearnedObject.AUTHOR, extras.getString("author"));
            System.out.println("extras: " + extras);
            System.out.println("extras.name: " + extras.getString("name"));
            int id = extras.getInt("id");
            getContentResolver().update(
                    Uri.withAppendedPath(getIntent().getData(), "" + id),
                    values, null, null);
            break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("Delete All");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        getContentResolver().delete(LearnedObject.CONTENT_URI, null, null);
        return super.onOptionsItemSelected(item);
    }

    /**
     * A simple adapter which maintains an ArrayList of photo resource Ids. Each
     * photo is displayed as an image. This adapter supports clearing the list
     * of photos and adding a new photo.
     * 
     */
    public class PhotoAdapter extends ResourceCursorAdapter {

        public PhotoAdapter(Context context) {
            super(context, R.layout.objects_list_item, mCursor);
        }

        public void bindView(View view, Context context, Cursor cursor) {
            TextView name = (TextView) view.findViewById(R.id.nameTextView);
            name.setText("Object: "
                    + cursor.getString(cursor
                            .getColumnIndex(LearnedObject.OBJECT_NAME)));
            TextView author = (TextView) view.findViewById(R.id.authorTextView);
            author.setText("Author: "
                    + cursor.getString(cursor
                            .getColumnIndex(LearnedObject.AUTHOR)));
            ImageView thumbnail = (ImageView) view
                    .findViewById(R.id.thumbnailImageView);
            Uri thumbnailUri = Uri.withAppendedPath(getIntent().getData(),
                    "thumbs/"
                            + cursor.getInt(cursor
                                    .getColumnIndex(LearnedObject._ID)));
            thumbnail.setImageURI(thumbnailUri);
        }

    }
}
