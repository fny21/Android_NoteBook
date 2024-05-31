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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity {

    private List<String> label_names;
    private List<String> list_for_adapter;
    private TreeMap<Integer, Integer> adapter_map_to_label;
    private TreeMap<Integer, Integer> label_map_to_adapter;
    private float dp_to_px_ratio;  // 将dp转为像素值时乘的比例因子
    private int delete_label_position;
    private int unlabeled_label_position;
    private TreeMap<Integer, Integer> deleted_label_data_map;
    private labelRecycleViewAdapter label_recycle_view_adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        label_names = new ArrayList<>();
        list_for_adapter = new ArrayList<>();
        adapter_map_to_label = new TreeMap<>();
        label_map_to_adapter = new TreeMap<>();
        deleted_label_data_map = new TreeMap<>();
        delete_label_position = 0;
        unlabeled_label_position = 0;

        label_names.add("Label-hide: You have not created a label, this label is really really long, long long long long long...");
        label_names.add("Note------: 1");
        label_names.add("Note------: 2");
        label_names.add("Note------: 3");
        label_names.add("Label-show: Again! You have not created a label, this label is really really long, long long long long long...");
        label_names.add("Note------: 4");
        label_names.add("Note------: 5");
        label_names.add("Note------: 6");
        label_names.add("Note------: 7");
        label_names.add("Label-hide: Last! You have not created a label, this label is really really long, long long long long long...");
        label_names.add("Label-hide: Unlabeled notes");
        label_names.add("Label-hide: Recently Deleted");

        dp_to_px_ratio = getResources().getDisplayMetrics().density;

        // 笔记列表
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.label_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this );
        //设置布局管理器
        recyclerView.setLayoutManager(layoutManager);
        //设置为垂直布局，这也是默认的
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        //传给Adapter的list
        boolean status = false;  // 0 for normal, 1 for add notes of one label
        for (int i=0; i<label_names.size(); i++){
            String one_item = label_names.get(i);
            if(one_item.startsWith("Label") && one_item.substring(12).startsWith("Recently Deleted")){
                delete_label_position = i;
            }
            if(one_item.startsWith("Label") && one_item.substring(12).startsWith("Unlabeled notes")){
                unlabeled_label_position = i;
            }
            if(!status){
                if(one_item.startsWith("Label-hide:")){
                    put_new_adapter_label_map(list_for_adapter.size(), i);
                    list_for_adapter.add(one_item);
                }
                else if(one_item.startsWith("Label-show:")){
                    put_new_adapter_label_map(list_for_adapter.size(), i);
                    list_for_adapter.add(one_item);
                    status=true;
                }
            }
            else{
                if(one_item.startsWith("Note-")){
                    list_for_adapter.add(one_item);
                }
                else if(one_item.startsWith("Label-show:")){
                    put_new_adapter_label_map(list_for_adapter.size(), i);
                    list_for_adapter.add(one_item);
                }
                else if(one_item.startsWith("Label-hide:")){
                    put_new_adapter_label_map(list_for_adapter.size(), i);
                    list_for_adapter.add(one_item);
                    status=false;
                }
            }
        }
        //设置Adapter
        label_recycle_view_adapter = new labelRecycleViewAdapter(this, list_for_adapter, this.dp_to_px_ratio);
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

                if(clicked_item==1){  // "show_or_hide"
                    String temp_item_name = list_for_adapter.get(position);
                    int index_in_label_names = adapter_map_to_label.get(position);

                    if (temp_item_name.startsWith("Label-hide:")) {
                        change_in_label_and_adapter(position, index_in_label_names, "Label-show: " + temp_item_name.substring(12));
                        int new_changed_item_number = adapter_hide_to_show(position+1, index_in_label_names+1);
                        change_map(position, new_changed_item_number);
                    }
                    else if(temp_item_name.startsWith("Label-show: ")){
                        change_in_label_and_adapter(position, index_in_label_names, "Label-hide: " + temp_item_name.substring(12));
                        int hide_item_number = adapter_show_to_hide(position+1);
                        change_map(position, -hide_item_number);
                    }
                    else{
                        Log.e(String.valueOf(this), "unexpected prefix in show_or_hide: "+temp_item_name);
                    }
                }
                else if(clicked_item==3){  // "add_or_edit"
                    String temp_item_name = list_for_adapter.get(position);
                    if(temp_item_name.startsWith("Note-")){

                    }
                    else {
                        // change shown list
                        int index_in_label_names = adapter_map_to_label.get(position);
                        if (temp_item_name.startsWith("Label-hide:")) {
                            change_in_label_and_adapter(position, index_in_label_names, "Label-show: " + temp_item_name.substring(12));
                            String new_note_name = "Note------: added note";
                            add_in_label_and_adapter(position+1, index_in_label_names+1, new_note_name);
                            int new_shown_item_number = 1 + adapter_hide_to_show(position+2, index_in_label_names+2);
                            change_map(position, new_shown_item_number, 1);
                        }
                        else if (temp_item_name.startsWith("Label-show:")) {
                            String new_note_name = "Note------: added note";
                            add_in_label_and_adapter(position+1, index_in_label_names+1, new_note_name);
                            change_map(position, 1, 1);
                        }
                        else{
                            Log.e(String.valueOf(this), "unexpected prefix in add_or_edit: "+temp_item_name);
                        }
                    }
                }
                else if(clicked_item==4){  // "delete"
                    String temp_item_name = list_for_adapter.get(position);
                    if(temp_item_name.startsWith("Note-")){
                        if(temp_item_name.startsWith("Note---del")){  // 彻底删除
                            int position_in_label = locate_in_label_from_position_in_adapter(position);
                            delete_from_adapter_and_label(position, position_in_label);
                            change_map(position, -1, -1);
                        }
                        else{  // 移动到最近删除
                            move_note_to_recent_delete(position, temp_item_name);
                        }
                    }
                    else if(temp_item_name.startsWith("Label-")){
                        if(temp_item_name.substring(12).startsWith("Recently Deleted")){  // 清空最近删除
                            int index_in_label_names = adapter_map_to_label.get(position);
                            if(index_in_label_names!=delete_label_position){
                                Log.e(String.valueOf(this), "delete_label_position is not right, please check!");
                            }
                            if(temp_item_name.startsWith("Label-hide:")){  // 如果隐藏，先展开
                                change_in_label_and_adapter(position, index_in_label_names, "Label-show: " + temp_item_name.substring(12));
                                int new_changed_item_number = adapter_hide_to_show(position+1, index_in_label_names+1);
                                change_map(position, new_changed_item_number);
                            }

                            // 删除所有笔记
                            for(int i=index_in_label_names+1; i<label_names.size();i++){
                                delete_from_adapter_and_label(position+i-index_in_label_names, i);
                            }
                            change_map(position+1, index_in_label_names+1-label_names.size());

                            // 最后再隐藏
                            change_in_label_and_adapter(position, index_in_label_names, "Label-hide: " + temp_item_name.substring(12));
                            int hide_item_number = adapter_show_to_hide(position+1);
                            if(hide_item_number!=0){
                                Log.e(String.valueOf(this), "after delete add, there is still note in recent delete, please check!");
                            }
                        }
                        else{  // 递归移动到最近删除
                            int index_in_label_names = adapter_map_to_label.get(position);
                            if(temp_item_name.startsWith("Label-hide:")){  // 如果隐藏，先展开
                                change_in_label_and_adapter(position, index_in_label_names, "Label-show: " + temp_item_name.substring(12));
                                int new_changed_item_number = adapter_hide_to_show(position+1, index_in_label_names+1);
                                change_map(position, new_changed_item_number);
                            }

                            delete_label_notes(position);

                            // delete label
                            if(!temp_item_name.substring(12).startsWith("Unlabeled notes")){
                                delete_from_adapter_and_label(position, index_in_label_names);
                                remove_label_adapter_map(position, index_in_label_names);
                                change_map(position, -1, -1);
                            }
                        }
                    }
                    else{
                        Log.e(String.valueOf(this), "unexpected name in delete: "+temp_item_name);
                    }
                }
            }
        });
        recyclerView.setAdapter(label_recycle_view_adapter);
        //设置分隔线
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        //设置增加或删除条目的动画
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    void put_new_adapter_label_map(int adapter_position, int label_position){
        adapter_map_to_label.put(adapter_position, label_position);
        label_map_to_adapter.put(label_position, adapter_position);
    }
    void remove_label_adapter_map(int adapter_position, int label_position){
        adapter_map_to_label.remove(adapter_position);
        label_map_to_adapter.remove(label_position);
    }

    void change_map(int start_position, int add_or_delete_num){  // label_name未改变
        Set<Integer> keys = adapter_map_to_label.keySet();
        List<Integer> keys_to_change = new ArrayList<>();
        List<Integer> values_to_change = new ArrayList<>();
        for(int key : keys){
            if(key>start_position){
                keys_to_change.add(key);
            }
        }
        for(int i=0; i<keys_to_change.size(); i++){
            int previous_map_index = adapter_map_to_label.get(keys_to_change.get(i));
            values_to_change.add(previous_map_index);
            remove_label_adapter_map(keys_to_change.get(i), previous_map_index);
        }
        for(int i=0; i<keys_to_change.size(); i++){
            put_new_adapter_label_map(keys_to_change.get(i)+add_or_delete_num, values_to_change.get(i));
        }
    }

    void change_map(int start_position, int adapter_add_or_delete_num, int label_name_add_or_delete_num){
        Set<Integer> keys = adapter_map_to_label.keySet();
        List<Integer> keys_to_change = new ArrayList<>();
        List<Integer> values_to_change = new ArrayList<>();
        for(int key : keys){
            if(key>start_position){
                keys_to_change.add(key);
            }
        }
        for(int i=0; i<keys_to_change.size(); i++){
            int previous_map_index = adapter_map_to_label.get(keys_to_change.get(i));
            values_to_change.add(previous_map_index);
            remove_label_adapter_map(keys_to_change.get(i), previous_map_index);
        }
        for(int i=0; i<keys_to_change.size(); i++){
            put_new_adapter_label_map(keys_to_change.get(i)+adapter_add_or_delete_num, values_to_change.get(i)+label_name_add_or_delete_num);
        }
    }

    void add_in_adapter(int position, String item_name){
        list_for_adapter.add(position, item_name);
        label_recycle_view_adapter.addData(position, item_name);
    }

    void add_in_label_and_adapter(int position_in_adapter, int position_in_label, String item_name){
        label_names.add(position_in_label, item_name);
        if(position_in_label<delete_label_position){  // this should always be true
            delete_label_position+=1;
        }
        else{
            Log.e(String.valueOf(this), "current position is "+position_in_label+", which is less than delete_label_position "+delete_label_position);
        }
        if(position_in_label<unlabeled_label_position){
            unlabeled_label_position+=1;
        }
        list_for_adapter.add(position_in_adapter, item_name);
        label_recycle_view_adapter.addData(position_in_adapter, item_name);
    }

    void change_in_label_and_adapter(int position_in_adapter, int position_in_label, String item_name){
        label_names.set(position_in_label, item_name);
        list_for_adapter.set(position_in_adapter, item_name);
        label_recycle_view_adapter.changeData(position_in_adapter, item_name);
    }

    int adapter_hide_to_show(int first_note_position_in_adapter, int first_note_position_in_label){
        int new_shown_item_number=0;
        for (int i = first_note_position_in_label; i < label_names.size(); i++) {
            String one_note_name = label_names.get(i);
            if (!one_note_name.startsWith("Note-")) {
                break;
            }
            new_shown_item_number+=1;
            add_in_adapter(first_note_position_in_adapter + i - first_note_position_in_label, one_note_name);
        }
        return new_shown_item_number;
    }

    int adapter_show_to_hide(int first_note_position_in_adapter){
        int hide_item_number=0;
        int current_position = first_note_position_in_adapter;
        while(true){
            if(current_position>=list_for_adapter.size()){
                break;
            }
            String one_note_name = list_for_adapter.get(current_position);
            if(!one_note_name.startsWith("Note-")){
                break;
            }
            hide_item_number+=1;
            list_for_adapter.remove(current_position);
            label_recycle_view_adapter.deleteData(current_position);
        }
        return hide_item_number;
    }

    int locate_in_label_from_position_in_adapter(int position_in_adapter){
        Set<Integer> keys = adapter_map_to_label.keySet();
        int previous_key=0;
        for(int key : keys){  // 这里的key应当不会和position_in_adapter重合
            if(key>position_in_adapter){
                break;
            }
            else{
                previous_key = key;
            }
        }
        int aim_label_in_labels = adapter_map_to_label.get(previous_key);
        return aim_label_in_labels + (position_in_adapter-previous_key);
    }

    void delete_from_adapter_and_label(int position_in_adapter, int position_in_label){
        label_names.remove(position_in_label);
        list_for_adapter.remove(position_in_adapter);
        label_recycle_view_adapter.deleteData(position_in_adapter);

        if(position_in_label<delete_label_position){
            delete_label_position-=1;
        }
        if(position_in_label<unlabeled_label_position){
            unlabeled_label_position-=1;
        }
    }

    void move_note_to_recent_delete(int position_in_adapter, String note_name){
        int position_in_label = locate_in_label_from_position_in_adapter(position_in_adapter);
        delete_from_adapter_and_label(position_in_adapter, position_in_label);
        change_map(position_in_adapter, -1, -1);

        String new_deleted_note_name = "Note---del: " + note_name.substring(12);
        int delete_position_in_adapter = label_map_to_adapter.get(delete_label_position);

        note_name = list_for_adapter.get(delete_position_in_adapter);
        if(note_name.startsWith("Label-show:")) {
            add_in_label_and_adapter(delete_position_in_adapter + 1, delete_label_position + 1, new_deleted_note_name);
        }
        else{
            label_names.add(delete_label_position+1, note_name);
        }
    }
    void delete_label_notes(int position_in_adapter){
        int delete_start_index = position_in_adapter+1;
        int delete_end_index = -1;
        Set<Integer> keys = adapter_map_to_label.keySet();
        boolean find_key=false;
        for(int key : keys){
            if(find_key){
                delete_end_index=key;
                break;
            }
            if(key==position_in_adapter){
                find_key=true;
            }
        }
        if(delete_end_index==-1){
            Log.e(String.valueOf(this), "in delete_label_notes, can't find delete_end_index");
        }

        for(int i=delete_start_index; i<delete_end_index; i++){
            String temp_note_name = list_for_adapter.get(delete_start_index);  // 这里就是delete_start_index，因为每次删除后面的东西都会前移
            move_note_to_recent_delete(delete_start_index, temp_note_name);
        }
    }
}