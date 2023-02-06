package baidumapsdk.demo.geometry;

import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;

public class CarsMovementModel {

    private LatLng[] coordinateList;
    /// 司机名称
    private String driverName;
    /// 司机ID
    private String driverId;


    public CarsMovementModel(String driverName, String driverId, LatLng[] coordinateList) {
        this.driverId = driverId;
        this.driverName = driverName;
        this.coordinateList = coordinateList;
    }


    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }


    public LatLng[] getCoordinateList() {
        return coordinateList;
    }

    public void setCoordinateList(LatLng[] coordinateList) {
        this.coordinateList = coordinateList;
    }

}
