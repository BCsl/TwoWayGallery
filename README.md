# 介绍
支持垂直水平两个方向，并能循环（后续添加）的Gallery，并且扩展实现了CoverFlow，支持对未选中Item进行缩放、透明度改变和层次覆盖感觉

# TwoWayGallery

`TwoWayGallery`的使用和`Gallery`的一样，并在`Gallery`基础上增加了方向属性，支持垂直和水平方向

使用

```java
//水平方向
...
<com.hellocsl.twowaygallery.TwoWayGallery
           android:id="@+id/gallery_vertical"
           android:layout_width="wrap_content"
           android:layout_height="match_parent"
           android:layout_gravity="center"
           app:orientation="horizontal"
           app:spacing="2dip"
           app:unselectedAlpha="0.7"
           />
...

//or 垂直方向
...
<com.hellocsl.twowaygallery.TwoWayGallery
           android:id="@+id/gallery_vertical"
           android:layout_width="wrap_content"
           android:layout_height="match_parent"
           android:layout_gravity="center"
           app:orientation="vertical"
           app:spacing="2dip"
           app:unselectedAlpha="0.7"
           />
...
```

# CoverFlow

类似[FancyCoverFlow](https://github.com/davidschreiber/FancyCoverFlow)，并且更有效率，不会不断地调用`getChildStaticTransformation`方法，显示效果为平铺，通过`coverage`属性修改层次覆盖属性

## 使用
```java
...
<com.hellocsl.twowaygallery.flow.CoverFlow
          android:id="@+id/gallery_horizontal"
          android:layout_width="match_parent"
          android:layout_height="wrap_content"
          android:layout_gravity="center"
          app:coverage="0.2"
          app:orientation="horizontal"
          app:unselectedAlpha="0.7"
          app:unselectedScale="0.8"
          />
...
or
...
<com.hellocsl.twowaygallery.flow.CoverFlow
    android:id="@+id/gallery_vertical"
    android:layout_width="wrap_content"
    android:layout_height="match_parent"
    android:layout_gravity="center"
    app:orientation="vertical"
    app:spacing="2dip"
    app:unselectedAlpha="0.7"
    app:unselectedScale="0.8"
    />
...
```

## 效果

![Demo](https://github.com/BCsl/TwoWayGallery/tree/master/ScreenShot/screenshot1.gif)
