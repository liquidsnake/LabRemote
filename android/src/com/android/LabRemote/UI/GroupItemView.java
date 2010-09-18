package com.android.LabRemote.UI;

import android.content.Context;

import com.android.LabRemote.R;
import com.android.LabRemote.Utils.MListItem;

public class GroupItemView extends ListItemView {
	
	public GroupItemView(Context context, MListItem item) {
		super(context, item, R.layout.group_item);
		initImage(R.id.groupPhoto);
		initName(R.id.groupName);
		initGrade(R.id.groupGrade);
	}

}
