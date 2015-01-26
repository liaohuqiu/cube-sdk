[![Build Status](https://travis-ci.org/etao-open-source/cube-sdk.svg?branch=dev)](https://travis-ci.org/etao-open-source/cube-sdk)

##[中文说明](https://github.com/etao-open-source/cube-sdk/blob/master/README-cn.md)


`Cube` is a light package for Android development.

![Screen Shot](https://raw.githubusercontent.com/etao-open-source/cube-sdk/dev/screen-shot.png)

DEMO project has been moved to [HERE](https://github.com/liaohuqiu/android-cube-app).

All of the `readme` content and document are moved to Github Pages, please visit the Github Pages for more imformation:

http://cube-sdk.liaohuqiu.net

### Import to your project

Cube-SDK has been pushed to Maven Central, both in `aar` and `apklib` format.

##### Using in pom.xml

```xml
<dependency>
    <groupId>in.srain.cube</groupId>
    <artifactId>cube-sdk</artifactId>
    <type>apklib</type>
    <version>{cube_sdk_version}</version>
</dependency>
```

or:

```
<dependency>
    <groupId>in.srain.cube</groupId>
    <artifactId>cube-sdk</artifactId>
    <type>aar</type>
    <version>{cube_sdk_version}</version>
</dependency>
```

###### Gradle / Android Studio

``` gradle 
compile 'in.srain.cube:cube-sdk:{cube_sdk_version}@aar'
`````

###### Eclipse

Load the content into you eclipse, it's library project. Then use it in your application project.


##### Components

cube-sdk contains:

* CLog
