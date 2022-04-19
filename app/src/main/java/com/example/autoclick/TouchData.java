package com.example.autoclick;

import java.util.ArrayList;

/**
 * Created by 170605 on 2017-07-24.
 */

public class TouchData {

	private String test_name;

	private ArrayList<TouchEvent> touchList;

	public ArrayList<TouchEvent> getTouchList() {
		return touchList;
	}

	public void setTouchList(ArrayList<TouchEvent> touchList) {
		this.touchList = touchList;
	}

	public void setTest_name(String test_name) {
		this.test_name = test_name;
	}

	public String getTest_name() {
		return test_name;
	}
}
