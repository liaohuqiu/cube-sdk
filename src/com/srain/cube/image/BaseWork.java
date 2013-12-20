/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.srain.cube.image;

/**
 * A class which encapsulate a work that can excute in background thread an can be cancelled.
 * 
 * @author huqiu.lhq
 */
public abstract class BaseWork {

	private Object mTagObject;
	private boolean mIsCancelled;

	/**
	 * A woker will excute this method in a background thead
	 */
	public abstract void doInBackground();

	/**
	 * will be called after doInBackground();
	 */
	public abstract void onPostExecute();

	/**
	 * set a object which is related to the work
	 */
	public void setTag(Object tag) {
		mTagObject = tag;
	}

	/**
	 * get theobject which is related to the work
	 */
	public Object getTag() {
		return mTagObject;
	}

	/**
	 * check whether this work is canceled.
	 */
	public boolean isCancelled() {
		return mIsCancelled;
	}

	/**
	 * cancel work
	 */
	public void cancel() {
		mIsCancelled = true;
	}
}