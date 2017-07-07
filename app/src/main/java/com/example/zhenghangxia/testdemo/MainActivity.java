package com.example.zhenghangxia.testdemo;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.zhenghangxia.testdemo.adapter.MsgAdapter;
import com.example.zhenghangxia.testdemo.bean.Msg;
import com.turing.androidsdk.HttpRequestListener;
import com.turing.androidsdk.RecognizeListener;
import com.turing.androidsdk.RecognizeManager;
import com.turing.androidsdk.TTSListener;
import com.turing.androidsdk.TTSManager;
import com.turing.androidsdk.TuringManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText mMessageSent;
    private Button mBtnSent;
    private RecyclerView mRecyclerView;
    private MsgAdapter mAdapter;
    private List<Msg> mList = new ArrayList<>();

    private static final int REQUEST_CODE = 0x00;
    private RecognizeManager mRecognizerManager;
    private TTSManager mTtsManager;
    private final String TAG = MainActivity.class.getSimpleName();
    /**
     * 返回结果，开始说话
     */
    public static final int MSG_SPEECH_START = 0;
    /**
     * 开始识别
     */
    public static final int MSG_RECOGNIZE_RESULT = 1;
    /**
     * 开始识别
     */
    public static final int MSG_RECOGNIZE_START = 2;
    private TuringManager mTuringManager;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SPEECH_START:
                    Msg msg1 = new Msg((String) msg.obj,Msg.TYPE_RECEIVED);
                    mList.add(msg1);
                    mAdapter.notifyItemInserted(mList.size()-1);
                    mRecyclerView.scrollToPosition(mList.size()-1);
                    // 开始语音合成
                    mTtsManager.startTTS((String) msg.obj);
                    break;
                case MSG_RECOGNIZE_RESULT:
                    Msg msg2 = new Msg((String) msg.obj,Msg.TYPE_RECEIVED);
                    mList.add(msg2);
                    mAdapter.notifyItemInserted(mList.size()-1);
                    mRecyclerView.scrollToPosition(mList.size()-1);
                    // 发送请求
                    mTuringManager.requestTuring((String) msg.obj);
                    break;
                case MSG_RECOGNIZE_START:
                    // 开启语音识别
                    mRecognizerManager.startRecognize();
                    break;
            }
        }

        ;
    };

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,
                        Uri.parse("package:" + getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivityForResult(intent, REQUEST_CODE);
            } else {
                //有了权限，你要做什么呢？具体的动作
                init();
            }
        }else {
            init();
        }

    }

    private void init() {
        mMessageSent = (EditText) findViewById(R.id.et_chat_message_send);
        mBtnSent = (Button) findViewById(R.id.btn_send);
        mRecyclerView = (RecyclerView) findViewById(R.id.rv_chat_list);

        LinearLayoutManager manager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(manager);
        mAdapter = new MsgAdapter(mList);
        mRecyclerView.setAdapter(mAdapter);
        mBtnSent.setOnClickListener(this);

        // 实例化RecognizeManager(目前支持百度ASR方式) 第二个参数是bdAPI_KEY,第三个参数是bdSECRET_KEY
        mRecognizerManager = new RecognizeManager(
                this,
                "NekDp8UyG1rbBtGio33FHGt9",
                "13953b00c5fdaa869c350ec5787206bd");

        // 实例化TTSManager(语音合成管理类)，目前只支持百度
        mTtsManager = new TTSManager(
                this,
                "NekDp8UyG1rbBtGio33FHGt9",
                "13953b00c5fdaa869c350ec5787206bd");

        // 设置ASR状态监听
        // （myVoiceRecognizeListener需要实现接口RecognizeListener，在不同状态下回调不同的接口）
        mRecognizerManager.setVoiceRecognizeListener(myVoiceRecognizeListener);

        // 设置TTS监听器
        mTtsManager.setTTSListener(myTTSListener);

        // 实例化TuringManager类
        // 第二个参数是图灵key,第三个参数是图灵secret
        mTuringManager = new TuringManager(
                this,
                "436c3aba216b4a0c877829a0a96e633b",
                "c764c1145b3b197d");

        // 设置网络请求监听器
        mTuringManager.setHttpRequestListener(myHttpConnectionListener);

        // 开始语音合成
        mTtsManager.startTTS("你好啊");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_send:
                String content = mMessageSent.getText().toString();
                if (!"".equals(content)) {
                    Msg msg = new Msg(content,Msg.TYPE_SENT);
                    mList.add(msg);
                    mAdapter.notifyItemInserted(mList.size()-1);
                    mRecyclerView.scrollToPosition(mList.size()-1);
                    mMessageSent.setText("");
                    Message message = new Message();
                    message.what = MSG_RECOGNIZE_RESULT;
                    message.obj = content;
                    mHandler.sendMessage(message);
                }
                break;
        }
    }

    /**
     * 语音识别回调
     */
    RecognizeListener myVoiceRecognizeListener = new RecognizeListener() {

        @Override
        public void onVolumeChange(int volume) {
            // 仅讯飞回调
        }

        @Override
        public void onStartRecognize() {
            // 仅针对百度回调
        }

        @Override
        public void onRecordStart() {

        }

        @Override
        public void onRecordEnd() {

        }

        @Override
        public void onRecognizeResult(String result) {
            Log.d(TAG, "识别结果：" + result);
            if (result == null) {
                mHandler.sendEmptyMessage(MSG_RECOGNIZE_START);
                return;
            }
            mHandler.obtainMessage(MSG_RECOGNIZE_RESULT, result).sendToTarget();
        }

        @Override
        public void onRecognizeError(String error) {
            Log.e(TAG, "识别错误：" + error);
            mHandler.sendEmptyMessage(MSG_RECOGNIZE_START);
        }
    };

    /**
     * TTS回调
     */
    TTSListener myTTSListener = new TTSListener() {

        @Override
        public void onSpeechStart() {
            Log.d(TAG, "onSpeechStart");
        }

        @Override
        public void onSpeechProgressChanged() {

        }

        @Override
        public void onSpeechPause() {
            Log.d(TAG, "onSpeechPause");
        }

        @Override
        public void onSpeechFinish() {
            Log.d(TAG, "onSpeechFinish");
            mHandler.sendEmptyMessage(MSG_RECOGNIZE_START);
        }

        @Override
        public void onSpeechError(int errorCode) {
            Log.d(TAG, "onSpeechError：" + errorCode);
            mHandler.sendEmptyMessage(MSG_RECOGNIZE_START);
        }

        @Override
        public void onSpeechCancel() {
            Log.d(TAG, "TTS Cancle!");
        }
    };

    /**
     * 网络请求回调
     */
    HttpRequestListener myHttpConnectionListener = new HttpRequestListener() {

        @Override
        public void onSuccess(String result) {
            if (result != null) {
                try {
                    Log.e(TAG, "result" + result);
                    JSONObject result_obj = new JSONObject(result);
                    if (result_obj.has("text")) {
                        Log.e(TAG, result_obj.get("text").toString());
                        mHandler.obtainMessage(MSG_SPEECH_START,
                                result_obj.get("text")).sendToTarget();
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "JSONException:" + e.getMessage());
                }
            }
        }

        @Override
        public void onFail(int code, String error) {
            Log.e(TAG, "onFail code:" + code + "|error:" + error);
            mHandler.obtainMessage(MSG_SPEECH_START, "网络慢脑袋不灵了").sendToTarget();
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            if (Settings.System.canWrite(this)) {
                Toast.makeText(MainActivity.this,"ok",Toast.LENGTH_SHORT).show();
                init();
            } else {
                Toast.makeText(MainActivity.this,"failed",Toast.LENGTH_SHORT).show();
            }
        }
    }
}