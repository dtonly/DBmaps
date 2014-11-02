package com.dt.dbmap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MyMap extends Activity {
	ImageView leftBack = null;
	TextView info = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mymap);
		leftBack = (ImageView) findViewById(R.id.iv_topbar_left_back);
		info = (TextView) findViewById(R.id.user_info_name);
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		String str = bundle.getString("textview");// getString()返回指定key的值
		info.setText(str);
		leftBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				MyMap.this.finish();
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
		super.onDestroy();
	}
}
