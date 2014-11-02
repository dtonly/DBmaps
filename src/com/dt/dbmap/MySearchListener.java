package com.dt.dbmap;

import com.baidu.mapapi.search.MKAddrInfo;
import com.baidu.mapapi.search.MKBusLineResult;
import com.baidu.mapapi.search.MKDrivingRouteResult;
import com.baidu.mapapi.search.MKPoiResult;
import com.baidu.mapapi.search.MKSearchListener;
import com.baidu.mapapi.search.MKShareUrlResult;
import com.baidu.mapapi.search.MKSuggestionResult;
import com.baidu.mapapi.search.MKTransitRouteResult;
import com.baidu.mapapi.search.MKWalkingRouteResult;

public class MySearchListener implements MKSearchListener {
	@Override
	public void onGetAddrResult(MKAddrInfo result, int iError) {
		// 返回地址信息搜索结果
	}

	@Override
	public void onGetDrivingRouteResult(MKDrivingRouteResult result, int iError) {
		// 返回驾乘路线搜索结果
	}

	@Override
	public void onGetPoiResult(MKPoiResult result, int type, int iError) {
		// 返回poi搜索结果
	}

	@Override
	public void onGetTransitRouteResult(MKTransitRouteResult result, int iError) {
		// 返回公交搜索结果
	}

	@Override
	public void onGetWalkingRouteResult(MKWalkingRouteResult result, int iError) {
		// 返回步行路线搜索结果
	}

	@Override
	public void onGetBusDetailResult(MKBusLineResult result, int iError) {
		// 返回公交车详情信息搜索结果
	}

	@Override
	public void onGetShareUrlResult(MKShareUrlResult result, int type, int error) {
		// 在此处理短串请求返回结果.
	}

	@Override
	public void onGetPoiDetailSearchResult(int arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onGetSuggestionResult(MKSuggestionResult arg0, int arg1) {
		// TODO Auto-generated method stub
		// 返回联想词信息搜索结果
	}
}
