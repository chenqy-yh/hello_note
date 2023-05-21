package net.micode.notes.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import net.micode.notes.tool.NoteHttpServer;
import net.micode.notes.tool.NoteRemoteConfig;
import net.micode.notes.tool.SyncNoteUtils;
import net.micode.notes.tool.UIUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class NoteDelButton extends androidx.appcompat.widget.AppCompatButton implements View.OnClickListener {
    private static final int CROSS_LENGTH = 40;
    private static final int STROKE_WIDTH = 7;
    private static final int BTN_BG_COLOR = Color.BLACK;
    private SyncListAdapter adapter;
    private ProgressBar progressBar;
    private Context context;


    public NoteDelButton(@NonNull @NotNull Context context) {
        super(context);
    }

    public NoteDelButton(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public NoteDelButton(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        init();
    }

    private void init() {
        this.context = getContext();
        this.progressBar = new ProgressBar(context);
        setClickable(true);
        this.setOnClickListener(this);
    }

    public void setAdapter(SyncListAdapter adapter){
        this.adapter = adapter;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawButtonShape(canvas);
    }

    private void drawButtonShape(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(BTN_BG_COLOR);
        paint.setAntiAlias(true);
        float radius = Math.min(getWidth(), getHeight()) / 2f;
        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(centerX, centerY, radius, paint);

        canvas.save(); // 保存当前的绘制状态
        canvas.rotate(45, centerX, centerY); // 旋转45度

        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(STROKE_WIDTH);
        canvas.drawLine(centerX, centerY - CROSS_LENGTH / 2, centerX, centerY + CROSS_LENGTH / 2, paint);
        canvas.drawLine(centerX - CROSS_LENGTH / 2, centerY, centerX + CROSS_LENGTH / 2, centerY, paint);

        canvas.restore(); // 恢复原始的绘制状态
    }

    @Override
    public void onClick(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("确定删除这条记录?");
        builder.setPositiveButton("确定", (dialog, which) -> {
            //设置隐藏背景
            progressBar.setVisibility(View.VISIBLE);

            NoteHttpServer server = new NoteHttpServer();
            HttpUrl url = HttpUrl.parse(NoteRemoteConfig.generateUrl("/note/deletenote"));
            JSONObject body = new JSONObject();
                SyncNoteUtils.SyncNoteItemData item = (SyncNoteUtils.SyncNoteItemData) this.getTag();
            try {
                body.put("note_token",item.getNote_token());
                server.sendAsyncPostRequest(url, body.toString(), NoteHttpServer.BodyType.JSON, new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        UIUtils.sendMsg((Activity) context, "删除失败");
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        if(response.code() == NoteRemoteConfig.RESPONSE_SUCCESS){
                            UIUtils.sendMsg((Activity) context, "删除成功");
                            UIUtils.runInUI((Activity) context,() -> {
                                adapter.deleteItem(item);
                            });
                        }else{
                            UIUtils.sendMsg((Activity) context, "删除失败");
                        }
                    }
                });

            } catch (JSONException e) {
                UIUtils.sendMsg((Activity) context, "删除失败");
            }
            progressBar.setVisibility(GONE);
        });
        builder.setNegativeButton("取消", (dialog, which) -> dialog.cancel());
        builder.show();
    }
}
