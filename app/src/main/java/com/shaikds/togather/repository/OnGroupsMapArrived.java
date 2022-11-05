package com.shaikds.togather.repository;

import com.shaikds.togather.model.Code;

import java.util.List;

public interface OnGroupsMapArrived {
    void onGroupMapsArrived(List<Code> codeList);

    void isLoaded(boolean isLoaded);
}
