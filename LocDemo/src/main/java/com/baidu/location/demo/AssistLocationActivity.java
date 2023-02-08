package com.baidu.location.demo;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Window;
import android.webkit.GeolocationPermissions;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.baidu.baidulocationdemo.R;
import com.baidu.location.service.LocationService;

/**
 * H5 辅助定位功能
 */
public class AssistLocationActivity extends Activity {

    private WebView contentWebView;
    private LocationService locService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.assist_activity);
        locService = ((LocationApplication)getApplication()).locationService;
        contentWebView = (WebView) findViewById(R.id.webview);
        // 启动H5辅助定位
        locService.enableAssistanLocation(contentWebView);
        locService.start();
        setWebView();
    }

    /**
     * 设置setWebView
     */
    private void setWebView(){
        contentWebView.loadUrl("file:///android_asset/AssistantLoc.html");
        WebSettings webSettings = contentWebView.getSettings();
        // 允许webview执行javaScript脚本
        webSettings.setJavaScriptEnabled(true);
        // 设置是否允许定位，这里为了使用H5辅助定位，设置为false。
	    //设置为true不一定会进行H5辅助定位，设置为true时只有H5定位失败后才会进行辅助定位
//		webSettings.setGeolocationEnabled(false);
        contentWebView.setWebViewClient(new WebViewClient() {

            // 页面请求完成
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }

            // 页面开始加载
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }
        });

        contentWebView.setWebChromeClient(new WebChromeClient() {
            // 处理javascript中的alert
            public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
                return true;
            };

            // 处理javascript中的confirm
            public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
                return true;
            };

            // 处理定位权限请求
            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
                super.onGeolocationPermissionsShowPrompt(origin, callback);
            }
            @Override
            // 设置网页加载的进度条
            public void onProgressChanged(WebView view, int newProgress) {
                AssistLocationActivity.this.getWindow().setFeatureInt(
                        Window.FEATURE_PROGRESS, newProgress * 100);
                super.onProgressChanged(view, newProgress);
            }

            // 设置应用程序的标题title
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != locService) {
            locService.disableAssistantLocation();
            locService.stop();
        }
        if(null != contentWebView){
            contentWebView.destroy();
        }
    }

}
