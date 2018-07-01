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
    private File mCascade1File;
    private File mCascade2File;
    private CascadeClassifier mJavaDetector1;
    private CascadeClassifier mJavaDetector2;
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
                    InputStream is = context.getResources().openRawResource(R.raw.cascade1);
                    File cascade1Dir = context.getDir("cascade1", Context.MODE_PRIVATE);
                    mCascade1File = new File(cascade1Dir, "cascade1.xml");
                    FileOutputStream os = new FileOutputStream(mCascade1File);

                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        os.write(buffer, 0, bytesRead);
                    }
                    is.close();
                    os.close();

                    // load cascade file from application resources
                    InputStream ise = context.getResources().openRawResource(R.raw.cascade2);
                    File cascade2Dir = context.getDir("cascade2", Context.MODE_PRIVATE);
                    mCascade2File = new File(cascade2Dir, "cascade2.xml");
                    FileOutputStream ose = new FileOutputStream(mCascade2File);

                    while ((bytesRead = ise.read(buffer)) != -1) {
                        ose.write(buffer, 0, bytesRead);
                    }

                    ise.close();
                    ose.close();

                    mJavaDetector1 = new CascadeClassifier(mCascade1File.getAbsolutePath());
                    if (mJavaDetector1.empty()) {
                        Log.e(TAG, "Failed to load cascade classifier");
                        mJavaDetector1 = null;
                    } else
                        Log.i(TAG, "Loaded cascade classifier from " + mCascade1File.getAbsolutePath());

                    mJavaDetector2 = new CascadeClassifier(mCascade2File.getAbsolutePath());
                    if (mJavaDetector2.empty()) {
                        Log.e(TAG, "Failed to load cascade classifier");
                        mJavaDetector2 = null;
                    } else {
                        Log.i(TAG, "Loaded cascade classifier from " + mCascade2File.getAbsolutePath());
                    }

                    cascade1Dir.delete();
                    cascade2Dir.delete();
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

    public class Picture_Detector1 implements Runnable{
        private Bitmap bitmap;
        private Handler UIHandler;
        private Rect[] result;

        public void begin() {
            new Thread(this).start();
        }
        public Picture_Detector1(Bitmap bitmap,Handler UIHandler){
            this.bitmap = bitmap;
            this.UIHandler = UIHandler;
        }

        @Override
        public void run() {
            Mat srcimg = new Mat();
            Utils.bitmapToMat(bitmap, srcimg);
            Imgproc.cvtColor(srcimg, srcimg,Imgproc.COLOR_RGBA2GRAY);
            result = detector1(srcimg);
            Message msg = new Message();
            Bundle b = new Bundle();// 存放数据
            b.putSerializable("picturedetector1", result);
            msg.setData(b);
            UIHandler.sendMessage(msg);
        }
    }

    public Rect[] detector1(Mat srcimg) {
        if (mAbsoluteTargetSize == 0) {
            int height = srcimg.rows();
            if (Math.round(height * mRelativeTargetSize) > 0) {
                mAbsoluteTargetSize = Math.round(height * mRelativeTargetSize);
            }
        }
        MatOfRect targets = new MatOfRect();
        if (mDetectorType == JAVA_DETECTOR) {
            if (mJavaDetector1 != null)
                mJavaDetector1.detectMultiScale(srcimg, targets, 1.1, 6, 2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
                        new Size(mAbsoluteTargetSize, mAbsoluteTargetSize), new Size());
        }
        else {
            Log.e(TAG, "Detection method is not selected!");
        }
        return targets.toArray();
    }

    public class Picture_Detector2 implements Runnable{
        private Bitmap bitmap;
        private Handler UIHandler;
        private Rect[] result;

        public void begin() {
            new Thread(this).start();
        }
        public Picture_Detector2(Bitmap bitmap,Handler UIHandler){
            this.bitmap = bitmap;
            this.UIHandler = UIHandler;
        }

        @Override
        public void run() {
            Mat srcimg = new Mat();
            Utils.bitmapToMat(bitmap, srcimg);
            Imgproc.cvtColor(srcimg, srcimg,Imgproc.COLOR_RGBA2GRAY);
            result = detector2(srcimg);
            Message msg = new Message();
            Bundle b = new Bundle();// 存放数据
            b.putSerializable("picturedetector2", result);
            msg.setData(b);
            UIHandler.sendMessage(msg);
        }
    }

    public Rect[] detector2(Mat srcimg) {
        if (mAbsoluteTargetSize == 0) {
            int height = srcimg.rows();
            if (Math.round(height * mRelativeTargetSize) > 0) {
                mAbsoluteTargetSize = Math.round(height * mRelativeTargetSize);
            }
        }
        MatOfRect targets = new MatOfRect();
        if (mDetectorType == JAVA_DETECTOR) {
            if (mJavaDetector2 != null)
                mJavaDetector2.detectMultiScale(srcimg, targets, 1.1, 3, 2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
                        new Size(mAbsoluteTargetSize, mAbsoluteTargetSize), new Size());
        }
        else {
            Log.e(TAG, "Detection method is not selected!");
        }
        return targets.toArray();
    }
}
