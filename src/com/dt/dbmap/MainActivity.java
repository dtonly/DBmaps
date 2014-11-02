
package com.dt.dbmap;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.MKGeneralListener;
import com.baidu.mapapi.map.LocationData;
import com.baidu.mapapi.map.MKEvent;
import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationOverlay;
import com.baidu.mapapi.map.PopupClickListener;
import com.baidu.mapapi.map.PopupOverlay;
import com.baidu.mapapi.map.RouteOverlay;
import com.baidu.mapapi.search.MKAddrInfo;
import com.baidu.mapapi.search.MKBusLineResult;
import com.baidu.mapapi.search.MKDrivingRouteResult;
import com.baidu.mapapi.search.MKPoiInfo;
import com.baidu.mapapi.search.MKPoiResult;
import com.baidu.mapapi.search.MKSearch;
import com.baidu.mapapi.search.MKShareUrlResult;
import com.baidu.mapapi.search.MKSuggestionInfo;
import com.baidu.mapapi.search.MKSuggestionResult;
import com.baidu.mapapi.search.MKTransitRouteResult;
import com.baidu.mapapi.search.MKWalkingRouteResult;
import com.baidu.platform.comapi.basestruct.GeoPoint;

@SuppressLint("NewApi")
public class MainActivity extends Activity {

    public static MainActivity instance = null;
    private boolean menu_display = false;
    private PopupWindow menuWindow;
    private LayoutInflater inflater;
    private View layout;
    private LinearLayout mClose;
    private LinearLayout mCloseBtn;

    // 路况及影像矢量
    ImageButton roadCondition = null;
    ImageButton mapLayers = null;
    private boolean roadConditionFlag = false;
    private boolean mapLayersFlag = false;
    // 放大缩小
    private ImageButton zoomIn = null;
    private ImageButton zoomOut = null;
    // 搜索服务
    private MKSearch mMKSearch = null;
    private BMapManager mBMapMan = null;
    MapView mMapView = null; // 地图View
    private MapController mMapController = null;
    // poi搜索
    private final ImageView poiSearchImage = null;
    private AutoCompleteTextView keyWorldsView = null;
    private ArrayAdapter<String> sugAdapter = null;
    private int load_Index;
    //
    RelativeLayout nearBy = null;
    RelativeLayout route = null;
    RelativeLayout toPersonal = null;
    RelativeLayout tools = null;
    // 定位相关
    /**
     * 定位SDK的核心类
     */
    private LocationClient mLocClient;
    /**
     * 用户位置信息
     */
    private LocationData mLocData;
    private Toast mToast;
    /**
     * 我的位置图层
     */
    private LocationOverlay myLocationOverlay = null;
    /**
     * 弹出窗口图层
     */
    private PopupOverlay mPopupOverlay = null;

    private boolean isRequest = false;// 是否手动触发请求定位
    private boolean isFirstLoc = true;// 是否首次定位
    /**
     * 弹出窗口图层的View
     */
    private View mPopupView;
    private BDLocation location;

