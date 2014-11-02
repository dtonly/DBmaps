package com.dt.dbmap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.map.MKEvent;
import com.baidu.mapapi.map.MKMapTouchListener;
import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationOverlay;
import com.baidu.mapapi.map.PopupClickListener;
import com.baidu.mapapi.map.PopupOverlay;
import com.baidu.mapapi.map.RouteOverlay;
import com.baidu.mapapi.map.TransitOverlay;
import com.baidu.mapapi.search.MKAddrInfo;
import com.baidu.mapapi.search.MKBusLineResult;
import com.baidu.mapapi.search.MKDrivingRouteResult;
import com.baidu.mapapi.search.MKPlanNode;
import com.baidu.mapapi.search.MKPoiResult;
import com.baidu.mapapi.search.MKRoute;
import com.baidu.mapapi.search.MKSearch;
import com.baidu.mapapi.search.MKSearchListener;
import com.baidu.mapapi.search.MKShareUrlResult;
import com.baidu.mapapi.search.MKSuggestionResult;
import com.baidu.mapapi.search.MKTransitRouteResult;
import com.baidu.mapapi.search.MKWalkingRouteResult;
import com.baidu.platform.comapi.basestruct.GeoPoint;

/**
 * 此demo用来展示如何进行驾车、步行、公交路线搜索并在地图使用RouteOverlay、TransitOverlay绘制
 * 同时展示如何进行节点浏览并弹出泡泡
 * 
 */
public class RoutePlan extends Activity {

	// UI相关
	ImageView leftBack = null;
	ImageView mBtnDrive = null; // 驾车搜索
	ImageView mBtnTransit = null; // 公交搜索
	ImageView mBtnWalk = null; // 步行搜索
	EditText editSt = null;
	EditText editEn = null;
	MKPlanNode stNode = null;
	MKPlanNode enNode = null;
	// 浏览路线节点相关
	Button mBtnPre = null;// 上一个节点
	Button mBtnNext = null;// 下一个节点
	int nodeIndex = -2;// 节点索引,供浏览节点时使用
	MKRoute route = null;// 保存驾车/步行路线数据的变量，供浏览节点时使用
	TransitOverlay transitOverlay = null;// 保存公交路线图层数据的变量，供浏览节点时使用
	RouteOverlay routeOverlay = null;
	boolean useDefaultIcon = false;
	int searchType = -1;// 记录搜索的类型，区分驾车/步行和公交
	private PopupOverlay pop = null;// 弹出泡泡图层，浏览节点时使用
	private TextView popupText = null;// 泡泡view
	private View viewCache = null;
	/**
	 * 弹出窗口图层的View
	 */
	private View mPopupView;
	private BDLocation location;
	/**
	 * 弹出窗口图层
	 */
	private PopupOverlay mPopupOverlay = null;
	boolean flag = false;
	boolean flagSearchWay = false;

	// 地图相关，使用继承MapView的MyRouteMapView目的是重写touch事件实现泡泡处理
	// 如果不处理touch事件，则无需继承，直接使用MapView即可
	MyMapView mMapView = null; // 地图View
	// 搜索相关
	MKSearch mSearch = null; // 搜索模块，也可去掉地图模块独立使用
	BMapManager mBMapMan = null;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mBMapMan = new BMapManager(getApplication());
		mBMapMan.init("EimeukKOqcU5fZGSVaIQMLVp", null);
		// 注意：请在试用setContentView前初始化BMapManager对象，否则会报错
		setContentView(R.layout.routesearch);
		mMapView = (MyMapView) findViewById(R.id.bmapView);
		mMapView.setBuiltInZoomControls(true);
		// 设置启用内置的缩放控件
		MapController mMapController = mMapView.getController();
		// 得到mMapView的控制权,可以用它控制和驱动平移和缩放
		mMapController.setZoom(14);// 设置地图zoom级别
		// 初始化地图
		mMapView.getController().enableClick(true);

