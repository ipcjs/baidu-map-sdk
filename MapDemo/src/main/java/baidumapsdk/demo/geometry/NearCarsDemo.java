package baidumapsdk.demo.geometry;

import android.animation.TypeEvaluator;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.baidu.mapapi.animation.Animation;
import com.baidu.mapapi.animation.Transformation;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import baidumapsdk.demo.R;

public class NearCarsDemo extends AppCompatActivity {

    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private List<CarsMovementModel> mDriversInfo;
    private List<Marker> mCarMarkers;

    // 通过设置间隔时间和距离可以控制速度和图标移动的距离
    private BitmapDescriptor mGreenTexture = BitmapDescriptorFactory.fromAsset("Icon_road_green_arrow.png");
    private BitmapDescriptor mBitmapCar = BitmapDescriptorFactory.fromResource(R.drawable.car);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_near_cars);
        mMapView = (MapView) findViewById(R.id.bmap);
        mBaiduMap = mMapView.getMap();
        MapStatus.Builder builder = new MapStatus.Builder();
        builder.target(new LatLng(40.056981, 116.306987));
        builder.zoom(18.0f);
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
        mMapView.showZoomControls(false);

        mDriversInfo = getDriversInfo();
        mBaiduMap.setOnMapLoadedCallback(new BaiduMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                drawCar(mDriversInfo);

            }
        });


    }

    private void movingCars(List<Marker> carsMarkerList, List<Animation> animations) {
        if (null == carsMarkerList || null == animations || carsMarkerList.size() != animations.size()) {
            return;
        }
        for (int i = 0; i < carsMarkerList.size(); i++) {
            Marker marker = carsMarkerList.get(i);
            marker.setAnimation(animations.get(i), new PositionEvaluatorWithRotate(marker));
            marker.startAnimation();
        }
    }

    /**
     * 自定义估值器，在小车移动过程中更新小车旋转角度
     */
    public class PositionEvaluatorWithRotate implements TypeEvaluator {

        private Marker mMarker;

        // 传入要更新的Marker
        PositionEvaluatorWithRotate(Marker marker) {
            this.mMarker = marker;
        }
        @Override
        public Object evaluate(float fraction, Object startValue, Object endValue) {
            LatLng startPoint = (LatLng) startValue;
            LatLng endPoint = (LatLng) endValue;
            double x = startPoint.longitude + fraction * (endPoint.longitude - startPoint.longitude);
            double y = startPoint.latitude + fraction * (endPoint.latitude - startPoint.latitude);

            float angle = (float) getAngle(startPoint, endPoint);
            // 更新小车旋转角度
            if (mMarker != null && angle != mMarker.getRotate()) {
                mMarker.setRotate(angle);
            }

            return new LatLng(y,x);
        }
    }


    private void drawCar(List<CarsMovementModel> mDriversInfo) {
        if (null == mDriversInfo) {
            return;
        }
        ArrayList<Marker> carMarkers = new ArrayList<>();
        ArrayList<Animation> carAnimators = new ArrayList<>();
        for (int i = 0; i < mDriversInfo.size(); i++) {
            // 添加小车marker
            CarsMovementModel car = mDriversInfo.get(i);
            if (car == null) {
                break;
            }
            MarkerOptions mCarOptions = new MarkerOptions().flat(true).anchor(0.5f, 0.5f).icon(mBitmapCar).
                    position(car.getCoordinateList()[0]).scaleX(0.8f).scaleY(0.8f);
            Marker marker = (Marker) mBaiduMap.addOverlay(mCarOptions);

            // 设置动画
            Transformation transformation = new Transformation(car.getCoordinateList());
            transformation.setDuration(3000*car.getCoordinateList().length);
            transformation.setRepeatMode(Animation.RepeatMode.RESTART);// 动画重复模式
            transformation.setRepeatCount(100);// 动画重复次数
            transformation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart() {
                }

                @Override
                public void onAnimationEnd() {
                }

                @Override
                public void onAnimationCancel() {
                }

                @Override
                public void onAnimationRepeat() {

                }
            });

            carAnimators.add(transformation);
            carMarkers.add(marker);
        }

        mCarMarkers = carMarkers;
        // 开始小车平移动画
        movingCars(carMarkers, carAnimators);
    }

    /**
     * 根据两点算取图标转的角度
     */
    private double getAngle(LatLng fromPoint, LatLng toPoint) {
        double slope = getSlope(fromPoint, toPoint);
        if (slope == Double.MAX_VALUE) {
            if (toPoint.latitude > fromPoint.latitude) {
                return 0;
            } else {
                return 180;
            }
        } else if (slope == 0.0) {
            if (toPoint.longitude > fromPoint.longitude) {
                return -90;
            } else {
                return 90;
            }
        }
        float deltAngle = 0;
        if ((toPoint.latitude - fromPoint.latitude) * slope < 0) {
            deltAngle = 180;
        }
        double radio = Math.atan(slope);
        double angle = 180 * (radio / Math.PI) + deltAngle - 90;
        return angle;
    }

    /**
     * 算斜率
     */
    private double getSlope(LatLng fromPoint, LatLng toPoint) {
        if (toPoint.longitude == fromPoint.longitude) {
            return Double.MAX_VALUE;
        }
        double slope = ((toPoint.latitude - fromPoint.latitude) / (toPoint.longitude - fromPoint.longitude));
        return slope;
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
        mBitmapCar.recycle();
        mGreenTexture.recycle();
        // 停止动画并移除Marker
        if (mCarMarkers != null) {
            for (int i = 0; i < mCarMarkers.size(); i++) {
                mCarMarkers.get(i).cancelAnimation();
                mCarMarkers.get(i).remove();
            }
            mCarMarkers.clear();
        }
        mBaiduMap.clear();
        mMapView.onDestroy();

    }

    private List<CarsMovementModel> getDriversInfo() {
        List<CarsMovementModel> list = new ArrayList<CarsMovementModel>();
        InputStream inputStream = getResources().openRawResource(R.raw.driverdata);
        String json = new Scanner(inputStream).useDelimiter("\\A").next();
        JSONArray array;
        try {
            array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.optJSONObject(i);
                String driverId = object.optString("driverId");
                String driverName = object.optString("driverName");
                JSONArray carLocationsArray = object.optJSONArray("locations");
                LatLng[] coordinateList = new LatLng[carLocationsArray.length()];


                for (int j = 0; j < carLocationsArray.length(); j++) {
                    JSONObject latObject = carLocationsArray.optJSONObject(j);
                    double lat = latObject.getDouble("lat");
                    double lng = latObject.getDouble("lng");
                    coordinateList[j] = new LatLng(lat, lng);
                }

                list.add(new CarsMovementModel(driverName, driverId, coordinateList));
            }

            inputStream.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return list;
    }
}
