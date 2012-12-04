package com.memory.clear;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	TextView processView;
	private boolean re = true;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		processView = (TextView) findViewById(R.id.process);
		showPro();

		TextView totalMemView = (TextView) findViewById(R.id.total_mem);
		TextView currentMem = (TextView) findViewById(R.id.current_mem);
		totalMemView.setText("" + getTotalMemory(this));
		currentMem.setText("" + getAvailMemory(this));
	}

	private long getAvailMemory(Context context) {
		// 获取android当前可用内存大小
		ActivityManager am = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		MemoryInfo mi = new MemoryInfo();
		am.getMemoryInfo(mi);
		// mi.availMem; 当前系统的可用内存
		// return Formatter.formatFileSize(context, mi.availMem);// 将获取的内存大小规格化
		return mi.availMem / (1024 * 1024);
	}

	private long getTotalMemory(Context context) {
		String str1 = "/proc/meminfo";// 系统内存信息文件
		String str2;
		String[] arrayOfString;
		long initial_memory = 0;

		try {
			FileReader localFileReader = new FileReader(str1);
			BufferedReader localBufferedReader = new BufferedReader(
					localFileReader, 8192);
			str2 = localBufferedReader.readLine();// 读取meminfo第一行，系统总内存大小

			arrayOfString = str2.split("\\s+");

			initial_memory = Integer.valueOf(arrayOfString[1]).intValue() * 1024;// 获得系统总内存，单位是KB，乘以1024转换为Byte
			localBufferedReader.close();

		} catch (IOException e) {
		}
		// return Formatter.formatFileSize(context, initial_memory);//
		// Byte转换为KB或者MB，内存大小规格化
		return initial_memory / (1024 * 1024);
	}

	public void clear(View v) {
		ActivityManager activityManger = (ActivityManager) this
				.getSystemService(ACTIVITY_SERVICE);
		List<ActivityManager.RunningAppProcessInfo> list = activityManger
				.getRunningAppProcesses();
		if (list != null)
			for (int i = 0; i < list.size(); i++) {
				ActivityManager.RunningAppProcessInfo apinfo = list.get(i);

				String[] pkgList = apinfo.pkgList;
				if (apinfo.importance > ActivityManager.RunningAppProcessInfo.IMPORTANCE_SERVICE) {
					// Process.killProcess(apinfo.pid);
					for (int j = 0; j < pkgList.length; j++) {
						// 2.2以上是过时的,请用killBackgroundProcesses代替
						activityManger.killBackgroundProcesses(pkgList[j]);
						// activityManger.restartPackage(pkgList[j]);
					}
				}
			}
		onCreate(null);
		new Handler().postDelayed(new Runnable() {
			public void run() {
				Toast.makeText(MainActivity.this, getString(R.string.quit),
						Toast.LENGTH_SHORT).show();
				System.exit(0);
			}
		}, 5000);
	}

	private void showPro() {
		final ActivityManager activityManger = (ActivityManager) this
				.getSystemService(ACTIVITY_SERVICE);
		new AsyncTask<Void, Void, String>() {
			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				processView.setText(getString(R.string.wait));
			}

			@Override
			protected String doInBackground(Void... params) {
				List<ActivityManager.RunningAppProcessInfo> list = activityManger
						.getRunningAppProcesses();
				StringBuilder sb = new StringBuilder("");
				if (list != null) {
					for (int i = 0; i < list.size(); i++) {
						ActivityManager.RunningAppProcessInfo apinfo = list
								.get(i);
						sb.append(i + 1 + "==pid:" + apinfo.pid
								+ "#processName:" + apinfo.processName
								+ "#importance:" + apinfo.importance
								+ "\n--------------------------------\n");
					}
				}
				TextView titleTextView = (TextView) findViewById(R.id.title);
				titleTextView.setText(titleTextView.getText() + " "
						+ list.size());
				sb.append(getString(R.string.xiaodong));
				return sb.toString();
			}

			@Override
			protected void onPostExecute(String result) {
				super.onPostExecute(result);
				processView.setText(result);
				if (re) {
					re = false;
					Toast.makeText(MainActivity.this, getString(R.string.des1),
							Toast.LENGTH_LONG).show();
					new Handler().postDelayed(new Runnable() {
						public void run() {
							clear(null);
						}
					}, 2000);
				}
			}
		}.execute();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		System.exit(0);
	}
}
