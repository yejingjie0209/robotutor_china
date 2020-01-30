package cmu.xprize.robotutor.download;

/**
 * Description：
 * Created by kang on 2018/3/9.
 */

public interface DownloadListener {
    void onStart();

    void onProgress(int currentLength);

    void onFinish(String localPath);

    void onFailure(int code, String msg);
}