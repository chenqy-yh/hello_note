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

package net.micode.notes.tool;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.RemoteException;
import android.util.Log;

import net.micode.notes.data.Notes;
import net.micode.notes.data.Notes.CallNote;
import net.micode.notes.data.Notes.NoteColumns;
import net.micode.notes.ui.NotesListAdapter.AppWidgetAttribute;

import java.util.ArrayList;
import java.util.HashSet;


public class DataUtils {
    public static final String TAG = "DataUtils";

    /**
     * 批量删除笔记方法
     *
     * @param resolver ContentResolver对象
     * @param ids      HashSet<Long>类型，需要删除的笔记id集合
     * @return boolean类型，表示是否删除成功
     */
    public static boolean batchDeleteNotes(ContentResolver resolver, HashSet<Long> ids) {
        // 如果ids为空或者size为0，则直接返回true
        if (ids == null) {
            Log.d(TAG, "the ids is null");
            return true;
        }
        if (ids.size() == 0) {
            Log.d(TAG, "no id is in the hashset");
            return true;
        }

        // 创建操作列表
        ArrayList<ContentProviderOperation> operationList = new ArrayList<>();
        for (long id : ids) {
            // 不包括系统文件夹根节点
            if(id == Notes.ID_ROOT_FOLDER) {
                Log.e(TAG, "Don't delete system folder root");
                continue;
            }
            // 创建ContentProviderOperation.Builder对象来构建删除该id的操作，并将其添加到operationList中
            ContentProviderOperation.Builder builder = ContentProviderOperation
                    .newDelete(ContentUris.withAppendedId(Notes.CONTENT_NOTE_URI, id));
            operationList.add(builder.build());
        }
        try {
            // 通过ContentResolver的applyBatch()方法将所有操作提交给ContentProvider执行
            ContentProviderResult[] results = resolver.applyBatch(Notes.AUTHORITY, operationList);
            if (results == null || results.length == 0 || results[0] == null) {
                Log.d(TAG, "delete notes failed, ids:" + ids);
                return false;
            }
            // 如果成功则返回true
            return true;
        } catch (RemoteException | OperationApplicationException e) {
            Log.e(TAG, String.format("%s: %s", e, e.getMessage()));
        }
        // 失败则返回false
        return false;
    }



    /**
     * 将笔记移动到指定文件夹下
     *
     * @param resolver     ContentResolver对象
     * @param id           需要移动的笔记id
     * @param srcFolderId  原文件夹id
     * @param desFolderId  目标文件夹id
     */
    public static void moveNoteToFoler(ContentResolver resolver, long id, long srcFolderId, long desFolderId) {
        // 创建ContentValues对象，用于存放需要更新的字段和值
        ContentValues values = new ContentValues();
        // 更新Parent_Id字段为目标文件夹id
        values.put(NoteColumns.PARENT_ID, desFolderId);
        // 更新Origin_Parent_Id字段为原文件夹id
        values.put(NoteColumns.ORIGIN_PARENT_ID, srcFolderId);
        // 更新Local_Modified字段为1
        values.put(NoteColumns.LOCAL_MODIFIED, 1);

        // 调用ContentResolver的update()方法来更新数据
        resolver.update(ContentUris.withAppendedId(Notes.CONTENT_NOTE_URI, id), values, null, null);
    }

