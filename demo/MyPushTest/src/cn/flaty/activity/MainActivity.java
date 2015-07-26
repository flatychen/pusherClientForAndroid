package cn.flaty.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import cn.flaty.PushTest.R;
import cn.flaty.push.PushBootStrap;

public class MainActivity extends Activity {

	protected static final String TAG = MainActivity.class.getName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		
		
		this.initCustom();
		this.initServices();
		this.initViews();
		this.addViewsListeners();

	}

	private void initCustom() {

		// 启动push
		PushBootStrap.getInstance().start(this);

	}

	private void initServices() {

	}

	private void addViewsListeners() {
	}

	private void initViews() {
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onPause() {
		Log.i(this.getClass().toString(), "onPause");
		super.onPause();
	}

	@Override
	protected void onStart() {
		Log.i(this.getClass().toString(), "onStart");
		super.onStart();
	}

	@Override
	protected void onDestroy() {
		Log.i(this.getClass().toString(), "onDestroy");
		super.onDestroy();
	}

}
