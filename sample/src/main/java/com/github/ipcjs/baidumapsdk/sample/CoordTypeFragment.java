package com.github.ipcjs.baidumapsdk.sample;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.github.ipcjs.explorer.menu.MenuCreator;
import com.github.ipcjs.explorer.menu.MenuFragment;

import static com.github.ipcjs.explorer.ExUtils.p;

/**
 * Created by maomaoku on 2017/9/28.
 */

public class CoordTypeFragment extends MenuFragment {
    @MenuCreator.MenuItem
    public void  convert(){
        double lat = 22.535697; // 我家, 桃园路
        double lng = 113.915547;

        p(new CoordinateConverter().from(CoordinateConverter.CoordType.GPS).coord(new LatLng(lat, lng)).convert());
    }
}