    /**
     * 批量将笔记移动到指定文件夹下
     *
     * @param resolver   ContentResolver对象
     * @param ids        HashSet<Long>类型，需要移动的笔记id集合
     * @param folderId   目标文件夹id
     * @return boolean类型，表示是否移动成功
     */
    public static boolean batchMoveToFolder(ContentResolver resolver, HashSet<Long> ids, long folderId) {
        // 如果ids为空，则直接返回true
        if (ids == null) {
            Log.d(TAG, "the ids is null");
            return true;
        }

        // 创建操作列表
        ArrayList<ContentProviderOperation> operationList = new ArrayList<>();
        for (long id : ids) {
            // 创建ContentProviderOperation.Builder对象来构建更新该id的操作，并将其添加到operationList中
            ContentProviderOperation.Builder builder = ContentProviderOperation
                    .newUpdate(ContentUris.withAppendedId(Notes.CONTENT_NOTE_URI, id));
            // 更新Parent_Id字段为目标文件夹id
            builder.withValue(NoteColumns.PARENT_ID, folderId);
            // 更新Local_Modified字段为1
            builder.withValue(NoteColumns.LOCAL_MODIFIED, 1);
            operationList.add(builder.build());
        }

        try {
            // 通过ContentResolver的applyBatch()方法将所有操作提交给ContentProvider执行
            ContentProviderResult[] results = resolver.applyBatch(Notes.AUTHORITY, operationList);
            if (results == null || results.length == 0 || results[0] == null) {
                Log.d(TAG, "move notes failed, ids:" + ids);
                return false;
            }
            // 如果成功则返回true
            return true;
        } catch (RemoteException | OperationApplicationException e) {
            Log.e(TAG, String.format("%s: %s", e, e.getMessage()));
        }
        // 失败则返回false
        return false;
    }


    /**
     * Get the all folder count except system folders {@link Notes#TYPE_SYSTEM}}
     */
    /**
     * 获取用户自定义文件夹数量
     *
     * @param resolver ContentResolver对象
     * @return int类型，表示用户自定义文件夹数量
     */
    public static int getUserFolderCount(ContentResolver resolver) {
        // 查询ContentProvider中的所有符合条件的数据，并返回查询结果Cursor对象
        Cursor cursor =resolver.query(Notes.CONTENT_NOTE_URI,
                new String[] { "COUNT(*)" },
                NoteColumns.TYPE + "=? AND " + NoteColumns.PARENT_ID + "<>?",
                new String[] { String.valueOf(Notes.TYPE_FOLDER), String.valueOf(Notes.ID_TRASH_FOLER)},
                null);

        int count = 0;
        if(cursor != null) {
            if(cursor.moveToFirst()) {
                try {
                    // 从Cursor对象中获取查询结果中count字段的值
                    count = cursor.getInt(0);
                } catch (IndexOutOfBoundsException e) {
                    Log.e(TAG, "get folder count failed:" + e);
                } finally {
                    // 关闭游标
                    cursor.close();
                }
            }
        }
        // 返回用户自定义文件夹数量
        return count;
    }

