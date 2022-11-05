package com.shaikds.togather.repository;

import android.util.Log;

import com.shaikds.togather.model.Code;
import com.shaikds.togather.model.User;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CodeRepo {
    private static final String TAG = "CodeRepo";
    List<Code> codesList = new ArrayList<>();
    OnGroupsMapArrived groupsMapArrived;
    FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    CollectionReference referenceAllCodes = firebaseFirestore.collection("codes");


    public CodeRepo(OnGroupsMapArrived groupsMapArrived) {
        this.groupsMapArrived = groupsMapArrived;
    }


    //get all posts
    public List<Code> getAllCodesByUid(String groupId, List<User> currGroupUsers) {
        if (groupId != null && currGroupUsers != null) {

            referenceAllCodes.document(groupId).get().addOnCompleteListener(task -> {
                DocumentSnapshot documentSnapshot = task.getResult();
                if (documentSnapshot.exists()) {
                    Map<String, Object> codesMap = documentSnapshot.getData();
                    for (Map.Entry<String, Object> stringObjectEntry : codesMap.entrySet()) {
                        final Code code = new Code();
                        code.mapToCode(stringObjectEntry);
                        codesList.add(code);
                        groupsMapArrived.onGroupMapsArrived(codesList);
                        if (codesList.size() == codesMap.entrySet().size()) {
                            // last index --> lets check everyone in group have their codes.

                            checkIfEveryoneHaveCodes(groupId, currGroupUsers, codesMap);
                        }
                        Log.d(TAG, "User Code In Group exists with the code num : " + code.getCode());
                    }
                } else {
                    // if group id isn't in db-> create all codes.
                    createAllCodes(groupId, currGroupUsers);
                }
            });
        }
        return codesList;
    }


    public void deleteUidCode(String deleteUid, String groupId, Map<String, Object> codesMap) {
        //delete the code by user uid key in map,
        //&& then sets new map in firestore db .
        codesMap.remove(deleteUid);
        referenceAllCodes.document(groupId).set(codesMap)
                .addOnCompleteListener(task -> {
                    Log.d(TAG, "onCompleteDeleteCode: Delete result is True.");
                });
    }


    //add codes.
    public void addCodeToDB(String groupId, Map<String, Object> code) {
        //referenceAllCodes.document(groupId).collection(code.getUserUid()).document("code").set(code)
        referenceAllCodes.document(groupId).set(code)
                .addOnCompleteListener(task -> {
                    Log.d(TAG, "addCodeToDB: Added new thing to codes in group-> " + groupId);
                });
    }

    //add first time codes.
    public void addFirstTimeCodeToDB(String groupId, Map<String, Object> codeList) {
        referenceAllCodes.document(groupId)
                .set(codeList)
                .addOnCompleteListener(task -> {
                    Log.d(TAG, "addCodeToDB: Added new thing to codes in group-> " + groupId);
                });

    }


    //create codes for all members currently in group .
    public void createAllCodes(String groupId, List<User> userList) {
        if (userList.size() > 0) {
            final Map<String, Object> finalCodeMap = new HashMap<>();
            if (codesList == null || codesList.size() == 0) {
                for (User user : userList) {
                    if (user != null) {
                        final Code code = new Code();
                        String uniqueCode = String.valueOf((int) (Math.round(Math.random() * 89999) + 10000));
                        code.setGroupUid(groupId);
                        code.setCode(uniqueCode);
                        code.setUserUid(user.getUid());
                        finalCodeMap.put(code.getUserUid(), code);
                    }
                }
                addFirstTimeCodeToDB(groupId, finalCodeMap);
            }
        }
    }

    //every time db updated, check if all users in group have their code.
    public void checkIfEveryoneHaveCodes(String groupId, List<User> memberList, Map<String, Object> currentMap) {
        final Map<String, Object> codesMap = new HashMap<>(currentMap);
        // there are codes in db of that group-> lets check current user has his code .
        //if everyone have their codes so just pass the list of codes to payment view.
        //1st of all, delete all members who left the group .
        deleteUsersLeftGroup(memberList, codesMap);
        for (User user : memberList) {
            // for each user check if he has code.
            innerLoop:
            for (Code code : codesList) {
                if (user.getUid().equals(code.getUserUid())) {
                    // same user uid -->move to another user.
                    break innerLoop;
                }
                if (codesList.get(codesList.size() - 1).equals(code) && !code.getUserUid().equals(user.getUid())) {
                    //if it's last code and not equals to user uid, user has no code-> create new code for him.
                    final Code newCode = new Code();
                    newCode.setUserUid(user.getUid());
                    newCode.setGroupUid(groupId);
                    newCode.createCode();
                    codesMap.put(user.getUid(), newCode);
                }
            }
            if (memberList.get(memberList.size() - 1).equals(user)) {//if were at last user
                addCodeToDB(groupId, codesMap);  //add map only once! not many times.
                groupsMapArrived.isLoaded(true);
            }
        }
    }

    public void deleteUsersLeftGroup(List<User> memberList, Map<String, Object> currentMap) {
        if (codesList.size() > 1) {
            for (Code code : codesList) {
                for (User user : memberList) {
                    if (user.getUid().equals(code.getUserUid())) {
                        break; // MOVE TO ANOTHER CODE
                    }
                    // codeUid NOT equals last userUid? -> user is not inside group, delete his code ƒrom map.
                    if (memberList.get(memberList.size() - 1).equals(user) && !user.getUid().equals(code.getUserUid())) {
                        currentMap.remove(code.getUserUid());

                    }
                }
            }
        }
    }
}








