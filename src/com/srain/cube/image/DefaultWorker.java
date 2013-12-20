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

import java.lang.ref.WeakReference;

import com.srain.cube.image.ImageLoader.IWorker;

import android.os.AsyncTask;

/**
 * Simply use AsyncTask to execute a work.
 */
public class DefaultWorker implements IWorker {

	@Override
	public void doWork(BaseWork work) {
		Task task = new Task(work);
		work.setTag(new WeakReference<Task>(task));
		task.execute();
	}

	/**
	 * Cancel the task which is related to the work if it is still running.
	 */
	@Override
	public void cancleWork(BaseWork work) {
		final Object tag = work.getTag();
		if (tag != null && tag instanceof WeakReference<?>) {
			@SuppressWarnings("unchecked")
			final WeakReference<Task> taskReference = (WeakReference<Task>) tag;
			final Task task = taskReference.get();
			if (task != null) {
				task.cancel(true);
			}
		}
	}

	private static class Task extends AsyncTask<Object, Void, Void> {

		private BaseWork work;

		Task(BaseWork work) {
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