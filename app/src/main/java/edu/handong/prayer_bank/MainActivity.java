package edu.handong.prayer_bank;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;


public class MainActivity extends AppCompatActivity {

    DrawerLayout drawer;
    ActionBarDrawerToggle toggle;
    boolean isDrawerOpend;
    private AppBarConfiguration mAppBarConfiguration;
    RecyclerView recyclerView;
    PreferenceManager pref;

    // Firebase Analytics
    private FirebaseAnalytics mFirebaseAnalytics;

    //write
    FrameLayout today_prayer, today_thanks, favorites;
    LinearLayout prayer_list, write_page;
    MemoAdapter memoAdapter;
    ImageButton write_btn;
    private int REQUEST_TEST = 200;
    //show
    TextView show_pt;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Firebase Analytics 연동
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        ImageButton buttonOpen = findViewById(R.id.menuButton);
        buttonOpen.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v){
                // Drawer를 onlcilk 안에 만들어서 그렇다!
                drawer = (DrawerLayout) findViewById(R.id.main_screen);
                if (drawer.isDrawerOpen(Gravity.LEFT)){
                    drawer.closeDrawer(Gravity.LEFT);
                }
            }
        });

        //etDisplayShowTitleEnabled(false);

        drawer = (DrawerLayout) findViewById(R.id.main_screen);
        toggle = new ActionBarDrawerToggle(this, drawer, R.string.drawer_open, R.string.drawer_close){
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
            @Override
            public boolean onOptionsItemSelected(MenuItem item) {
                if (toggle.onOptionsItemSelected(item)) {
                    return true;
                }
                return super.onOptionsItemSelected(item);
            }
        };
        drawer.addDrawerListener(toggle);  //error
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toggle.syncState();
        // no pointer exception handler를 만들어야함.

        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        NavigationView navigationView = (NavigationView)
                findViewById(R.id.activity_navigation);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            @SuppressWarnings("StatementWithEmptyBody")
            @Override
            public boolean onNavigationItemSelected(MenuItem Item) {
                int id=Item.getItemId();
                FragmentManager manager = getSupportFragmentManager();
                if(id==R.id.nav_me) {
                    manager.beginTransaction().replace(R.id.activity_main_drawer, new Category_me());
                } else if(id == R.id.nav_family) {
                    manager.beginTransaction().replace(R.id.activity_main_drawer, new Category_family());
                } else if(id == R.id.nav_friends) {
                    manager.beginTransaction().replace(R.id.activity_main_drawer, new Category_friends());
                }
                drawer.closeDrawer(GravityCompat.START);
                return false;
            }


        });


        //make the main button
        ImageButton praybutton = findViewById(R.id.prayButton);
        ImageButton mypagebutton = findViewById(R.id.mypageButton);
        ImageButton summarybutton = findViewById(R.id.summaryButton);
        ImageButton calenderbutton = findViewById(R.id.calendarButton);
        ImageButton homebutton = findViewById(R.id.homeButton);
        ImageButton addPrayerButton = findViewById(R.id.plus);
        ImageButton addThanksgivingButton = findViewById(R.id.pencil);


        //screen change
        //main -> pray screen
        praybutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent pray = new Intent(MainActivity.this, Prayer.class);
                startActivity(pray);
            }
        });
        //main -> my page screen
        mypagebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mypage = new Intent(MainActivity.this, MyPage.class);
                startActivity(mypage);
            }
        });
        //main -> summary screen
        summarybutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent summary = new Intent(MainActivity.this, Summary.class);
                startActivity(summary);
            }
        });
        //main -> calender
        calenderbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent calender = new Intent(MainActivity.this, Calendar.class);
                startActivity(calender);
            }
        });
        //main -> home
        homebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent home = new Intent(MainActivity.this, MainActivity.class);
                startActivity(home);
            }
        });
        //add prayer
        addPrayerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addPrayer = new Intent(MainActivity.this, Prayer_topics.class);
                startActivity(addPrayer);

            }
        });
        //add thanksgiving
        addThanksgivingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addThanksgiving = new Intent(MainActivity.this, Thanksgiving.class);
                startActivity(addThanksgiving);

            }
        });

        // preferences 모든 값 가져오기
        /*
        SharedPreferences showpre = getSharedPreferences("memo_contain", MODE_PRIVATE);
        Collection<?> col_val = showpre.getAll().values();
        Iterator<?> it_val = col_val.iterator();
        Collection<?> col_key = showpre.getAll().keySet();
        Iterator<?> it_key = col_key.iterator();
        show_pt = findViewById(R.id.topics);

        while ((!it_val.hasNext()) && (!it_key.hasNext())) {
            String key = (String) it_key.next();
            String value = (String) it_val.next();
            try {
                JSONObject jsonObject = new JSONObject(value);
                String content = (String) jsonObject.getString("content");
                show_pt.setText(content);
            } catch (JSONException e) {
                Log.d("MainActivity", "JSONObject : " + e);
            }


        }

         */



        // key값 = 작성 시간






        //onCreate


        /*
        write_page = findViewById(R.id.write_page);
        today_prayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //버튼을 눌러 이동할 액티비티를 정해줌
                Intent intent = new Intent(getApplicationContext(), Write_prayer_topics.class);
                //startActivity(intent);
                startActivityForResult(intent, REQUEST_TEST);
            }
        });


        //onCreate
        //리사이클러뷰 세팅
        LinearLayoutManager linearLayoutManager;
        recyclerView = findViewById(R.id.today_prayer);//리사이클러뷰 findView
        linearLayoutManager = new LinearLayoutManager(MainActivity.this, LinearLayoutManager.VERTICAL, false);

        //쉐어드 모든 키 벨류 가져오기
        SharedPreferences prefb =getSharedPreferences("memo_contain", MODE_PRIVATE);
        Collection<?> col_val =  prefb.getAll().values();
        Iterator<?> it_val = col_val.iterator();
        Collection<?> col_key = prefb.getAll().keySet();
        Iterator<?> it_key = col_key.iterator();


        while(it_val.hasNext() && it_key.hasNext()) {
            String key = (String) it_key.next();
            String value = (String) it_val.next();

            // value 값은 다음과 같이 저장되어있다
            // "{\"category\":\"hi category\",\"prayer_topic\":\"hi prayer_topic\"}"
            try {
                // String으로 된 value를 JSONObject로 변환하여 key-value로 데이터 추출
                JSONObject jsonObject = new JSONObject(value);
                String category = (String) jsonObject.getString("category");
                String prayer_topic = (String) jsonObject.getString("prayer_topic");
                // 리사이클러뷰 어뎁터 addItem으로 목록 추가
                memoAdapter.addItem(new MemoItem(key, category, prayer_topic));
            } catch (JSONException e) {

            }

            // 목록 갱신하여 뷰에 띄어줌
            memoAdapter.notifyDataSetChanged();

        }


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return false;
        }
        return super.onOptionsItemSelected(item);
    }
    private void showToast(String message) {
        Toast toast=Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_TEST) {
            if (resultCode == RESULT_OK) {
                // 전달 받은 값
                Intent intent = getIntent();
                String get_date = data.getStringExtra("date");
                String get_category = data.getStringExtra("category");
                String get_prayer_topics = data.getStringExtra("prayer_topic");
                // 리사이클러뷰 목록에 추가
                memoAdapter.addItem(new MemoItem(get_date,get_category,get_prayer_topics));
                // 목록 갱신
                memoAdapter.notifyDataSetChanged();
                Toast.makeText(MainActivity.this, "작성 되었습니다", Toast.LENGTH_SHORT).show();
            } else {   // RESULT_CANCEL
                Toast.makeText(MainActivity.this, "저장 되지 않음", Toast.LENGTH_SHORT).show();
            }

        }*/
    }




}
        /*checkBox1.setOnCheckedChangeListener((buttonView, isChecked) -> {
        editText1.setEnabled(isChecked);
        });
        checkBox2.setOnCheckedChangeListener((buttonView, isChecked) -> {
        editText2.setEnabled(isChecked);
        });
        checkBox3.setOnCheckedChangeListener((buttonView, isChecked) -> {
        editText3.setEnabled(isChecked);
        });*/




