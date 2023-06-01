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
import android.graphics.Typeface;
import android.preference.PreferenceManager;

import net.micode.notes.R;
import net.micode.notes.ui.NotesPreferenceActivity;

public class ResourceParser {

    public static final int YELLOW = 0;
    public static final int BLUE = 1;
    public static final int WHITE = 2;
    public static final int GREEN = 3;
    public static final int RED = 4;

    public static final int BG_DEFAULT_COLOR = YELLOW;

    public static final int TEXT_SMALL = 0;
    public static final int TEXT_MEDIUM = 1;
    public static final int TEXT_LARGE = 2;
    public static final int TEXT_SUPER = 3;

    public static final int BG_DEFAULT_FONT_SIZE = TEXT_MEDIUM;

    public static class NoteBgResources {
        // 存储五种不同颜色的背景图片资源id
        private final static int[] BG_EDIT_RESOURCES = new int[]{
                R.drawable.edit_yellow,  // 黄色背景图片
                R.drawable.edit_blue,    // 蓝色背景图片
                R.drawable.edit_white,   // 白色背景图片
                R.drawable.edit_green,   // 绿色背景图片
                R.drawable.edit_red      // 红色背景图片
        };

        // 存储五种不同颜色的标题栏背景图片资源id
        private final static int[] BG_EDIT_TITLE_RESOURCES = new int[]{
                R.drawable.edit_title_yellow,  // 黄色标题栏背景图片
                R.drawable.edit_title_blue,    // 蓝色标题栏背景图片
                R.drawable.edit_title_white,   // 白色标题栏背景图片
                R.drawable.edit_title_green,   // 绿色标题栏背景图片
                R.drawable.edit_title_red      // 红色标题栏背景图片
        };

        /**
         * 获取给定 id 对应的笔记项背景资源 ID
         *
         * @param id 笔记项背景色的 ID，从 0 到 4 分别表示黄、蓝、白、绿、红五种颜色
         * @return 笔记项背景资源的 ID
         */
        public static int getNoteBgResource(int id) {
            return BG_EDIT_RESOURCES[id];
        }

        /**
         * 获取给定 id 对应的标题栏背景资源 ID
         *
         * @param id 标题栏背景色的 ID，从 0 到 2 分别表示橙色、蓝色和绿色三种颜色
         * @return 标题栏背景资源的 ID
         */
        public static int getNoteTitleBgResource(int id) {
            return BG_EDIT_TITLE_RESOURCES[id];
        }

    }

