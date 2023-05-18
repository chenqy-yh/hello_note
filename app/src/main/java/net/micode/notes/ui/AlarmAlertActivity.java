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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.Window;
import android.view.WindowManager;

import net.micode.notes.R;
import net.micode.notes.data.Notes;
import net.micode.notes.tool.DataUtils;

import java.io.IOException;


public class AlarmAlertActivity extends Activity implements OnClickListener, OnDismissListener {
    /**
     * 当前笔记的 ID
     */
    private long mNoteId;

    /**
     * 笔记摘录，用于在对话框中显示当前笔记的内容
     */
    private String mSnippet;

    /**
     * 笔记摘录预览的最大长度
     */
    private static final int SNIPPET_PREW_MAX_LEN = 60;

    /**
     * 媒体播放器，用于播放提醒音
     */
    MediaPlayer mPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        // 将 Activity 显示在锁屏界面上
        final Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

        // 如果屏幕还未点亮，保持屏幕常亮并将屏幕点亮
        if (!isScreenOn()) {
            win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR);
        }

        // 获取 intent 中传递的笔记 ID，并根据 ID 获取笔记摘录
        Intent intent = getIntent();
        try {
            mNoteId = Long.parseLong(intent.getData().getPathSegments().get(1));
            mSnippet = DataUtils.getSnippetById(this.getContentResolver(), mNoteId);

            // 将笔记摘录截取到预设的最大长度，并添加省略号
            mSnippet = mSnippet.length() > SNIPPET_PREW_MAX_LEN ? mSnippet.substring(0,
                    SNIPPET_PREW_MAX_LEN) + getResources().getString(R.string.notelist_string_info)
                    : mSnippet;
        } catch (IllegalArgumentException e) {
            // 如果 intent 中未传递有效的笔记 ID，则打印异常并退出 Activity
            e.printStackTrace();
            return;
        }

        // 创建媒体播放器，并在数据库中找到当前笔记，如果存在则显示提醒对话框并播放提醒音
        mPlayer = new MediaPlayer();
        if (DataUtils.visibleInNoteDatabase(getContentResolver(), mNoteId, Notes.TYPE_NOTE)) {
            showActionDialog();
            playAlarmSound();
        } else {
            finish();
        }
    }


    /**
     * 判断屏幕是否点亮
     *
     * @return 如果屏幕点亮则返回 true，否则返回 false
     */
    private boolean isScreenOn() {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        return pm.isScreenOn();
    }

    /**
     * 播放提醒音
     */
    private void playAlarmSound() {
        // 获取默认的闹钟铃声 Uri
        Uri url = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_ALARM);

        // 获取静音模式下允许播放的音频流类型
        int silentModeStreams = Settings.System.getInt(getContentResolver(),
                Settings.System.MODE_RINGER_STREAMS_AFFECTED, 0);

        // 根据静音模式设置媒体播放器的音频流类型
        if ((silentModeStreams & (1 << AudioManager.STREAM_ALARM)) != 0) {
            mPlayer.setAudioStreamType(silentModeStreams);
        } else {
            mPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
        }

        // 设置媒体播放器的数据源，并开始播放
        try {
            mPlayer.setDataSource(this, url);
            mPlayer.prepare();
            mPlayer.setLooping(true);
            mPlayer.start();
        } catch (IllegalArgumentException | SecurityException | IllegalStateException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 显示提醒对话框
     */
    private void showActionDialog() {
        // 构建对话框，并设置标题、内容和两个按钮（确认和进入）
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(R.string.app_name);
        dialog.setMessage(mSnippet);
        dialog.setPositiveButton(R.string.notealert_ok, this);

        // 如果屏幕点亮，则添加“进入”按钮
        if (isScreenOn()) {
            dialog.setNegativeButton(R.string.notealert_enter, this);
        }

        // 显示对话框，并设置对话框关闭时的监听器
        dialog.show().setOnDismissListener(this);
    }

    /**
     * 处理对话框按钮的点击事件
     *
     * @param dialog 点击的对话框
     * @param which  点击的按钮类型（确定或取消）
     */
    public void onClick(DialogInterface dialog, int which) {
        // 根据不同的按钮类型执行不同的操作
        if (which == DialogInterface.BUTTON_NEGATIVE) {// 如果点击了“进入”按钮，则打开笔记编辑界面并查看当前笔记
            Intent intent = new Intent(this, NoteEditActivity.class);
            intent.setAction(Intent.ACTION_VIEW);
            intent.putExtra(Intent.EXTRA_UID, mNoteId);
            startActivity(intent);
        }
    }


    /**
     * 对话框关闭时的回调方法
     *
     * @param dialog 对话框
     */
    public void onDismiss(DialogInterface dialog) {
        // 停止播放提醒音并关闭 Activity
        stopAlarmSound();
        finish();
    }

    /**
     * 停止播放提醒音
     */
    private void stopAlarmSound() {
        // 如果媒体播放器不为 null，则停止播放并释放资源
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }
    }

}
