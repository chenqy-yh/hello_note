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

package net.micode.notes.gtask.data;

// 导入所需的类和包
import android.database.Cursor;
import android.util.Log;

import net.micode.notes.tool.GTaskStringUtils;

import org.json.JSONException;
import org.json.JSONObject;

// MetaData类继承自Task类，用于处理与Google任务相关的元数据
public class MetaData extends Task {
    // 定义类的TAG，用于日志记录
    private final static String TAG = MetaData.class.getSimpleName();
    // 定义一个变量用于存储与Google任务相关的ID
    private String mRelatedGid = null;

    // 设置元数据方法，接收一个gid和一个metaInfo JSONObject对象
    public void setMeta(String gid, JSONObject metaInfo) {
        try {
            // 将gid存储在metaInfo的JSON对象中
            metaInfo.put(GTaskStringUtils.META_HEAD_GTASK_ID, gid);
        } catch (JSONException e) {
            // 记录错误日志
            Log.e(TAG, "failed to put related gid");
        }
        // 记录错误日志
        setNotes(metaInfo.toString());
        // 设置元数据任务的名称
        setName(GTaskStringUtils.META_NOTE_NAME);
    }

    // 获取与Google任务相关的ID的方法
    public String getRelatedGid() {
        return mRelatedGid;
    }

    // 重写isWorthSaving方法，判断是否值得保存
    @Override
    public boolean isWorthSaving() {
        return getNotes() != null;
    }

    // 从远程JSON设置内容
    @Override
    public void setContentByRemoteJSON(JSONObject js) {
        super.setContentByRemoteJSON(js);
        if (getNotes() != null) {
            try {
                // 解析notes中的JSON字符串
                JSONObject metaInfo = new JSONObject(getNotes().trim());
                // 从JSON对象中获取与Google任务相关的ID
                mRelatedGid = metaInfo.getString(GTaskStringUtils.META_HEAD_GTASK_ID);
            } catch (JSONException e) {
                // 记录警告日志
                Log.w(TAG, "failed to get related gid");
                mRelatedGid = null;
            }
        }
    }

    // 从本地JSON设置内容，但是不应该被调用，因此抛出异常
    @Override
    public void setContentByLocalJSON(JSONObject js) {
        // this function should not be called
        throw new IllegalAccessError("MetaData:setContentByLocalJSON should not be called");
    }

    // 从内容获取本地JSON，但是不应该被调用，因此抛出异常
    @Override
    public JSONObject getLocalJSONFromContent() {
        throw new IllegalAccessError("MetaData:getLocalJSONFromContent should not be called");
    }
    // 获取同步操作，但是不应该被调用，因此抛出异常
    @Override
    public int getSyncAction(Cursor c) {
        throw new IllegalAccessError("MetaData:getSyncAction should not be called");
    }

}
