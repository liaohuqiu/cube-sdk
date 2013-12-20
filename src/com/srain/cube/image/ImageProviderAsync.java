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

import com.srain.cube.image.ImageLoader.IWorker;

/**
 * Using a {@link IWorker} to manipulate the IO operate.
 * 
 * @author huqiu.lhq
 */
public class ImageProviderAsync extends ImageProvider {

	private IWorker mWorker;

	public ImageProviderAsync(ImageCacheParams cacheParams, IWorker worker) {
		super(cacheParams);
		mWorker = worker;
	}

	protected enum ImageCacheWorkType {
		init_cache, close_cache, flush_cache
	}

	/**
	 * A helper class to encapsulate the operate into a Work which will be excuted by the Worker.
	 * 
	 * @author huqiu.lhq
	 * 
	 */
	private class ImageCacheWork extends BaseWork {

		private ImageCacheWork(ImageCacheWorkType workType) {
			mWorkType = workType;
		}

		private ImageCacheWorkType mWorkType;

		@Override
		public void doInBackground() {

			switch (mWorkType) {
			case init_cache:
				initDiskCache();
				break;
			case close_cache:
				closeDiskCache();
				break;
			case flush_cache:
				flushDishCache();
				break;
			default:
				break;
			}
		}

		@Override
		public void onPostExecute() {
		}

		void run() {
			mWorker.doWork(this);
		}
	}

	/**
	 * initiate the disk cache
	 */
	public void initDiskCacheAsync() {
		new ImageCacheWork(ImageCacheWorkType.init_cache).run();
	}

	/**
	 * close the disk cache
	 */
	public void closeDiskCacheAsync() {
		new ImageCacheWork(ImageCacheWorkType.close_cache).run();
	}

	/**
	 * flush the data to disk cache
	 */
	public void flushDishCacheAsync() {
		new ImageCacheWork(ImageCacheWorkType.flush_cache).run();
	}

}
