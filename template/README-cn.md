[![Build Status](https://travis-ci.org/etao-open-source/cube-sdk.svg?branch=dev)](https://travis-ci.org/etao-open-source/cube-sdk)

Cube-SDK 是一个开发框架。这个框架致力于快速实现需求，解放生产力。

![Screen Shot](https://raw.githubusercontent.com/etao-open-source/cube-sdk/dev/screen-shot.png)

文档: http://cube-sdk.liaohuqiu.net/cn

Demo 项目移到了这里: https://github.com/liaohuqiu/android-cube-app

### 在项目中引入


项目现在已经发布到了maven中央库，有 `aar` and `apklib` 和两种格式

##### 在 pom.xml 中

引用apklib:

```xml
<dependency>
    <groupId>in.srain.cube</groupId>
    <artifactId>cube-sdk</artifactId>
    <type>apklib</type>
    <version>{cube_sdk_version}</version>
</dependency>
```

或者引入aar:

```
<dependency>
    <groupId>in.srain.cube</groupId>
    <artifactId>cube-sdk</artifactId>
    <type>aar</type>
    <version>{cube_sdk_version}</version>
</dependency>
```

##### 在 Gradle / Android Studio

``` gradle 
compile 'in.srain.cube:cube-sdk:{cube_sdk_version}@aar'
`````

##### eclipse

直接将core文件夹下的内容作为一个类库加载，然后在项目中引用