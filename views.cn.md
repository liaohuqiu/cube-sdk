---
layout: default
title: 常用组件
lead: ""
---
<h1 id="page-indicator">翻页联动Tab</h1>

<h1 id="ptr-frame">下拉刷新容器</h1>

利用下拉刷新容器，可以轻松实现任意布局的的下拉刷新功能，以`GridView`为例：

```xml
<in.srain.cube.sample.ui.views.header.ptr.PtrFrameDemo

    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:id="@+id/ly_ptr_frame"
    android:layout_height="match_parent"
    android:layout_width="match_parent"
    app:ptr_content="@+id/ly_image_list_grid">

    <GridView
        android:id="@+id/ly_image_list_grid"
        android:paddingLeft="12dp"
        android:paddingRight="12dp"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:fadingEdge="none"
        android:focusable="false"
        android:horizontalSpacing="10dp"
        android:listSelector="@android:color/transparent"
        android:numColumns="2"
        android:scrollbarStyle="outsideOverlay"
        android:stretchMode="columnWidth" />

</in.srain.cube.sample.ui.views.header.ptr.PtrFrameDemo>
```
