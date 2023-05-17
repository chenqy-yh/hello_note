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

import android.appwidget.AppWidgetManager;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

import net.micode.notes.data.Notes;
import net.micode.notes.data.Notes.CallNote;
import net.micode.notes.data.Notes.DataColumns;
import net.micode.notes.data.Notes.DataConstants;
import net.micode.notes.data.Notes.NoteColumns;
import net.micode.notes.data.Notes.TextNote;
import net.micode.notes.tool.ResourceParser.NoteBgResources;


public class WorkingNote {
    // Note for the working note
    private Note mNote;
    // Note Id
    private long mNoteId;
    // Note content
    private String mContent;
    // Note mode
    private int mMode;

    private long mAlertDate;

    private long mModifiedDate;

    private int mBgColorId;

    private int mWidgetId;

    private int mWidgetType;

    private long mFolderId;

    private Context mContext;

    private static final String TAG = "WorkingNote";

    private boolean mIsDeleted;

    private NoteSettingChangedListener mNoteSettingStatusListener;

    public static final String[] DATA_PROJECTION = new String[] {
            DataColumns.ID,                // 表示数据行的 ID
            DataColumns.CONTENT,           // 表示数据行的内容
            DataColumns.MIME_TYPE,         // 表示数据行的 MIME 类型
            DataColumns.DATA1,             // 表示数据行的附加数据
            DataColumns.DATA2,             // 表示数据行的附加数据
            DataColumns.DATA3,             // 表示数据行的附加数据
            DataColumns.DATA4,             // 表示数据行的附加数据
            DataColumns.DATA5,             // 表示是否为同步数据
    };
    public static final String[] NOTE_PROJECTION = new String[] {
            NoteColumns.PARENT_ID,      // 表示笔记的父笔记 ID
            NoteColumns.ALERTED_DATE,   // 表示笔记的提醒时间
            NoteColumns.BG_COLOR_ID,    // 表示笔记的背景颜色 ID
            NoteColumns.WIDGET_ID,      // 表示笔记的小部件 ID
            NoteColumns.WIDGET_TYPE,    // 表示笔记的小部件类型
            NoteColumns.MODIFIED_DATE   // 表示笔记的修改时间
    };

    private static final int DATA_ID_COLUMN = 0;

    private static final int DATA_CONTENT_COLUMN = 1;

    private static final int DATA_MIME_TYPE_COLUMN = 2;

    private static final int DATA_MODE_COLUMN = 3;

    private static final int NOTE_PARENT_ID_COLUMN = 0;

    private static final int NOTE_ALERTED_DATE_COLUMN = 1;

    private static final int NOTE_BG_COLOR_ID_COLUMN = 2;

    private static final int NOTE_WIDGET_ID_COLUMN = 3;

    private static final int NOTE_WIDGET_TYPE_COLUMN = 4;

    private static final int NOTE_MODIFIED_DATE_COLUMN = 5;

    // New note construct
    /**
     * 构造方法，创建一个新的 WorkingNote 对象。
     * @param context 上下文对象
     * @param folderId 笔记所属文件夹的 ID
     */
    private WorkingNote(Context context, long folderId) {
        // 初始化成员变量
        mContext = context;                     // 上下文对象
        mAlertDate = 0;                         // 提醒时间
        mModifiedDate = System.currentTimeMillis();  // 修改时间，初始化为当前时间戳
        mFolderId = folderId;                   // 笔记所属文件夹的 ID
        mNote = new Note();                     // Note 对象
        mNoteId = 0;                            // 笔记 ID，初始值为 0
        mIsDeleted = false;                     // 是否已删除，初始值为 false
        mMode = 0;                              // 模式，初始值为 0
        mWidgetType = Notes.TYPE_WIDGET_INVALIDE;   // 小部件类型，初始值为无效类型
    }


