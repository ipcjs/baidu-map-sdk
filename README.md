[![Release](https://jitpack.io/v/ipcjs/baidu-map-sdk.svg)](https://jitpack.io/#ipcjs/baidu-map-sdk)

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

### 百度地图SDK和module的对应关系

- 定位SDK => `location`
- 地图SDK => `base`/`map`/`bikennavi`
- 导航SDK => `navi`
- 全景SDK => `panorama`

### 版本号命名规则

- 库module版本号: `v1.0.3`
    - 第一个数字是大版本号, 只有4个百度地图SDK中的某个的大版本号增加了, 才增加; 
    - 第二个数字是小版本号, 只要某个百度地图SDK的版本号增加了, 就增加;
    - 第三个数字, 百度地图SDK的版本号不变, 只是修改了库module时, 增加;
- 组合module版本号: `g01_1.0.4`
    - 最前面的`g01`, 用于区分不同的自定义下载组合; 
    - 后面的三个数字和库module的相同, 同时保证第一/二的数字和库module对应

#### module版本号和百度地图SDK版本号的对应关系

| 库module | 组合module | 定位SDK |  地图SDK   | 导航SDK | 全景SDK |
|----------|------------|---------|------------|---------|---------|
| v1.0.3   | g01_1.0.4  | v7.2.0  | v4.4.1     | v3.3.1  | v2.6.0  |
| v1.1.0   | g01_1.1.0  | v7.2.0  | **v4.5.0** | v3.3.1  | v2.6.0  |

### 引入

```groovy
dependencies {
    // 引入已有的组合module, 包含的模块详见: https://github.com/ipcjs/baidu-map-sdk/tree/g01
    compile 'com.github.ipcjs:baidu-map-sdk:g01_1.1.0'
    
    // 分别引入库module, 注意: BaiduLBS_Android.jar文件需要自己去官网下载
    def baiduMapSdkVersion = 'v1.1.0'
    compile "com.github.ipcjs.baidu-map-sdk:location:${baiduMapSdkVersion}"
    compile "com.github.ipcjs.baidu-map-sdk:base:${baiduMapSdkVersion}"
    compile "com.github.ipcjs.baidu-map-sdk:map:${baiduMapSdkVersion}"
    compile "com.github.ipcjs.baidu-map-sdk:bikenavi:${baiduMapSdkVersion}"
    compile "com.github.ipcjs.baidu-map-sdk:navi:${baiduMapSdkVersion}"
    compile "com.github.ipcjs.baidu-map-sdk:panorama:${baiduMapSdkVersion}"
}
```
