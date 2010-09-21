package com.android.LabRemote.UI;

import android.content.Context;

import com.android.LabRemote.R;
import com.android.LabRemote.Utils.MListItem;

public class AttendanceItemView extends ListItemView {
	public AttendanceItemView(Context context, MListItem item) {
		super(context, item, R.layout.individual_grade);
		initName(R.id.labIndex);
		initGrade(R.id.labGrade);
	}
}
