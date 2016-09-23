package com.yzxdemo.activity;

import java.util.Timer;
import java.util.TimerTask;

import com.yzxdemo.R;
import com.yzx.api.UCSService;
import com.yzx.tools.CustomLog;
import com.yzxdemo.action.UIDfineAction;
import com.yzxdemo.restClient.JsonReqClient;
import com.yzxdemo.tools.Config;
import com.yzxdemo.tools.DataTools;
import com.yzxdemo.tools.DfineAction;
import com.yzxdemo.tools.LoginConfig;
import com.yzxdemo.ui.LoginDialog;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends BaseActivity{
	
	private ProgressDialog mProgressDialog;
	private LoginDialog mLoginDialog;
	private EditText login_admin;
	private EditText login_pwd;
	private String login_admin_str="";
	private Timer timer;//��¼��ʱ��ʱ��
	
	private BroadcastReceiver br = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if(mProgressDialog != null){
				mProgressDialog.dismiss();
				mProgressDialog = null;
			}
			//�������ӻص����͵Ĺ㲥
			if(intent.getAction().equals(UIDfineAction.ACTION_TCP_LOGIN_RESPONSE)){
				//�����һ�����յ��Ļص��㲥�������ׯ����Ϣ�������ڶԻ�������ʾ����
				if(null==mLoginDialog || !mLoginDialog.isShowing()){
					int result = intent.getIntExtra(UIDfineAction.RESULT_KEY, 1);
					if(result == 0){
						CustomLog.v(DfineAction.TAG_TCP, "�����˺ŶԻ���");
						Toast.makeText(LoginActivity.this, "��¼�ɹ�",Toast.LENGTH_SHORT).show();
						//�����˺������ȥ����ǰ׺��modified by zhaohaojie 20150906
						if (DataTools.istest){
							LoginConfig.saveCurrentSidAndToken(LoginActivity.this, "*#*" + login_admin_str, login_pwd.getText().toString());
							LoginConfig.saveCurrentSid(LoginActivity.this, "*#*" + login_admin_str);
						}
						else {
							LoginConfig.saveCurrentSidAndToken(LoginActivity.this, login_admin_str, login_pwd.getText().toString());
							LoginConfig.saveCurrentSid(LoginActivity.this, login_admin_str);
						}
						Config.initProperties(LoginActivity.this);
						if(null!=mLoginDialog){
							mLoginDialog=null;
						}
						mLoginDialog = new LoginDialog(LoginActivity.this);
						mLoginDialog.show();
					}else{
						String str = "";
						switch(result){
						case 10:
							str = "JSON����";
							break;
						case 11:
							str = "û��SD�����ڴ治��";
							break;
						case 12:
							str = "IO����";
							break;
						case 101111:
							str = "�û������������";
							break;
						default:
							str = result+"";
							break;
						}
						Toast.makeText(LoginActivity.this, "��¼ʧ��:"+str,Toast.LENGTH_SHORT).show();
					}
				}
			}else if(intent.getAction().equals(UIDfineAction.ACTION_TCP_LOGIN_CLIENT_RESPONSE)){
				//����ڶ������յ��Ļص��㲥�����˻�����ɹ���������������չʾ����
				if(null!=mLoginDialog && mLoginDialog.isShowing()){
					if(intent.getIntExtra(UIDfineAction.RESULT_KEY, 1) == 0){
						Toast.makeText(LoginActivity.this, "�ɹ�",Toast.LENGTH_SHORT).show();
						new Handler().postDelayed(new Runnable() {
							@Override
							public void run() {
								startActivity(new Intent(LoginActivity.this, AbilityShowActivity.class));
								if(null!=mLoginDialog){
									mLoginDialog.dismiss();
								}
								LoginActivity.this.finish();
							}
						}, 1000);
					}else{
						Toast.makeText(LoginActivity.this, "ʧ��:"+intent.getIntExtra(UIDfineAction.REASON_KEY, 1),Toast.LENGTH_SHORT).show();
					}
				}
			}
		}
	};
	
	protected void onCreate(android.os.Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		IntentFilter ift = new IntentFilter();
        ift.addAction(UIDfineAction.ACTION_TCP_LOGIN_RESPONSE);
        ift.addAction(UIDfineAction.ACTION_TCP_LOGIN_CLIENT_RESPONSE);
        registerReceiver(br, ift);
        
        login_admin = (EditText)findViewById(R.id.login_admin);
        login_pwd = (EditText)findViewById(R.id.login_pwd);
        
        //��������������ļ��������˻�����ֱ�ӵ������˻�
//        Config.initProperties(this);
//        if(!"".equals(Config.getClient_id())){
//        	mLoginDialog = new LoginDialog(LoginActivity.this);
//    		mLoginDialog.show();
//        }
        
        //���������ļ��б������˻�������
        final String token[] = LoginConfig.getCurrentSidAndToken(this);
        if(token != null && token.length >= 2){
        	login_admin.setText(token[0]!= null ? token[0]:"");
        	login_pwd.setText(token[1] != null ? token[1]:"");
        	if(getIntent().getBooleanExtra("AutoLogin", false) && mLoginDialog==null){
        		CustomLog.v(DfineAction.TAG_TCP, "��ʼ�Զ�����");
        		checkTest();
        		startCallTimer();
        		showProgressDialog();
				new Thread(new Runnable() {
					@Override
					public void run() {
						//�����˺�ȥ��ǰ׺��added by zhaohaojie 20150906
						if(token[0].startsWith("*#*")){
							token[0] = token[0].substring(3);
						}
						JsonReqClient client = new JsonReqClient();
						String json = client.login(token[0], token[1]);
						CustomLog.v(DfineAction.TAG_TCP,"RESULT:"+json);
						Config.parseConfig(json,LoginActivity.this);
					}
				}).start();
        	}
        }
        
        //�����һ����������˺���Ϣ
        findViewById(R.id.login_btn).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				checkTest();
				if(login_admin.getText().length() > 0
						&& login_pwd.getText().length() > 0){
					startCallTimer();
					showProgressDialog();
					new Thread(new Runnable() {
						@Override
						public void run() {
							JsonReqClient client = new JsonReqClient();
							String json = client.login(login_admin_str, login_pwd.getText().toString());
							CustomLog.v(DfineAction.TAG_TCP,"RESULT:"+json);
							Config.parseConfig(json,LoginActivity.this);
						}
					}).start();
				}
			}
		});
        
        //��ע��˵��
        findViewById(R.id.login_register).setOnClickListener(new View.OnClickListener() {
     			@Override
     			public void onClick(View v) {
     				startActivity(new Intent(LoginActivity.this,RegisterActivity.class));
     			}
     	});
        
        String version = "SDK_VERSION:"+UCSService.getSDKVersion()+ "\r\nDEMO_VERSION:"+DataTools.getSoftVersion(this);
    	
    	((TextView)findViewById(R.id.package_version)).setText(Html.fromHtml(version));
	}
	
	public void checkTest(){
		if(login_admin.getText().toString().startsWith("*#*")){
			DataTools.istest = true;
			login_admin_str = login_admin.getText().toString().substring(3);
		}else {
			DataTools.istest = false;
			login_admin_str = login_admin.getText().toString();
		}
	}
	
	private void showProgressDialog() {
		if (mProgressDialog == null) {
			mProgressDialog = new ProgressDialog(LoginActivity.this);
		}

		mProgressDialog.setIndeterminate(true);
		mProgressDialog.setCancelable(true);
		mProgressDialog.setMessage("���ڻ�ȡ�����˺�,���Ե�...");
		mProgressDialog.show();
	}
	
	@Override
	protected void onDestroy() {
		if(mProgressDialog != null){
			mProgressDialog.dismiss();
		}
		unregisterReceiver(br);
		stopCallTimer();
		super.onDestroy();
	}
	
	/**
	 * ������¼��ʱ��ʱ��
	 */
	private void startCallTimer() {
		stopCallTimer();
		if(timer == null){
			timer = new Timer();
		}
		
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				handler.sendEmptyMessage(0);
			}
		}, 30000);
	}
	
	/**
	 * ֹͣ��¼��ʱ��ʱ��
	 */
	private void stopCallTimer(){
		if (timer != null){
			timer.cancel();
			timer=null;
		}
	}
	
	private Handler handler = new Handler () {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			if (msg.what == 0)
			{
				if(mProgressDialog != null){
					mProgressDialog.dismiss();
					mProgressDialog = null;
					Toast.makeText(LoginActivity.this, "��¼ʧ��",Toast.LENGTH_SHORT).show();
				}
			}

		}
	};
}
