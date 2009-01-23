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

import android.net.Uri;
import android.provider.BaseColumns;

public final class LearnedObjects {
    public static final String AUTHORITY = "ibis.dog.learnedobjects.provider";

    // This class cannot be instantiated
    private LearnedObjects() {
    }

    public static final class LearnedObject implements BaseColumns {
        // This class cannot be instantiated
        private LearnedObject() {
        }

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + AUTHORITY + "/learnedobjects");

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of
         * learned objects.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.ibis.dog.learned";

        /**
         * The MIME type of a {@link #CONTENT_URI} sub-directory of a single
         * learned object.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.ibis.dog.learned";

        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER = "modified ASC";

        /**
         * The title of the note
         * <P>
         * Type: TEXT
         * </P>
         */
        public static final String OBJECT_NAME = "objectname";

        /**
         * The note itself
         * <P>
         * Type: TEXT
         * </P>
         */
        public static final String AUTHOR = "author";

        /**
         * The timestamp for when the note was created
         * <P>
         * Type: INTEGER (long from System.curentTimeMillis())
         * </P>
         */
        public static final String CREATED_DATE = "created";

        /**
         * The timestamp for when the note was last modified
         * <P>
         * Type: INTEGER (long from System.curentTimeMillis())
         * </P>
         */
        public static final String MODIFIED_DATE = "modified";

        public static Uri getThumbUri(Uri uri) {
            return Uri.parse(CONTENT_URI + "/thumbs/"
                    + uri.getPathSegments().get(1));
        }

        public static Uri getThumbUri(int id) {
            return Uri.parse(CONTENT_URI + "/thumbs/" + id);
        }

        public static Uri getFeatureVectorUri(Uri uri) {
            return Uri.parse(CONTENT_URI + "/featurevectors/"
                    + uri.getPathSegments().get(1));
        }

        public static Uri getFeatureVectorUri(int id) {
            return Uri.parse(CONTENT_URI + "/featurevectors/" + id);
        }
    }
}