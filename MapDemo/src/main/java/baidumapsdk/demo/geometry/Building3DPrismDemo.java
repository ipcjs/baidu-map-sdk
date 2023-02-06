package baidumapsdk.demo.geometry;

import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.EncodePointType;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polygon;
import com.baidu.mapapi.map.PolygonOptions;
import com.baidu.mapapi.map.Prism;
import com.baidu.mapapi.map.PrismOptions;
import com.baidu.mapapi.map.Projection;
import com.baidu.mapapi.map.UiSettings;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.mapapi.search.building.BuildingResult;
import com.baidu.mapapi.search.building.BuildingSearch;
import com.baidu.mapapi.search.building.BuildingSearchOption;
import com.baidu.mapapi.search.building.OnGetBuildingSearchResultListener;
import com.baidu.mapapi.search.core.BuildingInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.district.DistrictResult;
import com.baidu.mapapi.search.district.DistrictSearch;
import com.baidu.mapapi.search.district.DistrictSearchOption;
import com.baidu.mapapi.search.district.OnGetDistricSearchResultListener;

import java.util.List;

import javax.microedition.khronos.opengles.GL10;

import baidumapsdk.demo.R;

public class Building3DPrismDemo extends AppCompatActivity implements
        OnGetBuildingSearchResultListener,
        View.OnClickListener, OnGetDistricSearchResultListener {

    private BuildingSearch mBuildingSearch;
    private DistrictSearch mDistrictSearch;
    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private Button mDistrictPrismButton;
    private Button mBuildingPrismButton;
    private Button mCleanPrismButton;
    private Prism mBuildingPrism;
    private Prism mCustomPrism;
    private Marker mMarkerA;
    private InfoWindow mInfoWindowA = null;
    private LatLng requestLatlng;
    // 楼面外接矩形
    private LatLngBounds latLngBounds;

    // BuildingInfo列表
    List<BuildingInfo> buildingList;
    // 建筑物俯视图
    private UiSettings mUiSettings;
    private BitmapDescriptor bitmapA = BitmapDescriptorFactory.fromResource(R.drawable.icon_marka);
    private BitmapDescriptor bitmapE = BitmapDescriptorFactory.fromResource(R.drawable.icon_marke);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_building_3d_overlay_demo);
        requestLatlng = new LatLng(23.008468, 113.72953);
        initView();
        if (mBaiduMap != null) {
            mBaiduMap.setOnMapDrawFrameCallback(new BaiduMap.OnMapDrawFrameCallback() {
                @Override
                public void onMapDrawFrame(GL10 gl, MapStatus drawingMapStatus) {

                }
                @Override
                public void onMapDrawFrame(MapStatus drawingMapStatus) {
                    if (null != mBaiduMap.getMapStatus() && null != mBuildingPrism) {
                        Projection projection = mBaiduMap.getProjection();
                        // 每一帧绘制时重新计算3D marker的外接矩形，根据外接矩形重设infowindow坐标
                        if (mBuildingPrism != null && !mBuildingPrism.isRemoved() && mBuildingPrism.getBuildingInfo() != null) {
                            int height = (int) mBuildingPrism.getBuildingInfo().getHeight();
                            Point srcPoint = projection.geoPoint3toScreenLocation(requestLatlng, height);
                            LatLng locationLat = projection.fromScreenLocation(srcPoint);
                            if (mMarkerA != null && !mMarkerA.isRemoved() && locationLat != null) {
                                mMarkerA.setPosition(locationLat);
                                if (mInfoWindowA != null && locationLat != null) {
                                    mInfoWindowA.setPosition(locationLat);
                                }
                            }
                        }
                    }
                }
            });

        }
    }

    private void initView() {
        mMapView = findViewById(R.id.bmapView);
        mDistrictPrismButton = findViewById(R.id.district_prism);
        mBuildingPrismButton = findViewById(R.id.building_prism);
        mCleanPrismButton = findViewById(R.id.clean_prism);
        mDistrictPrismButton.setOnClickListener(this);
        mBuildingPrismButton.setOnClickListener(this);
        mCleanPrismButton.setOnClickListener(this);
        mBaiduMap = mMapView.getMap();
        mUiSettings = mBaiduMap.getUiSettings();
        mUiSettings.setEnlargeCenterWithDoubleClickEnable(true);
        mUiSettings.setFlingEnable(false);

        MapStatus mapStatus = new MapStatus.Builder().target(requestLatlng).overlook(-30f).build();
        MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mapStatus);
        mBaiduMap.setMapStatus(mapStatusUpdate);
        mDistrictSearch = mDistrictSearch.newInstance();
        mBuildingSearch = BuildingSearch.newInstance();
        mBuildingSearch.setOnGetBuildingSearchResultListener(this);

    }

    private void searchBuilding() {
        BuildingSearchOption buildingSearchOption = new BuildingSearchOption();
        buildingSearchOption.setLatLng(requestLatlng);
        mBuildingSearch.requestBuilding(buildingSearchOption);
    }

    @Override
    public void onGetBuildingResult(BuildingResult result) {
        if (null == result || result.error != SearchResult.ERRORNO.NO_ERROR) {
            return;
        }

        buildingList = result.getBuildingList();
        // 楼面外接矩形建造器
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        for (int i = 0; i < buildingList.size(); i++) {
            BuildingInfo buildingInfo = buildingList.get(i);
            // 创建3D棱柱覆盖物选类配置参数
            PrismOptions prismOptions = new PrismOptions();
            prismOptions.setBuildingInfo(buildingInfo);
            prismOptions.setSideFaceColor(0xAAFF0000);
            prismOptions.setTopFaceColor(0xAA00FF00);
            // 控制3D建筑物单体动画
            prismOptions.setAnimation(true);
            // 设置3D建筑物开始显示层级
            prismOptions.setShowLevel(17);
            LatLngBounds latLngBounds = mBaiduMap.getOverlayLatLngBounds(prismOptions);
            if (latLngBounds != null) {
                boundsBuilder.include(latLngBounds.northeast).include(latLngBounds.southwest);
            }
            // 添加3D棱柱
            mBuildingPrism = (Prism) mBaiduMap.addOverlay(prismOptions);
        }

        if (mMarkerA != null) {
            mMarkerA.remove();
        }

        if (mBaiduMap.getMapStatus() != null && mBuildingPrism != null) {
            Projection projection = mBaiduMap.getProjection();
            Point srcPoint = projection.geoPoint3toScreenLocation(requestLatlng, (int) mBuildingPrism.getBuildingInfo().getHeight());
            MarkerOptions markerOptionsA = new MarkerOptions().position(projection.fromScreenLocation(srcPoint))
                    .icon(bitmapA)// 设置 Marker 覆盖物的图标
                    .perspective(false) // 关闭近大远小效果
                    .clickable(true); // 设置Marker是否可点击
            mMarkerA = (Marker) mBaiduMap.addOverlay(markerOptionsA);
        }


        // 获取3D建筑物外接矩形
        latLngBounds = boundsBuilder.build();
        // 令3D建筑物适应地图展示
        if (latLngBounds != null) {
            mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLngBounds(latLngBounds));
            MapStatus.Builder builder = new MapStatus.Builder();
            builder.zoom(21).overlook(-40.0f);
            mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
        }
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
        if (null != mBuildingSearch) {
            mBuildingSearch.destroy();
        }
        if (null != mDistrictSearch) {
            mDistrictSearch.destroy();
        }
    }

    private void searchDistrict() {
        mDistrictSearch.setOnDistrictSearchListener(this);
        mDistrictSearch.searchDistrict(new DistrictSearchOption().cityName("北京市").districtName("海淀区"));
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.district_prism) {
            clean3DPrim();
            searchDistrict();

        } else if (v.getId() == R.id.building_prism) {
            clean3DPrim();
            searchBuilding();

            mBaiduMap.setBuildingsEnabled(false);
            // 设置Marker 点击事件监听
            mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
                public boolean onMarkerClick(final Marker marker) {
                    Button button = new Button(getApplicationContext());
                    button.setBackgroundResource(R.drawable.popup);

                    InfoWindow.OnInfoWindowClickListener listener = null;
                    if (marker == mMarkerA) {
                        if (mInfoWindowA != null) {
                            mInfoWindowA = null;
                        }
                        button.setText("更改图标");
                        button.setTextColor(Color.BLACK);
                        listener = new InfoWindow.OnInfoWindowClickListener() {
                            public void onInfoWindowClick() {
                                marker.setIcon(bitmapE);
                                mBaiduMap.hideInfoWindow();
                                mInfoWindowA = null;
                            }
                        };
                        LatLng latLng = marker.getPosition();
                        // 创建InfoWindow
                        mInfoWindowA = new InfoWindow(BitmapDescriptorFactory.fromView(button), latLng, -95, listener);
                        // 显示 InfoWindow, 该接口会先隐藏其他已添加的InfoWindow, 再添加新的InfoWindow
                        mBaiduMap.showInfoWindow(mInfoWindowA);
                    }
                    return true;
                }
            });
        } else if (v.getId() == R.id.clean_prism) {
            clean3DPrim();
        }
    }

    private void clean3DPrim() {
        if (null != mBuildingPrism) {
            // 清除建筑物3D棱柱
            mBuildingPrism.remove();
        }
        if (null != mCustomPrism) {
            // 清除自定义3D棱柱
            mCustomPrism.remove();
        }
        if (null != mBaiduMap) {
            mBaiduMap.clear();
            mInfoWindowA = null;
        }

    }

    @Override
    public void onGetDistrictResult(DistrictResult result) {
        if (null != result && result.error == SearchResult.ERRORNO.NO_ERROR) {

            List<List<LatLng>> polyLines = result.getPolylines();
            if (polyLines == null) {
                return;
            }
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (List<LatLng> polyline : polyLines) {
                PrismOptions prismOptions = new PrismOptions();
                prismOptions.setHeight(200);
                prismOptions.setPoints(polyline);
                prismOptions.setSideFaceColor(0xAAFF0000);
                prismOptions.setTopFaceColor(0xAA00FF00);
                prismOptions.customSideImage(BitmapDescriptorFactory.fromResource(R.drawable.wenli));
                // 添加自定3D棱柱
                mCustomPrism = (Prism) mBaiduMap.addOverlay(prismOptions);
                for (LatLng latLng : polyline) {
                    builder.include(latLng);
                }
            }
            mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLngBounds(builder.build()));
            MapStatus.Builder builder1 = new MapStatus.Builder();
            builder1.overlook(-30.0f);
            mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(builder1.build()));
        }
    }
}