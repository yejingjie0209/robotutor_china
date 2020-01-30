package cmu.xprize.robotutor;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import cmu.xprize.robotutor.download.DownloadListener;
import cmu.xprize.robotutor.download.DownloadUtil;

public class MyRobotutor extends AppCompatActivity {
    private String[] list =
            {
                    "config.json"
                    , "English_StoriesAudio.1.1.0.zip"
                    , "English_TutorAudio.1.1.0.zip"
                    , "StoryWords.drive.google.txt"
                    , "English_StoryWords.1.1.0.zip"
            };
    private String TAG = "jason";

    private TextView tvName;
    private ProgressBar progressBar;
    private int currIndex = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_robotutor_layout);
        tvName = findViewById(R.id.tv_name);
        progressBar = findViewById(R.id.progress_bar);
        downloadPicture(currIndex);
    }

    private void downloadPicture(int index) {
        DownloadUtil mDownloadUtil = new DownloadUtil();
        final String name = list[index];
        mDownloadUtil.downloadFile("android/" + list[index], new DownloadListener() {
            @Override
            public void onStart() {
                Log.e(TAG, "onStart: ");
                runOnUiThread(new Runnable() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void run() {
                        tvName.setText("正在下载数据包:" + name);

                        progressBar.setProgress(0);
//                        fl_circle_progress.setVisibility(View.VISIBLE);
                    }
                });
            }

            @Override
            public void onProgress(final int currentLength) {
                Log.e(TAG, "onLoading: " + currentLength);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setProgress(currentLength);
                    }
                });

            }

            @Override
            public void onFinish(final String localPath) {
                Log.e(TAG, "onFinish: " + localPath);
                if (currIndex < list.length - 1) {
                    downloadPicture(++currIndex);
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Intent it = new Intent(MyRobotutor.this, RoboTutor.class);
                            startActivity(it);
                        }
                    });
                    finish();
                }

            }

            @Override
            public void onFailure(int code, String msg) {
                Log.e(TAG, "onFailure: code=" + code + "msg=" + msg);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        fl_circle_progress.setVisibility(View.GONE);
                    }
                });
                Toast.makeText(MyRobotutor.this, "onFailure: code=" + code + "msg=" + msg, Toast.LENGTH_SHORT).show();
                MyRobotutor.this.finish();
            }
        });
    }
}
