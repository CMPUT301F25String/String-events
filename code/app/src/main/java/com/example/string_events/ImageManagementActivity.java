package com.example.string_events;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ImageManagementActivity extends AppCompatActivity {

    private ImageListAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_image_management_screen);

        ImageButton back = findViewById(R.id.btn_back);
        back.setOnClickListener(v -> finish());

        // 示例数据：三张相同图片（你后续可替换为真实数据/从数据库读取）
        List<Integer> images = new ArrayList<>(Arrays.asList(
                R.drawable.sample_event,
                R.drawable.sample_event,
                R.drawable.sample_event
        ));

        ListView listView = findViewById(R.id.image_list_view);
        adapter = new ImageListAdapter(this, images);
        listView.setAdapter(adapter);

        // 点击选择要删除的图片
        listView.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) ->
                adapter.setSelectedPosition(position)
        );

        Button delete = findViewById(R.id.btn_delete);
        delete.setOnClickListener(v -> {
            if (adapter.getSelectedPosition() == -1) {
                Toast.makeText(this, "Please tap an image to select, then delete.", Toast.LENGTH_SHORT).show();
            } else {
                adapter.removeSelected();
                Toast.makeText(this, "Image deleted.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
