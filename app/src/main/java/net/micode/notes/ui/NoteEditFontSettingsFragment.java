package net.micode.notes.ui;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.Nullable;
import com.google.gson.Gson;
import net.micode.notes.R;
import net.micode.notes.tool.ResourceParser;

import java.util.ArrayList;
import java.util.Arrays;

public class NoteEditFontSettingsFragment extends Fragment implements View.OnClickListener{

    private Context context;
    private View convertView;


    private SeekBar font_size_seekbar;
    private TextView font_size_text;
    private Button btn_back;
    private Spinner typeface_spinner;
    private NoteTypefaceAdapter noteTypefaceAdapter;
    private SharedPreferences mSharedPrefs;
    private static final String[] FONT_SIZE_DESC = {"小", "中", "大", "特大"};



    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        this.context = getActivity();
        this.convertView = inflater.inflate(R.layout.note_edit_settings_font, container, false);
        bindView(convertView);
        return this.convertView;
    }

    private void bindView(View convertView){
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        int fontSize = mSharedPrefs.getInt(NoteEditActivity.PREFERENCE_FONT_SIZE, 1);
        btn_back = convertView.findViewById(R.id.note_edit_font_settings_back);
        btn_back.setOnClickListener(this);
        font_size_seekbar = convertView.findViewById(R.id.font_size_seekbar);
        font_size_text = convertView.findViewById(R.id.font_size_desc);
        font_size_seekbar.setProgress(fontSize);
        font_size_text.setText(FONT_SIZE_DESC[fontSize]);
        font_size_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                font_size_text.setText(FONT_SIZE_DESC[progress]);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Intent it = new Intent(NoteEditActivity.CHANGE_FONT_SIZE);
                it.putExtra("font_size", seekBar.getProgress());
                //发送It到noteditactivity
                context.sendBroadcast(it);
            }
        });

        //字体下拉选项框
        typeface_spinner= convertView.findViewById(R.id.typeface_spinner);
        ArrayList<NoteTypefaceAdapter.ItemData> typeface_list = new ArrayList<>();
        for (int i = 0; i < ResourceParser.TextAppearanceResources.TYPEFACE_STR_LIST.length; i++) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                typeface_list.add(new NoteTypefaceAdapter.ItemData(
                        ResourceParser.TextAppearanceResources.TYPEFACE_STR_LIST[i],
                        getResources().getFont(ResourceParser.TextAppearanceResources.TYPEFACE_RES_LIST[i])
                        ));
            }else{
                typeface_list.add(new NoteTypefaceAdapter.ItemData(
                        ResourceParser.TextAppearanceResources.TYPEFACE_STR_LIST[i],
                        Typeface.DEFAULT
                ));
            }
        }
        noteTypefaceAdapter = new NoteTypefaceAdapter(context, android.R.layout.simple_spinner_item, typeface_list);
        typeface_spinner.setAdapter(noteTypefaceAdapter);
        typeface_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Intent it = new Intent(NoteEditActivity.CHANGE_FONT_FAMILY);
                String typeface_name = noteTypefaceAdapter.getItem(position).getTypeface_name();
                int tar_idx= Arrays.asList(ResourceParser.TextAppearanceResources.TYPEFACE_STR_LIST).indexOf(typeface_name);
                it.putExtra("typeface_res", ResourceParser.TextAppearanceResources.TYPEFACE_RES_LIST[tar_idx]);
                //发送It到noteditactivity
                context.sendBroadcast(it);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.note_edit_font_settings_back:
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out, android.R.animator.fade_in, android.R.animator.fade_out);
                ft.replace(R.id.note_edit_settings_container, new NoteEditSettingsMenu()).commit();
                break;
        }
    }
}
