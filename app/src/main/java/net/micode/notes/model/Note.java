/*
 * Copyright (c) 2010-2011, The MiCode Open Source Community (www.micode.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.micode.notes.model;

import android.content.*;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;
import net.micode.notes.data.Notes;
import net.micode.notes.data.Notes.CallNote;
import net.micode.notes.data.Notes.DataColumns;
import net.micode.notes.data.Notes.NoteColumns;
import net.micode.notes.data.Notes.TextNote;

import java.util.ArrayList;


public class Note {
    private ContentValues mNoteDiffValues;
    private NoteData mNoteData;
    private static final String TAG = "Note";
    /**
     * 获取新笔记的唯一标识符
     * @param context 上下文对象
     * @param folderId 新建笔记所属文件夹的id
     * @return 新建笔记的唯一标识符
     */
    public static synchronized long getNewNoteId(Context context, long folderId) {
        // 创建一个新的ContentValues对象，并设置相关键值对
        ContentValues values = new ContentValues();
        long createdTime = System.currentTimeMillis(); // 获取当前时间作为创建时间
        values.put(NoteColumns.CREATED_DATE, createdTime); // 存入创建时间
        values.put(NoteColumns.MODIFIED_DATE, createdTime); // 存入修改时间（初始与创建时间相同）
        values.put(NoteColumns.TYPE, Notes.TYPE_NOTE); // 存入笔记类型
        values.put(NoteColumns.LOCAL_MODIFIED, 1); // 存入本地修改状态
        values.put(NoteColumns.PARENT_ID, folderId); // 存入所属文件夹id

        // 向数据库中插入新笔记数据，并获取包含新建笔记Uri的对象uri
        Uri uri = context.getContentResolver().insert(Notes.CONTENT_NOTE_URI, values);

        long noteId;
        try {
            // 从uri中获得新建笔记的id，即路径segments的第二个元素，并将其转换为long类型返回
            noteId = Long.parseLong(uri.getPathSegments().get(1));
        } catch (NumberFormatException e) {
            // 如果获取不到这个id或者它等于-1，则会抛出相应的异常
            Log.e(TAG, "Get note id error :" + e);
            noteId = 0;
        }
        if (noteId == -1) {
            throw new IllegalStateException("Wrong note id:" + noteId);
        }
        return noteId;
    }

    /**
     * Note类的构造函数，用于初始化对象
     * 该代码定义了一个名为"Note"的类的构造函数。这个类可能是一个笔记应用程序的一部分，用于表示一个笔记。
     */
    public Note() {
        // 创建一个ContentValues对象，用于保存笔记中改变的值，即笔记已经被修改但还没同步到数据库
        mNoteDiffValues = new ContentValues();
        // 创建一个NoteData对象，用于保存笔记的所有数据，包括标题、正文、颜色等
        mNoteData = new NoteData();
    }


    /**
     * 设置笔记的key-value值
     * @param key 键名
     * @param value 键值
     *
     *              在构造函数中，首先创建一个空的ContentValues对象mNoteDiffValues，用于保存笔记中改变的值，因为当我们修改笔记后并不会立即同步到数据库，而是需要等待一段时间或者手动进行同步。接着创建一个NoteData对象mNoteData，用于保存笔记的所有数据，包括标题、正文、颜色等。
     *
     *
     */
    public void setNoteValue(String key, String value) {
        // 向ContentValues对象中添加键值对
        mNoteDiffValues.put(key, value);
        // 将本地修改状态设置为1，表示笔记已经被修改但还没同步到数据库
        mNoteDiffValues.put(NoteColumns.LOCAL_MODIFIED, 1);
        // 将修改时间设置为当前时间
        mNoteDiffValues.put(NoteColumns.MODIFIED_DATE, System.currentTimeMillis());
    }


    /**
     * 设置笔记文本数据
     *
     * @param key   一个字符串类型的键，用于标识要设置的文本数据类型。例如："title"、"content"等。
     * @param value 一个字符串类型的值，表示要设置的文本数据内容。例如：标题或正文内容等。
     *
     * 在构造函数中，首先创建一个空的ContentValues对象mNoteDiffValues，用于保存笔记中改变的值。因为当我们修改笔记后并不会立即同步到数据库，而是需要等待一段时间或者手动进行同步。接着创建一个NoteData对象mNoteData，用于保存笔记的所有数据，包括标题、正文、颜色等。
     */
    public void setTextData(String key, String value) {
        // 调用mNoteData对象的setTextData方法，将指定键（key）和值（value）添加到笔记的文本数据中
        mNoteData.setTextData(key, value);
    }

    /**
     * 设置笔记文本数据id
     *
     * @param id    一个长整型的值，表示要设置的笔记文本数据的id。
     *
     * 该函数用于将指定的长整型数值作为笔记文本数据的id进行设置。
     * mNoteData是在类构造函数中创建的NoteData对象，通过调用它的setTextDataId方法，可以设置笔记的文本数据id。
     */
    public void setTextDataId(long id) {
        // 调用mNoteData对象的setTextDataId方法，将指定的长整型数值作为笔记文本数据的id进行设置
        mNoteData.setTextDataId(id);
    }



    /**
     * 获取笔记文本数据id
     *
     * @return 一个长整型的值，表示当前笔记文本数据的id。
     *
     * 该函数用于从当前Note对象中获取保存的笔记文本数据的id。
     * mNoteData是在类构造函数中创建的NoteData对象，其中mTextDataId是NoteData类成员变量，用于保存笔记文本数据的id。
     */
    public long getTextDataId() {
        // 返回mNoteData对象中保存的笔记文本数据的id
        return mNoteData.mTextDataId;
    }


    /**
     * 设置笔记的通话记录id
     *
     * @param id 一个长整型的值，表示要设置的笔记通话记录的id。
     *
     * 该函数用于将指定的长整型数值作为笔记的通话记录id进行设置。
     * mNoteData是在类构造函数中创建的NoteData对象，通过调用它的setCallDataId方法，可以设置笔记的通话记录id。
     */
    public void setCallDataId(long id) {
        // 调用mNoteData对象的setCallDataId方法，将指定的长整型数值作为笔记的通话记录id进行设置
        mNoteData.setCallDataId(id);
    }


    /**
     * 设置笔记通话记录数据
     *
     * @param key   一个字符串类型的键，用于标识要设置的通话记录数据类型。例如："name"、"number"等。
     * @param value 一个字符串类型的值，表示要设置的通话记录数据内容。例如：联系人姓名或电话号码等。
     *
     * 该函数用于向当前Note对象中添加指定的通话记录数据。
     * mNoteData是在类构造函数中创建的NoteData对象，通过调用它的setCallData方法，可以将指定的键/值对添加到笔记的通话记录数据中。
     */
    public void setCallData(String key, String value) {
        // 调用mNoteData对象的setCallData方法，将指定键（key）和值（value）添加到笔记的通话记录数据中
        mNoteData.setCallData(key, value);
    }


    /**
     * 检查笔记是否已经被本地修改
     *
     * @return 一个布尔值，表示当前笔记是否已经被本地修改。
     *
     * 该函数用于检查当前Note对象是否已经被本地修改。它会检查mNoteDiffValues和mNoteData对象，
     * 如果它们中的任意一个在本地被修改过，则返回true；否则返回false。
     */
    public boolean isLocalModified() {
        // 判断mNoteDiffValues对象是否包含数据，或者mNoteData对象中是否有数据被本地修改。
        return mNoteDiffValues.size() > 0 || mNoteData.isLocalModified();
    }

    /**
     * 同步笔记数据到本地数据库
     *
     * @param context 一个Context对象，用于访问应用程序的内容提供器。
     * @param noteId 一个长整型的值，表示要同步的笔记ID。
     *
     * @return 一个布尔值，表示笔记是否成功同步到本地数据库。
     *
     * 该函数用于将当前Note对象保存到本地数据库中。如果当前Note对象没有被本地修改，则直接返回true；
     * 否则，使用ContentResolver对象将mNoteDiffValues中保存的数值更新到NoteColumns.LOCAL_MODIFIED和NoteColumns.MODIFIED_DATE字段上。
     * 如果更新失败，则在日志中记录错误信息，并返回false；否则，清空mNoteDiffValues中的数据，并将mNoteData中的数据保存到本地数据库中。
     * 如果保存失败，则返回false；否则，返回true。
     */
    public boolean syncNote(Context context, long noteId) {
        // 检查noteId参数是否合法，如果小于等于0，则抛出IllegalArgumentException异常
        if (noteId <= 0) {
            throw new IllegalArgumentException("Wrong note id:" + noteId);
        }

        // 如果当前Note对象没有被本地修改，则直接返回true
        if (!isLocalModified()) {
            return true;
        }


        // 使用ContentResolver对象将mNoteDiffValues中保存的数值更新到NoteColumns.LOCAL_MODIFIED和NoteColumns.MODIFIED_DATE字段上
        if (context.getContentResolver().update(
                ContentUris.withAppendedId(Notes.CONTENT_NOTE_URI, noteId), mNoteDiffValues, null,
                null) == 0) {
            // 如果更新失败，则在日志中记录错误信息
            Log.e(TAG, "Update note error, should not happen");
            // Do not return, fall through
        }

        // 清空mNoteDiffValues中的数据
        mNoteDiffValues.clear();

        // 将mNoteData中的数据保存到本地数据库中
        // 如果保存失败，则返回false
        return !mNoteData.isLocalModified()
                || (mNoteData.pushIntoContentResolver(context, noteId) != null);

        // 如果保存成功，则返回true
    }


    private class NoteData {


        private static final String TAG = "NoteData";
        private long mTextDataId;
        private long mCallDataId;


        private ContentValues mTextDataValues;
        private ContentValues mCallDataValues;


        /**
         * 构造函数
         *
         * 该函数用于创建一个新的NoteData对象，并初始化mTextDataValues、mCallDataValues、mTextDataId和mCallDataId成员变量。
         * mTextDataValues和mCallDataValues对象分别用于保存笔记的文本数据和通话记录数据。
         * mTextDataId和mCallDataId分别表示当前笔记的文本数据ID和通话记录数据ID，初始值为0。
         */
        public NoteData() {
            // 创建一个新的ContentValues对象mTextDataValues，用于保存笔记的文本数据
            mTextDataValues = new ContentValues();
            // 创建一个新的ContentValues对象mCallDataValues，用于保存笔记的通话记录数据
            mCallDataValues = new ContentValues();
            // 将mTextDataId和mCallDataId的初始值设置为0
            mTextDataId = 0;
            mCallDataId = 0;
        }


        /**
         * 检查笔记数据是否已经被本地修改
         *
         * @return 一个布尔值，表示当前笔记数据是否已经被本地修改。
         *
         * 该函数用于检查当前NoteData对象是否已经被本地修改。它会检查mTextDataValues和mCallDataValues对象，
         * 如果它们中的任意一个在本地被修改过，则返回true；否则返回false。
         */
        boolean isLocalModified() {
            // 判断mTextDataValues和mCallDataValues对象是否包含数据，如果有数据被本地修改过，则返回true
            return mTextDataValues.size() > 0 || mCallDataValues.size() > 0;
        }

        /**
         * 设置笔记的文本数据id
         *
         * @param id 一个长整型的值，表示要设置的笔记文本数据的id。
         *
         * 该函数用于将指定的长整型数值作为笔记的文本数据id进行设置。如果指定的id小于等于0，则抛出IllegalArgumentException异常。
         * mTextDataId是NoteData类成员变量，用于保存笔记文本数据的ID。
         */
        void setTextDataId(long id) {
            // 如果指定的id小于等于0，则抛出IllegalArgumentException异常
            if(id <= 0) {
                throw new IllegalArgumentException("Text data id should larger than 0");
            }
            // 将指定的长整型数值作为笔记的文本数据id进行设置
            mTextDataId = id;
        }


        /**
         * 设置笔记的通话记录数据id
         *
         * @param id 一个长整型的值，表示要设置的笔记通话记录数据的id。
         *
         * 该函数用于将指定的长整型数值作为笔记的通话记录数据id进行设置。如果指定的id小于等于0，
         * 则抛出IllegalArgumentException异常。
         * mCallDataId是NoteData类成员变量，用于保存笔记的通话记录数据ID。
         */
        void setCallDataId(long id) {
            // 如果指定的id小于等于0，则抛出IllegalArgumentException异常
            if (id <= 0) {
                throw new IllegalArgumentException("Call data id should larger than 0");
            }
            // 将指定的长整型数值作为笔记的通话记录数据id进行设置
            mCallDataId = id;
        }


        /**
         * 设置笔记的通话记录数据
         *
         * @param key 一个字符串，表示要设置的通话记录数据的键。
         * @param value 一个字符串，表示要设置的通话记录数据的值。
         *
         * 该函数用于将指定的字符串作为笔记的通话记录数据进行设置。它会将键值对保存到mCallDataValues对象中，并将NoteColumns.LOCAL_MODIFIED和NoteColumns.MODIFIED_DATE字段的值更新为1和当前时间戳，
         * 表示笔记已被本地修改过。
         */
        void setCallData(String key, String value) {
            // 将键值对保存到mCallDataValues对象中
            mCallDataValues.put(key, value);
            // 将NoteColumns.LOCAL_MODIFIED字段的值设置为1，表示笔记已被本地修改过
            mNoteDiffValues.put(NoteColumns.LOCAL_MODIFIED, 1);
            // 将NoteColumns.MODIFIED_DATE字段的值更新为当前时间戳，表示笔记最近一次被修改的时间
            mNoteDiffValues.put(NoteColumns.MODIFIED_DATE, System.currentTimeMillis());
        }


        /**
         * 设置笔记的文本数据
         *
         * @param key 一个字符串，表示要设置的文本数据的键。
         * @param value 一个字符串，表示要设置的文本数据的值。
         *
         * 该函数用于将指定的字符串作为笔记的文本数据进行设置。它会将键值对保存到mTextDataValues对象中，并将NoteColumns.LOCAL_MODIFIED和NoteColumns.MODIFIED_DATE字段的值更新为1和当前时间戳，
         * 表示笔记已被本地修改过。
         */
        void setTextData(String key, String value) {
            // 将键值对保存到mTextDataValues对象中
            mTextDataValues.put(key, value);
            // 将NoteColumns.LOCAL_MODIFIED字段的值设置为1，表示笔记已被本地修改过
            mNoteDiffValues.put(NoteColumns.LOCAL_MODIFIED, 1);
            // 将NoteColumns.MODIFIED_DATE字段的值更新为当前时间戳，表示笔记最近一次被修改的时间
            mNoteDiffValues.put(NoteColumns.MODIFIED_DATE, System.currentTimeMillis());
        }


        /**
         * 将笔记数据插入ContentResolver中
         *
         * @param context Context对象，表示当前应用程序的上下文环境。
         * @param noteId 一个长整型数值，表示要插入的笔记的id。
         *
         * @return 返回插入成功的Uri对象，如果插入失败则返回null。
         *
         * 该函数用于将当前NoteData对象保存到ContentResolver中。它会将mTextDataValues和mCallDataValues对象中的数据保存到ContentResolver中，并更新NoteColumns.LOCAL_MODIFIED和NoteColumns.MODIFIED_DATE字段的值为0和当前时间戳，
         * 表示笔记已被同步到云端。
         */
        Uri pushIntoContentResolver(Context context, long noteId) {
            /**
             * 检查noteId是否合法
             */
            if (noteId <= 0) {
                throw new IllegalArgumentException("Wrong note id:" + noteId);
            }

            // 初始化operationList列表和builder对象
            ArrayList<ContentProviderOperation> operationList = new ArrayList<>();
            ContentProviderOperation.Builder builder;

            // 如果mTextDataValues对象不为空，则将其中的数据保存到ContentResolver中
            if(mTextDataValues.size() > 0) {
                // 为mTextDataValues对象添加笔记id
                mTextDataValues.put(DataColumns.NOTE_ID, noteId);
                if (mTextDataId == 0) {
                    // 如果mTextDataId为0，则表明当前笔记没有文本数据，需要进行插入操作
                    mTextDataValues.put(DataColumns.MIME_TYPE, TextNote.CONTENT_ITEM_TYPE);
                    // 通过ContentResolver插入数据，并获取插入的Uri对象
                    Uri uri = context.getContentResolver().insert(Notes.CONTENT_DATA_URI,
                            mTextDataValues);
                    try {
                        // 解析Uri中的id值，并将其设置为mTextDataId
                        setTextDataId(Long.parseLong(uri.getPathSegments().get(1)));
                    } catch (NumberFormatException e) {
                        Log.e(TAG, "Insert new text data fail with noteId" + noteId);
                        // 插入失败，清空mTextDataValues，并返回null
                        mTextDataValues.clear();
                        return null;
                    }
                } else {
                    // 如果mTextDataId不为0，则表明当前笔记已经有文本数据，需要进行更新操作
                    builder = ContentProviderOperation.newUpdate(ContentUris.withAppendedId(
                            Notes.CONTENT_DATA_URI, mTextDataId));
                    builder.withValues(mTextDataValues);
                    operationList.add(builder.build());
                }
                // 清空mTextDataValues对象
                mTextDataValues.clear();
            }

            // 如果mCallDataValues对象不为空，则将其中的数据保存到ContentResolver中
            if(mCallDataValues.size() > 0) {
                // 为mCallDataValues对象添加笔记id
                mCallDataValues.put(DataColumns.NOTE_ID, noteId);
                if (mCallDataId == 0) {
                    // 如果mCallDataId为0，则表明当前笔记没有通话记录数据，需要进行插入操作
                    mCallDataValues.put(DataColumns.MIME_TYPE, CallNote.CONTENT_ITEM_TYPE);
                    // 通过ContentResolver插入数据，并获取插入的Uri对象
                    Uri uri = context.getContentResolver().insert(Notes.CONTENT_DATA_URI,
                            mCallDataValues);
                    try {
                        // 解析Uri中的id值，并将其设置为mCallDataId
                        setCallDataId(Long.parseLong(uri.getPathSegments().get(1)));
                    } catch (NumberFormatException e) {
                        Log.e(TAG, "Insert new call data fail with noteId" + noteId);
                        // 插入失败，清空mCallDataValues，并返回null
                        mCallDataValues.clear();
                        return null;
                    }
                } else {
                    // 如果mCallDataId不为0，则表明当前笔记已经有通话记录数据，需要进行更新操作
                    builder = ContentProviderOperation.newUpdate(ContentUris.withAppendedId(
                            Notes.CONTENT_DATA_URI, mCallDataId));
                    builder.withValues(mCallDataValues);
                    operationList.add(builder.build());
                }
                // 清空mCallDataValues对象
                mCallDataValues.clear();
            }
            if (operationList.size() > 0) { // 如果操作列表中有操作
                try {
                    // 调用 ContentResolver 的 applyBatch 方法执行操作
                    ContentProviderResult[] results = context.getContentResolver().applyBatch(
                            Notes.AUTHORITY, operationList);
                    // 判断操作是否成功，并返回对应的 Uri
                    return (results == null || results.length == 0 || results[0] == null) ? null
                            : ContentUris.withAppendedId(Notes.CONTENT_NOTE_URI, noteId);
                } catch (RemoteException e) { // 捕获 RemoteException 异常
                    Log.e(TAG, String.format("%s: %s", e, e.getMessage())); // 记录异常信息到日志
                    return null; // 返回 null
                } catch (OperationApplicationException e) { // 捕获 OperationApplicationException 异常
                    Log.e(TAG, String.format("%s: %s", e, e.getMessage())); // 记录异常信息到日志
                    return null; // 返回 null
                }
            }
            return null; // 如果操作列表为空，则返回 null
        }
    }
}
