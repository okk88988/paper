package com.apple.paper;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class EditMemoActivity extends AppCompatActivity implements View.OnClickListener {
    TextView txtTitle;
    EditText edt_memo;
    Button btn_ok, btn_back;
    Spinner sp_color;
    String new_memo, currentTime;
    Bundle bundle;
    String[] colors;
    SpinnerAdapter spinnerAdapter;
    int index;
    String selected_color;
    private DbAdapter dbAdapter;

    DateFormat df = new SimpleDateFormat("yyyy-MM-dd  HH:mm");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_memo);
        initView();
        dbAdapter = new DbAdapter(this);
        bundle = this.getIntent().getExtras();
        //判斷目前是否為編輯狀態
        if(bundle.getString("type").equals("edit")){
            txtTitle.setText("編輯便條");
            index = bundle.getInt("item_id");
            Cursor cursor = dbAdapter.queryById(index);
            edt_memo.setText(cursor.getString(2));
            // sp_color.setSelection(cursor.getString(4));
        }
    }
    private void initView(){
        txtTitle = findViewById(R.id.txtTitle);
        edt_memo = findViewById(R.id.edtMemo);
        edt_memo.setOnClickListener(this);
        sp_color = findViewById(R.id.sp_colors);
        colors =  getResources().getStringArray(R.array.colors);
        //spinnerAdapter = new SpinnerAdapter(this,colors);
        Log.i("color=",String.valueOf(colors));
        LinearLayout container = new LinearLayout(this);
        final ArrayList<ItemData> color_list = new ArrayList<ItemData>();

        color_list.add(new ItemData("Pink","#FFE391E9"));
        color_list.add(new ItemData("Green","#FF91EC91"));
        color_list.add(new ItemData("Blue","#FF8BE6D8"));
        color_list.add(new ItemData("Orange","#FFE7D495"));
        spinnerAdapter = new SpinnerAdapter(this,color_list);
        sp_color.setAdapter(spinnerAdapter);
        sp_color.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ImageView img =  ((view.findViewById(R.id.ticket)));
                ColorDrawable drawable = (ColorDrawable) img.getBackground();
                selected_color = Integer.toHexString(drawable.getColor()).substring(2);
                Log.i("selected_color=",selected_color);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        btn_ok = findViewById(R.id.btn_ok);
        btn_back = findViewById(R.id.btn_back);
        btn_ok.setOnClickListener(this);
        btn_back.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.edtMemo:
                if(bundle.getString("type").equals("add")) edt_memo.setText("");
                break;

            case R.id.btn_ok:
                //取得edit資料

                new_memo = edt_memo.getText().toString();
                Log.i("memo=",new_memo);
                String currentTime = df.format(new Date(System.currentTimeMillis()));
                if(bundle.getString("type").equals("edit")){
                    try{
                        //更新資料庫中的資料
                        dbAdapter.updateMemo(index, currentTime, new_memo,null, selected_color);
                    }catch (Exception e){
                        e.printStackTrace();
                    }finally {
                        //回到ShowActivity
                        Intent i = new Intent(this, MainActivity.class);
                        startActivity(i);
                    }
                }else {

                    currentTime = df.format(new Date(System.currentTimeMillis()));
                    try {
                        //呼叫adapter的方法處理新增
                        dbAdapter.createMemo(currentTime, new_memo, null, selected_color);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        //回到列表
                        Intent i = new Intent(this, MainActivity.class);
                        startActivity(i);
                    }
                }
                break;
            case R.id.btn_back:
                Intent i = new Intent(this, MainActivity.class);
                startActivity(i);
                break;
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        if(bundle.getString("type").equals("edit")){
            menuInflater.inflate(R.menu.del_menu,menu);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_del:
                //刪除聯絡人
                AlertDialog.Builder builder = null;
                builder = new AlertDialog.Builder(this);
                builder.setTitle("警告")
                        .setMessage("確定要刪除此筆資料? 刪除後無法回復")
                        .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                            //設定確定按鈕
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Boolean isDeleted = dbAdapter.deleteMemo(index);
                                if(isDeleted) {
                                    Toast.makeText(EditMemoActivity.this, "已刪除!", Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(EditMemoActivity.this, MainActivity.class);
                                    startActivity(intent);
                                }
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            //設定取消按鈕
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        }).show();

                break;
        }
        return super.onOptionsItemSelected(item);

    }
}
