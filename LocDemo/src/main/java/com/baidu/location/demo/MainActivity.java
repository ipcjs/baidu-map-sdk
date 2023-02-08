package com.baidu.location.demo;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.baidu.baidulocationdemo.R;
import com.baidu.location.LocationClient;
import com.baidu.location.service.LocationService;
import com.baidu.location.service.Utils;
import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/***
 * 本类代码同定位业务本身无关，负责实现列表
 *
 * @author baidu
 *
 */
public class MainActivity extends Activity {
    private final int SDK_PERMISSION_REQUEST = 127;
    private ListView FunctionList;
    private String permissionInfo;

    private final int DIALOG_KEY_BACK = 1;
    private final int DIALOG_PERMISSION = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.function_list);

        //创建弹窗显示隐私政策
        if (!Utils.contains(this, Utils.SP_PRIVACY_DIALOG)) {
            createPrivacyDialog();
        } else {
            boolean status = Utils.getString(MainActivity.this, Utils.SP_PRIVACY_STATUS).equals("1");
            initSDK(status);
        }

        FunctionList = (ListView) findViewById(R.id.functionList);
        FunctionList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, getData()));
        // after andrioid m,must request Permiision on runtime
        IntentFilter filter = new IntentFilter();
        // 点击home键广播，由系统发出
        filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        registerReceiver(mHomeAndLockReceiver, filter);
    }

    private void initSDK(boolean status) {
        LocationClient.setAgreePrivacy(status);
        SDKInitializer.setAgreePrivacy(getApplicationContext(), status);
        ((LocationApplication)getApplication()).locationService = new LocationService(getApplicationContext());
        try {
            SDKInitializer.initialize(getApplicationContext());
            SDKInitializer.setCoordType(CoordType.BD09LL);
        } catch (Exception e) {
            e.printStackTrace();
        }

        onUsePermission();
        getPersimmions();
    }

    private void createPrivacyDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String notifyString = "为进一步加强对最终用户个人信息的安全保护措施, 请仔细阅读如下隐私政策并确认是否同意：\n《服务隐私政策》";
        SpannableStringBuilder spannableString = new SpannableStringBuilder(notifyString);
        Pattern pattern = Pattern.compile("《服务隐私政策》");
        Matcher matcher = pattern.matcher(spannableString);
        while (matcher.find()) {
            setClickableSpan(spannableString, matcher);
        }

        View view = View.inflate(this, R.layout.notify_privacy_text, null);
        TextView notifyText = (TextView) view.findViewById(R.id.notify_text);
        notifyText.setText(spannableString);
        notifyText.setMovementMethod(LinkMovementMethod.getInstance());

        builder.setView(view);
        builder.setPositiveButton("同意", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                initSDK(true);
                Utils.putString(MainActivity.this, Utils.SP_PRIVACY_DIALOG, Utils.SP_PRIVACY_DIALOG);
                Utils.putString(MainActivity.this, Utils.SP_PRIVACY_STATUS, "1");
            }
        });

        builder.setNegativeButton("不同意", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                initSDK(false);
                Utils.putString(MainActivity.this, Utils.SP_PRIVACY_STATUS, "0");
            }
        });


        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();
        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) positiveButton.getLayoutParams();
        layoutParams.weight = 10;
        positiveButton.setLayoutParams(layoutParams);
        negativeButton.setLayoutParams(layoutParams);
    }

    private void setClickableSpan(SpannableStringBuilder span, Matcher matcher) {
        int start = matcher.start();
        int end = matcher.end();

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                final Uri uri = Uri.parse("https://lbsyun.baidu.com/index.php?title=openprivacy");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }

            @Override
            public void updateDrawState(TextPaint textPaint) {
                textPaint.setUnderlineText(false);
            }
        };

        span.setSpan(new ForegroundColorSpan(Color.CYAN), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        span.setSpan(clickableSpan, start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
    }

    @TargetApi(23)
    private void getPersimmions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ArrayList<String> permissions = new ArrayList<String>();
            /***
             * 定位权限为必须权限，用户如果禁止，则每次进入都会申请
             */
            // 定位精确位置
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }
            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            }
            /*
             * 读写权限和电话状态权限非必要权限(建议授予)只会申请一次，用户同意或者禁止，只会弹一次
             */
            // 读写权限
            if (addPermission(permissions, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                permissionInfo += "Manifest.permission.WRITE_EXTERNAL_STORAGE Deny \n";
            }
            if (permissions.size() > 0) {
                requestPermissions(permissions.toArray(new String[permissions.size()]), SDK_PERMISSION_REQUEST);
            }
        }
    }

    @TargetApi(23)
    private boolean addPermission(ArrayList<String> permissionsList, String permission) {
        // 如果应用没有获得对应权限,则添加到列表中,准备批量申请
        if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(permission)) {
                return true;
            } else {
                permissionsList.add(permission);
                return false;
            }
        } else {
            return true;
        }
    }

    @TargetApi(23)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // TODO Auto-generated method stub
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        FunctionList.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                // TODO Auto-generated method stub
                Class<?> TargetClass = null;
                switch (arg2) {
                    case 0:
                        TargetClass = LocationActivity.class;
                        break;
                    case 1:
                        TargetClass = LocationOption.class;
                        break;
                    case 2:
                        TargetClass = LocationAutoNotify.class;
                        break;
                    case 3:
                        TargetClass = LocationFilter.class;
                        break;
                    case 4:
                        TargetClass = NotifyActivity.class;
                        break;
                    case 5:
                        TargetClass = IndoorLocationActivity.class;
                        break;
                    case 6:
                        TargetClass = GeoFenceMultipleActivity.class;
                        break;
                    case 7:
                        TargetClass = SceneLocationActivity.class;
                        break;
                    case 8:
                        TargetClass = MockLocationActivity.class;
                        break;
                    case 9:
                        TargetClass = LocPreventCheatActivity.class;
                        break;
                    case 10:
                        TargetClass = ForegroundActivity.class;
                        break;
                    case 11:
                        TargetClass = AssistLocationActivity.class;
                        break;
                    case 12:
                        TargetClass = LocationNotifyActivity.class;
                        break;
                    case 13:
                        TargetClass = IsHotWifiActivity.class;
                        break;
                    case 14:
                        TargetClass = QuestActivity.class;
                        break;
                    default:
                        break;
                }
                if (TargetClass != null) {
                    Intent intent = new Intent(MainActivity.this, TargetClass);
                    intent.putExtra("from", 0);
                    startActivity(intent);
                }
            }
        });
    }

    private List<String> getData() {
        List<String> data = new ArrayList<String>();
        data.add("基础定位功能");
        data.add("配置定位参数");
        data.add("自定义回调示例");
        data.add("连续定位示例");
        data.add("位置消息提醒");
        data.add("室内定位功能");
        data.add("地理围栏功能");
        data.add("场景定位");
        data.add("仿真定位");
        data.add("定位防作弊");
        data.add("android 8.0/9.0后台定位示例");
        data.add("H5辅助定位");
        data.add("位置提醒");
        data.add("判断移动热点");
        data.add("常见问题说明");
        return data;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (!Utils.contains(this, Utils.SP_KEY_BACK_RETURN)) {
                Utils.putString(this, Utils.SP_KEY_BACK_RETURN, Utils.SP_KEY_BACK_RETURN);
                showMissingPermissionDialog("提示", getString(R.string.action_background), DIALOG_KEY_BACK);
                return false;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 显示提示信息
     */
    private void showMissingPermissionDialog(String title, String message, final int type) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);

        builder.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (type == DIALOG_KEY_BACK) {
                            Intent intent = new Intent(Intent.ACTION_MAIN);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.addCategory(Intent.CATEGORY_HOME);
                            startActivity(intent);
                        } else {
                            dialog.dismiss();
                        }

                    }
                });

        builder.setCancelable(false);

        builder.show();
    }

    private void onUsePermission() {
        if (!Utils.contains(this, Utils.SP_PERMISSION_DIALOG)) {
            Utils.putString(this, Utils.SP_PERMISSION_DIALOG, Utils.SP_PERMISSION_DIALOG);
            showMissingPermissionDialog("定位SDK DEMO在使用时需要申请以下权限：", "1、定位权限，用于定位功能测试。\n2、读写权限，用于写入离线定位数据。\n", DIALOG_PERMISSION);
        }
    }

    /**
     * 监听是否点击了home键将客户端推到后台
     */
    private BroadcastReceiver mHomeAndLockReceiver = new BroadcastReceiver() {
        String SYSTEM_REASON = "reason";
        String SYSTEM_HOME_KEY = "homekey";
        String SYSTEM_HOME_RECENT = "recentapps";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                String reason = intent.getStringExtra(SYSTEM_REASON);
                if (TextUtils.equals(reason, SYSTEM_HOME_KEY) || TextUtils.equals(reason, SYSTEM_HOME_RECENT)) {
                    if (!Utils.contains(MainActivity.this, Utils.SP_HOME_BACK_RETURN)) {
                        Utils.putString(MainActivity.this, Utils.SP_HOME_BACK_RETURN, Utils.SP_HOME_BACK_RETURN);
                        Toast.makeText(context, context.getString(R.string.action_background), Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mHomeAndLockReceiver);
    }
}
