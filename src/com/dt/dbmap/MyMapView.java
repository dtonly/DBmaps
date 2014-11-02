package com.dt.dbmap;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.baidu.mapapi.map.MapView;
import com.baidu.platform.comapi.basestruct.GeoPoint;

class MyMapView extends MapView {

	public MyMapView(Context arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public MyMapView(Context arg0, AttributeSet arg1, int arg2) {
		super(arg0, arg1, arg2);
		// TODO Auto-generated constructor stub
	}

	public MyMapView(Context arg0, AttributeSet arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		int x = (int) event.getX();
		int y = (int) event.getY();
		GeoPoint pt = this.getProjection().fromPixels(x, y);
		return super.onTouchEvent(event);
	}

}
