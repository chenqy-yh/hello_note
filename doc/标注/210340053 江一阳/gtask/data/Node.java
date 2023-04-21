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

import org.json.JSONObject;

// 定义一个抽象类Node，用于表示各种数据节点
public abstract class Node {
    // 定义同步操作的常量
    public static final int SYNC_ACTION_NONE = 0;

    public static final int SYNC_ACTION_ADD_REMOTE = 1;

    public static final int SYNC_ACTION_ADD_LOCAL = 2;

    public static final int SYNC_ACTION_DEL_REMOTE = 3;

    public static final int SYNC_ACTION_DEL_LOCAL = 4;

    public static final int SYNC_ACTION_UPDATE_REMOTE = 5;

    public static final int SYNC_ACTION_UPDATE_LOCAL = 6;

    public static final int SYNC_ACTION_UPDATE_CONFLICT = 7;

    public static final int SYNC_ACTION_ERROR = 8;
    // 定义节点的基本属性
    private String mGid;

    private String mName;

    private long mLastModified;

    private boolean mDeleted;

    // 构造方法，初始化节点的属性
    public Node() {
        mGid = null;
        mName = "";
        mLastModified = 0;
        mDeleted = false;
    }

    // 定义抽象方法，用于获取创建操作的JSON对象
    public abstract JSONObject getCreateAction(int actionId);

    // 定义抽象方法，用于获取更新操作的JSON对象
    public abstract JSONObject getUpdateAction(int actionId);

    // 定义抽象方法，用于根据远程JSON设置节点的内容
    public abstract void setContentByRemoteJSON(JSONObject js);

    // 定义抽象方法，用于根据本地JSON设置节点的内容
    public abstract void setContentByLocalJSON(JSONObject js);

    // 定义抽象方法，用于根据内容获取本地JSON
    public abstract JSONObject getLocalJSONFromContent();

    // 定义抽象方法，用于获取同步操作类型
    public abstract int getSyncAction(Cursor c);

    // 定义setter方法，用于设置节点的属性
    public void setGid(String gid) {
        this.mGid = gid;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public void setLastModified(long lastModified) {
        this.mLastModified = lastModified;
    }

    public void setDeleted(boolean deleted) {
        this.mDeleted = deleted;
    }

    // 定义getter方法，用于获取节点的属性
    public String getGid() {
        return this.mGid;
    }

    public String getName() {
        return this.mName;
    }

    public long getLastModified() {
        return this.mLastModified;
    }

    public boolean getDeleted() {
        return this.mDeleted;
    }

}
