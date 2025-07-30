package com.jstart.keyunautocodebackend.auth;

import lombok.Getter;

@Getter
public enum RoleEnum {

    SUPER_ADMIN(0,"super_admin"),
    ADMIN(1,"admin"),
    NORMAL_USER(2,"normal_user");

    private final Integer key;
    private final String value;

    RoleEnum(int key, String value) {
        this.key = key;
        this.value = value;
    }

    public static RoleEnum getByKey(int key) {
        for (RoleEnum roleEnum : RoleEnum.values()) {
            if (roleEnum.getKey() == key) {
                return roleEnum;
            }
        }
        return null;
    }



}
