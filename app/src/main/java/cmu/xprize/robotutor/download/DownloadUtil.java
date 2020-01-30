package cmu.xprize.robotutor.download;

import android.os.Environment;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Description：下载文件工具类
 * Created by kang on 2018/3/9.
 */

public class DownloadUtil {
    private static final String TAG = "DownloadUtil";
    private static final String PATH_CHALLENGE_VIDEO = Environment.getExternalStorageDirectory() + "/Download";
    //视频下载相关
    protected ApiInterface mApi;
    private Call<ResponseBody> mCall;
    private File mFile;
    private Thread mThread;
    private String mVideoPath;

    public DownloadUtil() {
        if (mApi == null) {
            //初始化网络请求接口
            mApi = ApiHelper.getInstance().buildRetrofit("http://ke.robotutor.cn/")
                    .createService(ApiInterface.class);
        }
    }

    public void downloadFile(String url, final DownloadListener downloadListener) {
        String name = url;
        //通过Url得到文件并创建本地文件
        if (FileUtils.createOrExistsDir(PATH_CHALLENGE_VIDEO)) {
            int i = name.lastIndexOf('/');//一定是找最后一个'/'出现的位置
            if (i != -1) {
                name = name.substring(i);
                mVideoPath = PATH_CHALLENGE_VIDEO +
                        name;
            }
        }
        if (TextUtils.isEmpty(mVideoPath)) {
            Log.e(TAG, "downloadVideo: 存储路径为空了");
            return;
        }
        //建立一个文件
        mFile = new File(mVideoPath);
        if (!FileUtils.isFileExists(mFile) && FileUtils.createOrExistsFile(mFile) || mFile.length() == 0) {
            if (mFile.exists()) {
                mFile.delete();
            }
            if (mApi == null) {
                Log.e(TAG, "downloadVideo: 下载接口为空了");
                return;
            }
            mCall = mApi.downloadFile(url);
            mCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull final Response<ResponseBody> response) {
                    if (response.code() != 200) {
                        downloadListener.onFailure(response.code(), response.message()); //下载失败
                    } else {
                        //下载文件放在子线程
                        mThread = new Thread() {
                            @Override
                            public void run() {
                                super.run();
                                //保存到本地
                                File tempFile = new File(mVideoPath + ".temp");
                                if (tempFile.exists()) {
                                    tempFile.delete();
                                }
                                writeFile2Disk(response, tempFile, downloadListener);
                            }
                        };
                        mThread.start();
                    }

                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    downloadListener.onFailure(0, t.getMessage()); //下载失败
                }
            });
        } else {
            downloadListener.onFinish(mVideoPath); //下载完成
        }
    }

    //将下载的文件写入本地存储
    private void writeFile2Disk(Response<ResponseBody> response, File file, DownloadListener downloadListener) {
        downloadListener.onStart();
        long currentLength = 0;
        OutputStream os = null;

        InputStream is = response.body().byteStream(); //获取下载输入流
        long totalLength = response.body().contentLength();

        try {
            os = new FileOutputStream(file); //输出流
            int len;
            byte[] buff = new byte[1024];
            while ((len = is.read(buff)) != -1) {
                os.write(buff, 0, len);
                currentLength += len;
                Log.e(TAG, "当前进度: " + currentLength);
                //计算当前下载百分比，并经由回调传出
                downloadListener.onProgress((int) (100 * currentLength / totalLength));
                //当百分比为100时下载结束，调用结束回调，并传出下载后的本地路径
                if ((int) (100 * currentLength / totalLength) == 100) {
                    //重命名
                    FileUtils.rename(file.getAbsolutePath(), mVideoPath);

                    File resultFile = new File(mVideoPath);
                    if (resultFile.exists() && resultFile.length() > 0) {
                        downloadListener.onFinish(mVideoPath); //下载完成
                    } else {
                        downloadListener.onFailure(0, "文件为空");
                    }

                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (os != null) {
                try {
                    os.close(); //关闭输出流
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (is != null) {
                try {
                    is.close(); //关闭输入流
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}