package com.example.mroot.ncsecurelocation;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.ConnectConstant;
import connect.MyClientSocket;
import data.MsgType;
import fr.quentinklein.slt.LocationTracker;
import fr.quentinklein.slt.TrackerSettings;
import utils.MyThreadPool;
import utils.ToolUtils;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.sv_prompt)
    ScrollView scrollView;
    @BindView(R.id.tv_prompt)
    TextView tv_prompt;


    private long exitTime = 0;
    private static MainActivity mainActivity;
    // 设置一个经纬度默认值  教研室
    private volatile String lonAndLat = "118.922918,32.116803";

    MyClientSocket myClientSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        requestPermissions();
        mainActivity = this;
        //滚动到最下面
        scrollView.getViewTreeObserver().addOnGlobalLayoutListener(() ->
                scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN)));

        init();
    }

    private void init() {
        myClientSocket = new MyClientSocket();
    }

    @OnClick(R.id.btn_connect)
    public void connect() {

        MyThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                myClientSocket.connect(ConnectConstant.SERVER_IP, ConnectConstant.SERVER_PORT);
            }
        });

    }

    @OnClick(R.id.btn_test)
    public void test() {
        Toast.makeText(this, "test", Toast.LENGTH_SHORT).show();
//        try {
//           //String gbk = URLEncoder.encode("msg test","GBK");
//            //System.out.println(gbk);
//            myClientSocket.sendMessage("msg test");
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
        String msg = lonAndLat;
        int len = msg.getBytes().length;
        System.out.println("len = " + len);
        //myClientSocket.sendMessage(lonAndLat);

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                myClientSocket.sendMessage(lonAndLat);
            }
        }, 0, 1000);
    }


    /**
     * 处理各个线程发来的消息
     */
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(final Message msg) {
            MsgType msgType = MsgType.values()[msg.what];
            switch (msgType) {
                case SHOW_MSG:
                    String prompt = msg.obj.toString();
                    setPrompt(prompt);
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };


    /**
     * 设置提示
     *
     * @param prompt
     */
    private void setPrompt(String prompt) {
        // 获取的值是之前的行数加1 所以这里减1
        int lineNum = tv_prompt.getLineCount();
        Log.v("hanhai", "prompt中有" + lineNum + "行数据");
        // 控制最大显示行数为400行  最大显示其实为402行
        if (lineNum > 400) {
            String text = tv_prompt.getText().toString();
            // 去掉两行  下面会增加两行
            text = text.substring(text.indexOf("\n") + 1);
            text = text.substring(text.indexOf("\n") + 1);
            tv_prompt.setText(text);
        }
        String strTime = ToolUtils.getCurrentTime();
        tv_prompt.append(strTime + "\n");
        tv_prompt.append(prompt + "\n");
    }


    //定时获取定位的经纬度
    //每3秒执行一次
    public void startLocation() {
        String permissionName = "android.permission.ACCESS_FINE_LOCATION";
        PackageManager pm = getPackageManager();
        if (PackageManager.PERMISSION_GRANTED ==
                pm.checkPermission(permissionName, this.getPackageName())) {
            TrackerSettings settings =
                    new TrackerSettings()
                            .setUseGPS(true)
                            .setUseNetwork(true)
                            .setUsePassive(true)
                            .setTimeBetweenUpdates(3 * 1000)
                            .setMetersBetweenUpdates(1);  //与上次定位相差1米范围内，不提醒
            LocationTracker tracker = new LocationTracker(this, settings) {
                @Override
                public void onLocationFound(Location location) {
                    // Do some stuff when a new location has been found.
                    //获取到定位经纬度
                    lonAndLat = location.getLongitude() + "," + location.getLatitude();
                    //tv_sample_text.setText(lonAndLat);

                    Log.e("hanhai", "经纬度更新：" + lonAndLat);
                    sendMsg2UIThread(MsgType.SHOW_MSG.ordinal(), "获取到经纬度：" + lonAndLat);
                }

                @Override
                public void onTimeout() {
                    Log.e("hanhai", "定位超时");
                }
            };
            tracker.startListening();
        } else {
            Toast.makeText(this, "定位权限不可用", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onBackPressed() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {
            finish();
            //不会调用周期函数，如onDestroy()
            System.exit(0);
        }
    }

    //全局发送message到handle处理的方法
    private void send2UI(int what, Object obj) {
        //sendMsg2UIThread(MsgType.SHOW_MSG.ordinal(),"hello, world!");
        if (handler != null) {
            Message.obtain(handler, what, obj).sendToTarget();
        }
    }

    public static void sendMsg2UIThread(int what, Object obj) {
        mainActivity.send2UI(what, obj);
    }

    /**
     * 申请权限
     */
    private void requestPermissions() {
        // 检查权限是否获取（android6.0及以上系统可能默认关闭权限，且没提示）
        AndPermission.with(this)
                .runtime()
                .permission(Permission.Group.STORAGE)
                .permission(Permission.Group.LOCATION)
                .onGranted(permissions -> {
                    // Storage permission are allowed.
                    startLocation();
                })
                .onDenied(permissions -> {
                    // Storage permission are not allowed.
                    AndPermission.with(this)
                            .runtime()
                            .setting()
                            .onComeback(() -> {
                                // 用户从设置回来了。
                                //Toast.makeText(this, "用户从设置回来了", Toast.LENGTH_SHORT).show();
                            })
                            .start();
                })
                .start();
    }
}
