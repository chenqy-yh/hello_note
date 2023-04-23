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

 package net.micode.notes.gtask.remote;

 import android.app.Activity;
 import android.content.ContentResolver;
 import android.content.ContentUris;
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.util.Log;
 
 import net.micode.notes.R;
 import net.micode.notes.data.Notes;
 import net.micode.notes.data.Notes.DataColumns;
 import net.micode.notes.data.Notes.NoteColumns;
 import net.micode.notes.gtask.data.MetaData;
 import net.micode.notes.gtask.data.Node;
 import net.micode.notes.gtask.data.SqlNote;
 import net.micode.notes.gtask.data.Task;
 import net.micode.notes.gtask.data.TaskList;
 import net.micode.notes.gtask.exception.ActionFailureException;
 import net.micode.notes.gtask.exception.NetworkFailureException;
 import net.micode.notes.tool.DataUtils;
 import net.micode.notes.tool.GTaskStringUtils;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 
 
 public class GTaskManager {
     private static final String TAG = GTaskManager.class.getSimpleName();
 
     public static final int STATE_SUCCESS = 0;
 
     public static final int STATE_NETWORK_ERROR = 1;
 
     public static final int STATE_INTERNAL_ERROR = 2;
 
     public static final int STATE_SYNC_IN_PROGRESS = 3;
 
     public static final int STATE_SYNC_CANCELLED = 4;
 
     private static GTaskManager mInstance = null;
 
     private Activity mActivity;
 
     private Context mContext;
 
     private ContentResolver mContentResolver;
 
     private boolean mSyncing;
 
     private boolean mCancelled;
 
     private HashMap<String, TaskList> mGTaskListHashMap;
 
     private HashMap<String, Node> mGTaskHashMap;
 
     private HashMap<String, MetaData> mMetaHashMap;
 
     private TaskList mMetaList;
 
     private HashSet<Long> mLocalDeleteIdMap;
 
     private HashMap<String, Long> mGidToNid;
 
     private HashMap<Long, String> mNidToGid;
 
     // 定义了一个名为 GTaskManager 的私有类
private GTaskManager() {
    // 初始化一些变量
    mSyncing = false; // 当前不在同步状态
    mCancelled = false; // 当前没有取消
    mGTaskListHashMap = new HashMap<String, TaskList>(); // 存储多个任务列表的 HashMap
    mGTaskHashMap = new HashMap<String, Node>(); // 存储任务的 HashMap
    mMetaHashMap = new HashMap<String, MetaData>(); // 存储元数据的 HashMap
    mMetaList = null; // 初始化元数据列表为空
    mLocalDeleteIdMap = new HashSet<Long>(); // 存储本地删除的任务的 ID 集合
    mGidToNid = new HashMap<String, Long>(); // 存储 GTask ID 到 Node ID 的映射
    mNidToGid = new HashMap<Long, String>(); // 存储 Node ID 到 GTask ID 的映射
}

 
     // 定义了一个名为 getInstance 的静态方法，返回一个 GTaskManager 对象
public static synchronized GTaskManager getInstance() {
    // 如果当前 GTaskManager 对象为空，就创建一个新的 GTaskManager 对象
    if (mInstance == null) {
        mInstance = new GTaskManager();
    }
    // 返回 GTaskManager 对象
    return mInstance;
}

    // 定义了一个名为 setActivityContext 的方法，用于设置 Activity 的上下文
public synchronized void setActivityContext(Activity activity) {
    // 用于获取授权令牌（authtoken）的 Activity 上下文
    mActivity = activity;
}

 
    // 定义了一个名为 sync 的方法，用于同步 Google 任务列表
public int sync(Context context, GTaskASyncTask asyncTask) {
    // 如果正在同步，则返回“同步进行中”状态
    if (mSyncing) {
        Log.d(TAG, "Sync is in progress");
        return STATE_SYNC_IN_PROGRESS;
    }
    // 重置相关变量
    mContext = context;
    mContentResolver = mContext.getContentResolver();
    mSyncing = true;
    mCancelled = false;
    mGTaskListHashMap.clear();
    mGTaskHashMap.clear();
    mMetaHashMap.clear();
    mLocalDeleteIdMap.clear();
    mGidToNid.clear();
    mNidToGid.clear();

    try {
        // 获取 GTaskClient 的单例对象
        GTaskClient client = GTaskClient.getInstance();
        // 重置更新数组
        client.resetUpdateArray();

        // 登录 Google 任务列表
        if (!mCancelled) {
            if (!client.login(mActivity)) {
                throw new NetworkFailureException("login google task failed");
            }
        }

        // 从 Google 获取任务列表
        asyncTask.publishProgess(mContext.getString(R.string.sync_progress_init_list));
        initGTaskList();

        // 进行内容同步工作
        asyncTask.publishProgess(mContext.getString(R.string.sync_progress_syncing));
        syncContent();
    } catch (NetworkFailureException e) {
        Log.e(TAG, e.toString());
        return STATE_NETWORK_ERROR;
    } catch (ActionFailureException e) {
        Log.e(TAG, e.toString());
        return STATE_INTERNAL_ERROR;
    } catch (Exception e) {
        Log.e(TAG, e.toString());
        e.printStackTrace();
        return STATE_INTERNAL_ERROR;
    } finally {
        // 清空相关变量
        mGTaskListHashMap.clear();
        mGTaskHashMap.clear();
        mMetaHashMap.clear();
        mLocalDeleteIdMap.clear();
        mGidToNid.clear();
        mNidToGid.clear();
        mSyncing = false;
    }

    // 如果已取消，则返回“同步已取消”状态；否则返回“成功”状态
    return mCancelled ? STATE_SYNC_CANCELLED : STATE_SUCCESS;
}

    // 初始化 GTask 列表

     private void initGTaskList() throws NetworkFailureException {
        // 检查是否已取消
         if (mCancelled)
             return;
             // 获取 GTask 客户端
         GTaskClient client = GTaskClient.getInstance();
         try {
            // 获取任务列表的 JSON 数据
             JSONArray jsTaskLists = client.getTaskLists();
 
             // 先初始化元列表
             mMetaList = null;
             for (int i = 0; i < jsTaskLists.length(); i++) {
                // 获取 JSON 数据中的任务列表对象
                 JSONObject object = jsTaskLists.getJSONObject(i);
                 String gid = object.getString(GTaskStringUtils.GTASK_JSON_ID);
                 String name = object.getString(GTaskStringUtils.GTASK_JSON_NAME);
                // 如果任务列表名称为“MIUI_FOLDER_PREFFIX + FOLDER_META”，则表示为元列表
                 if (name.equals(GTaskStringUtils.MIUI_FOLDER_PREFFIX + GTaskStringUtils.FOLDER_META)) {
                    // 初始化元列表 
                     mMetaList = new TaskList();
                     mMetaList.setContentByRemoteJSON(object);
 
                     // 加载元数据
                     JSONArray jsMetas = client.getTaskList(gid);
                     for (int j = 0; j < jsMetas.length(); j++) {
                         object = (JSONObject) jsMetas.getJSONObject(j);
                         MetaData metaData = new MetaData();
                         metaData.setContentByRemoteJSON(object);
                         // 如果元数据值得保存，将其添加到元列表中
                         if (metaData.isWorthSaving()) {
                             mMetaList.addChildTask(metaData);
                             // 将元数据关联的 GID 和元数据存储在哈希映射表中
                             if (metaData.getGid() != null) {
                                 mMetaHashMap.put(metaData.getRelatedGid(), metaData);
                             }
                         }
                     }
                 }
             }
 
            // 如果元列表不存在，则创建元列表
             if (mMetaList == null) {
                 mMetaList = new TaskList();
                 mMetaList.setName(GTaskStringUtils.MIUI_FOLDER_PREFFIX
                         + GTaskStringUtils.FOLDER_META);
                 GTaskClient.getInstance().createTaskList(mMetaList);
             }
 
             // 初始化任务列表
             for (int i = 0; i < jsTaskLists.length(); i++) {
                // 获取 JSON 数据中的任务列表对象
                 JSONObject object = jsTaskLists.getJSONObject(i);
                 String gid = object.getString(GTaskStringUtils.GTASK_JSON_ID);
                 String name = object.getString(GTaskStringUtils.GTASK_JSON_NAME);
                // 如果任务列表名称以“MIUI_FOLDER_PREFFIX”开头且不是元列表，则为任务列表
                 if (name.startsWith(GTaskStringUtils.MIUI_FOLDER_PREFFIX)
                         && !name.equals(GTaskStringUtils.MIUI_FOLDER_PREFFIX
                                 + GTaskStringUtils.FOLDER_META)) {
                    // 初始化任务列表
                     TaskList tasklist = new TaskList();
                     tasklist.setContentByRemoteJSON(object);
                     mGTaskListHashMap.put(gid, tasklist);
                     mGTaskHashMap.put(gid, tasklist);
 
                     // 加载任务
                     JSONArray jsTasks = client.getTaskList(gid);
                     for (int j = 0; j < jsTasks.length(); j++) {
                         object = (JSONObject) jsTasks.getJSONObject(j);
                         gid = object.getString(GTaskStringUtils.GTASK_JSON_ID);
                         Task task = new Task();
                         task.setContentByRemoteJSON(object);
                         // 如果任务值得保存，将其添加
                         if (task.isWorthSaving()) {
                             task.setMetaInfo(mMetaHashMap.get(gid));// 设置任务元信息
                             tasklist.addChildTask(task);// 将任务添加到任务列表的子任务中
                             mGTaskHashMap.put(gid, task); // 在哈希表中添加任务的ID和任务对象
                         }
                     }
                 }
             }
         } catch (JSONException e) {
             Log.e(TAG, e.toString());
             e.printStackTrace();
             throw new ActionFailureException("initGTaskList: handing JSONObject failed");
         }
     }
        // 同步笔记内容 
     private void syncContent() throws NetworkFailureException {
         int syncType;
         Cursor c = null;
         String gid;
         Node node;

         mLocalDeleteIdMap.clear();
         if (mCancelled) {// 如果已取消，直接返回
             return;
         }
 
         // 处理本地已删除笔记
         try {
            // 查询非系统类型且父级 ID 不是回收站的笔记
             c = mContentResolver.query(Notes.CONTENT_NOTE_URI, SqlNote.PROJECTION_NOTE,
                     "(type<>? AND parent_id=?)", new String[] {
                             String.valueOf(Notes.TYPE_SYSTEM), String.valueOf(Notes.ID_TRASH_FOLER)
                     }, null);
             if (c != null) {
                 while (c.moveToNext()) {
                     gid = c.getString(SqlNote.GTASK_ID_COLUMN);
                     node = mGTaskHashMap.get(gid);
                     if (node != null) {
                         mGTaskHashMap.remove(gid);
                         doContentSync(Node.SYNC_ACTION_DEL_REMOTE, node, c);
                     }
 
                     mLocalDeleteIdMap.add(c.getLong(SqlNote.ID_COLUMN));
                 }
             } else {
                 Log.w(TAG, "failed to query trash folder");
             }
         } finally {
             if (c != null) {
                 c.close();
                 c = null;
             }
         }
 
         // 先同步文件夹
         syncFolder();
 
         // 处理已存在于数据库中的笔记
         try {
            // 查询类型为笔记且父级 ID 不是回收站的笔记，按类型倒序排序
             c = mContentResolver.query(Notes.CONTENT_NOTE_URI, SqlNote.PROJECTION_NOTE,
                     "(type=? AND parent_id<>?)", new String[] {
                             String.valueOf(Notes.TYPE_NOTE), String.valueOf(Notes.ID_TRASH_FOLER)
                     }, NoteColumns.TYPE + " DESC");
             if (c != null) {
                 while (c.moveToNext()) {
                     gid = c.getString(SqlNote.GTASK_ID_COLUMN);
                     node = mGTaskHashMap.get(gid);
                     if (node != null) {
                         mGTaskHashMap.remove(gid);
                         mGidToNid.put(gid, c.getLong(SqlNote.ID_COLUMN));
                         mNidToGid.put(c.getLong(SqlNote.ID_COLUMN), gid);
                         syncType = node.getSyncAction(c);// 获取同步类型
                     } else {
                         if (c.getString(SqlNote.GTASK_ID_COLUMN).trim().length() == 0) {
                             // 本地新增
                             syncType = Node.SYNC_ACTION_ADD_REMOTE;
                         } else {
                             // 远程删除
                             syncType = Node.SYNC_ACTION_DEL_LOCAL;
                         }
                     }
                     doContentSync(syncType, node, c);
                 }
             } else {
                 Log.w(TAG, "failed to query existing note in database");
             }
 
         } finally {
             if (c != null) {
                 c.close();
                 c = null;
             }
         }
 
         // 处理剩余项
         Iterator<Map.Entry<String, Node>> iter = mGTaskHashMap.entrySet().iterator();
         while (iter.hasNext()) {// 如果还有下一个键值对
             Map.Entry<String, Node> entry = iter.next();// 获取下一个键值对
             node = entry.getValue();// 获取键值对中的value并将其赋值给node
             doContentSync(Node.SYNC_ACTION_ADD_LOCAL, node, null);// 将node添加到本地同步
         }
 
         // mCancelled 可能会被另一个线程设置，因此我们需要逐个检查
        // 清除本地删除表
         if (!mCancelled) {// 如果未被取消
             if (!DataUtils.batchDeleteNotes(mContentResolver, mLocalDeleteIdMap)) {// 批量删除本地删除表中的笔记
                 throw new ActionFailureException("failed to batch-delete local deleted notes");
             }// 如果删除失败则抛出异常
         }
 
         // 刷新本地同步id
         if (!mCancelled) {
             GTaskClient.getInstance().commitUpdate();
             refreshLocalSyncId();
         }
 
     }
 
     private void syncFolder() throws NetworkFailureException {
         Cursor c = null;// 游标对象，用于访问数据
         String gid;// GTask任务列表的ID
         Node node;// GTask任务列表对应的本地节点
         int syncType;// 本地与远程数据同步的类型
 
         if (mCancelled) {// 如果同步已被取消，则直接返回
             return;
         }
 
         // for root folder
         try {
            // 查询根目录的数据
             c = mContentResolver.query(ContentUris.withAppendedId(Notes.CONTENT_NOTE_URI,
                     Notes.ID_ROOT_FOLDER), SqlNote.PROJECTION_NOTE, null, null, null);
             if (c != null) {
                 c.moveToNext();// 将游标移动到下一个记录
                 gid = c.getString(SqlNote.GTASK_ID_COLUMN);// 获取GTask任务列表ID
                 node = mGTaskHashMap.get(gid);// 从GTask任务列表映射中获取对应本地节点
                 if (node != null) {
                     mGTaskHashMap.remove(gid);// 从映射中移除该节点
                     mGidToNid.put(gid, (long) Notes.ID_ROOT_FOLDER);// 将GTask ID映射到本地ID
                     mNidToGid.put((long) Notes.ID_ROOT_FOLDER, gid);// 将本地ID映射到GTask ID
                     // 仅当名称不是默认值时，才更新远程名称
                     if (!node.getName().equals(
                             GTaskStringUtils.MIUI_FOLDER_PREFFIX + GTaskStringUtils.FOLDER_DEFAULT))
                         doContentSync(Node.SYNC_ACTION_UPDATE_REMOTE, node, c);// 同步本地和远程数据
                 } else {
                     doContentSync(Node.SYNC_ACTION_ADD_REMOTE, node, c);// 将本地数据同步到远程
                 }
             } else {
                 Log.w(TAG, "failed to query root folder");// 记录警告信息
             }
         } finally {
             if (c != null) {
                 c.close();// 关闭游标
                 c = null;// 将游标对象设置为null
             }
         }
 
         // for call-note folder
         try {
            // 查询通话记录文件夹的数据
             c = mContentResolver.query(Notes.CONTENT_NOTE_URI, SqlNote.PROJECTION_NOTE, "(_id=?)",
                     new String[] {
                         String.valueOf(Notes.ID_CALL_RECORD_FOLDER)
                     }, null);
             if (c != null) {
                 if (c.moveToNext()) {
                     gid = c.getString(SqlNote.GTASK_ID_COLUMN);// 获取GTask任务列表ID
                     node = mGTaskHashMap.get(gid);// 从GTask任务列表映射中获取对应本地节点
                     if (node != null) {
                         mGTaskHashMap.remove(gid);// 从映射中移除该节点
                         mGidToNid.put(gid, (long) Notes.ID_CALL_RECORD_FOLDER);// 将GTask ID映射到本地ID
                         mNidToGid.put((long) Notes.ID_CALL_RECORD_FOLDER, gid);// 将本地ID映射到GTask ID
                         // 仅当名称不是默认值时，才更新远端
                         if (!node.getName().equals(
                                 GTaskStringUtils.MIUI_FOLDER_PREFFIX
                                         + GTaskStringUtils.FOLDER_CALL_NOTE))
                             doContentSync(Node.SYNC_ACTION_UPDATE_REMOTE, node, c);
                     } else {
                         doContentSync(Node.SYNC_ACTION_ADD_REMOTE, node, c);
                     }
                 }
             } else {
                 Log.w(TAG, "failed to query call note folder");
             }
         } finally {
             if (c != null) {
                 c.close();
                 c = null;
             }
         }
 
         // 处理本地现有文件夹
         try {
             c = mContentResolver.query(Notes.CONTENT_NOTE_URI, SqlNote.PROJECTION_NOTE,
                     "(type=? AND parent_id<>?)", new String[] {
                             String.valueOf(Notes.TYPE_FOLDER), String.valueOf(Notes.ID_TRASH_FOLER)
                     }, NoteColumns.TYPE + " DESC");
             if (c != null) {
                 while (c.moveToNext()) {
                     gid = c.getString(SqlNote.GTASK_ID_COLUMN);
                     node = mGTaskHashMap.get(gid);
                     if (node != null) {
                         mGTaskHashMap.remove(gid);
                         mGidToNid.put(gid, c.getLong(SqlNote.ID_COLUMN));
                         mNidToGid.put(c.getLong(SqlNote.ID_COLUMN), gid);
                         syncType = node.getSyncAction(c);
                     } else {
                         if (c.getString(SqlNote.GTASK_ID_COLUMN).trim().length() == 0) {
                             // 本地新增
                             syncType = Node.SYNC_ACTION_ADD_REMOTE;
                         } else {
                             // 远端删除
                             syncType = Node.SYNC_ACTION_DEL_LOCAL;
                         }
                     }
                     doContentSync(syncType, node, c);
                 }
             } else {
                 Log.w(TAG, "failed to query existing folder");
             }
         } finally {
             if (c != null) {
                 c.close();
                 c = null;
             }
         }
 
         // 处理远端新增文件夹
         Iterator<Map.Entry<String, TaskList>> iter = mGTaskListHashMap.entrySet().iterator();
         while (iter.hasNext()) {
             Map.Entry<String, TaskList> entry = iter.next();
             gid = entry.getKey();
             node = entry.getValue();
             if (mGTaskHashMap.containsKey(gid)) {
                 mGTaskHashMap.remove(gid);
                 doContentSync(Node.SYNC_ACTION_ADD_LOCAL, node, null);
             }
         }
 
         if (!mCancelled)
             GTaskClient.getInstance().commitUpdate();
     }
 
     private void doContentSync(int syncType, Node node, Cursor c) throws NetworkFailureException {
         // 如果取消了同步，则直接返回 
        if (mCancelled) {
             return;
         }
          // 元数据对象
         MetaData meta;
         // 根据同步类型执行相应的操作
         switch (syncType) {
            // 本地添加节点
             case Node.SYNC_ACTION_ADD_LOCAL:
                 addLocalNode(node);
                 break;
            // 远程添加节点
             case Node.SYNC_ACTION_ADD_REMOTE:
                 addRemoteNode(node, c);
                 break;
             // 本地删除节点
             case Node.SYNC_ACTION_DEL_LOCAL:
             // 获取需要删除的节点的元数据
                 meta = mMetaHashMap.get(c.getString(SqlNote.GTASK_ID_COLUMN));
                 if (meta != null) {
                    // 使用 GTaskClient 删除节点
                     GTaskClient.getInstance().deleteNode(meta);
                 }
                 // 将节点 ID 添加到本地删除 ID 映射表中
                 mLocalDeleteIdMap.add(c.getLong(SqlNote.ID_COLUMN));
                 break;
                 // 远程删除节点
             case Node.SYNC_ACTION_DEL_REMOTE:
             // 获取需要删除的节点的元数据
                 meta = mMetaHashMap.get(node.getGid());
                 if (meta != null) {
                    // 使用 GTaskClient 删除节点
                     GTaskClient.getInstance().deleteNode(meta);
                 }
                 // 使用 GTaskClient 删除节点
                 GTaskClient.getInstance().deleteNode(node);
                 break;
                 // 本地更新节点
             case Node.SYNC_ACTION_UPDATE_LOCAL:
                 updateLocalNode(node, c);
                 break;
                 // 远程更新节点
             case Node.SYNC_ACTION_UPDATE_REMOTE:
                 updateRemoteNode(node, c);
                 break;
                 // 更新冲突
             case Node.SYNC_ACTION_UPDATE_CONFLICT:
                 // 可以考虑合并两个修改
            // 现在先简单地使用本地更新
                 updateRemoteNode(node, c);
                 break;
                 // 不执行任何操作
             case Node.SYNC_ACTION_NONE:
                 break;
                 // 同步操作失败
             case Node.SYNC_ACTION_ERROR:
             default:
              // 抛出异常
                 throw new ActionFailureException("unkown sync action type");
         }
     }
 
     private void addLocalNode(Node node) throws NetworkFailureException {
        // 如果取消了操作，直接返回
         if (mCancelled) {
             return;
         }
         // 声明一个 SqlNote 对象
         SqlNote sqlNote;
         // 如果是 TaskList，判断是哪种文件夹
         if (node instanceof TaskList) {
            // 如果是默认文件夹
             if (node.getName().equals(
                     GTaskStringUtils.MIUI_FOLDER_PREFFIX + GTaskStringUtils.FOLDER_DEFAULT)) {
                        // 新建一个根文件夹的 SqlNote 对象
                 sqlNote = new SqlNote(mContext, Notes.ID_ROOT_FOLDER);
             } 
             // 如果是通话记录文件夹
             else if (node.getName().equals(
                     GTaskStringUtils.MIUI_FOLDER_PREFFIX + GTaskStringUtils.FOLDER_CALL_NOTE)) {
                        // 新建一个通话记录文件夹的 SqlNote 对象
                 sqlNote = new SqlNote(mContext, Notes.ID_CALL_RECORD_FOLDER);
             } 
             // 如果是其他类型的文件夹
             else {
                // 新建一个普通的 SqlNote 对象，并设置其内容和父文件夹 ID
                 sqlNote = new SqlNote(mContext);
                 sqlNote.setContent(node.getLocalJSONFromContent());
                 sqlNote.setParentId(Notes.ID_ROOT_FOLDER);
             }
         }
         // 如果是 Task
         else {
            // 新建一个普通的 SqlNote 对象
             sqlNote = new SqlNote(mContext);
             // 从 Task 中获取其内容的 JSON 对象
             JSONObject js = node.getLocalJSONFromContent();
             try {
                // 如果 JSON 对象中包含元数据头部信息
                 if (js.has(GTaskStringUtils.META_HEAD_NOTE)) {
                    // 获取元数据头部信息中的 Note 对象
                     JSONObject note = js.getJSONObject(GTaskStringUtils.META_HEAD_NOTE);
                     // 如果 Note 对象中包含 ID
                     if (note.has(NoteColumns.ID)) {
                        // 获取 Note 的 ID
                         long id = note.getLong(NoteColumns.ID);
                         // 如果该 ID 在 Note 数据库中已经存在
                         if (DataUtils.existInNoteDatabase(mContentResolver, id)) {
                             // 该 ID 已经被占用，需要新建一个 ID
                             note.remove(NoteColumns.ID);
                         }
                     }
                 }
                 // 如果 JSON 对象中包含元数据数据部分
                 if (js.has(GTaskStringUtils.META_HEAD_DATA)) {
                    // 获取元数据数据部分的 JSON 数组
                     JSONArray dataArray = js.getJSONArray(GTaskStringUtils.META_HEAD_DATA);
                     // 遍历该 JSON 数组
                     for (int i = 0; i < dataArray.length(); i++) {
                        // 获取数组中的 JSON 对象
                         JSONObject data = dataArray.getJSONObject(i);
                         // 如果该 JSON 对象中包含 ID
                         if (data.has(DataColumns.ID)) {
                            // 获取该数据的 ID
                             long dataId = data.getLong(DataColumns.ID);
                             // 如果该 ID 在数据数据库中已经存在
                             if (DataUtils.existInDataDatabase(mContentResolver, dataId)) {
                                  // 该 ID 已经被占用，需要新建一个 ID
                                 data.remove(DataColumns.ID);
                             }
                         }
                     }
 
                 }
             } catch (JSONException e) {// 捕获 JSON 解析异常
                 Log.w(TAG, e.toString());// 记录警告日志
                 e.printStackTrace();// 打印异常堆栈信息
             }
             sqlNote.setContent(js);// 设置 SQLNote 的内容为解析出的 js
 
             Long parentId = mGidToNid.get(((Task) node).getParent().getGid());// 获取 Task 节点的父节点的 GID 对应的 NID
             // 如果获取到的 NID 为空
             if (parentId == null) {
                 Log.e(TAG, "cannot find task's parent id locally");// 记录错误日志
                 throw new ActionFailureException("cannot add local node");// 抛出 ActionFailureException 异常
             }
             sqlNote.setParentId(parentId.longValue());// 将 SQLNote 的父节点 ID 设置为获取到的 NID
         }
 
         // 创建本地节点
         sqlNote.setGtaskId(node.getGid());// 设置 SQLNote 的 GTask ID 为节点的 GID
         sqlNote.commit(false);// 将 SQLNote 提交到本地数据库，不需要立即同步到远程服务器
 
         // 更新 GID-NID 映射
         mGidToNid.put(node.getGid(), sqlNote.getId());// 将节点的 GID 映射为 SQLNote 的 ID
         mNidToGid.put(sqlNote.getId(), node.getGid());// 将 SQLNote 的 ID 映射为节点的 GID
 
         // 更新元数据
         updateRemoteMeta(node.getGid(), sqlNote);// 同步 SQLNote 的元数据到远程服务器
     }
 
     private void updateLocalNode(Node node, Cursor c) throws NetworkFailureException {
        // 如果操作已经被取消，则不进行任何操作
        if (mCancelled) {
            return;
        }
    
        SqlNote sqlNote;
        // 将数据库查询结果保存到 SqlNote 对象中
        sqlNote = new SqlNote(mContext, c);
        // 将节点内容转换为 JSON 格式并保存到 SqlNote 对象中
        sqlNote.setContent(node.getLocalJSONFromContent());
    
        // 如果节点是任务类型，则将其父节点的 ID 设置为对应的本地 ID，
        // 否则将其父节点的 ID 设置为根文件夹的 ID
        Long parentId = (node instanceof Task) ? mGidToNid.get(((Task) node).getParent().getGid())
                : new Long(Notes.ID_ROOT_FOLDER);
        // 如果 parentId 为 null，则说明无法在本地找到该任务的父节点 ID，
        // 此时抛出一个异常
        if (parentId == null) {
            Log.e(TAG, "cannot find task's parent id locally");
            throw new ActionFailureException("cannot update local node");
        }
        // 将父节点 ID 设置为 sqlNote 对象的 parentId 属性
        sqlNote.setParentId(parentId.longValue());
        // 将 sqlNote 对象的属性保存到数据库中
        sqlNote.commit(true);
    
        // 更新远程节点的元信息
        updateRemoteMeta(node.getGid(), sqlNote);
    }
 
     private void addRemoteNode(Node node, Cursor c) throws NetworkFailureException {
        // 如果任务已经被取消，直接返回
        if (mCancelled) {
             return;
         }
         // 通过Cursor获取SqlNote对象
         SqlNote sqlNote = new SqlNote(mContext, c);
         Node n;
 
         // 如果SqlNote是任务类型，进行以下操作
         if (sqlNote.isNoteType()) {
            // 创建一个Task对象并用SqlNote的内容更新它
             Task task = new Task();
             task.setContentByLocalJSON(sqlNote.getContent());
            
             // 获取Task对象的父任务列表的gid
             String parentGid = mNidToGid.get(sqlNote.getParentId());
            // 如果无法找到父任务列表的gid，抛出ActionFailureException异常
             if (parentGid == null) {
                 Log.e(TAG, "cannot find task's parent tasklist");
                 throw new ActionFailureException("cannot add remote task");
             }
             // 在父任务列表中添加子任务
             mGTaskListHashMap.get(parentGid).addChildTask(task);
             // 通过GTaskClient实例创建Task任务
             GTaskClient.getInstance().createTask(task);
             n = (Node) task;
 
             // 添加元数据
             updateRemoteMeta(task.getGid(), sqlNote);
         } else {
             TaskList tasklist = null;
         // 如果SqlNote是文件夹类型，进行以下操作
        // 如果是根文件夹，设置文件夹名为默认文件夹
             String folderName = GTaskStringUtils.MIUI_FOLDER_PREFFIX;
             if (sqlNote.getId() == Notes.ID_ROOT_FOLDER)
                 folderName += GTaskStringUtils.FOLDER_DEFAULT;
                 // 如果是通话记录文件夹，设置文件夹名为通话记录文件夹
             else if (sqlNote.getId() == Notes.ID_CALL_RECORD_FOLDER)
                 folderName += GTaskStringUtils.FOLDER_CALL_NOTE;
                 // 否则，将文件夹名设置为SqlNote对象的片段内容
             else
                 folderName += sqlNote.getSnippet();
            // 遍历mGTaskListHashMap查找是否存在与SqlNote对象匹配的TaskList对象
             Iterator<Map.Entry<String, TaskList>> iter = mGTaskListHashMap.entrySet().iterator();
             while (iter.hasNext()) {
                 Map.Entry<String, TaskList> entry = iter.next();
                 String gid = entry.getKey();
                 TaskList list = entry.getValue();
                // 如果存在匹配的TaskList对象，则将tasklist设置为该对象
                 if (list.getName().equals(folderName)) {
                     tasklist = list;
                     // 如果mGTaskHashMap包含gid，将其从中移除
                     if (mGTaskHashMap.containsKey(gid)) {
                         mGTaskHashMap.remove(gid);
                     }
                     break;
                 }
             }
 
             // 如果未找到匹配的TaskList对象，新建一个TaskList对象
             if (tasklist == null) {
                 tasklist = new TaskList();
                 tasklist.setContentByLocalJSON(sqlNote.getContent());
                 GTaskClient.getInstance().createTaskList(tasklist);
                 mGTaskListHashMap.put(tasklist.getGid(), tasklist);
             }
             n = (Node) tasklist;
         }
 
         // 更新SqlNote对象的GTaskId，并将其提交到本地数据库
         sqlNote.setGtaskId(n.getGid());
         sqlNote.commit(false);
         sqlNote.resetLocalModified();
         sqlNote.commit(true);
 
         // gid-id 映射表
         mGidToNid.put(n.getGid(), sqlNote.getId());
         mNidToGid.put(sqlNote.getId(), n.getGid());
     }
 
     private void updateRemoteNode(Node node, Cursor c) throws NetworkFailureException {
        if (mCancelled) {
            return;  // 如果任务已取消，则退出此方法
        }
    
        SqlNote sqlNote = new SqlNote(mContext, c);  // 创建 SqlNote 实例
    
        // 更新远程服务器的节点内容
        node.setContentByLocalJSON(sqlNote.getContent());
        GTaskClient.getInstance().addUpdateNode(node);
    
        // 更新节点的元数据
        updateRemoteMeta(node.getGid(), sqlNote);
    
        // 如果是笔记类型，则移动任务（子任务）到另一个任务列表中
        if (sqlNote.isNoteType()) {
            Task task = (Task) node;
            TaskList preParentList = task.getParent();  // 获取任务当前的父任务列表
    
            // 查找新的父任务列表的 GID
            String curParentGid = mNidToGid.get(sqlNote.getParentId());
            if (curParentGid == null) {
                Log.e(TAG, "cannot find task's parent tasklist");  // 找不到新父任务列表，记录错误并抛出异常
                throw new ActionFailureException("cannot update remote task");
            }
            TaskList curParentList = mGTaskListHashMap.get(curParentGid);  // 获取新父任务列表的 TaskList 实例
    
            // 如果新旧父任务列表不同，则移动任务到新的父任务列表
            if (preParentList != curParentList) {
                preParentList.removeChildTask(task);  // 从旧父任务列表中移除该任务
                curParentList.addChildTask(task);  // 添加该任务到新的父任务列表中
                GTaskClient.getInstance().moveTask(task, preParentList, curParentList);  // 移动任务到新父任务列表
            }
        }
    
        sqlNote.resetLocalModified();  // 重置本地修改标记
        sqlNote.commit(true);  // 将 SqlNote 中的修改提交到本地 SQLite 数据库中
    }
    
    private void updateRemoteMeta(String gid, SqlNote sqlNote) throws NetworkFailureException {
        // 如果 sqlNote 不为空且为笔记类型，则执行以下代码
        if (sqlNote != null && sqlNote.isNoteType()) {
            // 获取 gid 对应的 metaData
            MetaData metaData = mMetaHashMap.get(gid);
            if (metaData != null) {
                // 设置 gid 对应的 metaData 的内容为 sqlNote 的内容
                metaData.setMeta(gid, sqlNote.getContent());
                // 将 metaData 添加到更新队列中
                GTaskClient.getInstance().addUpdateNode(metaData);
            } else {
                // 如果 metaData 为空，则创建新的 metaData，并设置其内容为 sqlNote 的内容
                metaData = new MetaData();
                metaData.setMeta(gid, sqlNote.getContent());
                // 将 metaData 添加到 mMetaList 的子任务列表中
                mMetaList.addChildTask(metaData);
                // 将 metaData 添加到 mMetaHashMap 中
                mMetaHashMap.put(gid, metaData);
                // 创建任务并将其添加到 GTaskClient 的任务列表中
                GTaskClient.getInstance().createTask(metaData);
            }
        }
    }
    
 
     // 声明一个方法，用于更新本地同步 ID，如果网络故障则抛出 NetworkFailureException 异常
    private void refreshLocalSyncId() throws NetworkFailureException {
        // 如果已取消，则直接返回
        if (mCancelled) {
         return;
        }

        // 清空哈希表
        mGTaskHashMap.clear();
        mGTaskListHashMap.clear();
        mMetaHashMap.clear();
        // 初始化 GTask 列表
        initGTaskList();

        // 声明一个游标，用于查询本地便签
        Cursor c = null;
        try {
            // 查询不属于系统类型和垃圾桶类型的所有便签
            c = mContentResolver.query(
                   Notes.CONTENT_NOTE_URI,
                    SqlNote.PROJECTION_NOTE,
                    "(type<>? AND parent_id<>?)",
                    new String[] {
                            String.valueOf(Notes.TYPE_SYSTEM),
                        String.valueOf(Notes.ID_TRASH_FOLER)
                    },
                    NoteColumns.TYPE + " DESC"
            );

            // 如果查询结果不为空
            if (c != null) {
                // 遍历查询结果
                while (c.moveToNext()) {
                    // 获取当前便签的 GTask ID
                    String gid = c.getString(SqlNote.GTASK_ID_COLUMN);
                    // 根据 GTask ID 从 GTask 哈希表中获取节点信息
                    Node node = mGTaskHashMap.get(gid);
                    // 如果节点信息不为空
                    if (node != null) {
                        // 从 GTask 哈希表中移除当前 GTask ID 对应的节点
                        mGTaskHashMap.remove(gid);
                        // 创建一个 ContentValues 对象，用于更新本地便签的同步 ID
                        ContentValues values = new ContentValues();
                        values.put(NoteColumns.SYNC_ID, node.getLastModified());
                        // 更新本地便签的同步 ID
                         mContentResolver.update(
                                ContentUris.withAppendedId(
                                        Notes.CONTENT_NOTE_URI,
                                        c.getLong(SqlNote.ID_COLUMN)
                                ),
                                values,
                                null,
                                null
                        );
                    } else {
                         // 如果节点信息为空，打印日志并抛出 ActionFailureException 异常
                        Log.e(TAG, "something is missed");
                        throw new ActionFailureException(
                                "some local items don't have gid after sync"
                        );
                    }
                }
            } else {
                // 如果查询结果为空，打印警告日志
                Log.w(TAG, "failed to query local note to refresh sync id");
            }
        } finally {
            // 关闭游标
            if (c != null) {
                c.close();
                c = null;
            }
        }
    }

 
     public String getSyncAccount() {
         return GTaskClient.getInstance().getSyncAccount().name;
     }
 
     public void cancelSync() {
         mCancelled = true;
     }
 }
 