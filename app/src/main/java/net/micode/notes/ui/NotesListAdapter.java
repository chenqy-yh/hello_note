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

package net.micode.notes.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

import android.widget.ImageView;
import net.micode.notes.data.Notes;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;


public class NotesListAdapter extends CursorAdapter {
    private static final String TAG = "NotesListAdapter";
    private Context mContext;
    private HashMap<Integer, Boolean> mSelectedIndex;
    private int mNotesCount;
    private boolean mChoiceMode;

    public NotesListAdapter(Context context, Cursor c) {
        super(context, c);
    }

    public static class AppWidgetAttribute {
        public int widgetId;
        public int widgetType;
    }

    public NotesListAdapter(Context context) {
        super(context, null);
        mSelectedIndex = new HashMap<>();
        mContext = context;
        mNotesCount = 0;
    }

    /**
     * 在列表视图中创建新项目的方法。
     *
     * @param context 上下文对象，表示当前应用程序的状态信息。
     * @param cursor  数据库游标对象，用于访问查询结果集中的行。
     * @param parent  列表视图的父级布局对象，用于在其中显示新项目。
     * @return 返回一个 NotesListItem 对象，该对象表示列表视图中的新项目。
     */
    @SuppressLint("Range")
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        NotesListItem notesListItem = new NotesListItem(context);
//        long note_id = cursor.getLong(cursor.getColumnIndex(Notes.NoteColumns.ID));
//        notesListItem.setOnPinListener(fire -> {
//            Cursor pin_query = context.getContentResolver().query(Notes.CONTENT_NOTE_URI, new String[]{Notes.NoteColumns.PIN}, Notes.NoteColumns.ID + " = ?", new String[]{String.valueOf(note_id)}, null);
//            if (pin_query != null) {
//                pin_query.moveToFirst();
//                int pin = pin_query.getInt(pin_query.getColumnIndex(Notes.NoteColumns.PIN));
//                if (pin == 1) {
//                    fire.setVisibility(View.VISIBLE);
//                    ((AnimationDrawable) fire.getBackground()).start();
//                } else {
//                    fire.setVisibility(View.GONE);
//                    ((AnimationDrawable) fire.getBackground()).stop();
//                }
//            }
//        });
        return notesListItem;
    }


    /**
     * 将数据绑定到列表视图中的每个项目的方法。
     *
     * @param view    表示当前项目的视图对象。对应 newView中返回的 NotesListItem 对象。
     * @param context 上下文对象，表示当前应用程序的状态信息。
     * @param cursor  数据库游标对象，用于访问查询结果集中的行。
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        if (view instanceof NotesListItem) {
            // 使用 NoteItemData 类来获取要填充到视图中的数据
            NoteItemData itemData = new NoteItemData(context, cursor);
            // 将数据填充到 List Item View 中
            ((NotesListItem) view).bind(context, itemData, mChoiceMode,
                    isSelectedItem(cursor.getPosition()));
        }
    }


    /**
     * 当内容发生变化时被调用的方法。
     */
    @Override
    protected void onContentChanged() {
        super.onContentChanged();
        calcNotesCount();
    }

    /**
     * 在更改 Cursor 对象时被调用的方法。
     *
     * @param cursor 表示新数据集的游标对象。
     */
    @Override
    public void changeCursor(Cursor cursor) {
        super.changeCursor(cursor);
        calcNotesCount();
    }


    /**
     * 将指定位置的列表项标记为已选或未选状态。
     *
     * @param position 列表项的位置。
     * @param checked  是否选中该列表项。
     */
    public void setCheckedItem(final int position, final boolean checked) {
        // 将指定位置的列表项的选中状态更新到 mSelectedIndex 中
        mSelectedIndex.put(position, checked);
        // 通知适配器数据集发生了变化
        notifyDataSetChanged();
    }


    /**
     * 检查当前是否处于选择模式。
     *
     * @return 如果当前处于选择模式，则返回 true，否则返回 false。
     */
    public boolean isInChoiceMode() {
        return mChoiceMode;
    }

    /**
     * 设置选择模式。
     *
     * @param mode 要设置的选择模式。如果为 true，则表示启用选择模式；否则停用选择模式。
     */
    public void setChoiceMode(boolean mode) {
        // 清空已选项目列表
        mSelectedIndex.clear();
        // 更新选择模式
        mChoiceMode = mode;
    }

    /**
     * 选择或取消选择所有可编辑项目。
     *
     * @param checked 是否选择所有项目。
     */
    public void selectAll(boolean checked) {
        Cursor cursor = getCursor();
        for (int i = 0; i < getCount(); i++) {
            if (cursor.moveToPosition(i)) {
                // 检查当前项目是否可以编辑（即是否为笔记类型）
                if (NoteItemData.getNoteType(cursor) == Notes.TYPE_NOTE) {
                    // 将当前项目标记为选中或未选中状态
                    setCheckedItem(i, checked);
                }
            }
        }
    }

    /**
     * 获取当前已选项目的 ID 集合。
     *
     * @return 返回一个包含已选项目 ID 的 HashSet。
     */
    public HashSet<Long> getSelectedItemIds() {
        HashSet<Long> itemSet = new HashSet<>();
        // 遍历已选项目列表，并将所有已选项目的 ID 添加到 HashSet 中
        for (Integer position : mSelectedIndex.keySet()) {
            if (Boolean.TRUE.equals(mSelectedIndex.get(position))) {
                long id = getItemId(position);
                if (id == Notes.ID_ROOT_FOLDER) {
                    Log.d(TAG, "Wrong item id, should not happen");
                } else {
                    itemSet.add(id);
                }
            }
        }

        return itemSet;
    }

    /**
     * 获取当前已选项目的小部件信息集合。
     *
     * @return 返回一个包含已选项目小部件信息的 HashSet。
     */
    public HashSet<AppWidgetAttribute> getSelectedWidget() {
        HashSet<AppWidgetAttribute> itemSet = new HashSet<>();
        // 遍历已选项目列表，并将所有已选项目的小部件信息添加到 HashSet 中
        for (Integer position : mSelectedIndex.keySet()) {
            if (Boolean.TRUE.equals(mSelectedIndex.get(position))) {
                Cursor c = (Cursor) getItem(position);
                if (c != null) {
                    AppWidgetAttribute widget = new AppWidgetAttribute();
                    NoteItemData item = new NoteItemData(mContext, c);
                    widget.widgetId = item.getWidgetId();
                    widget.widgetType = item.getWidgetType();
                    itemSet.add(widget);
                } else {
                    Log.e(TAG, "Invalid cursor");
                    return null;
                }
            }
        }
        return itemSet;
    }


    /**
     * 获取已选项目的数量。
     *
     * @return 返回已选项目的数量。
     */
    public int getSelectedCount() {
        Collection<Boolean> values = mSelectedIndex.values();
        Iterator<Boolean> iter = values.iterator();
        int count = 0;
        while (iter.hasNext()) {
            if (iter.next()) {
                count++;
            }
        }
        return count;
    }

    /**
     * 检查是否已经选择了所有项目。
     *
     * @return 如果所有项目都已经被选中，则返回 true，否则返回 false。
     */
    public boolean isAllSelected() {
        int checkedCount = getSelectedCount();
        return (checkedCount != 0 && checkedCount == mNotesCount);
    }

    /**
     * 检查指定位置的项目是否已被选中。
     *
     * @param position 要检查其选中状态的项目位置。
     * @return 如果该项目已被选中，则返回 true，否则返回 false。
     */
    public boolean isSelectedItem(final int position) {
        if (null == mSelectedIndex.get(position)) {
            return false;
        }
        return Boolean.TRUE.equals(mSelectedIndex.get(position));
    }


    /**
     * 计算列表视图中笔记项目的数量。
     */
    private void calcNotesCount() {
        mNotesCount = 0;
        int totalCount = getCount();
        for (int i = 0; i < totalCount; i++) {
            Cursor c = (Cursor) getItem(i);
            if (c != null) {
                int noteType = NoteItemData.getNoteType(c);
                if (noteType == Notes.TYPE_NOTE) {
                    mNotesCount++;
                }
            } else {
                Log.e(TAG, "Invalid cursor");
                return;
            }
        }
    }

}
