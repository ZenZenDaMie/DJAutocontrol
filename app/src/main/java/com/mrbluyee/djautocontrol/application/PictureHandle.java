package com.mrbluyee.djautocontrol.application;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

public class PictureHandle extends BaseLoaderCallback {
    private boolean isInit = false;
    private Context context;
    public PictureHandle (Context context){
        super(context);
        this.context = context;
    }
    @Override
    public void onManagerConnected(int status) {
        switch (status) {
            case LoaderCallbackInterface.SUCCESS:
                isInit = true;
                break;
            default:
                isInit = false;
                super.onManagerConnected(status);
                break;
        }
    }

    //match_method = Imgproc.TM_SQDIFF 平方差匹配法：该方法采用平方差来进行匹配；最好的匹配值为0；匹配越差，匹配值越大。
    //match_method = Imgproc.TM_CCORR 相关匹配法：该方法采用乘法操作；数值越大表明匹配程度越好。
    //match_method = Imgproc.TM_CCOEFF 相关系数匹配法：1表示完美的匹配；-1表示最差的匹配。
    //match_method = Imgproc.TM_SQDIFF_NORMED 归一化平方差匹配法。
    //match_method = Imgproc.TM_CCORR_NORMED 归一化相关匹配法。
    //match_method = Imgproc.TM_CCOEFF_NORMED 归一化相关系数匹配法。
    public void match(Bitmap srcmap, Bitmap matchmap, int match_method) {
        Mat srcimg = new Mat();
        Utils.bitmapToMat(srcmap, srcimg);
        Imgproc.cvtColor(srcimg, srcimg,Imgproc.COLOR_RGBA2GRAY );
        Mat matchimg = new Mat();
        Utils.bitmapToMat(matchmap, matchimg);

        // / Create the result matrix
        int result_cols =  srcimg .cols() - matchimg.cols() + 1;
        int result_rows =  srcimg .rows() - matchimg.rows() + 1;
        Mat result = new Mat(result_rows, result_cols, CvType.CV_32FC1);

        // / Do the Matching and Normalize
        Imgproc.matchTemplate(srcimg ,matchimg,result,match_method);
        Core.normalize(result,result,0,1,Core.NORM_MINMAX,-1,new Mat());

        // / Localizing the best match with minMaxLoc
        Core.MinMaxLocResult mmr = Core.minMaxLoc(result);

        Point matchLoc;
        if(match_method ==Imgproc.TM_SQDIFF ||match_method ==Imgproc.TM_SQDIFF_NORMED) {
            matchLoc = mmr.minLoc;
        } else {
            matchLoc = mmr.maxLoc;
        }
        // / Show me what you got
        Imgproc.rectangle(srcimg,matchLoc,new Point(matchLoc.x + matchimg.cols(), matchLoc.y + matchimg.rows()),new Scalar(0,255,0));

        try {
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "station_camera.jpg");
            file.createNewFile();
            FileOutputStream os = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(os);

            Utils.matToBitmap(srcimg,srcmap);
            srcmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);

            bos.flush();
            bos.close();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
