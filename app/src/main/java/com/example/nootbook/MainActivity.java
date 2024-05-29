package com.example.nootbook;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private List<String> label_names;
    private float dp_to_px_ratio;  // 将dp转为像素值时乘的比例因子

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        label_names=new ArrayList<>();

        label_names.add("Label-hide: You have not created a label, this label is really really long, long long long long long...");
        label_names.add("Label-show: Again! You have not created a label, this label is really really long, long long long long long...");
        label_names.add("Note------: 1");
        label_names.add("Note------: 2");
        label_names.add("Note------: 3");
        label_names.add("Note------: 4");
        label_names.add("Note------: 5");
        label_names.add("Note------: 6");
        label_names.add("Note------: 7");
        label_names.add("Label-hide: Last! You have not created a label, this label is really really long, long long long long long...");

        dp_to_px_ratio = getResources().getDisplayMetrics().density;

        // 笔记列表
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.label_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this );
        //设置布局管理器
        recyclerView.setLayoutManager(layoutManager);
        //设置为垂直布局，这也是默认的
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        //设置Adapter
        labelRecycleViewAdapter label_recycle_view_adapter = new labelRecycleViewAdapter(this, this.label_names, this.dp_to_px_ratio);
        label_recycle_view_adapter.setOnItemClickListener(new labelRecycleViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, int clicked_item) {
                String item_name;
                if(clicked_item==0){
                    item_name = "background";
                }
                else if(clicked_item==1){
                    item_name = "show_or_hide";
                }
                else if(clicked_item==2){
                    item_name = "name_or_title";
                }
                else if(clicked_item==3){
                    item_name = "add_or_edit";
                }
                else if(clicked_item==4){
                    item_name = "delete";
                }
                else{
                    item_name = "error-unknown" + clicked_item;
                }
                Log.e(String.valueOf(this), "position: "+position+", item: "+item_name+" been clicked");
            }
        });
        recyclerView.setAdapter(label_recycle_view_adapter);
        //设置分隔线
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        //设置增加或删除条目的动画
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }
}