		// 初始化按键
		editSt = (EditText) findViewById(R.id.EditText_navsearch_start);
		editEn = (EditText) findViewById(R.id.EditText_navsearch_end);
		leftBack = (ImageView) findViewById(R.id.iv_topbar_left_back);
		mBtnDrive = (ImageView) findViewById(R.id.iv_topbar_middle_car);
		mBtnTransit = (ImageView) findViewById(R.id.iv_topbar_middle_bus);
		mBtnWalk = (ImageView) findViewById(R.id.iv_topbar_middle_foot);
		mBtnPre = (Button) findViewById(R.id.pre);
		mBtnNext = (Button) findViewById(R.id.next);
		mBtnPre.setVisibility(View.INVISIBLE);
		mBtnNext.setVisibility(View.INVISIBLE);
		stNode = new MKPlanNode();
		enNode = new MKPlanNode();
		editSt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				stNode = new MKPlanNode();
				enNode = new MKPlanNode();
			}
		});
		editEn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				stNode = new MKPlanNode();
				enNode = new MKPlanNode();
			}
		});
		// 按键点击事件
		leftBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				RoutePlan.this.finish();
			}
		});
		OnClickListener clickListener = new OnClickListener() {
			public void onClick(View v) {
				// 发起搜索
				SearchButtonProcess(v);
			}
		};
		OnClickListener nodeClickListener = new OnClickListener() {
			public void onClick(View v) {
				// 浏览路线节点
				nodeClick(v);
			}
		};

		mBtnDrive.setOnClickListener(clickListener);
		mBtnTransit.setOnClickListener(clickListener);
		mBtnWalk.setOnClickListener(clickListener);
		mBtnPre.setOnClickListener(nodeClickListener);
		mBtnNext.setOnClickListener(nodeClickListener);
		// 创建 弹出泡泡图层
		createPaopao();
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
				if (flag == false)
					editEn.setHint("地图上的点");
				else
					editSt.setHint("地图上的点");
				mPopupOverlay.hidePop();
				flagSearchWay = false;
			}

		});

		// 地图点击事件处理
		mMapView.regMapTouchListner(new MKMapTouchListener() {

			@Override
			public void onMapClick(GeoPoint point) {
				// 在此处理地图点击事件
				// 消隐pop
				if (pop != null) {
					pop.hidePop();
				}
				showPopupOverlay(point);				
			}

			@Override
			public void onMapDoubleClick(GeoPoint point) {

			}

			@Override
			public void onMapLongClick(GeoPoint point) {
 
			}

		});
		// 初始化搜索模块，注册事件监听
		mSearch = new MKSearch();
		mSearch.init(mBMapMan, new MKSearchListener() {

			public void onGetDrivingRouteResult(MKDrivingRouteResult res,
					int error) {
				// 起点或终点有歧义，需要选择具体的城市列表或地址列表
				if (error == MKEvent.ERROR_ROUTE_ADDR) {
					// 遍历所有地址
					// ArrayList<MKPoiInfo> stPois =
					// res.getAddrResult().mStartPoiList;
					// ArrayList<MKPoiInfo> enPois =
					// res.getAddrResult().mEndPoiList;
					// ArrayList<MKCityListInfo> stCities =
					// res.getAddrResult().mStartCityList;
					// ArrayList<MKCityListInfo> enCities =
					// res.getAddrResult().mEndCityList;
					return;
				}
				// 错误号可参考MKEvent中的定义
				if (error != 0 || res == null) {
					Toast.makeText(RoutePlan.this, "抱歉，未找到结果",
							Toast.LENGTH_SHORT).show();
					return;
				}

				searchType = 0;
				routeOverlay = new RouteOverlay(RoutePlan.this, mMapView);
				// 此处仅展示一个方案作为示例
				routeOverlay.setData(res.getPlan(0).getRoute(0));
				// 清除其他图层
				mMapView.getOverlays().clear();
				// 添加路线图层
				mMapView.getOverlays().add(routeOverlay);
				// 执行刷新使生效
				mMapView.refresh();
				// 使用zoomToSpan()绽放地图，使路线能完全显示在地图上
				mMapView.getController().zoomToSpan(
						routeOverlay.getLatSpanE6(),
						routeOverlay.getLonSpanE6());
				// 移动地图到起点
				mMapView.getController().animateTo(res.getStart().pt);
				// 将路线数据保存给全局变量
				route = res.getPlan(0).getRoute(0);
				// 重置路线节点索引，节点浏览时使用
				nodeIndex = -1;
				mBtnPre.setVisibility(View.VISIBLE);
				mBtnNext.setVisibility(View.VISIBLE);
			}

			public void onGetTransitRouteResult(MKTransitRouteResult res,
					int error) {
				// 起点或终点有歧义，需要选择具体的城市列表或地址列表
				if (error == MKEvent.ERROR_ROUTE_ADDR) {
					// 遍历所有地址
					// ArrayList<MKPoiInfo> stPois =
					// res.getAddrResult().mStartPoiList;
					// ArrayList<MKPoiInfo> enPois =
					// res.getAddrResult().mEndPoiList;
					// ArrayList<MKCityListInfo> stCities =
					// res.getAddrResult().mStartCityList;
					// ArrayList<MKCityListInfo> enCities =
					// res.getAddrResult().mEndCityList;
					return;
				}
				if (error != 0 || res == null) {
					Toast.makeText(RoutePlan.this, "抱歉，未找到结果",
							Toast.LENGTH_SHORT).show();
					return;
				}

				searchType = 1;
				transitOverlay = new TransitOverlay(RoutePlan.this, mMapView);
				// 此处仅展示一个方案作为示例
				/**** 多个方案显示**调用方法获取哪条路线 */

				transitOverlay.setData(res.getPlan(0));
				// 清除其他图层
				mMapView.getOverlays().clear();
				// 添加路线图层
				mMapView.getOverlays().add(transitOverlay);
				// 执行刷新使生效
				mMapView.refresh();
				// 使用zoomToSpan()绽放地图，使路线能完全显示在地图上
				mMapView.getController().zoomToSpan(
						transitOverlay.getLatSpanE6(),
						transitOverlay.getLonSpanE6());
				// 移动地图到起点
				mMapView.getController().animateTo(res.getStart().pt);
				// 重置路线节点索引，节点浏览时使用
				nodeIndex = 0;
				mBtnPre.setVisibility(View.VISIBLE);
				mBtnNext.setVisibility(View.VISIBLE);
			}

			public void onGetWalkingRouteResult(MKWalkingRouteResult res,
					int error) {
				// 起点或终点有歧义，需要选择具体的城市列表或地址列表
				if (error == MKEvent.ERROR_ROUTE_ADDR) {
					// 遍历所有地址
					// ArrayList<MKPoiInfo> stPois =
					// res.getAddrResult().mStartPoiList;
					// ArrayList<MKPoiInfo> enPois =
					// res.getAddrResult().mEndPoiList;
					// ArrayList<MKCityListInfo> stCities =
					// res.getAddrResult().mStartCityList;
					// ArrayList<MKCityListInfo> enCities =
					// res.getAddrResult().mEndCityList;
					return;
				}
				if (error != 0 || res == null) {
					Toast.makeText(RoutePlan.this, "抱歉，未找到结果",
							Toast.LENGTH_SHORT).show();
					return;
				}

				searchType = 2;
				routeOverlay = new RouteOverlay(RoutePlan.this, mMapView);
				// 此处仅展示一个方案作为示例
				routeOverlay.setData(res.getPlan(0).getRoute(0));
				// 清除其他图层
				mMapView.getOverlays().clear();
				// 添加路线图层
				mMapView.getOverlays().add(routeOverlay);
				// 执行刷新使生效
				mMapView.refresh();
				// 使用zoomToSpan()绽放地图，使路线能完全显示在地图上
				mMapView.getController().zoomToSpan(
						routeOverlay.getLatSpanE6(),
						routeOverlay.getLonSpanE6());
				// 移动地图到起点
				mMapView.getController().animateTo(res.getStart().pt);
				// 将路线数据保存给全局变量
				route = res.getPlan(0).getRoute(0);
				// 重置路线节点索引，节点浏览时使用
				nodeIndex = -1;
				mBtnPre.setVisibility(View.VISIBLE);
				mBtnNext.setVisibility(View.VISIBLE);

			}

			public void onGetAddrResult(MKAddrInfo res, int error) {
				if (error != 0) {
					String str = String.format("错误号：%d", error);
					return;
				}
				String strInfo = String.format("纬度：%f 经度：%f 地址：%s\r\n",
						res.geoPt.getLatitudeE6() / 1e6,
						res.geoPt.getLongitudeE6() / 1e6,
						res.addressComponents.city
								+ res.addressComponents.district
								+ res.addressComponents.street);
			}

			public void onGetPoiResult(MKPoiResult res, int arg1, int arg2) {
			}

			public void onGetBusDetailResult(MKBusLineResult result, int iError) {
			}

			@Override
			public void onGetSuggestionResult(MKSuggestionResult res, int arg1) {
			}

			@Override
			public void onGetPoiDetailSearchResult(int type, int iError) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onGetShareUrlResult(MKShareUrlResult result, int type,
					int error) {
				// TODO Auto-generated method stub

			}
		});
	}

	/**
	 * 发起路线规划搜索示例
	 * 
	 * @param v
	 */
	void SearchButtonProcess(View v) {
		// 重置浏览节点的路线数据
		route = null;
		routeOverlay = null;
		transitOverlay = null;
		mBtnPre.setVisibility(View.INVISIBLE);
		mBtnNext.setVisibility(View.INVISIBLE);
		// 对起点终点的name进行赋值，也可以直接对坐标赋值，赋值坐标则将根据坐标进行搜索
		if (flagSearchWay == false) {

			stNode.name = editSt.getText().toString();
			enNode.name = editEn.getText().toString();
		}
		// 实际使用中请对起点终点城市进行正确的设定
		if (mBtnDrive.equals(v)) {
			mSearch.drivingSearch("北京", stNode, "北京", enNode);
		} else if (mBtnTransit.equals(v)) {
			mSearch.transitSearch("北京", stNode, enNode);
		} else if (mBtnWalk.equals(v)) {
			mSearch.walkingSearch("北京", stNode, "北京", enNode);
		}
	}

	/**
	 * 节点浏览示例
	 * 
	 * @param v
	 */
	public void nodeClick(View v) {
		viewCache = getLayoutInflater()
				.inflate(R.layout.custom_text_view, null);
		popupText = (TextView) viewCache.findViewById(R.id.textcache);
		if (searchType == 0 || searchType == 2) {
			// 驾车、步行使用的数据结构相同，因此类型为驾车或步行，节点浏览方法相同
			if (nodeIndex < -1 || route == null
					|| nodeIndex >= route.getNumSteps())
				return;

			// 上一个节点
			if (mBtnPre.equals(v) && nodeIndex > 0) {
				// 索引减
				nodeIndex--;
				// 移动到指定索引的坐标
				mMapView.getController().animateTo(
						route.getStep(nodeIndex).getPoint());
				// 弹出泡泡
				popupText.setBackgroundResource(R.drawable.popup);
				popupText.setText(route.getStep(nodeIndex).getContent());
				pop.showPopup(BMapUtil.getBitmapFromView(popupText), route
						.getStep(nodeIndex).getPoint(), 5);
			}
			// 下一个节点
			if (mBtnNext.equals(v) && nodeIndex < (route.getNumSteps() - 1)) {
				// 索引加
				nodeIndex++;
				// 移动到指定索引的坐标
				mMapView.getController().animateTo(
						route.getStep(nodeIndex).getPoint());
				// 弹出泡泡
				popupText.setBackgroundResource(R.drawable.popup);
				popupText.setText(route.getStep(nodeIndex).getContent());
				pop.showPopup(BMapUtil.getBitmapFromView(popupText), route
						.getStep(nodeIndex).getPoint(), 5);
			}
		}
		if (searchType == 1) {
			// 公交换乘使用的数据结构与其他不同，因此单独处理节点浏览
			if (nodeIndex < -1 || transitOverlay == null
					|| nodeIndex >= transitOverlay.getAllItem().size())
				return;

			// 上一个节点
			if (mBtnPre.equals(v) && nodeIndex > 1) {
				// 索引减
				nodeIndex--;
				// 移动到指定索引的坐标
				mMapView.getController().animateTo(
						transitOverlay.getItem(nodeIndex).getPoint());
				// 弹出泡泡
				popupText.setBackgroundResource(R.drawable.popup);
				popupText.setText(transitOverlay.getItem(nodeIndex).getTitle());
				pop.showPopup(BMapUtil.getBitmapFromView(popupText),
						transitOverlay.getItem(nodeIndex).getPoint(), 5);
			}
			// 下一个节点
			if (mBtnNext.equals(v)
					&& nodeIndex < (transitOverlay.getAllItem().size() - 2)) {
				// 索引加
				nodeIndex++;
				// 移动到指定索引的坐标
				mMapView.getController().animateTo(
						transitOverlay.getItem(nodeIndex).getPoint());
				// 弹出泡泡
				popupText.setBackgroundResource(R.drawable.popup);
				popupText.setText(transitOverlay.getItem(nodeIndex).getTitle());
				pop.showPopup(BMapUtil.getBitmapFromView(popupText),
						transitOverlay.getItem(nodeIndex).getPoint(), 5);
			}
		}

	}

	/**
	 * 创建弹出泡泡图层
	 */
	public void createPaopao() {

		// 泡泡点击响应回调
		PopupClickListener popListener = new PopupClickListener() {
			@Override
			public void onClickedPopup(int index) {
				Log.v("click", "clickapoapo");
			}
		};
		pop = new PopupOverlay(mMapView, popListener);
	}

	@Override
	protected void onPause() {
		mMapView.onPause();
		super.onPause();
	}

	@Override
	protected void onResume() {
		mMapView.onResume();
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		mMapView.destroy();
		mSearch.destory();
		super.onDestroy();
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

	private void showPopupOverlay(GeoPoint pt) {
		TextView popText = ((TextView) mPopupView
				.findViewById(R.id.location_tips));
		flagSearchWay = true;
		if (flag == false) {
			popText.setText("设为起点");
			stNode.pt = pt;
			flag = true;
		} else {
			popText.setText("设为终点");
			enNode.pt = pt;
			flag = false;
		}
		mPopupOverlay.showPopup(getBitmapFromView(popText), pt, 15);

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
}