    // Existing note construct
    /**
     * 构造方法，创建一个新的 WorkingNote 对象。
     * @param context 上下文对象
     * @param noteId 笔记的ID
     * @param folderId 笔记所属文件夹的 ID
     */
    private WorkingNote(Context context, long noteId, long folderId) {
        // 初始化成员变量
        mContext = context;                     // 上下文对象
        mNoteId = noteId;                       // 笔记 ID
        mFolderId = folderId;                   // 笔记所属文件夹的 ID
        mIsDeleted = false;                     // 是否已删除，初始值为 false
        mNote = new Note();                     // Note 对象
        loadNote();                             // 加载笔记数据
    }


    /**
     * 从内容提供程序中使用当前笔记 ID 加载笔记的详细信息。
     * 查询 CONTENT_NOTE_URI，检索与指定笔记 ID 相对应的记录。
     * 如果游标不为空并且可以将其移动到第一个结果，则结果将填充 mFolderId、mBgColorId、
     * mWidgetId、mWidgetType、mAlertDate 和 mModifiedDate 变量。
     * 然后关闭游标并调用 loadNoteData() 方法以加载笔记剩余部分的数据。
     * 如果没有检索到任何记录，则在打印错误日志后抛出 IllegalArgumentException 异常。
     */
    private void loadNote() {
        Cursor cursor = mContext.getContentResolver().query(
                ContentUris.withAppendedId(Notes.CONTENT_NOTE_URI, mNoteId),
                NOTE_PROJECTION,
                null,
                null,
                null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                // 结果将填充以下变量：mFolderId、mBgColorId、mWidgetId、mWidgetType、mAlertDate 和 mModifiedDate
                mFolderId = cursor.getLong(NOTE_PARENT_ID_COLUMN);
                mBgColorId = cursor.getInt(NOTE_BG_COLOR_ID_COLUMN);
                mWidgetId = cursor.getInt(NOTE_WIDGET_ID_COLUMN);
                mWidgetType = cursor.getInt(NOTE_WIDGET_TYPE_COLUMN);
                mAlertDate = cursor.getLong(NOTE_ALERTED_DATE_COLUMN);
                mModifiedDate = cursor.getLong(NOTE_MODIFIED_DATE_COLUMN);
            }
            cursor.close();
        } else {
            // 没有检索到任何记录，则在打印错误日志后抛出 IllegalArgumentException 异常
            Log.e(TAG, "No note with id:" + mNoteId);
            throw new IllegalArgumentException("Unable to find note with id " + mNoteId);
        }
        // 加载笔记剩余部分的数据
        loadNoteData();
    }


    /**
     * 加载笔记数据。
     *
     * 该方法用于从Content Provider中加载当前Note对象的数据。它会查询Notes.CONTENT_DATA_URI，条件为
     * DataColumns.NOTE_ID等于mNoteId，如果存在符合条件的数据，则将其加载到Note对象中。
     * 如果在查询时出现错误（例如Content Provider不存在或无法工作、指定的笔记ID无效等），则会抛出IllegalArgumentException异常。
     */
    private void loadNoteData() {
        Cursor cursor = mContext.getContentResolver().query(Notes.CONTENT_DATA_URI, DATA_PROJECTION,
                DataColumns.NOTE_ID + "=?", new String[] { String.valueOf(mNoteId) }, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                // 遍历所有数据并将它们加载到Note对象中。
                do {
                    String type = cursor.getString(DATA_MIME_TYPE_COLUMN);
                    if (DataConstants.NOTE.equals(type)) {
                        mContent = cursor.getString(DATA_CONTENT_COLUMN);
                        mMode = cursor.getInt(DATA_MODE_COLUMN);
                        mNote.setTextDataId(cursor.getLong(DATA_ID_COLUMN));
                    } else if (DataConstants.CALL_NOTE.equals(type)) {
                        mNote.setCallDataId(cursor.getLong(DATA_ID_COLUMN));
                    } else {
                        Log.d(TAG, "Wrong note type with type:" + type);
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
        } else {
            // 如果未找到任何数据，则抛出异常。
            Log.e(TAG, "No data with id:" + mNoteId);
            throw new IllegalArgumentException("Unable to find note's data with id " + mNoteId);
        }
    }


    /**
     * 创建一个空的笔记对象。
     *
     * 该方法用于创建一个空白的WorkingNote对象，并设置其初始值。它接受context、folderId、widgetId、
     * widgetType和defaultBgColorId等参数作为输入，并返回一个新的WorkingNote对象。
     *
     * @param context           上下文环境
     * @param folderId          笔记所在文件夹的ID
     * @param widgetId          笔记所绑定的Widget的ID（如果有）
     * @param widgetType        笔记所绑定的Widget的类型（例如便签、待办事项等）
     * @param defaultBgColorId  笔记默认的背景颜色ID
     * @return                  返回一个新的WorkingNote对象
     */
    public static WorkingNote createEmptyNote(Context context, long folderId, int widgetId,
                                              int widgetType, int defaultBgColorId) {
        // 创建一个空白的WorkingNote对象
        WorkingNote note = new WorkingNote(context, folderId);
        // 设置笔记的默认背景颜色
        note.setBgColorId(defaultBgColorId);
        // 设置笔记所绑定的Widget的ID
        note.setWidgetId(widgetId);
        // 设置笔记所绑定的Widget的类型
        note.setWidgetType(widgetType);
        // 返回一个新的WorkingNote对象
        return note;
    }


    /**
     * 从数据库中加载一个WorkingNote对象。
     *
     * 该方法用于从数据库中加载一个指定ID的WorkingNote对象。它接受context和id等参数作为输入，并返回
     * 一个新的WorkingNote对象。
     *
     * @param context   上下文环境
     * @param id        待加载的WorkingNote对象的ID
     * @return          返回一个新的WorkingNote对象
     */
    public static WorkingNote load(Context context, long id) {
        // 创建一个新的WorkingNote对象，并将id和0作为参数传入构造函数中
        return new WorkingNote(context, id, 0);
    }


    /**
     * 保存笔记对象。
     *
     * 该方法用于将当前WorkingNote对象（如果有必要）保存到数据库中。如果该笔记尚未存在于数据库中，
     * 则会先创建一个新的笔记记录。它还会更新绑定在该笔记上的Widget内容（如果该笔记绑定了Widget）。
     *
     * @return  如果笔记成功保存，则返回true；否则返回false
     */
    public synchronized boolean saveNote() {
        // 检查该笔记是否值得被保存
        if (isWorthSaving()) {
            // 如果该笔记尚未存在于数据库中，则需要先创建一个新的笔记记录。
            if (!existInDatabase()) {
                // 获取一个新的笔记ID
                if ((mNoteId = Note.getNewNoteId(mContext, mFolderId)) == 0) {
                    Log.e(TAG, "Create new note fail with id:" + mNoteId);
                    return false;
                }
            }

            // 将当前WorkingNote对象同步到Note对象中
            mNote.syncNote(mContext, mNoteId);

            /**
             * 如果该笔记绑定了Widget，则需要更新Widget的内容。
             */
            if (mWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID
                    && mWidgetType != Notes.TYPE_WIDGET_INVALIDE
                    && mNoteSettingStatusListener != null) {
                mNoteSettingStatusListener.onWidgetChanged();
            }

            return true;
        } else {
            return false;
        }
    }


    /**
     * 检查当前WorkingNote对象是否存在于数据库中。
     *
     * 该方法用于检查当前WorkingNote对象是否已经存在于数据库中。如果该笔记已经保存过，则existInDatabase()
     * 方法会返回true；否则返回false。
     *
     * @return  如果笔记已经存在于数据库中，则返回true；否则返回false
     */
    public boolean existInDatabase() {
        return mNoteId > 0;
    }

    /**
     * 判断笔记是否值得被保存。
     *
     * 该方法用于判断当前WorkingNote对象是否值得被保存到数据库中。它会根据笔记内容、是否被删除以及
     * 是否已经保存到数据库等因素做出判断。如果该笔记不需要被保存，则isWorthSaving()方法会返回
     * false；否则返回true。
     *
     * @return  如果笔记值得被保存到数据库中，则返回true；否则返回false
     */
    private boolean isWorthSaving() {
        if (mIsDeleted || (!existInDatabase() && TextUtils.isEmpty(mContent))
                || (existInDatabase() && !mNote.isLocalModified())) {
            // 如果该笔记已经被标记为“已删除”，或者它既没有保存到数据库中，也没有任何数据，或者它已经存在于
            // 数据库中但没有被修改过，则认为该笔记不需要被保存。
            return false;
        } else {
            return true;
        }
    }


    /**
     * 设置笔记设置状态更改监听器。
     *
     * 该方法用于设置一个NoteSettingChangedListener对象，以便能够在笔记设置状态发生更改时接收通知。
     *
     * @param l     笔记设置状态更改监听器
     */
    public void setOnSettingStatusChangedListener(NoteSettingChangedListener l) {
        mNoteSettingStatusListener = l;
    }


    /**
     * 设置笔记提醒日期。
     *
     * 该方法用于为当前WorkingNote对象设置一个提醒日期。它接受date和set两个参数作为输入，分别表示
     * 待设置的提醒日期和是否需要进行设置。如果待设置的提醒日期与当前笔记的提醒日期不同，则会将其更新到
     * Note对象中，并通知设置状态更改监听器。
     *
     * @param date  待设置的提醒日期
     * @param set   是否需要进行设置
     */
    public void setAlertDate(long date, boolean set) {
        if (date != mAlertDate) {
            // 如果待设置的提醒日期与当前笔记的提醒日期不同，则需要更新Note对象中的数据。
            mAlertDate = date;
            mNote.setNoteValue(NoteColumns.ALERTED_DATE, String.valueOf(mAlertDate));
        }
        // 通知设置状态更改监听器。
        if (mNoteSettingStatusListener != null) {
            mNoteSettingStatusListener.onClockAlertChanged(date, set);
        }
    }


    /**
     * 标记笔记是否被删除。
     *
     * 该方法用于标记当前WorkingNote对象是否已经被删除。它接受一个mark参数作为输入，如果mark为true，
     * 则表示该笔记已经被删除，否则表示该笔记未被删除。在标记笔记删除状态的同时，该方法还会检查
     * 是否存在与该笔记绑定的Widget，如果有，则需要更新Widget的内容。
     *
     * @param mark  是否将当前WorkingNote对象标记为“已删除”
     */
    public void markDeleted(boolean mark) {
        // 标记当前WorkingNote对象是否已经被删除。
        mIsDeleted = mark;
        // 检查是否存在与该笔记绑定的Widget，如果有，则需要更新Widget的内容。
        if (mWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID
                && mWidgetType != Notes.TYPE_WIDGET_INVALIDE
                && mNoteSettingStatusListener != null) {
            mNoteSettingStatusListener.onWidgetChanged();
        }
    }


    /**
     * 设置笔记的背景颜色ID。
     *
     * 该方法用于为当前WorkingNote对象设置一个新的背景颜色ID。它接受一个id参数作为输入，表示待设置
     * 的背景颜色ID。如果待设置的背景颜色ID与当前笔记的背景颜色ID不同，则会将其更新到Note对象中，
     * 并通知设置状态更改监听器。
     *
     * @param id    待设置的背景颜色ID
     */
    public void setBgColorId(int id) {
        if (id != mBgColorId) {
            // 如果待设置的背景颜色ID与当前笔记的背景颜色ID不同，则需要更新Note对象中的数据。
            mBgColorId = id;
            mNote.setNoteValue(NoteColumns.BG_COLOR_ID, String.valueOf(id));
            // 通知设置状态更改监听器。
            if (mNoteSettingStatusListener != null) {
                mNoteSettingStatusListener.onBackgroundColorChanged();
            }
        }
    }

    /**
     * 设置笔记的清单模式。
     *
     * 该方法用于设置当前WorkingNote对象的清单模式。它接受一个mode参数作为输入，表示待设置的清单模式。
     * 如果当前笔记的清单模式与待设置的清单模式不同，则会将其更新到Note对象中，并通知设置状态更改监听器。
     *
     * @param mode  待设置的清单模式
     */
    public void setCheckListMode(int mode) {
        if (mMode != mode) {
            // 如果当前笔记的清单模式与待设置的清单模式不同，则需要更新Note对象中的数据。
            mMode = mode;
            mNote.setTextData(TextNote.MODE, String.valueOf(mMode));
            // 通知设置状态更改监听器。
            if (mNoteSettingStatusListener != null) {
                mNoteSettingStatusListener.onCheckListModeChanged(mMode, mode);
            }
        }
    }

    /**
     * 设置笔记绑定的Widget类型。
     *
     * 该方法用于设置当前WorkingNote对象绑定的Widget类型。它接受一个type参数作为输入，表示待设置的
     * Widget类型。如果当前笔记绑定的Widget类型与待设置的Widget类型不同，则会将其更新到Note对象中。
     *
     * @param type  待设置的Widget类型
     */
    public void setWidgetType(int type) {
        if (type != mWidgetType) {
            // 如果当前笔记绑定的Widget类型与待设置的Widget类型不同，则需要更新Note对象中的数据。
            mWidgetType = type;
            mNote.setNoteValue(NoteColumns.WIDGET_TYPE, String.valueOf(mWidgetType));
        }
    }

    /**
     * 设置笔记绑定的Widget ID。
     *
     * 该方法用于设置当前WorkingNote对象绑定的Widget ID。它接受一个id参数作为输入，表示待设置的
     * Widget ID。如果当前笔记绑定的Widget ID与待设置的Widget ID不同，则会将其更新到Note对象中。
     *
     * @param id    待设置的Widget ID
     */
    public void setWidgetId(int id) {
        if (id != mWidgetId) {
            // 如果当前笔记绑定的Widget ID与待设置的Widget ID不同，则需要更新Note对象中的数据。
            mWidgetId = id;
            mNote.setNoteValue(NoteColumns.WIDGET_ID, String.valueOf(mWidgetId));
        }
    }


    /**
     * 设置笔记的文本内容。
     *
     * 该方法用于设置当前WorkingNote对象的文本内容。它接受一个text参数作为输入，表示待设置的文本内容。
     * 如果当前笔记的文本内容与待设置的文本内容不同，则会将其更新到Note对象中。
     *
     * @param text  待设置的文本内容
     */
    public void setWorkingText(String text) {
        if (!TextUtils.equals(mContent, text)) {
            // 如果当前笔记的文本内容与待设置的文本内容不同，则需要更新Note对象中的数据。
            mContent = text;
            mNote.setTextData(DataColumns.CONTENT, mContent);
        }
    }

    /**
     * 将笔记转换为通话记录。
     *
     * 该方法用于将当前WorkingNote对象转换为通话记录。它接受一个phoneNumber和callDate两个参数作为输入，
     * 分别表示通话记录的电话号码和通话日期。该方法会将笔记的类型设置为通话记录，并将相应的数据保存到Note对象中。
     *
     * @param phoneNumber   通话记录的电话号码
     * @param callDate      通话记录的通话日期
     */
    public void convertToCallNote(String phoneNumber, long callDate) {
        // 将笔记的类型设置为通话记录，并将相应的数据保存到Note对象中。
        mNote.setCallData(CallNote.CALL_DATE, String.valueOf(callDate));
        mNote.setCallData(CallNote.PHONE_NUMBER, phoneNumber);
        mNote.setNoteValue(NoteColumns.PARENT_ID, String.valueOf(Notes.ID_CALL_RECORD_FOLDER));
    }


    /**
     * 检查笔记是否设置了提醒。
     *
     * 该方法用于检查当前WorkingNote对象是否设置了提醒。如果已经设置了提醒，则返回true；否则返回false。
     *
     * @return  如果笔记设置了提醒，则返回true；否则返回false
     */
    public boolean hasClockAlert() {
        return (mAlertDate > 0 ? true : false);
    }

    /**
     * 获取笔记的文本内容。
     *
     * 该方法用于获取当前WorkingNote对象的文本内容。
     *
     * @return  当前WorkingNote对象的文本内容
     */
    public String getContent() {
        return mContent;
    }

    /**
     * 获取笔记的提醒日期。
     *
     * 该方法用于获取当前WorkingNote对象的提醒日期。
     *
     * @return  当前WorkingNote对象的提醒日期
     */
    public long getAlertDate() {
        return mAlertDate;
    }

    /**
     * 获取笔记的修改日期。
     *
     * 该方法用于获取当前WorkingNote对象的修改日期。
     *
     * @return  当前WorkingNote对象的修改日期
     */
    public long getModifiedDate() {
        return mModifiedDate;
    }

    /**
     * 获取笔记的背景颜色资源ID。
     *
     * 该方法用于获取当前WorkingNote对象的背景颜色资源ID。它会根据当前笔记的背景颜色ID返回相应的颜色资源ID。
     *
     * @return  当前WorkingNote对象的背景颜色资源ID
     */
    public int getBgColorResId() {
        return NoteBgResources.getNoteBgResource(mBgColorId);
    }


    /**
     * 获取笔记的背景颜色ID。
     *
     * 该方法用于获取当前WorkingNote对象的背景颜色ID。
     *
     * @return  当前WorkingNote对象的背景颜色ID
     */
    public int getBgColorId() {
        return mBgColorId;
    }

    /**
     * 获取笔记标题的背景颜色资源ID。
     *
     * 该方法用于获取当前WorkingNote对象的标题栏背景颜色资源ID。它会根据当前笔记的背景颜色ID返回相应的颜色资源ID。
     *
     * @return  当前WorkingNote对象的标题栏背景颜色资源ID
     */
    public int getTitleBgResId() {
        return NoteBgResources.getNoteTitleBgResource(mBgColorId);
    }

    /**
     * 获取笔记的清单模式。
     *
     * 该方法用于获取当前WorkingNote对象的清单模式。
     *
     * @return  当前WorkingNote对象的清单模式
     */
    public int getCheckListMode() {
        return mMode;
    }

    /**
     * 获取笔记的ID。
     *
     * 该方法用于获取当前WorkingNote对象对应的Note对象的ID。
     *
     * @return  当前WorkingNote对象对应的Note对象的ID
     */
    public long getNoteId() {
        return mNoteId;
    }


    public long getFolderId() {
        return mFolderId;
    }

    public int getWidgetId() {
        return mWidgetId;
    }

    public int getWidgetType() {
        return mWidgetType;
    }

    public interface NoteSettingChangedListener {
        /**
         * 当前笔记的背景颜色刚刚更改时调用
         */
        void onBackgroundColorChanged();

        /**
         * 用户设置闹钟时调用
         * @param date 闹钟日期时间
         * @param set 是否开启闹钟
         */
        void onClockAlertChanged(long date, boolean set);

        /**
         * 从小部件创建笔记时调用
         */
        void onWidgetChanged();

        /**
         * 在勾选列表模式和正常模式之间切换时调用
         * @param oldMode 切换前的模式
         * @param newMode 新模式
         */
        void onCheckListModeChanged(int oldMode, int newMode);
    }

}
