/**
 * ShowAvatar.java
 * 
 * Version 1.0
 * Copyright (C) 2010 LabRemote Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.android.LabRemote.Utils;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.android.LabRemote.UI.GroupItemView;

/**
 * Displays student avatar once it is downloaded
 */
public class ShowAvatar implements Runnable {
	public GroupItemView item;
	public Bitmap bmp;

	public ShowAvatar(GroupItemView item, Bitmap bmp) {
		this.bmp = bmp;
		this.item = item;
	}

	public void run()
	{
		ImageView img = item.getImg();
		img.setImageBitmap(bmp);
	}
}