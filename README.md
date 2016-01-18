# 介绍
支持垂直水平两个方向，并能循环的Gallery，并且扩展实现了`CoverFlow`，支持对未选中Item进行缩放、透明度改变和层次覆盖感觉

# TwoWayGallery

`TwoWayGallery`的使用和`Gallery`的一样，并在`Gallery`基础上增加了方向属性，支持垂直和水平方向，通过属性`orientation`修改，默认为水平方向

## 基本属性
```java
<attr name="flingAllow" format="boolean"/> //是否跟随手指滑动（`Fling`）而滚动，默认为true，如果该为`false`,`Fling`操作会触发移动到下一`Item`,类似`ViewPager`
<attr name="autoCycle" format="boolean"/>//自动循环，是否真正支持循环还需要根据实际情况来判断，见下面实例的说明
<attr name="gravity"/>
<attr name="animationDuration" format="integer" min="0"/>
<attr name="spacing" format="dimension"/>//`Item`之间的间隔
<attr name="unselectedAlpha" format="float"/>//未被选中的`Item View`的`alpha`值，默认1
<attr name="orientation"> //显示方向，默认水平方向
```

## 基本使用

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
           app:flingAllow="false"
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
           app:flingAllow="false"
           />
...
```
## 支持循环模式

通过修改属性`autoCycle`来支持循环模式

__注意__:是否真正支持循环还需要根据实际情况来判断

例如：当外部数据量为3，而整个的`TwoWayGallery`有够容纳3个Item的空间，这样的情况下是不支持循环模式的

### 效果

![Demo](https://github.com/BCsl/TwoWayGallery/blob/master/ScreenShot/cycle.gif)


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

![Demo](https://github.com/BCsl/TwoWayGallery/blob/master/ScreenShot/screenshot1.gif)
