package com.example.autoclick;

/**
 * Created by 170605 on 2017-07-24.
 */

public class TouchEvent {

//	private String test_name;
//
//	private ArrayList<String> touchList;
//
//	public ArrayList<String> getTouchList() {
//		return touchList;
//	}
//
//	public void setTouchList(ArrayList<String> touchList) {
//		this.touchList = touchList;
//	}

	private String touch_evnt = "onClick";

	private String position_x;
	private String position_y;
	private String delay_time;

	public void setDelay_time(String delay_time) {
		this.delay_time = delay_time;
	}

	public void setPosition_x(String position_x) {
		this.position_x = position_x;
	}

	public void setPosition_y(String position_y) {
		this.position_y = position_y;
	}

	public String getDelay_time() {
		return delay_time;
	}

	public String getPosition_x() {
		return position_x;
	}

	public String getPosition_y() {
		return position_y;
	}
}
