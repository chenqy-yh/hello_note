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

import android.content.Context;
import android.database.Cursor;
import android.os.Environment;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;

import net.micode.notes.R;
import net.micode.notes.data.Notes;
import net.micode.notes.data.Notes.DataColumns;
import net.micode.notes.data.Notes.DataConstants;
import net.micode.notes.data.Notes.NoteColumns;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;


public class BackupUtils {
    private static final String TAG = "BackupUtils";
    // Singleton stuff
    private static BackupUtils sInstance;

    /**
     * 返回 BackupUtils 类的单例实例，并确保线程安全。
     * @param context 应用程序的上下文。
     * @return BackupUtils 类的实例。
     */
    public static synchronized BackupUtils getInstance(Context context) {
        // 如果实例不存在，创建一个新的实例
        if (sInstance == null) {
            sInstance = new BackupUtils(context);
        }
        // 返回现有实例或新创建的实例
        return sInstance;
    }


    /**
     * Following states are signs to represents backup or restore
     * status
     */
    // Currently, the sdcard is not mounted
    // SD卡未挂载的状态
    public static final int STATE_SD_CARD_UNMOUONTED = 0;
    // 备份文件不存在的状态
    public static final int STATE_BACKUP_FILE_NOT_EXIST = 1;
    // 数据格式不正确，可能被其他程序更改的状态
    public static final int STATE_DATA_DESTROIED = 2;
    // 恢复或备份失败的状态，由运行时异常引起
    public static final int STATE_SYSTEM_ERROR = 3;
    // 备份或恢复成功的状态
    public static final int STATE_SUCCESS = 4;

    private TextExport mTextExport; // 用于导出文本数据的 TextExport 实例

    // 构造函数，用于创建 BackupUtils 类的实例
    private BackupUtils(Context context) {
        mTextExport = new TextExport(context); // 创建 TextExport 实例并保存到成员变量中
    }

    /**
     * 检查外部存储是否可用。
     * @return 如果外部存储可用，则返回 true；否则返回 false。
     */
    private static boolean externalStorageAvailable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    /**
     * 将数据导出到文本文件中。
     * @return 导出操作的结果状态。
     */
    public int exportToText() {
        return mTextExport.exportToText();
    }

    /**
     * 获取导出的文本文件名。
     * @return 导出的文本文件名。
     */
    public String getExportedTextFileName() {
        return mTextExport.mFileName;
    }

    /**
     * 获取导出的文本文件所在的目录。
     * @return 导出的文本文件所在的目录。
     */
    public String getExportedTextFileDir() {
        return mTextExport.mFileDirectory;
    }

    private static class TextExport {
        private static final String[] NOTE_PROJECTION = {
                NoteColumns.ID,               // 笔记的标识符
                NoteColumns.MODIFIED_DATE,    // 笔记最近修改的日期
                NoteColumns.SNIPPET,          // 笔记内容的片段，用于快速预览笔记
                NoteColumns.TYPE              // 笔记的类型
        };

        private static final int NOTE_COLUMN_ID = 0;                // 列 ID
        private static final int NOTE_COLUMN_MODIFIED_DATE = 1;     // 列 MODIFIED_DATE
        private static final int NOTE_COLUMN_SNIPPET = 2;           // 列 SNIPPET

        private static final String[] DATA_PROJECTION = {
                DataColumns.CONTENT,      // 数据的主体内容
                DataColumns.MIME_TYPE,    // 数据的 MIME 类型
                DataColumns.DATA1,        // 此列可能包含电话号码、电子邮件地址等。
                DataColumns.DATA2,        // 此列可能包含电话类型（家庭、工作等）、电子邮件类型等。
                DataColumns.DATA3,        // 通常情况下，此列的使用与 DATA2 相同。
                DataColumns.DATA4         // 保留供将来使用。
        };

        private static final int DATA_COLUMN_CONTENT = 0;          // 列 CONTENT
        private static final int DATA_COLUMN_MIME_TYPE = 1;        // 列 MIME_TYPE
        private static final int DATA_COLUMN_CALL_DATE = 2;        // 列 CALL_DATE


