package com.dt.dbmap;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.map.Geometry;
import com.baidu.mapapi.map.Graphic;
import com.baidu.mapapi.map.GraphicsOverlay;
import com.baidu.mapapi.map.MKMapTouchListener;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Symbol;
import com.baidu.mapapi.map.Symbol.Stroke;
import com.baidu.mapapi.map.TextItem;
import com.baidu.mapapi.map.TextOverlay;
import com.baidu.mapapi.utils.DistanceUtil;
import com.baidu.platform.comapi.basestruct.GeoPoint;

/**
 * 在地图上用GraphicsOverlay添加点、线 测量路线距离
 * 
 */
public class GeometryLine extends Activity {

	// 地图相关
	MyMapView mMapView = null;
	BMapManager mBMapMan = null;

	// UI相关
	ImageButton backBtn = null;
	ImageButton clearBtn = null;
	ImageView leftBack = null;
	TextView text = null;
	boolean clearFlag = false;
	boolean backFlag = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mBMapMan = new BMapManager(getApplication());
		mBMapMan.init("zwLfj1rEdfrp2stnbqFAtzWI", null);
		setContentView(R.layout.measure);
		// 初始化地图
		mMapView = (MyMapView) findViewById(R.id.mymapView);
		mMapView.getController().setZoom(12.5f);
		mMapView.getController().enableClick(true);

		// UI初始化
		clearBtn = (ImageButton) findViewById(R.id.btn_clear);
		leftBack = (ImageView) findViewById(R.id.left_back);
		text = (TextView) findViewById(R.id.total_dis_tv);
		mMapView.regMapTouchListner(new MKMapTouchListener() {

			List<GeoPoint> list = new ArrayList<GeoPoint>();

			@Override
			public void onMapClick(GeoPoint pt) {
				// TODO Auto-generated method stub

				if (clearFlag == false) {
					GraphicsOverlay graphicsOverlay = new GraphicsOverlay(
							mMapView);
					mMapView.getOverlays().add(graphicsOverlay);
					graphicsOverlay.setData(drawPoint(pt));
					list.add(pt);
					double distance = 0.0;
					for (int i = 0; i < list.size(); i++) {
						if (i == list.size() - 1) {
							distance += 0;
						} else
							distance += DistanceUtil.getDistance(list.get(i),
									list.get(i + 1));
					}
					Toast.makeText(
							GeometryLine.this,
							"距离：" + ((int) (distance / 1000 * 100) + 5) / 10
									/ 10.0 + "公里", Toast.LENGTH_SHORT).show();
					text.setText("" + ((int) (distance / 1000 * 100) + 5) / 10
							/ 10.0 + "公里");
					graphicsOverlay.setData(drawLine(list));
				} else {
					list.clear();
					clearFlag = false;
				}
			}

			@Override
			public void onMapLongClick(GeoPoint arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onMapDoubleClick(GeoPoint arg0) {
				// TODO Auto-generated method stub

			}

		});
		OnClickListener clearListener = new OnClickListener() {
			public void onClick(View v) {
				clearFlag = true;
				clearClick();
			}
		};
		OnClickListener backListener = new OnClickListener() {
			public void onClick(View v) {
				backFlag = true;
			}
		};

		clearBtn.setOnClickListener(clearListener);
		// backBtn.setOnClickListener(backListener);
		leftBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				GeometryLine.this.finish();
			}
		});
		// 界面加载时添加绘制图层
		// addCustomElementsDemo();
	}

	public void clearClick() {
		// 清除所有图层
		mMapView.getOverlays().clear();
	}

	/**
	 * 绘制折线，该折线状态随地图状态变化
	 * 
	 * @return 折线对象
	 */
	public Graphic drawLine(List<GeoPoint> list) {
		// 构建线
		GeoPoint[] lineGeoPoint = (GeoPoint[]) list.toArray(new GeoPoint[list
				.size()]);
		Geometry lineGeometry = new Geometry();
		lineGeometry.setPolyLine(lineGeoPoint);
		// 设定样式
		Symbol lineSymbol = new Symbol();
		Symbol.Color lineColor = lineSymbol.new Color();
		lineColor.red = 255;
		lineColor.green = 0;
		lineColor.blue = 0;
		lineColor.alpha = 255;
		lineSymbol.setLineSymbol(lineColor, 8);
		// 生成Graphic对象
		Graphic lineGraphic = new Graphic(lineGeometry, lineSymbol);
		return lineGraphic;
	}

	/**
	 * 绘制单点，该点状态不随地图状态变化而变化
	 * 
	 * @return 点对象
	 */
	public Graphic drawPoint(GeoPoint pt1) {
		// 构建点
		Geometry pointGeometry = new Geometry();
		// 设置坐标
		pointGeometry.setPoint(pt1, 8);
		// 设定样式
		Symbol pointSymbol = new Symbol();
		Symbol.Color pointColor = pointSymbol.new Color();
		pointColor.red = 0;
		pointColor.green = 126;
		pointColor.blue = 255;
		pointColor.alpha = 255;
		pointSymbol.setPointSymbol(pointColor);
		// 生成Graphic对象
		Graphic pointGraphic = new Graphic(pointGeometry, pointSymbol);
		return pointGraphic;
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

}