    // UI相关
    OnCheckedChangeListener radioButtonListener = null;
    ImageButton requestLocButton = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_PROGRESS);
        mBMapMan = new BMapManager(getApplication());
        mBMapMan.init("EimeukKOqcU5fZGSVaIQMLVp", null);
        // 注意：请在试用setContentView前初始化BMapManager对象，否则会报错

        setContentView(R.layout.activity_main);
        mMapView = (MapView) findViewById(R.id.map_layout_stub);

        roadCondition = (ImageButton) findViewById(R.id.road_condition);
        mapLayers = (ImageButton) findViewById(R.id.map_layers);
        nearBy = (RelativeLayout) findViewById(R.id.nearby);
        route = (RelativeLayout) findViewById(R.id.route);
        toPersonal = (RelativeLayout) findViewById(R.id.to_personal);
        tools = (RelativeLayout) findViewById(R.id.tools);
        zoomIn = (ImageButton) findViewById(R.id.zoom_in);
        zoomOut = (ImageButton) findViewById(R.id.zoom_out);
        // 定位相关
        requestLocButton = (ImageButton) findViewById(R.id.requestLocButton);
        requestLocButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                requestLocation();
            }
        });
        // 实时交通信息图
        roadCondition.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (roadConditionFlag == false) {
                    roadConditionFlag = true;
                    roadCondition.setAdjustViewBounds(false);
                    Drawable myIcon1 = getResources().getDrawable(
                            R.drawable.main_icon_roadcondition_on);
                    roadCondition.setBackground(myIcon1);
                    Toast.makeText(MainActivity.this, "实时路况已打开",
                            Toast.LENGTH_SHORT).show();
                } else {
                    roadConditionFlag = false;
                    roadCondition.setAdjustViewBounds(false);
                    Drawable myIcon1 = getResources().getDrawable(
                            R.drawable.main_icon_roadcondition_off);
                    roadCondition.setBackground(myIcon1);
                    Toast.makeText(MainActivity.this, "实时路况已关闭",
                            Toast.LENGTH_SHORT).show();
                }
                mMapView.setTraffic(roadConditionFlag);
            }
        });
        // 卫星图切换
        mapLayers.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (mapLayersFlag == false) {
                    mapLayersFlag = true;
                    mapLayers.setAdjustViewBounds(false);
                    Drawable myIcon1 = getResources().getDrawable(
                            R.drawable.main_map_mode_satellite_normal);
                    mapLayers.setBackground(myIcon1);

                    Toast.makeText(MainActivity.this, "卫星图", Toast.LENGTH_SHORT)
                            .show();
                } else {
                    mapLayersFlag = false;
                    mapLayers.setAdjustViewBounds(false);
                    Drawable myIcon1 = getResources().getDrawable(
                            R.drawable.main_map_mode_plain_normal);
                    mapLayers.setBackground(myIcon1);
                    Toast.makeText(MainActivity.this, "2D平面图",
                            Toast.LENGTH_SHORT).show();
                }
                mMapView.setSatellite(mapLayersFlag);
            }
        });
        // 地图缩放控件
        zoomIn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                mMapView.getController().setZoom(mMapView.getZoomLevel() + 1);
            }
        });
        zoomOut.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                mMapView.getController().setZoom(mMapView.getZoomLevel() - 1);
            }
        });

        mMapView.getController().enableClick(true);
        mMapView.getController().setZoom(14);
        // 弹出窗口
        mPopupView = LayoutInflater.from(this).inflate(R.layout.pop_layout,
                null);

        // 实例化弹出窗口图层
        mPopupOverlay = new PopupOverlay(mMapView, new PopupClickListener() {

            /**
             * 点击弹出窗口图层回调的方法
             */
            @Override
            public void onClickedPopup(int arg0) {
                // 隐藏弹出窗口图层
                mPopupOverlay.hidePop();
            }
        });

        // 设置启用内置的缩放控件
        mMapController = mMapView.getController();
        // 得到mMapView的控制权,可以用它控制和驱动平移和缩放
        // 搜索服务
        mMKSearch = new MKSearch();
        // 注意，MKSearchListener只支持一个，以最后一次设置为准
        mMKSearch.init(mBMapMan, new MySearchListener() {
            // 在此处理详情页结果
            @Override
            public void onGetPoiDetailSearchResult(int type, int error) {
                if (error != 0) {
                    Toast.makeText(MainActivity.this, "抱歉，未找到结果",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "成功，查看详情页面",
                            Toast.LENGTH_SHORT).show();
                }
            }

            /**
             * 在此处理poi搜索结果
             */
            @Override
            public void onGetPoiResult(MKPoiResult res, int type, int error) {
                // 错误号可参考MKEvent中的定义
                if (error != 0 || res == null) {
                    Toast.makeText(MainActivity.this, "抱歉，未找到结果",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                // 将地图移动到第一个POI中心点
                if (res.getCurrentNumPois() > 0) {
                    // 将poi结果显示到地图上
                    MyPoiOverlay poiOverlay = new MyPoiOverlay(
                            MainActivity.this, mMapView, mMKSearch);
                    poiOverlay.setData(res.getAllPoi());
                    mMapView.getOverlays().clear();
                    mMapView.getOverlays().add(poiOverlay);
                    mMapView.refresh();
                    // 当ePoiType为2（公交线路）或4（地铁线路）时， poi坐标为空
                    for (MKPoiInfo info : res.getAllPoi()) {
                        if (info.pt != null) {
                            mMapView.getController().animateTo(info.pt);
                            break;
                        }
                    }
                } else if (res.getCityListNum() > 0) {
                    // 当输入关键字在本市没有找到，但在其他城市找到时，返回包含该关键字信息的城市列表
                    String strInfo = "在";
                    for (int i = 0; i < res.getCityListNum(); i++) {
                        strInfo += res.getCityListInfo(i).city;
                        strInfo += ",";
                    }
                    strInfo += "找到结果";
                    Toast.makeText(MainActivity.this, strInfo,
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onGetDrivingRouteResult(MKDrivingRouteResult result,
                    int error) {
                if (result == null) {
                    return;
                }
                RouteOverlay routeOverlay = new RouteOverlay(MainActivity.this,
                        mMapView); // 此处仅展示一个方案作为示例
                routeOverlay.setData(result.getPlan(0).getRoute(0));
                mMapView.getOverlays().add(routeOverlay);
                mMapView.refresh();
            }

            @Override
            public void onGetTransitRouteResult(MKTransitRouteResult res,
                    int error) {
            }

            @Override
            public void onGetWalkingRouteResult(MKWalkingRouteResult result,
                    int error) {
                if (result == null) {
                    return;
                }
                RouteOverlay routeOverlay = new RouteOverlay(MainActivity.this,
                        mMapView); // 此处仅展示一个方案作为示例
                routeOverlay.setData(result.getPlan(0).getRoute(0));
                mMapView.getOverlays().add(routeOverlay);
                mMapView.refresh();
            }

            @Override
            public void onGetAddrResult(MKAddrInfo res, int error) {
            }

            @Override
            public void onGetBusDetailResult(MKBusLineResult result, int iError) {
                if (iError != 0 || result == null) {
                    Toast.makeText(MainActivity.this, "抱歉，未找到结果",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                RouteOverlay routeOverlay = new RouteOverlay(MainActivity.this,
                        mMapView); // 此处仅展示一个方案作为示例
                routeOverlay.setData(result.getBusRoute());
                mMapView.getOverlays().clear();
                mMapView.getOverlays().add(routeOverlay);
                mMapView.refresh();
                mMapView.getController().animateTo(
                        result.getBusRoute().getStart());
            }

            /**
             * 更新建议列表
             */
            @Override
            public void onGetSuggestionResult(MKSuggestionResult res, int arg1) {
                if (res == null || res.getAllSuggestions() == null) {
                    return;
                }
                sugAdapter.clear();
                for (MKSuggestionInfo info : res.getAllSuggestions()) {
                    if (info.key != null)
                        sugAdapter.add(info.key);
                }
                sugAdapter.notifyDataSetChanged();

            }

            @Override
            public void onGetShareUrlResult(MKShareUrlResult result, int type,
                    int error) {
                // TODO Auto-generated method stub

            }
        });
        keyWorldsView = (AutoCompleteTextView) findViewById(R.id.searchkey);
        sugAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line);
        keyWorldsView.setAdapter(sugAdapter);

        /**
         * 当输入关键字变化时，动态更新建议列表
         */
        keyWorldsView.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable arg0) {

            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1,
                    int arg2, int arg3) {

            }

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2,
                    int arg3) {
                if (cs.length() <= 0) {
                    return;
                }
                String city = "北京";
                /**
                 * 使用建议搜索服务获取建议列表，结果在onSuggestionResult()中更新
                 */
                mMKSearch.suggestionSearch(cs.toString(), city);
            }
        });
        // 按键点击事件
        OnClickListener nearByClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳转到附近
                intentToNearBy();

            }
        };
        OnClickListener routeClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳转到路线
                intentToRoute();

            }
        };
        OnClickListener toPersonalClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳转到我的
                intentToMine();

            }
        };
        OnClickListener settingClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳转到工具
                intentToSetting();

            }
        };
        nearBy.setOnClickListener(nearByClickListener);
        route.setOnClickListener(routeClickListener);
        toPersonal.setOnClickListener(toPersonalClickListener);
        tools.setOnClickListener(settingClickListener);

        // 实例化定位服务，LocationClient类必须在主线程中声明
        mLocClient = new LocationClient(getApplicationContext());
        mLocClient.registerLocationListener(new BDLocationListenerImpl());// 注册定位监听接口

        /**
         * LocationClientOption 该类用来设置定位SDK的定位方式。
         */
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开GPRS
        option.setAddrType("all");// 返回的定位结果包含地址信息
        option.setCoorType("bd09ll");// 返回的定位结果是百度经纬度,默认值gcj02
        option.setPriority(LocationClientOption.GpsFirst); // 设置GPS优先
        option.setScanSpan(10000); // 设置发起定位请求的间隔时间为5000ms
        option.disableCache(false);// 禁止启用缓存定位
        // option.setPoiNumber(5); //最多返回POI个数
        // option.setPoiDistance(1000); //poi查询距离
        // option.setPoiExtraInfo(true); //是否需要POI的电话和地址等详细信息
        mLocClient.setLocOption(option); // 设置定位参数
        mLocClient.start(); // 调用此方法开始定位
        // 定位图层初始化
        myLocationOverlay = new LocationOverlay(mMapView);
        // 实例化定位数据，并设置在我的位置图层
        mLocData = new LocationData();
        myLocationOverlay.setData(mLocData);

        // 添加定位图层
        mMapView.getOverlays().add(myLocationOverlay);

        // 修改定位数据后刷新图层生效
        mMapView.refresh();

    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) { // 获取
                                                                               // back键

            if (menu_display) { // 如果 Menu已经打开 ，先关闭Menu
                menuWindow.dismiss();
                menu_display = false;
            } else {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, Exit.class);
                startActivity(intent);
            }
        } else if (keyCode == KeyEvent.KEYCODE_MENU) {
            // 获取 Menu键
            if (!menu_display) {
                // 获取LayoutInflater实例
                inflater = (LayoutInflater) this
                        .getSystemService(LAYOUT_INFLATER_SERVICE);
                // 这里的main布局是在inflate中加入的，以前都是直接this.setContentView()
                // 该方法返回的是一个View的对象，是布局中的根
                layout = inflater.inflate(R.layout.main_menu, null);
                // 将layout加入到PopupWindow中
                menuWindow = new PopupWindow(layout, LayoutParams.MATCH_PARENT,
                        LayoutParams.WRAP_CONTENT); // 后两个参数是width和height
                menuWindow.showAtLocation(this.findViewById(R.id.main),
                        Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0); // 设置layout在PopupWindow中显示的位置
                menuWindow.showAsDropDown(layout); // 设置弹出效果
                // 获取main中的控件
                mClose = (LinearLayout) layout.findViewById(R.id.menu_close);
                mCloseBtn = (LinearLayout) layout
                        .findViewById(R.id.menu_close_btn);
                mCloseBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        Intent intent = new Intent();
                        intent.setClass(MainActivity.this, Exit.class);
                        startActivity(intent);
                        menuWindow.dismiss(); // 响应点击事件之后关闭Menu
                    }
                });
                menu_display = true;
            } else {
                // 如果当前已经为显示状态，则隐藏起来
                menuWindow.dismiss();
                menu_display = false;
            }
            return false;
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        mMapView.destroy();
        if (mBMapMan != null) {
            mBMapMan.destroy();
            mBMapMan = null;
        }
        if (mLocClient != null)
            mLocClient.stop();
        mMKSearch.destory();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        mMapView.onPause();
        if (mBMapMan != null) {
            mBMapMan.stop();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        mMapView.onResume();
        if (mBMapMan != null) {
            mBMapMan.start();
        }
        super.onResume();
    }

    private void initMapView() {
        mMapView.setLongClickable(true);
        mMapView.getController().setZoom(14);
        mMapView.getController().enableClick(true);
        mMapView.setBuiltInZoomControls(true);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mMapView.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    public void searchButtonProcess(View v) {
        String city = "北京";
        EditText editSearchKey = (EditText) findViewById(R.id.searchkey);
        mMKSearch.poiSearchInCity(city, editSearchKey.getText().toString());
    }

    public void intentToRoute() {
        // 跳转路线
        Intent intent = new Intent(this, RoutePlan.class);
        startActivity(intent);
    }

    public void intentToNearBy() {
        // 跳转周边
        Intent intent = new Intent(this, NearBy.class);
        intent.putExtra("Latitude", location.getLatitude());
        intent.putExtra("Longitude", location.getLongitude());

        startActivity(intent);
    }

    public void intentToMine() {
        // 跳转我的
        Intent intent = new Intent();
        intent.setClass(this, MyMap.class);
        intent.putExtra("textview", location.getAddrStr());
        startActivity(intent);
    }

    public void intentToSetting() {
        // 跳转工具
        Intent intent = new Intent(this, Tools.class);
        startActivity(intent);
    }

    /**
     * 定位接口，需要实现两个方法
     */
    public class BDLocationListenerImpl implements BDLocationListener {

        /**
         * 接收异步返回的定位结果，参数是BDLocation类型参数
         */
        @Override
        public void onReceiveLocation(BDLocation location) {
            if (location == null) {
                return;
            }

            StringBuffer sb = new StringBuffer(256);
            sb.append("time : ");
            sb.append(location.getTime());
            sb.append("\nerror code : ");
            sb.append(location.getLocType());
            sb.append("\nlatitude : ");
            sb.append(location.getLatitude());
            sb.append("\nlontitude : ");
            sb.append(location.getLongitude());
            sb.append("\nradius : ");
            sb.append(location.getRadius());
            if (location.getLocType() == BDLocation.TypeGpsLocation) {
                sb.append("\nspeed : ");
                sb.append(location.getSpeed());
                sb.append("\nsatellite : ");
                sb.append(location.getSatelliteNumber());
            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
            }

            Log.e("log", sb.toString());

            MainActivity.this.location = location;

            mLocData.latitude = location.getLatitude();
            mLocData.longitude = location.getLongitude();
            // 如果不显示定位精度圈，将accuracy赋值为0即可
            mLocData.accuracy = location.getRadius();
            mLocData.direction = location.getDerect();

            // 将定位数据设置到定位图层里
            myLocationOverlay.setData(mLocData);
            // 更新图层数据执行刷新后生效
            mMapView.refresh();

            if (isFirstLoc || isRequest) {
                // 将给定的位置点以动画形式移动至地图中心
                mMapController.animateTo(new GeoPoint((int) (location
                        .getLatitude() * 1e6),
                        (int) (location.getLongitude() * 1e6)));
                showPopupOverlay(location);
                isRequest = false;
            }
            isFirstLoc = false;

        }

        /**
         * 接收异步返回的POI查询结果，参数是BDLocation类型参数
         */

        @Override
        public void onReceivePoi(BDLocation poiLocation) {

        }

    }

    // 继承MyLocationOverlay重写dispatchTap方法
    private class LocationOverlay extends MyLocationOverlay {

        public LocationOverlay(MapView arg0) {
            super(arg0);
        }

        /**
         * 在“我的位置”坐标上处理点击事件。
         */
        @Override
        protected boolean dispatchTap() {
            // 点击我的位置显示PopupOverlay
            showPopupOverlay(location);
            return super.dispatchTap();
        }

    }

    /**
     * 显示弹出窗口图层PopupOverlay
     * 
     * @param location
     */
    private void showPopupOverlay(BDLocation location) {
        TextView popText = ((TextView) mPopupView
                .findViewById(R.id.location_tips));
        popText.setText("[我的位置]\n" + location.getAddrStr());
        mPopupOverlay.showPopup(getBitmapFromView(popText),
                new GeoPoint((int) (location.getLatitude() * 1e6),
                        (int) (location.getLongitude() * 1e6)), 15);

    }

    /**
     * 手动请求定位的方法
     */
    public void requestLocation() {
        isRequest = true;

        if (mLocClient != null && mLocClient.isStarted()) {
            showToast("正在定位......");
            mLocClient.requestLocation();
        } else {
            Log.d("log", "locClient is null or not started");
        }
    }

    /**
     * 显示Toast消息
     * 
     * @param msg
     */
    private void showToast(String msg) {
        if (mToast == null) {
            mToast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(msg);
            mToast.setDuration(Toast.LENGTH_SHORT);
        }
        mToast.show();
    }

    /**
     * 将View转换成Bitmap的方法
     * 
     * @param view
     * @return
     */
    public static Bitmap getBitmapFromView(View view) {
        view.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.buildDrawingCache();
        Bitmap bitmap = view.getDrawingCache();
        return bitmap;
    }

    /**
     * 常用事件监听，用来处理通常的网络错误，授权验证错误等
     * 
     * @author xiaanming
     */
    public class MKGeneralListenerImpl implements MKGeneralListener {

        /**
         * 一些网络状态的错误处理回调函数
         */
        @Override
        public void onGetNetworkState(int iError) {
            if (iError == MKEvent.ERROR_NETWORK_CONNECT) {
                showToast("您的网络出错啦！");
            }
        }

        /**
         * 授权错误的时候调用的回调函数
         */
        @Override
        public void onGetPermissionState(int iError) {
            if (iError == MKEvent.ERROR_PERMISSION_DENIED) {
                showToast("API KEY错误, 请检查！");
            }
        }

    }
}