        private static final int DATA_COLUMN_PHONE_NUMBER = 4;     // 列 PHONE_NUMBER

        private final String[] TEXT_FORMAT;                       // 存储导出文本的格式
        private static final int FORMAT_FOLDER_NAME = 0;           // 文件夹名对应的格式
        private static final int FORMAT_NOTE_DATE = 1;             // 笔记日期对应的格式
        private static final int FORMAT_NOTE_CONTENT = 2;          // 笔记内容对应的格式

        private Context mContext;                                  // 应用程序上下文
        private String mFileName;                                  // 导出文件的名称
        private String mFileDirectory;                             // 导出文件所在的目录


        /**
         * 构造函数，用于创建 TextExport 类的实例。
         *
         * @param context 应用程序上下文。
         */
        public TextExport(Context context) {
            TEXT_FORMAT = context.getResources().getStringArray(R.array.format_for_exported_note); // 从资源中获取导出文本的格式
            mContext = context;                              // 保存应用程序上下文
            mFileName = "";                                  // 导出文件名称为空
            mFileDirectory = "";                             // 导出文件所在目录为空
        }

        /**
         * 获取指定索引处的导出文本格式。
         *
         * @param id 索引值。
         * @return 指定索引处的导出文本格式。
         */
        private String getFormat(int id) {
            return TEXT_FORMAT[id];                           // 返回指定索引处的导出文本格式
        }

        /**
         * Export the folder identified by folder id to text
         */
        /**
         * 导出指定笔记本中的所有笔记到文本文件。
         *
         * @param folderId 笔记本的标识符。
         * @param ps       PrintStream 对象，用于写入导出的文本数据。
         */
        private void exportFolderToText(String folderId, PrintStream ps) {
            // 查询属于该笔记本的笔记
            Cursor notesCursor = mContext.getContentResolver().query(Notes.CONTENT_NOTE_URI,
                    NOTE_PROJECTION, NoteColumns.PARENT_ID + "=?", new String[]{folderId}, null);

            if (notesCursor != null) {
                if (notesCursor.moveToFirst()) {
                    do {
                        // 打印笔记的最近修改日期
                        ps.println(String.format(getFormat(FORMAT_NOTE_DATE), DateFormat.format(
                                mContext.getString(R.string.format_datetime_mdhm),
                                notesCursor.getLong(NOTE_COLUMN_MODIFIED_DATE))));
                        // 查询属于该笔记的数据
                        String noteId = notesCursor.getString(NOTE_COLUMN_ID);
                        exportNoteToText(noteId, ps);     // 将笔记导出到文本文件
                    } while (notesCursor.moveToNext());
                }
                notesCursor.close();
            }
        }

