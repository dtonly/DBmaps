package com.dt.dbmap;

import java.lang.reflect.Field;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class NearBy extends Activity {

	ImageView leftBack = null;
	Button[] button = new Button[18];
	Double Latitude;
	Double Longitude;
	int i = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.nearby);
		leftBack = (ImageView) findViewById(R.id.iv_topbar_left_back);
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
	    Latitude = bundle.getDouble("Latitude");// getString()返回指定key的值
		Longitude = bundle.getDouble("Longitude");
		OnClickListener clicklistener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				searchButtonProcess(v);
			}
		};
		for (i = 0; i < button.length; i++) {
			Button btn = null;
			button[i] = (Button) findViewById(getRes("scenebutton" + i));
			btn = button[i];
			button[i].setOnClickListener(clicklistener);
		}

		leftBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				NearBy.this.finish();
			}
		});
	}

	public void searchButtonProcess(View v) {
		String city = "北京";
		Button btn = (Button) v;
		String btntext = btn.getText().toString();
		Toast.makeText(NearBy.this, "" + btn.getText().toString(),
				Toast.LENGTH_SHORT).show();
		Intent intent = new Intent();
		intent.setClass(NearBy.this, Result.class);
		intent.putExtra("btnText", btntext);
		intent.putExtra("Latitude", Latitude);
		intent.putExtra("Longitude",Longitude);
		startActivity(intent);
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
		super.onDestroy();
	}

	/**
	 * 获取图片名称获取图片的资源id的方法
	 * 
	 * @param name
	 * @return
	 */

	public int getRes(String name) {
		ApplicationInfo appInfo = getApplicationInfo();
		int resID = getResources().getIdentifier(name, "id", "com.dt.dbmap");
		return resID;
	}
}
