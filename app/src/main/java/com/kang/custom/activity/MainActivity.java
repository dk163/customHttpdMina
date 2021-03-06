package com.kang.custom.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;

import com.communication.server.constant.Constant;
import com.communication.server.handler.ClientConnector;
import com.communication.server.httpd.NanoHTTPd;
import com.kang.custom.util.LogUtils;
import com.kang.custom.service.MinaClient;
import com.kang.custom.util.PermissionUtil;
import com.kang.customhttpdmina.R;


public class MainActivity extends AppCompatActivity{
    private final String TAG = "customLog";

    private final int TOAST_START_HTTPD = 0;
    private final int TOAST_STOP_HTTPD = 1;
    private final int TOAST_ERROR = 2;
    private final int TOAST_STOP_CLIENT = 3;
    private final int TOAST_START_HTTPD_CLIENT = 4;
    private final static int STOP_CLIENT = 5;
    private final int START_CLIENT_ALREADY = 6;

    private static NanoHTTPd na;
    private Context mContext;
    private static String tmp = "";

    private static MainHandler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        mHandler =  new MainHandler(Looper.getMainLooper());

        PermissionUtil.verifyStoragePermissions(this);

        if(na == null) {
            try {
                na = new NanoHTTPd(Constant.HTTPD_PORT);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mHandler.sendEmptyMessage(TOAST_START_HTTPD_CLIENT);
        }


        Button startClient = (Button)findViewById(R.id.startClient);
        startClient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(MinaClient.isClientInstance()){
                    mHandler.sendEmptyMessage(START_CLIENT_ALREADY);
                    return;
                }

                EditText edConIp = (EditText)findViewById(R.id.edConIp);
                tmp = edConIp.getText().toString();
                if(!(tmp.isEmpty()) && ((tmp.length()) != 0)){
                    Constant.setIP(tmp);//获取clent ip
                }

                Intent mIntent = new Intent(mContext, MinaClient.class);
                startService(mIntent);
            }
        });
        Button stopClient = (Button)findViewById(R.id.stopClient);
        stopClient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!MinaClient.isClientInstance()){
                    mHandler.sendEmptyMessage(TOAST_ERROR);
                }else{
                    Intent mIntent = new Intent(mContext, MinaClient.class);
                    stopService(mIntent);
                    mHandler.sendEmptyMessageDelayed(TOAST_STOP_CLIENT, 0);
                }
            }
        });


        Button startWebView = (Button)findViewById(R.id.startWebView);
        startWebView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText edHttpdUrl = (EditText) findViewById(R.id.edHttpdUrl);//httpd server url
                tmp = edHttpdUrl.getText().toString();
                if(!(tmp.isEmpty()) && (tmp.length() != 0)){
                    Constant.setHTTPIPPORT("http://"+tmp+":8080");
                    LogUtils.i(TAG, "httpd server ip: "+ ("http://"+tmp+":8080"));
                }

                Intent intent = new Intent();//download file to /mnt/sdcard/MyFavorite
                intent.setAction("android.intent.action.VIEW");
                Uri content_url = Uri.parse(Constant.HTTPDIPPORT);
                intent.setData(content_url);
                startActivity(intent);