        /**
         * 将指定 ID 的笔记导出到打印流中。
         *
         * @param noteId 要导出的笔记 ID。
         * @param ps     要写入笔记的打印流对象。
         */
        private void exportNoteToText(String noteId, PrintStream ps) {
            // 查询与该笔记关联的数据项
            Cursor dataCursor = mContext.getContentResolver().query(
                    Notes.CONTENT_DATA_URI, // 查询的 URI
                    DATA_PROJECTION, // 要查询的列名
                    DataColumns.NOTE_ID + "=?", // WHERE 子句
                    new String[]{noteId}, // WHERE 子句中占位符的值
                    null // 排序方式
            );

            if (dataCursor != null) {
                if (dataCursor.moveToFirst()) {
                    do {
                        // 获取当前数据项的 MIME 类型
                        String mimeType = dataCursor.getString(DATA_COLUMN_MIME_TYPE);
                        if (DataConstants.CALL_NOTE.equals(mimeType)) {
                            // 处理通话记录类型的笔记
                            String phoneNumber = dataCursor.getString(DATA_COLUMN_PHONE_NUMBER); // 电话号码
                            long callDate = dataCursor.getLong(DATA_COLUMN_CALL_DATE); // 通话日期
                            String location = dataCursor.getString(DATA_COLUMN_CONTENT); // 通话位置

                            if (!TextUtils.isEmpty(phoneNumber)) {
                                // 如果电话号码不为空，就将其写入打印流中
                                ps.println(String.format(getFormat(FORMAT_NOTE_CONTENT), phoneNumber));
                            }

                            // 将通话日期格式化为字符串，并写入打印流中
                            ps.println(String.format(getFormat(FORMAT_NOTE_CONTENT),
                                    DateFormat.format(mContext.getString(R.string.format_datetime_mdhm), callDate)));

                            if (!TextUtils.isEmpty(location)) {
                                // 如果通话位置不为空，就将其写入打印流中
                                ps.println(String.format(getFormat(FORMAT_NOTE_CONTENT), location));
                            }
                        } else if (DataConstants.NOTE.equals(mimeType)) {
                            // 处理普通笔记类型
                            String content = dataCursor.getString(DATA_COLUMN_CONTENT); // 笔记内容

                            if (!TextUtils.isEmpty(content)) {
                                // 如果笔记内容不为空，就将其写入打印流中
                                ps.println(String.format(getFormat(FORMAT_NOTE_CONTENT), content));
                            }
                        }
                    } while (dataCursor.moveToNext());
                }

                // 关闭游标
                dataCursor.close();
            }

            // 在每个笔记之间输出一个分隔符
            try {
                ps.write(new byte[]{Character.LINE_SEPARATOR, Character.LETTER_NUMBER});
            } catch (IOException e) {
                Log.e(TAG, e.toString());
            }
        }

        /**
         * 将笔记导出为纯文本文件。
         *
         * @return 导出的状态码。
         */
        public int exportToText() {
            // 检查外部存储是否可用
            if (!externalStorageAvailable()) {
                Log.d(TAG, "Media was not mounted");
                return STATE_SD_CARD_UNMOUONTED;
            }

            // 获取打印流对象，准备写入数据
            PrintStream ps = getExportToTextPrintStream();
            if (ps == null) {
                Log.e(TAG, "get print stream error");
                return STATE_SYSTEM_ERROR;
            }

            // 先导出所有的文件夹及其笔记
            Cursor folderCursor = mContext.getContentResolver().query(
                    Notes.CONTENT_NOTE_URI, // 查询的 URI
                    NOTE_PROJECTION, // 要查询的列名
                    "(" + NoteColumns.TYPE + "=" + Notes.TYPE_FOLDER + " AND "
                            + NoteColumns.PARENT_ID + "<>" + Notes.ID_TRASH_FOLER + ") OR "
                            + NoteColumns.ID + "=" + Notes.ID_CALL_RECORD_FOLDER, // WHERE 子句
                    null, // WHERE 子句中占位符的值
                    null // 排序方式
            );

            if (folderCursor != null) {
                if (folderCursor.moveToFirst()) {
                    do {
                        // 打印文件夹名称
                        String folderName = "";
                        if (folderCursor.getLong(NOTE_COLUMN_ID) == Notes.ID_CALL_RECORD_FOLDER) {
                            folderName = mContext.getString(R.string.call_record_folder_name);
                        } else {
                            folderName = folderCursor.getString(NOTE_COLUMN_SNIPPET);
                        }
                        if (!TextUtils.isEmpty(folderName)) {
                            ps.println(String.format(getFormat(FORMAT_FOLDER_NAME), folderName));
                        }
                        String folderId = folderCursor.getString(NOTE_COLUMN_ID);
                        // 导出当前文件夹下的所有笔记
                        exportFolderToText(folderId, ps);
                    } while (folderCursor.moveToNext());
                }

                // 关闭游标
                folderCursor.close();
            }

            // 导出根目录下的笔记
            Cursor noteCursor = mContext.getContentResolver().query(
                    Notes.CONTENT_NOTE_URI, // 查询的 URI
                    NOTE_PROJECTION, // 要查询的列名
                    NoteColumns.TYPE + "=" + +Notes.TYPE_NOTE + " AND " + NoteColumns.PARENT_ID + "=0", // WHERE 子句
                    null, // WHERE 子句中占位符的值
                    null // 排序方式
            );

            if (noteCursor != null) {
                if (noteCursor.moveToFirst()) {
                    do {
                        // 打印笔记的最后修改时间
                        ps.println(String.format(getFormat(FORMAT_NOTE_DATE), DateFormat.format(
                                mContext.getString(R.string.format_datetime_mdhm),
                                noteCursor.getLong(NOTE_COLUMN_MODIFIED_DATE))));
                        // 导出当前笔记的内容
                        String noteId = noteCursor.getString(NOTE_COLUMN_ID);
                        exportNoteToText(noteId, ps);
                    } while (noteCursor.moveToNext());
                }

                // 关闭游标
                noteCursor.close();
            }

            // 关闭打印流
            ps.close();
            return STATE_SUCCESS;
        }
        /**
         * Get a print stream pointed to the file {@generateExportedTextFile}
         */
        /**
         * 生成用于将数据输出到文本文件中的PrintStream对象。
         *
         * @return 用于将数据输出到文本文件中的PrintStream对象。
         */
        private PrintStream getExportToTextPrintStream() {
            // 生成指定路径和名称的文本文件，保存在SD卡上。
            File file = generateFileMountedOnSDcard(mContext, R.string.file_path,
                    R.string.file_name_txt_format);
            if (file == null) {
                // 如果创建文件失败，则记录错误日志并返回null。
                Log.e(TAG, "create file to exported failed");
                return null;
            }
            // 将文件名保存在成员变量中。
            mFileName = file.getName();
            // 将文件目录路径保存在成员变量中。
            mFileDirectory = mContext.getString(R.string.file_path);
            PrintStream ps = null;
            try {
                // 创建一个新的FileOutputStream对象，并将其包装在PrintStream对象中。
                FileOutputStream fos = new FileOutputStream(file);
                ps = new PrintStream(fos);
            } catch (FileNotFoundException e) {
                // 如果找不到文件，则打印堆栈跟踪并返回null。
                e.printStackTrace();
                return null;
            } catch (NullPointerException e) {
                // 如果发生空指针异常，则打印堆栈跟踪并返回null。
                e.printStackTrace();
                return null;
            }
            // 返回用于将数据输出到文本文件中的PrintStream对象。
            return ps;
        }
    }

