package com.example.s3bucket_upload_and_download;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferNetworkLossHandler;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;

import java.io.File;

public class MainActivity extends AppCompatActivity {

//    String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/img"; // 외부 저장소 경로 → 오류 발생
    String path = "/data/data/com.example.s3bucket_upload_and_download/img"; // 내부 저장소 경로
//    File file = new File(path);
    File file = new File(path + "/FennecFox.jpg");

    File downloadFile = new File(path + "/FennecFox2.jpg");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("외부 저장소 경로 : ", Environment.getExternalStorageDirectory().getAbsolutePath());
//                uploadWithTransferUtilty(file.getName(), file);
                uploadWithTransferUtilty("FennecFox", file);
            }
        });

        Button button2 = (Button) findViewById(R.id.button2);
        button2.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Button : ", "S3 Bucket Download Button Click!");
                downloadWithTransferUtilty("FennecFox2.jpg", downloadFile);
            }
        });
    }

    public void uploadWithTransferUtilty(String fileName, File file) {

        AWSCredentials awsCredentials = new BasicAWSCredentials("access_Key", "secret_Key");    // IAM User의 (accessKey, secretKey)
        AmazonS3Client s3Client = new AmazonS3Client(awsCredentials, Region.getRegion(Regions.US_EAST_1));

        TransferUtility transferUtility = TransferUtility.builder().s3Client(s3Client).context(this.getApplicationContext()).build();
        TransferNetworkLossHandler.getInstance(this.getApplicationContext());

        TransferObserver uploadObserver = transferUtility.upload("bucket_Name", fileName, file);    // (bucket name, file 이름, file 객체)

        uploadObserver.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                if (state == TransferState.COMPLETED) {
                    // Handle a completed upload
                    Log.d("S3 Bucket ", "Upload Completed!");
                }
            }

            @Override
            public void onProgressChanged(int id, long current, long total) {
                int done = (int) (((double) current / total) * 100.0);
                Log.d("MYTAG", "UPLOAD - - ID: $id, percent done = $done");
            }

            @Override
            public void onError(int id, Exception ex) {
                Log.d("MYTAG", "UPLOAD ERROR - - ID: $id - - EX:" + ex.toString());
            }
        });
    }

    public void downloadWithTransferUtilty(String fileName, File file) {
        AWSCredentials awsCredentials = new BasicAWSCredentials("access_Key", "secret_Key");    // IAM User의 (accessKey, secretKey)
        AmazonS3Client s3Client = new AmazonS3Client(awsCredentials, Region.getRegion(Regions.US_EAST_1));

        TransferUtility transferUtility = TransferUtility.builder().s3Client(s3Client).context(this.getApplicationContext()).build();
        TransferNetworkLossHandler.getInstance(this.getApplicationContext());

//        TransferObserver downloadObserver = transferUtility.download("bucket_Name", fileName, file);    // (bucket name, file 이름, file 객체)
        TransferObserver downloadObserver = transferUtility.download("bucket_Name/folder_Name", fileName, file);    // (bucket name/folder name, file 이름, file 객체)    // Bucket 안의 Folder에 있는 파일을 다운로드

        downloadObserver.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                if (state == TransferState.COMPLETED) {
                    // Handle a completed download
                    Log.d("S3 Bucket ", "Download Completed!");
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                int done = (int) (((double) bytesCurrent / bytesTotal) * 100.0);
                Log.d("MYTAG", "DOWNLOAD - - ID: " + id + " percent done = " + done);
            }

            @Override
            public void onError(int id, Exception ex) {
                Log.d("MYTAG", "DOWNLOAD ERROR - - ID: " + id + " - - EX: " + ex.toString());
            }
        });
    }
}