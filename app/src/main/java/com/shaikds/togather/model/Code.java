package com.shaikds.togather.model;

import java.util.Map;

public class Code {
    private String code;
    private String userUid;
    private String groupUid;


    public Code() {
    }

    public String getCode() {
        return code;
    }

    public void createCode() {
        this.code = String.valueOf((int) (Math.round(Math.random() * 89999) + 10000));
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getUserUid() {
        return userUid;
    }

    public void setUserUid(String userUid) {
        this.userUid = userUid;
    }

    public String getGroupUid() {
        return groupUid;
    }

    public void setGroupUid(String groupUid) {
        this.groupUid = groupUid;
    }

    public void mapToCode(Map.Entry<String, Object> code) {
        Map<String, String> stringCodeMap = (Map<String, String>) code.getValue();
        for (Map.Entry<String, String> stringStringEntry : stringCodeMap.entrySet()) {
            final String key = stringStringEntry.getKey();
            final String value = stringStringEntry.getValue();
            if (key.equals("code")) {
                this.setCode(value);

            } else if (key.equals("groupUid")) {
                this.setGroupUid(value);

            } else if (key.equals("userUid")) {
                this.setUserUid(value);

            }
        }
    }
}
