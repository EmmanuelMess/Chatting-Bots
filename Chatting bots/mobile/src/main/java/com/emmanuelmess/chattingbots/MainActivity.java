package com.emmanuelmess.chattingbots;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.code.chatterbotapi.ChatterBot;
import com.google.code.chatterbotapi.ChatterBotFactory;
import com.google.code.chatterbotapi.ChatterBotSession;
import com.google.code.chatterbotapi.ChatterBotType;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

	private static final String SET_PROGRESSBAR_VISIBLE = "v",
			SET_PROGRESSBAR_GONE = "g";

	private static final int TEXT_HEIGHT = 25;
	private static final int SCROLL_ERROR_MARGIN = TEXT_HEIGHT*6;

	private ChatterBotSession bot1session, bot2session;
	private ScrollView s;
	private TextView bots;
	private ProgressBar progressBar;
	private ChatterBotFactory factory = null;
	private boolean stopped = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		s = (ScrollView) findViewById(R.id.scrollView);
		bots = (TextView) findViewById(R.id.bots);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);

		final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
		fab.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN)
					s.getViewTreeObserver().removeOnGlobalLayoutListener(this);
				else//noinspection deprecation
					s.getViewTreeObserver().removeGlobalOnLayoutListener(this);

				findViewById(R.id.space).setMinimumHeight(fab.getHeight() + fab.getPaddingTop());
			}
		});
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				stopped = !stopped;
				if (!stopped) {
					fab.setImageResource(R.drawable.ic_stop_black_24dp);
					bots.setText("");

					(new AsyncTask<Void, String, Void>() {
						private int n = 0;
						@Override
						protected Void doInBackground(Void... params) {
							String s = "Hi";

							onProgressUpdate("", SET_PROGRESSBAR_VISIBLE);

							if(factory == null)
								factory = new ChatterBotFactory();

							try {
								ChatterBot bot1 = factory.create(ChatterBotType.CLEVERBOT);
								bot1session = bot1.createSession();

								ChatterBot bot2 = factory.create(ChatterBotType.PANDORABOTS, "b0dafd24ee35a477");
								bot2session = bot2.createSession();
							} catch (Exception e) {
								e.printStackTrace();
							}

							onProgressUpdate("", SET_PROGRESSBAR_GONE);

							while (!stopped) {
								try {
									onProgressUpdate("bot1> " + s + "\n");
									s = bot2session.think(s);
									onProgressUpdate("bot2> " + s + "\n");
									s = bot1session.think(s);
								} catch (Exception e) {
									e.printStackTrace();
								}
							}

							return null;
						}

						@Override
						protected void onProgressUpdate(final String... values) {
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									if(values.length == 2) {
										if(equal(values[1], SET_PROGRESSBAR_VISIBLE))
											progressBar.setVisibility(View.VISIBLE);
										else if(equal(values[1], SET_PROGRESSBAR_GONE))
											progressBar.setVisibility(View.GONE);
									}else {
										bots.append(Integer.toHexString(n++).toUpperCase() + " "
												+ values[0].replace("  ", " ").replace("\t", " "));
										if(s.getScrollY() + s.getHeight()
												>= bots.getHeight() - SCROLL_ERROR_MARGIN)
											s.fullScroll(ScrollView.FOCUS_DOWN);
									}
								}
							});
						}
					}).execute();
				} else
					fab.setImageResource(R.drawable.ic_play_arrow_black_24dp);

			}
		});
	}

	@Override
	public void onPause() {
		super.onPause();
		stopped = true;
	}

	public static boolean equal(Object o1, Object o2) {
		return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Objects.equals(o1, o2)) || o1.equals(o2);
	}
}
