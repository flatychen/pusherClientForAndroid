package cn.flaty.push.utils;

import android.content.Context;
import android.util.Log;

public class ApplicationUtil{
    
	private static String TAG = "ApplicationUtil";
	
    private  volatile static Context context = null;
    
    public static void init(Context c){
    	if( c == null){
    		throw new NullPointerException("Context 不能为空;");
    	}
        if(context == null){
        	context = c;
        }else{
        	Log.i(TAG,"已经初始化");
        }
    }
    
    public static Context getContext(){
    	if ( context == null ) {
    		throw new NullPointerException("请先初始化 init 方法;");
    	}
    	return context;
    }
}