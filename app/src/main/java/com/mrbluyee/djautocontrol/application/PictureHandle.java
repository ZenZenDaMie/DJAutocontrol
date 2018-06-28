package com.mrbluyee.djautocontrol.application;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.mrbluyee.djautocontrol.R;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class PictureHandle extends BaseLoaderCallback {
    private static final String TAG = "picture handle";
    public static final int JAVA_DETECTOR = 0;
    private boolean isInit = false;
    private Context context;
    private File mCascadeFile;
    private CascadeClassifier mJavaDetector;
    private int  mDetectorType = JAVA_DETECTOR;
    public float mRelativeTargetSize   = 0.2f;
    public int mAbsoluteTargetSize = 0;
    public PictureHandle (Context context){
        super(context);
        this.context = context;
    }
    @Override
    public void onManagerConnected(int status) {
        switch (status) {
            case LoaderCallbackInterface.SUCCESS:
                isInit = true;
                try {
                    // load cascade file from application resources
                    InputStream is = context.getResources().openRawResource(R.raw.cascade);
                    File cascadeDir = context.getDir("cascade", Context.MODE_PRIVATE);
                    mCascadeFile = new File(cascadeDir, "cascade.xml");
                    FileOutputStream os = new FileOutputStream(mCascadeFile);

                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        os.write(buffer, 0, bytesRead);
                    }
                    is.close();
                    os.close();

                    mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
                    if (mJavaDetector.empty()) {
                        Log.e(TAG, "Failed to load cascade classifier");
                        mJavaDetector = null;
                    } else
                        Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());
                    cascadeDir.delete();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
                }
                break;
            default:
                isInit = false;
                super.onManagerConnected(status);
                break;
        }
    }

    public class Picture_Match implements Runnable{
        private Bitmap bitmap;
        private Handler UIHandler;
        private Rect[] result;

        public void begin() {
            new Thread(this).start();
        }
        public Picture_Match(Bitmap bitmap,Handler UIHandler){
            this.bitmap = bitmap;
            this.UIHandler = UIHandler;
        }

        @Override
        public void run() {
            Mat srcimg = new Mat();
            Utils.bitmapToMat(bitmap, srcimg);
            Imgproc.cvtColor(srcimg, srcimg,Imgproc.COLOR_RGBA2GRAY);
            result = match(srcimg);
            Message msg = new Message();
            Bundle b = new Bundle();// 存放数据
            b.putSerializable("picturehandle", result);
            msg.setData(b);
            UIHandler.sendMessage(msg);
        }
    }

    public Rect[] match(Mat srcimg) {
        if (mAbsoluteTargetSize == 0) {
            int height = srcimg.rows();
            if (Math.round(height * mRelativeTargetSize) > 0) {
                mAbsoluteTargetSize = Math.round(height * mRelativeTargetSize);
            }
        }
        MatOfRect targets = new MatOfRect();
        if (mDetectorType == JAVA_DETECTOR) {
            if (mJavaDetector != null)
                mJavaDetector.detectMultiScale(srcimg, targets, 1.1, 8, 2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
                        new Size(mAbsoluteTargetSize, mAbsoluteTargetSize), new Size());
        }
        else {
            Log.e(TAG, "Detection method is not selected!");
        }
        return targets.toArray();
    }
}
