# baidu-map-sdk

[![Release](https://jitpack.io/v/ipcjs/baidu-map-sdk.svg)](https://jitpack.io/#ipcjs/baidu-map-sdk)

目前官方的国内版SDK已经支持通过[mavenCentral](https://lbsyun.baidu.com/index.php?title=android-navsdk/guide/projectConfiguration#:~:text=%E4%B8%80%E4%BD%93%E5%8C%96%E5%8C%85%20%E3%80%82-,%E9%80%9A%E8%BF%87Gradle%20%E9%9B%86%E6%88%90sdk,-1%E3%80%81%E5%9C%A8Project)集成进项目，但国外版还不支持，故当前项目从`v3.0.0`开始只维护外国版的包

## 文件结构

注意：由于百度操蛋的打包机制，[选择不同的模块](http://lbsyun.baidu.com/index.php?title=sdk/download&action#selected=mapsdk_basicmap,mapsdk_searchfunction,mapsdk_lbscloudsearch,mapsdk_calculationtool,mapsdk_radar)
下载的`BaiduLBS_Android.jar`文件不一样，故`库module`不提供`BaiduLBS_Android.jar`
，这个文件需要你手动下载，其他jar和so文件因为不存在复用的情况，这里都会提供。  
或者你也可以直接使用已经包含`BaiduLBS_Android.jar`的`组合module`: [g01](https://github.com/ipcjs/baidu-map-sdk/tree/g01)、[g02](https://github.com/ipcjs/baidu-map-sdk/tree/g02)

### so文件

so文件和module的对应关系详见：[copySoFilesToModules](build.gradle#L31)

### 自定义下载的12宫格和module的对应关系

- 基础定位、离线定位 => `location`
- 室内定位、全量定位 => `location-indoor`（依赖`location`）
- 基础地图 => `map`（依赖`base`）
- 骑行导航 => `bikenavi`（依赖`base`）
- 检索功能、LBS云检索、计算工具 => `base`
- 驾车导航 => `navi`（未维护）
- 全景图 => `panorama`
- LBS AR => `ar`（未维护）

### 百度地图SDK和module的对应关系

- 定位SDK => `location`/`location-indoor`
- 地图SDK => `base`/`map`/`bikennavi`
    - 点聚合工具 => `clusterutil`
- 导航SDK => `navi`（未维护）
- 全景SDK => `panorama`
- AR地图SDK => `ar`（未维护）

### 版本号命名规则

- `库module`版本号: `v1.0.3`
    - 第一个数字是大版本号, 只有4个百度地图SDK中的某个的大版本号增加了, 才增加;
    - 第二个数字是小版本号, 只要某个百度地图SDK的版本号增加了, 就增加;
    - 第三个数字, 百度地图SDK的版本号不变, 只是修改了`库module`时, 增加;
- `组合module`版本号: `g01_1.0.4`
    - 最前面的`g01`, 用于区分不同的自定义下载组合;
    - 后面的三个数字和`库module`的相同, 同时保证第一/二的数字和`库module`对应

#### module版本号和百度地图SDK版本号的对应关系

| 库module | g01       | g02         | 定位SDK | 地图SDK | 导航SDK | 全景SDK | AR地图SDK | 说明                              |
| -------- | --------- | ----------- | ------- | ------- | ------- | ------- | --------- | --------------------------------- |
| v1.0.3   | g01_1.0.4 | --          | v7.2.0  | v4.4.1  | v3.3.1  | v2.6.0  | --        | 建立项目                          |
| v1.1.0   | g01_1.1.0 | `g02_1.1.1` | --      | v4.5.0  | --      | --      | --        | 升级地图SDK                       |
| v1.1.2   | --        | --          | --      | --      | --      | --      | --        | 增加utils模块                     |
| v1.2.0   | g01_1.2.5 | --          | v7.3.0  | v4.5.2  | --      | --      | --        | 升级地图和定位                    |
| v2.0.0   | g01_2.0.1 | --          | v7.6.0  | v5.2.1  | --      | v2.6.2  | v1.0.0    | 除navi外全面升级                  |
| v2.1.0   | g01_2.1.1 | --          | v7.9.0  | v5.4.4  | --      | v2.8.5  | --        | 紧急升级g01相关的模块, 文档未升级 |
| v3.0.0   | g01_3.0.0 | --          | v9.3.6  | v7.5.4  | 未维护  | v2.9.2  | 未维护    | 全面升级到国外版SDK               |

### 引入

```groovy
dependencies {
    // 引入已有的组合module g01, 包含的模块详见: https://github.com/ipcjs/baidu-map-sdk/tree/g01
    compile 'com.github.ipcjs:baidu-map-sdk:g01_3.0.0'

    // 引入另一个组合module g02, 包含的模块详见: https://github.com/ipcjs/baidu-map-sdk/tree/g02
    compile 'com.github.ipcjs:baidu-map-sdk:g02_1.1.1'

    // 分别引入库module, 注意: BaiduLBS_Android.jar文件需要自己去官网下载
    def baiduMapSdkVersion = 'v3.0.0'
    compile "com.github.ipcjs.baidu-map-sdk:location:${baiduMapSdkVersion}"
    compile "com.github.ipcjs.baidu-map-sdk:base:${baiduMapSdkVersion}"
    compile "com.github.ipcjs.baidu-map-sdk:map:${baiduMapSdkVersion}"
    compile "com.github.ipcjs.baidu-map-sdk:bikenavi:${baiduMapSdkVersion}"
    compile "com.github.ipcjs.baidu-map-sdk:navi:${baiduMapSdkVersion}"
    compile "com.github.ipcjs.baidu-map-sdk:panorama:${baiduMapSdkVersion}"

    // utils模块, 包含点聚合功能; 注意: 需要手动引入该模块依赖的BaiduLBS_Android.jar和support-v4包
    compile "com.github.ipcjs.baidu-map-sdk:utils:${baiduMapSdkVersion}"
}
```
