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

package com.srain.sdk.image;

public class Worker {

	private static Worker mInstance;

	public static Worker getInstance() {
		if (mInstance == null) {
			mInstance = new Worker();
		}
		return mInstance;
	}

	public void doWork(WorkBase work) {
		Task task = new Task(work);
		task.execute();
		// task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	public void cancle(WorkBase work) {

	}

	private class Task extends AsyncTask<Object, Void, Void> {

		private WorkBase work;

		Task(WorkBase work) {
			this.work = work;
		}

		@Override
		protected Void doInBackground(Object... params) {
			try {
				work.doInBackground();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			work.onPostExecute();
		}
	}
}