//                mIntent = new Intent(mContext, WebViewActivity.class);
//                startActivity(mIntent);
                LogUtils.i(TAG, "open webview");
            }
        });

        Button startMtkLog = (Button)findViewById(R.id.startMtkLog);
        startMtkLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(null == ClientConnector.getClientAcceptorHandler()){
                    mHandler.sendEmptyMessageDelayed(TOAST_ERROR, 0);
                    return;
                }
                ClientConnector.getClientAcceptorHandler().sendEmptyMessage(Constant.MSG_START_MTKLOG);
                LogUtils.i(TAG, "start mtklog");
            }
        });
        Button stopMtkLog = (Button)findViewById(R.id.stopMtkLog);
        stopMtkLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(null == ClientConnector.getClientAcceptorHandler()){
                    mHandler.sendEmptyMessageDelayed(TOAST_ERROR, 0);
                    return;
                }
                ClientConnector.getClientAcceptorHandler().sendEmptyMessage(Constant.MSG_STOP_MTKLOG);
                LogUtils.i(TAG, "stop mtklog");
            }
        });

        Button clearCustomLog = (Button)findViewById(R.id.clearCustomLog);
        clearCustomLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(null == ClientConnector.getClientAcceptorHandler()){
                    mHandler.sendEmptyMessageDelayed(TOAST_ERROR, 0);
                    return;
                }
                ClientConnector.getClientAcceptorHandler().sendEmptyMessage(Constant.MSG_CLEAR_LOG);
                LogUtils.i(TAG, "clear NightVision log");
            }
        });

        //push file
        Button pushBtn = (Button) findViewById(R.id.pushButton);
        pushBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(null == ClientConnector.getClientAcceptorHandler()){
                    mHandler.sendEmptyMessageDelayed(TOAST_ERROR, 0);
                    return;
                }
                ClientConnector.getClientAcceptorHandler().sendEmptyMessage(Constant.MSG_PUSH_FILE);
                LogUtils.i(TAG, "push file to server");
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtils.d(TAG, "MainActivity onDestroy:");

        Intent mIntent = new Intent(mContext, MinaClient.class);
        stopService(mIntent);

        if(na != null) na.stop();
    }

    public static void stopClient(){
        mHandler.sendEmptyMessage(STOP_CLIENT);//broadcast stop mina client
    }

    private class MainHandler extends Handler {

        public MainHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case TOAST_START_HTTPD:
                    Toast.makeText(getApplicationContext(), "start httpd success", Toast.LENGTH_SHORT).show();
                    break;
                case TOAST_STOP_HTTPD:
                    Toast.makeText(getApplicationContext(), "stop httpd success", Toast.LENGTH_SHORT).show();
                    break;
                case TOAST_ERROR:
                    Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
                    break;
                case TOAST_STOP_CLIENT:
                    Toast.makeText(getApplicationContext(), "stop client success", Toast.LENGTH_SHORT).show();
                    break;
                case TOAST_START_HTTPD_CLIENT:
                    Toast.makeText(getApplicationContext(), "client start httpd success", Toast.LENGTH_SHORT).show();
                    break;
                case STOP_CLIENT:
                    Intent mIntent = new Intent(mContext, MinaClient.class);
                    stopService(mIntent);
                    Toast.makeText(getApplicationContext(), "wifi disConnect", Toast.LENGTH_SHORT).show();
                    break;
                case START_CLIENT_ALREADY:
                    Toast.makeText(getApplicationContext(), "client session already connect", Toast.LENGTH_SHORT).show();
                    break;

                default:
                    break;
            }
        }
    }


    //reserve for kang test
//    public static MainHandler getmHandler() {
//        return mHandler;
//    }

//    private void testLayout(){
//        //mina port 8081
//        Button startServer = (Button) findViewById(R.id.startServer);
//        startServer.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                EditText etPort = (EditText)findViewById(R.id.serverEditTextPort);
//                tmp = etPort.getText().toString();
//               if(!(tmp.isEmpty()) && (tmp.length() !=0)){
//                    Constant.setPORT(Integer.parseInt(tmp));//获取输入端口
//                }
//
//                Intent mIntent = new Intent(mContext, MinaServer.class);
//                startService(mIntent);
//            }
//        });
//        Button stopServer = (Button)findViewById(R.id.stopServer);
//        stopServer.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                MinaServer.getInstance().stopServer();
//                Intent mIntent = new Intent(mContext, MinaServer.class);
//                stopService(mIntent);
//            }
//        });
//
//        //httpd port 8080
//        Button startHttpd = (Button)findViewById(R.id.startHttpd);
//        startHttpd.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                    EditText edHttpd = (EditText) findViewById(R.id.edHttpdPort);
//                    tmp = edHttpd.getText().toString();
//                    if(!(tmp.isEmpty()) && (tmp.length() != 0)){
//                        Constant.setHttpdPort(Integer.parseInt(tmp));//获取httpd port
//                    }
//
//                    try {
//                        if(na == null){
//                            na = new NanoHTTPd(Constant.HTTPD_PORT);
//                            mHandler.sendEmptyMessage(TOAST_START_HTTPD);
//                        }
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//
//                LogUtils.i(TAG, "start com.communication.server.httpd");
//            }
//        });
//
//        Button stopHttpd = (Button)findViewById(R.id.stopHttpd);
//        stopHttpd.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if(na != null){
//                    na.stop();
//                }
//                LogUtils.i(TAG, "stop com.communication.server.httpd");
//            }
//        });
//
//    }
}

