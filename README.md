
## 文件结构

注意：由于百度操蛋的打包机制，[选择不同的模块](http://lbsyun.baidu.com/sdk/download?selected=mapsdk_basicmap,mapsdk_searchfunction,mapsdk_lbscloudsearch,mapsdk_calculationtool,mapsdk_radar)下载的`BaiduLBS_Android.jar`文件不一样，故该工程不提供`BaiduLBS_Android.jar`，这个文件需要你手动下载，其他jar和so文件因为不存在复用的情况，这里都会提供。


### so文件

- `libBaiduMapSDK_base_v4_4_1.so`：基础，多模块共用
- `liblocSDK7a.so`：定位，多模块共用
- `libBaiduMapSDK_map_v4_4_1.so`：地图
- `libBaiduMapSDK_map_for_bikenavi_v4_4_1.so`：骑行导航的地图
- `libBaiduMapSDK_bikenavi_v4_4_1.so`：骑行导航
- `libapp_BaiduPanoramaAppLib.so`：全景图
- ...

### 自定义下载的12宫格和module的对应关系

- 基础定位、离线定位、室内定位、全量定位 => `location-base`
- 基础地图 => `map`
- 骑行导航 => `bikenavi`
- 检索功能、LBS云检索、计算工具、周边雷达 => `base`
- 驾车导航 => `navi`
- 全景图 => `panorama`