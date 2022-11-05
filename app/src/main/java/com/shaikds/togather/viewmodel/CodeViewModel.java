package com.shaikds.togather.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.shaikds.togather.model.Code;
import com.shaikds.togather.model.User;
import com.shaikds.togather.repository.CodeRepo;
import com.shaikds.togather.repository.OnGroupsMapArrived;
import com.google.firebase.auth.FirebaseAuth;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CodeViewModel extends AndroidViewModel implements OnGroupsMapArrived {
    private static final String TAG = "CodeViewModel";
    CodeRepo codeRepo;
    private final MutableLiveData<List<Code>> codeList;
    private final MutableLiveData<Boolean> isLoadedLiveData;
    String groupId;


    public CodeViewModel(@NonNull Application application) {
        super(application);
        codeRepo = new CodeRepo(this);
        codeList = new MutableLiveData<>();
        isLoadedLiveData = new MutableLiveData<>();
        isLoadedLiveData.postValue(false);

    }

    public MutableLiveData<Boolean> getIsLoadedLiveData() {
        return isLoadedLiveData;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId, List<User> memberList) {
        this.groupId = groupId;
        codeRepo.getAllCodesByUid(groupId, memberList); // get all the codes of that group.
    }


    public void createCode(String groupId, Code code) {
        Map<String, Object> newCode = new HashMap<>();
        codeRepo.addCodeToDB(groupId, newCode);
    }


    public void deleteCodeUid(String userUid, String groupId, Map<String, Object> codeMap) {
        codeRepo.deleteUidCode(userUid, groupId, codeMap);
    }


    // unique code
    public MutableLiveData<List<Code>> getAllCodes() {
        return codeList;
    }

    public void setDestroyedGroupId() {
        this.groupId = null;
    }

    public String getMyCode() {
        final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String finalCode = null;
        for (Code code : codeList.getValue()) {
            if (code.getUserUid().equals(uid)) {
                finalCode = code.getCode();
            }
        }
        return finalCode;
    }

    @Override
    public void onGroupMapsArrived(List<Code> codes) {
        codeList.postValue(codes);
        Log.d(TAG, "onGroupMapsArrived: New Code List Arrived Of This Group. ");
    }

    @Override
    public void isLoaded(boolean isLoaded) {
        isLoadedLiveData.postValue(isLoaded);
    }
}
