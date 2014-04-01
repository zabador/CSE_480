package edu.oakland.cse480;

import com.appspot.testmavenagain.myendpoint.model.MyResult;

public interface OnUpdateFinish {
	
	void onPlaceBetFinish(int bet);
	void onGetGameStateFinish(MyResult result);
}
