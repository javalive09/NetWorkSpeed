package com.example.networkspeed;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity{

	ProgressBar pb = null;
	TextView bt = null;
	AsyncTask<Void, Integer, String> mAsynTask = null;
	private static final String TEST = "点击测试";
	private static final String TESTING = "测试中";
	private static final String PREFIX = "测试\n";
	private static final String SUFFIX = "Mb/s";
	private static final String[] URLS = {
			"www.baidu.com",
			"www.youku.com",
			"www.qq.com"};
	private boolean cancelTest = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_networkspeed);
		pb = (ProgressBar) findViewById(R.id.pb);
		bt = (TextView) findViewById(R.id.start);
		if(bt != null) {
			bt.setText(TEST);
		}
	}

	public void start(View button) {

		mAsynTask = new AsyncTask<Void, Integer, String>() {

			@Override
			protected String doInBackground(Void... params) {
				return testUrlSpeed(URLS);
			}

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				pb.setVisibility(View.VISIBLE);
				bt.setText(TESTING);
				bt.setClickable(false);
			}

			@Override
			protected void onPostExecute(String speed) {
				super.onPostExecute(speed);
				pb.setVisibility(View.INVISIBLE);
				speed = PREFIX + speed + SUFFIX;
				bt.setText(speed);
				bt.setClickable(true);
			}

		}.execute();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		cancelTest();
	}

	private void cancelTest() {
		cancelTest = true;
		if(mAsynTask != null) {
			mAsynTask.cancel(true);
		}
	}

	private String testUrlSpeed(String urls[]) {
		Float sumSpeed = 0f;
		Float averageSpeed;
		Float speed = 0f;
		for(String url: urls) {
			ArrayList<String> commands = new ArrayList<>();
			commands.add("ping");
			commands.add("-c");
			commands.add("5");
			commands.add("-s");
			commands.add("1000");
			commands.add("-i");
			commands.add("2");
			commands.add(url);

			ProcessBuilder pb = new ProcessBuilder(commands);
			try {
				if(cancelTest) {
					return "";
				}
				Process process = pb.start();
				InputStream in = process.getInputStream();
				BufferedReader br = new BufferedReader(new InputStreamReader(in));
				String s;
				ArrayList<Float> times = new ArrayList<>();
				while((s = br.readLine())!= null) {
					Log.i("peter", "cmd srt =" + s);
					Float time = matcher(s);
					if(time != null) {
						times.add(time);
					}
					Log.i("peter", "cmd time =" + time);
				}
				Float AllTimes = 0f;
				for(Float time : times) {
					AllTimes += time;
				}

				Float averageTime = AllTimes/times.size();

				speed = 1000 / averageTime / 8 ;

			} catch (IOException e) {
				e.printStackTrace();
			}
			sumSpeed += speed;
		}
		averageSpeed = sumSpeed/urls.length;

		return getSpeedString(averageSpeed * 8);

	}

	public String getSpeedString(Float speed) {
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);
		df.setMinimumFractionDigits(2);
		return df.format(speed);
	}

	private Float matcher(String str) {
		String regx = "time=.*ms";
		Pattern pattern = Pattern.compile(regx);
		Matcher matcher = pattern.matcher(str);
		if(matcher.find()) {
			String mat = matcher.group();
			int start = mat.indexOf("=") + 1;
			int end = mat.indexOf(" ");
			String time = mat.substring(start, end);
			return Float.valueOf(time);
		}
		return null;
	}
        

}