    /**
     * 获取笔记项的默认背景 ID
     *
     * @param context 上下文对象
     * @return 笔记项的默认背景资源 ID
     */
    public static int getDefaultBgId(Context context) {
        // 检查应用程序的共享首选项中是否启用了自定义笔记项背景颜色
        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
                NotesPreferenceActivity.PREFERENCE_SET_BG_COLOR_KEY, false)) {
            // 如果已经启用，则生成一个随机数并返回对应的笔记项背景资源 ID
            return (int) (Math.random() * NoteBgResources.BG_EDIT_RESOURCES.length);
        } else {
            // 如果未启用，则返回默认背景颜色（即 BG_DEFAULT_COLOR）所对应的资源 ID。
            return BG_DEFAULT_COLOR;
        }
    }


    public static class NoteItemBgResources {
        /**
         * 笔记项在列表中作为第一个条目时的背景资源 ID
         */
        private final static int[] BG_FIRST_RESOURCES = new int[]{
                R.drawable.list_yellow_up,
                R.drawable.list_blue_up,
                R.drawable.list_white_up,
                R.drawable.list_green_up,
                R.drawable.list_red_up
        };

        /**
         * 笔记项在列表中作为中间条目时的背景资源 ID
         */
        private final static int[] BG_NORMAL_RESOURCES = new int[]{
                R.drawable.list_yellow_middle,
                R.drawable.list_blue_middle,
                R.drawable.list_white_middle,
                R.drawable.list_green_middle,
                R.drawable.list_red_middle
        };

        /**
         * 笔记项在列表中作为最后一个条目时的背景资源 ID
         */
        private final static int[] BG_LAST_RESOURCES = new int[]{
                R.drawable.list_yellow_down,
                R.drawable.list_blue_down,
                R.drawable.list_white_down,
                R.drawable.list_green_down,
                R.drawable.list_red_down,
        };


        /**
         * 笔记项在列表中作为唯一条目时的背景资源 ID
         */
        private final static int[] BG_SINGLE_RESOURCES = new int[]{
                R.drawable.list_yellow_single,
                R.drawable.list_blue_single,
                R.drawable.list_white_single,
                R.drawable.list_green_single,
                R.drawable.list_red_single
        };

        /**
         * 获取给定 id 对应的笔记项在列表中作为第一个条目时的背景资源 ID
         *
         * @param id 背景颜色的 ID，从 0 到 4 分别表示黄、蓝、白、绿、红五种颜色
         * @return 笔记项在列表中作为第一个条目时的背景资源 ID
         */
        public static int getNoteBgFirstRes(int id) {
            return BG_FIRST_RESOURCES[id];
        }

        /**
         * 获取给定 id 对应的笔记项在列表中作为最后一个条目时的背景资源 ID
         *
         * @param id 背景颜色的 ID，从 0 到 4 分别表示黄、蓝、白、绿、红五种颜色
         * @return 笔记项在列表中作为最后一个条目时的背景资源 ID
         */
        public static int getNoteBgLastRes(int id) {
            return BG_LAST_RESOURCES[id];
        }

        /**
         * 获取给定 id 对应的笔记项在列表中作为唯一条目时的背景资源 ID
         *
         * @param id 背景颜色的 ID，从 0 到 4 分别表示黄、蓝、白、绿、红五种颜色
         * @return 笔记项在列表中作为唯一条目时的背景资源 ID
         */
        public static int getNoteBgSingleRes(int id) {
            return BG_SINGLE_RESOURCES[id];
        }


        /**
         * 获取给定 id 对应的笔记项在列表中作为中间条目时的背景资源 ID
         *
         * @param id 背景颜色的 ID，从 0 到 4 分别表示黄、蓝、白、绿、红五种颜色
         * @return 笔记项在列表中作为中间条目时的背景资源 ID
         */
        public static int getNoteBgNormalRes(int id) {
            return BG_NORMAL_RESOURCES[id];
        }

        /**
         * 返回文件夹列表中文件夹的背景资源 ID，该资源是一个 drawable（R.drawable.list_folder）
         *
         * @return 文件夹列表中文件夹的背景资源 ID
         */
        public static int getFolderBgRes() {
            return R.drawable.list_folder;
        }

    }

    public static class WidgetBgResources {
        /**
         * 小部件在 2x2 模式下的背景资源 ID 数组
         */
        private final static int[] BG_2X_RESOURCES = new int[]{
                R.drawable.widget_2x_yellow,
                R.drawable.widget_2x_blue,
                R.drawable.widget_2x_white,
                R.drawable.widget_2x_green,
                R.drawable.widget_2x_red,
        };

        /**
         * 获取给定 id 对应的小部件在 2x2 模式下的背景资源 ID
         *
         * @param id 背景颜色的 ID，从 0 到 4 分别表示黄、蓝、白、绿、红五种颜色
         * @return 给定 id 对应的小部件在 2x2 模式下的背景资源 ID
         */
        public static int getWidget2xBgResource(int id) {
            return BG_2X_RESOURCES[id];
        }

        /**
         * 小部件在 4x4 模式下的背景资源 ID 数组
         */
        private final static int[] BG_4X_RESOURCES = new int[]{
                R.drawable.widget_4x_yellow,
                R.drawable.widget_4x_blue,
                R.drawable.widget_4x_white,
                R.drawable.widget_4x_green,
                R.drawable.widget_4x_red
        };

        /**
         * 获取给定 id 对应的小部件在 4x4 模式下的背景资源 ID
         *
         * @param id 背景颜色的 ID，从 0 到 4 分别表示黄、蓝、白、绿、红五种颜色
         * @return 给定 id 对应的小部件在 4x4 模式下的背景资源 ID
         */
        public static int getWidget4xBgResource(int id) {
            return BG_4X_RESOURCES[id];
        }

    }

    public static class TextAppearanceResources {
        /**
         * 文字外观资源 ID 数组
         */
        private final static int[] TEXTAPPEARANCE_RESOURCES = new int[]{
                R.style.TextAppearanceNormal,
                R.style.TextAppearanceMedium,
                R.style.TextAppearanceLarge,
                R.style.TextAppearanceSuper
        };

        /**
         * 获取给定 id 对应的文字外观资源 ID
         *
         * @param id 文字外观的 ID，从 0 到 3 分别表示 Normal、Medium、Large、Super 四种大小
         * @return 给定 id 对应的文字外观资源 ID
         */
        public static int getTexAppearanceResource(int id) {
            /**
             * HACKME: 修复在共享偏好中存储资源 ID 的 bug。
             * 在某些情况下，id 可能大于资源数组的长度，在这种情况下，
             * 返回 {@link ResourceParser#BG_DEFAULT_FONT_SIZE}
             */
            if (id >= TEXTAPPEARANCE_RESOURCES.length) {
                return BG_DEFAULT_FONT_SIZE;
            }
            return TEXTAPPEARANCE_RESOURCES[id];
        }

        /**
         * 文字字体资源
         */
        public final static int[] TYPEFACE_RES_LIST = new int[]{
                R.font.roboto,
                R.font.consolas,
                R.font.ubuntu,
                R.font.poppins
        };

        private static final String TYPEFACE_STR_ROBOTO = "Roboto";
        private static final String TYPEFACE_STR_CONSOLAS = "Consolas";
        private static final String TYPEFACE_STR_UBUNTO = "Ubuntu";
        private static final String TYPEFACE_STR_POPPINS = "Poppins";


        public final static String[] TYPEFACE_STR_LIST = new String[]{
                TYPEFACE_STR_ROBOTO,
                TYPEFACE_STR_CONSOLAS,
                TYPEFACE_STR_UBUNTO,
                TYPEFACE_STR_POPPINS
        };

        /**
         * 获取文字外观资源 ID 数组的长度
         *
         * @return 文字外观资源 ID 数组的长度
         */
        public static int getResourcesSize() {
            return TEXTAPPEARANCE_RESOURCES.length;
        }
    }
}
