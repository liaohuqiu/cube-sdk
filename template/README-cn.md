[![Build Status](https://travis-ci.org/etao-open-source/cube-sdk.svg?branch=dev)](https://travis-ci.org/etao-open-source/cube-sdk)

Cube-SDK 是一个开发框架。这个框架致力于快速实现需求，解放生产力。

![Screen Shot](https://raw.githubusercontent.com/etao-open-source/cube-sdk/dev/screen-shot.png)

文档: http://cube-sdk.liaohuqiu.net/cn

Demo 项目移到了这里: https://github.com/liaohuqiu/android-cube-app

### 在项目中引入

项目已经发布到了Maven中央库，包括`aar`和`apklib`两种格式。在Maven或者Gradle中可如下直接引入。

#### 依赖源

最新版版本号: `{cube_sdk_version}`, 发布到了: `https://oss.sonatype.org/content/repositories/snapshots`

*   在gradle中:

```
maven {
    url 'https://oss.sonatype.org/content/repositories/snapshots'
}
```

*   pom.xml 中加入源:

```
<repository>
    <id>oss-snapshots</id>
    <url>https://oss.sonatype.org/content/repositories/snapshots</url>
    <releases>
        <enabled>false</enabled>
    </releases>
    <snapshots>
        <enabled>true</enabled>
    </snapshots>
</repository>
```

稳定版: `{cube_sdk_stable_version}`, 发布到了: `https://oss.sonatype.org/content/repositories/releases`

在gradle中:

```
mavenCentral()
```


#### 依赖

`pom.xml` 文件中

*   最新版:

```xml
<dependency>
    <groupId>in.srain.cube</groupId>
    <artifactId>cube-sdk</artifactId>
    <type>aar</type>
    <!-- or apklib format, if you want -->
    <!-- <type>apklib</type> -->
    <version>{cube_sdk_version}</version>
</dependency>
```

*   稳定版

```xml
<dependency>
    <groupId>in.srain.cube</groupId>
    <artifactId>cube-sdk</artifactId>
    <type>aar</type>
    <!-- or apklib format, if you want -->
    <!-- <type>apklib</type> -->
    <version>{cube_sdk_stable_version}</version>
</dependency>
```

*   gradle / Android Studio, 最新版

```
compile 'in.srain.cube:cube-sdk:{cube_sdk_version}@aar'
```

*   gradle / Android Studio, 稳定版

```
compile 'in.srain.cube:cube-sdk:{cube_sdk_stable_version}@aar'
```

##### eclipse

项目在Eclipse中也是可以加载编译的，但强烈建议使用Intellij IDEA 或者 Android Stuido。

直接将core文件夹下的内容作为一个类库加载，然后在项目中引用。