    /**
     * 查询指定id的笔记是否存在于ContentProvider中
     *
     * @param resolver ContentResolver对象
     * @param noteId   需要查询的笔记id
     * @param type     笔记类型，用于限定查询结果的笔记类型，默认为0表示查询所有类型的笔记
     * @return boolean类型，表示指定id的笔记是否存在于ContentProvider中
     */
    public static boolean visibleInNoteDatabase(ContentResolver resolver, long noteId, int type) {
        // 查询ContentProvider中指定id的笔记是否存在，并返回查询结果Cursor对象
        Cursor cursor = resolver.query(ContentUris.withAppendedId(Notes.CONTENT_NOTE_URI, noteId),
                null,
                NoteColumns.TYPE + "=? AND " + NoteColumns.PARENT_ID + "<>" + Notes.ID_TRASH_FOLER,
                new String [] {String.valueOf(type)},
                null);

        boolean exist = false;
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                exist = true;
            }
            // 关闭游标
            cursor.close();
        }
        // 返回指定id的笔记是否存在于ContentProvider中
        return exist;
    }


    /**
     * 查询指定id的笔记是否存在于ContentProvider中
     *
     * @param resolver ContentResolver对象
     * @param noteId   需要查询的笔记id
     * @return boolean类型，表示指定id的笔记是否存在于ContentProvider中
     */
    public static boolean existInNoteDatabase(ContentResolver resolver, long noteId) {
        // 查询ContentProvider中指定id的笔记是否存在，并返回查询结果Cursor对象
        Cursor cursor = resolver.query(ContentUris.withAppendedId(Notes.CONTENT_NOTE_URI, noteId),
                null, null, null, null);

        boolean exist = false;
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                exist = true;
            }
            // 关闭游标
            cursor.close();
        }
        // 返回指定id的笔记是否存在于ContentProvider中
        return exist;
    }

    /**
     * 查询指定id的数据是否存在于ContentProvider中
     *
     * @param resolver ContentResolver对象
     * @param dataId   需要查询的数据id
     * @return boolean类型，表示指定id的数据是否存在于ContentProvider中
     */
    public static boolean existInDataDatabase(ContentResolver resolver, long dataId) {
        // 查询ContentProvider中指定id的数据是否存在，并返回查询结果Cursor对象
        Cursor cursor = resolver.query(ContentUris.withAppendedId(Notes.CONTENT_DATA_URI, dataId),
                null, null, null, null);

        boolean exist = false;
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                exist = true;
            }
            // 关闭游标
            cursor.close();
        }
        // 返回指定id的数据是否存在于ContentProvider中
        return exist;
    }

    /**
     * 检查指定名称的文件夹是否存在于ContentProvider中
     *
     * @param resolver ContentResolver对象
     * @param name     需要检查的文件夹名称
     * @return boolean类型，表示指定名称的文件夹是否存在于ContentProvider中
     */
    public static boolean checkVisibleFolderName(ContentResolver resolver, String name) {
        // 查询ContentProvider中指定名称的文件夹是否存在，并返回查询结果Cursor对象
        Cursor cursor = resolver.query(Notes.CONTENT_NOTE_URI, null,
                NoteColumns.TYPE + "=" + Notes.TYPE_FOLDER +
                        " AND " + NoteColumns.PARENT_ID + "<>" + Notes.ID_TRASH_FOLER +
                        " AND " + NoteColumns.SNIPPET + "=?",
                new String[] { name }, null);
        boolean exist = false;
        if(cursor != null) {
            if(cursor.getCount() > 0) {
                exist = true;
            }
            // 关闭游标
            cursor.close();
        }
        // 返回指定名称的文件夹是否存在于ContentProvider中
        return exist;
    }

    /**
     * 获取指定文件夹下的所有Widget信息
     *
     * @param resolver ContentResolver对象
     * @param folderId 需要查询的文件夹id
     * @return HashSet<AppWidgetAttribute>类型，表示指定文件夹下的所有Widget信息
     */
    public static HashSet<AppWidgetAttribute> getFolderNoteWidget(ContentResolver resolver, long folderId) {
        // 查询ContentProvider中指定文件夹下的所有Widget信息，并返回查询结果Cursor对象
        Cursor c = resolver.query(Notes.CONTENT_NOTE_URI,
                new String[] { NoteColumns.WIDGET_ID, NoteColumns.WIDGET_TYPE },
                NoteColumns.PARENT_ID + "=?",
                new String[] { String.valueOf(folderId) },
                null);

        HashSet<AppWidgetAttribute> set = null;
        if (c != null) {
            if (c.moveToFirst()) {
                set = new HashSet<>();
                do {
                    try {
                        // 从Cursor对象中获取查询结果中Widget的id和类型，并将其封装到AppWidgetAttribute对象中
                        AppWidgetAttribute widget = new AppWidgetAttribute();
                        widget.widgetId = c.getInt(0);
                        widget.widgetType = c.getInt(1);
                        set.add(widget);
                    } catch (IndexOutOfBoundsException e) {
                        Log.e(TAG, e.toString());
                    }
                } while (c.moveToNext());
            }
            // 关闭游标
            c.close();
        }
        // 返回指定文件夹下的所有Widget信息
        return set;
    }


    /**
     * 根据笔记id获取该笔记对应的电话号码
     *
     * @param resolver ContentResolver对象
     * @param noteId   需要查询的笔记id
     * @return String类型，表示该笔记对应的电话号码
     */
    public static String getCallNumberByNoteId(ContentResolver resolver, long noteId) {
        // 查询ContentProvider中指定笔记id对应的电话号码，并返回查询结果Cursor对象
        Cursor cursor = resolver.query(Notes.CONTENT_DATA_URI,
                new String [] { CallNote.PHONE_NUMBER },
                CallNote.NOTE_ID + "=? AND " + CallNote.MIME_TYPE + "=?",
                new String [] { String.valueOf(noteId), CallNote.CONTENT_ITEM_TYPE },
                null);

        if (cursor != null && cursor.moveToFirst()) {
            try {
                return cursor.getString(0);
            } catch (IndexOutOfBoundsException e) {
                Log.e(TAG, "Get call number fails " + e);
            } finally {
                // 关闭游标
                cursor.close();
            }
        }
        // 如果查询失败则返回空字符串
        return "";
    }

    /**
     * 根据电话号码和通话日期获取该通话对应的笔记id
     *
     * @param resolver     ContentResolver对象
     * @param phoneNumber  需要查询的电话号码
     * @param callDate     需要查询的通话日期
     * @return long类型，表示该通话对应的笔记id
     */
    public static long getNoteIdByPhoneNumberAndCallDate(ContentResolver resolver, String phoneNumber, long callDate) {
        // 查询ContentProvider中指定电话号码和通话日期对应的笔记id，并返回查询结果Cursor对象
        Cursor cursor = resolver.query(Notes.CONTENT_DATA_URI,
                new String [] { CallNote.NOTE_ID },
                CallNote.CALL_DATE + "=? AND " + CallNote.MIME_TYPE + "=? AND PHONE_NUMBERS_EQUAL("
                        + CallNote.PHONE_NUMBER + ",?)",
                new String [] { String.valueOf(callDate), CallNote.CONTENT_ITEM_TYPE, phoneNumber },
                null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                try {
                    return cursor.getLong(0);
                } catch (IndexOutOfBoundsException e) {
                    Log.e(TAG, "Get call note id fails " + e);
                }
            }
            // 关闭游标
            cursor.close();
        }
        // 如果查询失败则返回0
        return 0;
    }


    /**
     * 根据笔记id获取该笔记的摘要信息
     *
     * @param resolver ContentResolver对象
     * @param noteId   需要查询的笔记id
     * @return String类型，表示该笔记的摘要信息
     */
    public static String getSnippetById(ContentResolver resolver, long noteId) {
        // 查询ContentProvider中指定笔记id对应的摘要信息，并返回查询结果Cursor对象
        Cursor cursor = resolver.query(Notes.CONTENT_NOTE_URI,
                new String [] { NoteColumns.SNIPPET },
                NoteColumns.ID + "=?",
                new String [] { String.valueOf(noteId)},
                null);

        if (cursor != null) {
            String snippet = "";
            if (cursor.moveToFirst()) {
                snippet = cursor.getString(0);
            }
            // 关闭游标
            cursor.close();
            return snippet;
        }
        // 如果查询失败则抛出异常
        throw new IllegalArgumentException("Note is not found with id: " + noteId);
    }

    /**
     * 对给定的摘要信息进行格式化
     *
     * @param snippet 需要格式化的摘要信息
     * @return String类型，表示格式化后的摘要信息
     */
    public static String getFormattedSnippet(String snippet) {
        if (snippet != null) {
            snippet = snippet.trim();	// 去除字符串两端的空格
            int index = snippet.indexOf('\n');	// 查找第一个换行符位置
            if (index != -1) {
                snippet = snippet.substring(0, index);	// 截取换行符之前的部分作为摘要信息
            }
        }
        return snippet;
    }

}
