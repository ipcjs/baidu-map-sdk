
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

- 基础定位、离线定位、室内定位、全量定位 => `location`
- 基础地图 => `map`（依赖`base`）
- 骑行导航 => `bikenavi`（依赖`base`）
- 检索功能、LBS云检索、计算工具、周边雷达 => `base`
- 驾车导航 => `navi`
- 全景图 => `panorama`

### 百度地图SDK及其版本和module的对应关系

- 定位SDK v7.2.0 => `location`
- 地图SDK v4.4.1 => `base`/`map`/`bikennavi`
- 导航SDK v3.3.1 => `navi`
- 全景SDK v2.6.0 => `panorama`