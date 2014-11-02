package com.dt.dbmap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.map.MKMapViewListener;
import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapView;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class Tools extends Activity {
	ImageView leftBack = null;
	TextView measure = null;
	TextView script = null;
	TextView polygon = null;
	/**
	 * MapView 是地图主控件
	 */
	private MapView mMapView = null;
	/**
	 * MKMapViewListener 用于处理地图事件回调
	 */
	MKMapViewListener mMapListener = null;
	private BMapManager mBMapMan = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mBMapMan = new BMapManager(getApplication());
		mBMapMan.init("zwLfj1rEdfrp2stnbqFAtzWI", null);
		setContentView(R.layout.tools);
		leftBack = (ImageView) findViewById(R.id.iv_topbar_left_back);
		measure = (TextView) findViewById(R.id.measure);
		script = (TextView) findViewById(R.id.script);
		polygon = (TextView) findViewById(R.id.measurepolygon);
		mMapView = (MapView) findViewById(R.id.bmapView);
		mMapView.getController().setZoom(12.5f);
		mMapListener = new MKMapViewListener() {

			@Override
			public void onClickMapPoi(MapPoi arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onGetCurrentMap(Bitmap b) {
				// TODO Auto-generated method stub

				/**
				 * 当调用过 mMapView.getCurrentMap()后，此回调会被触发 可在此保存截图至存储设备
				 * 
				 */
				if (!Environment.MEDIA_MOUNTED.equals(Environment
						.getExternalStorageState())) {
					Toast.makeText(Tools.this, "请插入SD卡", Toast.LENGTH_SHORT)
							.show();
					return;
				}

				File file = new File(Environment.getExternalStorageDirectory(),
						System.currentTimeMillis() + ".png");
				FileOutputStream out;
				try {

					out = new FileOutputStream(file);
					if (b.compress(Bitmap.CompressFormat.PNG, 70, out)) {
						out.flush();
						out.close();
					}
					Toast.makeText(Tools.this,
							"屏幕截图成功，图片存在: " + file.toString(),
							Toast.LENGTH_SHORT).show();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onMapAnimationFinish() {
				// TODO Auto-generated method stub

			}

			@Override
			public void onMapLoadFinish() {
				// TODO Auto-generated method stub

			}

			@Override
			public void onMapMoveFinish() {
				// TODO Auto-generated method stub

			}
		};
		mMapView.regMapViewListener(mBMapMan, mMapListener);
		leftBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Tools.this.finish();
			}
		});
		measure.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.setClass(Tools.this, GeometryLine.class);
				startActivity(intent);
			}
		});
		polygon.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.setClass(Tools.this, GeometryPolygon.class);
				startActivity(intent);
			}
		});
		script.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mMapView.getCurrentMap();
				Toast.makeText(Tools.this, "正在截取屏幕图片...", Toast.LENGTH_SHORT)
						.show();
				Log.e("error", "tantu");
			}
		});
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
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
