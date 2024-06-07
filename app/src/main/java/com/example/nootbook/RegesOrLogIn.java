package com.example.nootbook;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class RegesOrLogIn extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    List<Fragment> fragmentList = new ArrayList<>();//每个页面的列表
    ViewPagerAdapter viewPagerAdapter;
    String[] titles = {"登录","注册"};//设置导航栏各量级名称

    LoginPart login_part;
    RegesterPart regester_part;

    public interface login_created_listener {
        void set_login_listener();
    }

    public interface regester_created_listener {
        void set_regester_listener();
    }

    public static class LoginPart extends Fragment {
        public View view;
        private login_created_listener listener;
        public void setLifecycleListener(login_created_listener listener) {
            this.listener = listener;
        }
        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            // 加载 Fragment 的布局文件
            view = inflater.inflate(R.layout.log_in, container, false);
            if (listener != null) {
                listener.set_login_listener();
            }
            // 在这里添加代码，例如初始化视图、绑定数据等等
            return view;
        }
    }

    public static class RegesterPart extends Fragment {
        public View view;
        private regester_created_listener listener;
        public void setLifecycleListener(regester_created_listener listener) {
            this.listener = listener;
        }
        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            // 加载 Fragment 的布局文件
            view = inflater.inflate(R.layout.regester, container, false);
            // 在这里添加代码，例如初始化视图、绑定数据等等
            if (listener != null) {
                listener.set_regester_listener();
            }
            return view;
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reges_or_log_in);

        tabLayout = (TabLayout) findViewById(R.id.tablayout);
        viewPager = (ViewPager) findViewById(R.id.view_pager);

        login_part = new LoginPart();
        login_part.setLifecycleListener(this::set_login_listener);
        regester_part = new RegesterPart();
        regester_part.setLifecycleListener(this::set_regester_listener);

        fragmentList.add(login_part);//添加页面，有多少页面添加多少个fragment对象
        fragmentList.add(regester_part);//添加页面

        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), fragmentList, titles);
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
    }

    void set_login_listener(){
        Button login_button = login_part.view.findViewById(R.id.login_button);
        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                show_message("登录按钮被点击了");

                EditText username_edittext = login_part.view.findViewById(R.id.login_first_line_user_text);
                EditText password_edittext = login_part.view.findViewById(R.id.login_second_line_password_text);

                String input_username = username_edittext.getText().toString();
                String input_password = password_edittext.getText().toString();

                // TODO: check password

                if(input_username.length()>0&&input_password.length()>0) {
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("username", input_username);
                    returnIntent.putExtra("password", input_password);
                    setResult(RESULT_OK, returnIntent);
                    finish();
                }
            }
        });
    }

    void set_regester_listener(){
        Button regester_button = regester_part.view.findViewById(R.id.regester_button);
        regester_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                show_message("注册按钮被点击了");
            }
        });
    }

    void show_message(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}


