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
import android.app.Activity;
import android.content.Context;
import android.text.format.DateUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.micode.notes.R;
import net.micode.notes.data.Notes;
import net.micode.notes.tool.DataUtils;
import net.micode.notes.tool.ResourceParser.NoteItemBgResources;


public class NotesListItem extends LinearLayout {
    private final ImageView mAlert;
    private final TextView mTitle;
    private final TextView mTime;
    private final TextView mCallName;
    private NoteItemData mItemData;
    private final CheckBox mCheckBox;
    private PinFireImagView pin_fire;



    public NotesListItem(Context context) {
        super(context);
        inflate(context, R.layout.note_item, this);
        mAlert = findViewById(R.id.iv_alert_icon);
        mTitle = findViewById(R.id.tv_title);
        mTime = findViewById(R.id.tv_time);
        mCallName = findViewById(R.id.tv_name);
        mCheckBox = findViewById(android.R.id.checkbox);
        pin_fire = findViewById(R.id.pin_fire);
        // 设置笔记项之间的间隙
        int marginBottom = getResources().getDimensionPixelSize(R.dimen.note_item_margin_bottom);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 0, 0, marginBottom);
        setLayoutParams(layoutParams);

    }


    /**
     * 将NoteItemData对象绑定到ViewHolder中对应的UI元素。
     *
     * @param context    调用此方法的Activity或Fragment的上下文。
     * @param data       包含笔记项信息的NoteItemData对象。
     * @param choiceMode 表示RecyclerView是否处于选择 模式的布尔值。
     * @param checked    表示当前项目在选择模式下是否已选中的布尔值。
     */
    public void bind(Context context, NoteItemData data, boolean choiceMode, boolean checked) {
        // 根据RecyclerView是否处于选择模式以及笔记项类型，显示或隐藏复选框。
        if (choiceMode && data.getType() == Notes.TYPE_NOTE) {
            mCheckBox.setVisibility(View.VISIBLE);
            mCheckBox.setChecked(checked);
        } else {
            mCheckBox.setVisibility(View.GONE);
        }
        // 将笔记项数据设置为传入的NoteItemData对象。
        mItemData = data;
        // 如果笔记项是通话记录文件夹，
        if (data.getId() == Notes.ID_CALL_RECORD_FOLDER) {
            mCallName.setVisibility(View.GONE); // 隐藏通话名称TextView。
            mAlert.setVisibility(View.VISIBLE); // 显示警报图标。
            mTitle.setTextAppearance(context, R.style.TextAppearancePrimaryItem); // 将标题样式设置为主要项样式。
            mTitle.setText(context.getString(R.string.call_record_folder_name) + context.getString(R.string.format_folder_files_count, data.getNotesCount()));
            mAlert.setImageResource(R.drawable.call_record); // 将警报图标设置为电话记录图标。
        } else if (data.getParentId() == Notes.ID_CALL_RECORD_FOLDER) { // 如果笔记项是通话记录文件夹的子项，
            mCallName.setVisibility(View.VISIBLE); // 显示通话名称TextView。
            mCallName.setText(data.getCallName()); // 将通话名称文本设置为呼叫者的姓名。
            mTitle.setTextAppearance(context, R.style.TextAppearanceSecondaryItem); // 将标题样式设置为次要项样式。
            mTitle.setText(DataUtils.getFormattedSnippet(data.getSnippet())); // 将标题文本设置为笔记项的格式化片段。
            if (data.hasAlert()) { // 如果笔记项有警报，
                mAlert.setImageResource(R.drawable.clock); // 将警报图标设置为时钟图标。
                mAlert.setVisibility(View.VISIBLE); // 显示警报图标。
            } else {
                mAlert.setVisibility(View.GONE); // 否则隐藏警报图标。
            }
        } else { // 如果笔记项既不是通话记录文件夹，也不是通话记录文件夹的子项，
            mCallName.setVisibility(View.GONE); // 隐藏通话名称TextView。
            mTitle.setTextAppearance(context, R.style.TextAppearancePrimaryItem); // 将标题样式设置为主要项样式。

            // 如果笔记项是文件夹，
            if (data.getType() == Notes.TYPE_FOLDER) {
                mTitle.setText(data.getSnippet() // 设置标题文本为文件夹名称和文件夹中文件数量。
                        + context.getString(R.string.format_folder_files_count, data.getNotesCount()));
                mAlert.setVisibility(View.GONE); // 隐藏警报图标。
            } else { // 如果笔记项不是文件夹，说明是笔记
                pin_fire.setTag(data.getId());
                pin_fire.run();
                mTitle.setText(DataUtils.getFormattedSnippet(data.getSnippet())); // 将标题文本设置为笔记项的格式化片段。
                if (data.hasAlert()) { // 如果笔记项有警报，
                    mAlert.setImageResource(R.drawable.clock); // 将警报图标设置为时钟图标。
                    mAlert.setVisibility(View.VISIBLE); // 显示警报图标。
                } else {
                    mAlert.setVisibility(View.GONE); // 否则隐藏警报图标。
                }


            }
        }
        // 设置时间文本为笔记项修改日期的相对时间跨度。
        mTime.setText(DateUtils.getRelativeTimeSpanString(data.getModifiedDate()));
        setBackground(data);
    }

    private void setBackground(NoteItemData data) {
        int id = data.getBgColorId();
        if (data.getType() == Notes.TYPE_NOTE) {
            if (data.isSingle() || data.isOneFollowingFolder()) {
                setBackgroundResource(NoteItemBgResources.getNoteBgSingleRes(id));
            } else if (data.isLast()) {
                setBackgroundResource(NoteItemBgResources.getNoteBgLastRes(id));
            } else if (data.isFirst() || data.isMultiFollowingFolder()) {
                setBackgroundResource(NoteItemBgResources.getNoteBgFirstRes(id));
            } else {
                setBackgroundResource(NoteItemBgResources.getNoteBgNormalRes(id));
            }
        } else {
            setBackgroundResource(NoteItemBgResources.getFolderBgRes());
        }
    }

    public NoteItemData getItemData() {
        return mItemData;
    }



}
