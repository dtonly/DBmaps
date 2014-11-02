package com.dt.dbmap;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.MapView;
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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class Result extends Activity {
	TextView text = null;
	ImageView leftBack = null;
	// 搜索服务
	private MKSearch mMKSearch = null;
	private BMapManager mBMapMan = null;
	MapView mMapView = null; // 地图View
	private MapController mMapController = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mBMapMan = new BMapManager(getApplication());
		mBMapMan.init("zwLfj1rEdfrp2stnbqFAtzWI", null);
		setContentView(R.layout.nearby_result);
		text = (TextView) findViewById(R.id.title);
		mMapView = (MapView) findViewById(R.id.map_layout_stub);
		leftBack = (ImageView) findViewById(R.id.iv_topbar_left_back);
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		String str = bundle.getString("btnText");// getString()返回指定key的值
		Double Latitude = bundle.getDouble("Latitude");// getString()返回指定key的值
		Double Longitude = bundle.getDouble("Longitude");
		//(int) (mLat*1E6)
		GeoPoint point=new GeoPoint((int)(Latitude*1E6),(int)(Longitude*1E6));
		text.setText(str);
		// 设置启用内置的缩放控件
		mMapView.getController().enableClick(true);
		mMapView.getController().setZoom(14);
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
					Toast.makeText(Result.this, "抱歉，未找到结果", Toast.LENGTH_SHORT)
							.show();
				} else {
					Toast.makeText(Result.this, "成功，查看详情页面", Toast.LENGTH_SHORT)
							.show();
				}
			}

			/**
			 * 在此处理poi搜索结果
			 */
			public void onGetPoiResult(MKPoiResult res, int type, int error) {
				// 错误号可参考MKEvent中的定义
				if (error != 0 || res == null) {
					Toast.makeText(Result.this, "抱歉，未找到结果", Toast.LENGTH_LONG)
							.show();
					return;
				}
				// 将地图移动到第一个POI中心点
				if (res.getCurrentNumPois() > 0) {
					// 将poi结果显示到地图上
					MyPoiOverlay poiOverlay = new MyPoiOverlay(Result.this,
							mMapView, mMKSearch);
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
					Toast.makeText(Result.this, strInfo, Toast.LENGTH_LONG)
							.show();
				}
			}

			public void onGetDrivingRouteResult(MKDrivingRouteResult result,
					int error) {
				if (result == null) {
					return;
				}
				RouteOverlay routeOverlay = new RouteOverlay(Result.this,
						mMapView); // 此处仅展示一个方案作为示例
				routeOverlay.setData(result.getPlan(0).getRoute(0));
				mMapView.getOverlays().add(routeOverlay);
				mMapView.refresh();
			}

			public void onGetTransitRouteResult(MKTransitRouteResult res,
					int error) {
			}

			public void onGetWalkingRouteResult(MKWalkingRouteResult result,
					int error) {
			}

			public void onGetAddrResult(MKAddrInfo res, int error) {
			}

			public void onGetBusDetailResult(MKBusLineResult result, int iError) {

			}

			@Override
			public void onGetShareUrlResult(MKShareUrlResult result, int type,
					int error) {
				// TODO Auto-generated method stub

			}
		});
		;
		mMKSearch.poiSearchNearBy(str, point, 5000);
		leftBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Result.this.finish();
			}
		});
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

	@Override
	protected void onDestroy() {

		super.onDestroy();
	}

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
}