        /**
     * Generate the text file to store imported data
     */
    /**
     * 在SD卡上创建目录和指定格式的文件。
     *
     * @param context 上下文对象。
     * @param filePathResId 文件路径的字符串资源ID。
     * @param fileNameFormatResId 文件名格式的字符串资源ID。
     * @return 生成的文件对象，如果创建失败则返回null。
     */
    private static File generateFileMountedOnSDcard(Context context, int filePathResId, int fileNameFormatResId) {
        // 创建一个StringBuilder对象，用于拼接文件路径和名称。
        StringBuilder sb = new StringBuilder();
        // 将外部存储器目录添加到StringBuilder中。
        sb.append(Environment.getExternalStorageDirectory());
        // 将文件路径的字符串资源ID添加到StringBuilder中。
        sb.append(context.getString(filePathResId));
        // 创建一个包含文件路径的File对象。
        File filedir = new File(sb.toString());
        // 将文件名格式化并将其添加到StringBuilder末尾。
        sb.append(context.getString(fileNameFormatResId,
                DateFormat.format(context.getString(R.string.format_date_ymd), System.currentTimeMillis())));
        // 创建一个包含完整文件路径的File对象。
        File file = new File(sb.toString());

        try {
            // 如果文件夹不存在，则创建它。
            if (!filedir.exists()) {
                filedir.mkdir();
            }
            // 如果文件不存在，则创建它。
            if (!file.exists()) {
                file.createNewFile();
            }
            // 返回创建的文件对象。
            return file;
        } catch (SecurityException e) {
            // 捕获安全异常，并打印错误日志。
            e.printStackTrace();
        } catch (IOException e) {
            // 捕获I/O异常，并打印错误日志。
            e.printStackTrace();
        }

        // 如果创建文件失败，则返回null。
        return null;
    }

}


