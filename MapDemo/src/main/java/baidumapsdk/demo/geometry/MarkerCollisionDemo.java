package baidumapsdk.demo.geometry;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;

import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MultiPoint;
import com.baidu.mapapi.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import baidumapsdk.demo.R;

public class MarkerCollisionDemo extends AppCompatActivity implements View.OnClickListener {
    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.icon_marka);
    private BitmapDescriptor bitmapB = BitmapDescriptorFactory.fromResource(R.drawable.icon_markb);
    private BitmapDescriptor bitmapC = BitmapDescriptorFactory.fromResource(R.drawable.icon_markc);
    private BitmapDescriptor bitmapD = BitmapDescriptorFactory.fromResource(R.drawable.icon_markd);
    private BitmapDescriptor bitmapE = BitmapDescriptorFactory.fromResource(R.drawable.icon_marke);
    private Marker mMarker;
    private MultiPoint mMultiPoint;
    private MarkerOptions markerOptionsA;
    private MarkerOptions markerOptionsE;
    private InfoWindow mInfoWindow;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marker_collision);
        mMapView = findViewById(R.id.bmapView);
        Button addMultiPoint = findViewById(R.id.add_multi_marker);
        Button removeMultiPoint = findViewById(R.id.remove_multi_marker);
        addMultiPoint.setOnClickListener(this);
        removeMultiPoint.setOnClickListener(this);

        MapStatus mapStatus= new MapStatus.Builder().zoom(7).build();
        MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mapStatus);
        mBaiduMap = mMapView.getMap();
        mBaiduMap.setMapStatus(mapStatusUpdate);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        bitmap.recycle();
        bitmap.recycle();
        if (null != mMarker) {
            mMarker.remove();
        }
        mBaiduMap.clear();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.add_multi_marker) {
            List<LatLng> locations = getLocations();
            mBaiduMap.clear();
            if (mMarker != null) {
                mMarker.remove();
            }
            //随机生成优先级
            for (int i = 0; i < locations.size(); i++) {
                final double d = Math.random();
                final int prio = (int)(d* locations.size());
                markerOptionsA = new MarkerOptions()
                        .position(locations.get(i))
                        .icon(bitmap)// 设置 Marker 覆盖物的图标
                        .zIndex(9)// 设置 marker 覆盖物的 zIndex
                        .isJoinCollision(true)
                        .priority(locations.size()-i);// 设置 marker碰撞压盖优先级
                mMarker = (Marker) (mBaiduMap.addOverlay(markerOptionsA));
            }
            // markerB参与碰撞
            LatLng latLngB = new LatLng(39.89871, 116.36784);
            markerOptionsA = new MarkerOptions()
                    .position(latLngB)
                    .icon(bitmapB)// 设置 Marker 覆盖物的图标
                    .zIndex(9)// 设置 marker 覆盖物的 zIndex
                    .isForceDisPlay(false) //设置压盖时 marker强制展示
                    .priority(7)
                    .isJoinCollision(true);//marker是否参与碰撞
            mMarker = (Marker) (mBaiduMap.addOverlay(markerOptionsA));

            // markerC参与碰撞,
            LatLng latLngC = new LatLng(39.90071, 116.40704);
            markerOptionsA = new MarkerOptions()
                    .position(latLngC)
                    .icon(bitmapC)// 设置 Marker 覆盖物的图标
                    .zIndex(9)// 设置 marker 覆盖物的 zIndex
                    .isJoinCollision(true)  //marker是否参与碰撞
                    .priority(8)
                    .isForceDisPlay(false); //设置压盖时 marker强制展示

            mMarker = (Marker) (mBaiduMap.addOverlay(markerOptionsA));

            // markerD参与碰撞,  它强制展示,但是它重新设置了显示层级
            //{"lng": 116.652158, "lat": 39.912919},
            LatLng latLngD = new LatLng(39.94871, 116.43784);
            markerOptionsA = new MarkerOptions()
                    .position(latLngD)
                    .icon(bitmapD)// 设置 Marker 覆盖物的图标
                    .zIndex(9)// 设置 marker 覆盖物的 zIndex
                    .isJoinCollision(true)
                    .isForceDisPlay(false)
                    .priority(9)
                    .startLevel(10)
                    .endLevel(15); //设置压盖时 marker强制展示

            mMarker = (Marker) (mBaiduMap.addOverlay(markerOptionsA));

            // markerE不参与碰撞,压盖层级最高
            LatLng latLngE = new LatLng(39.96871, 116.45784);
            markerOptionsA = new MarkerOptions()
                    .position(latLngE)
                    .icon(bitmapE)// 设置 Marker 覆盖物的图标
                    .isJoinCollision(false)
                    .clickable(true)
                    .zIndex(10);// 设置 marker 覆盖物的 zIndex
            mMarker = (Marker) (mBaiduMap.addOverlay(markerOptionsA));

            // 设置Marker 点击事件监听
            mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
                public boolean onMarkerClick(final Marker marker) {
                    Button button = new Button(getApplicationContext());
                    button.setBackgroundResource(R.drawable.popup);
                    InfoWindow.OnInfoWindowClickListener listener = null;
                    button.setText("更改位置");
                    button.setTextColor(Color.BLACK);
                    button.setWidth(300);
                    // InfoWindow点击事件监听接口
                    listener = new InfoWindow.OnInfoWindowClickListener() {
                        public void onInfoWindowClick() {
                            LatLng latLng = marker.getPosition();
                            LatLng latLngNew = new LatLng(latLng.latitude + 0.005, latLng.longitude + 0.005);
                            marker.setPosition(latLngNew);
                            // 隐藏地图上的所有InfoWindow
                            mBaiduMap.hideInfoWindow();
                        }
                    };
                    LatLng latLng = marker.getPosition();
                    // 创建InfoWindow
                    mInfoWindow = new InfoWindow(BitmapDescriptorFactory.fromView(button), latLng, -47, listener);
                    // 显示 InfoWindow, 该接口会先隐藏其他已添加的InfoWindow, 再添加新的InfoWindow
                    mBaiduMap.showInfoWindow(mInfoWindow);
                    return true;
                }
            });

        } else if (v.getId() == R.id.remove_multi_marker) {
            mBaiduMap.clear();
            if (mMarker != null) {
                mMarker.remove();
            }
        }
    }

    private List<LatLng> getLocations() {
        List<LatLng> list = new ArrayList<LatLng>();
        InputStream inputStream = getResources().openRawResource(R.raw.locations);
        String json = new Scanner(inputStream).useDelimiter("\\A").next();
        JSONArray array;
        try {
            array = new JSONArray(json);
            for (int i = 0; i < 1000; i++) {
                JSONObject object = array.getJSONObject(i);
                double lat = object.getDouble("lat");
                double lng = object.getDouble("lng");
                list.add(new LatLng(lat, lng));
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return list;
    }
}
