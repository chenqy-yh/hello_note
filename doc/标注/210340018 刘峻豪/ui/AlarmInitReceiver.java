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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import net.micode.notes.data.Notes;
import net.micode.notes.data.Notes.NoteColumns;


public class AlarmInitReceiver extends BroadcastReceiver {

    /**
     * 查询提醒表中所有已提醒过的笔记
     */
    private static final String[] PROJECTION = new String[]{
            NoteColumns.ID,
            NoteColumns.ALERTED_DATE
    };

    /**
     * 笔记 ID 列的索引
     */
    private static final int COLUMN_ID = 0;

    /**
     * 提醒时间列的索引
     */
    private static final int COLUMN_ALERTED_DATE = 1;

    /**
     * 接收系统闹钟广播并处理
     *
     * @param context 广播接收器上下文
     * @param intent  要处理的广播消息
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        // 获取当前时间戳
        long currentDate = System.currentTimeMillis();

        // 查询提醒表中所有已提醒过的笔记，并设置提醒闹钟
        Cursor c = context.getContentResolver().query(Notes.CONTENT_NOTE_URI,
                PROJECTION,
                NoteColumns.ALERTED_DATE + ">? AND " + NoteColumns.TYPE + "=" + Notes.TYPE_NOTE,
                new String[]{String.valueOf(currentDate)},
                null);

        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    // 获取提醒时间和笔记 ID，并构建相应的 Intent 和 PendingIntent
                    long alertDate = c.getLong(COLUMN_ALERTED_DATE);
                    Intent sender = new Intent(context, AlarmReceiver.class);
                    sender.setData(ContentUris.withAppendedId(Notes.CONTENT_NOTE_URI, c.getLong(COLUMN_ID)));
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, sender, 0);

                    // 设置提醒闹钟
                    AlarmManager alermManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                    alermManager.set(AlarmManager.RTC_WAKEUP, alertDate, pendingIntent);
                } while (c.moveToNext());
            }
            c.close();
        }
    }

}
