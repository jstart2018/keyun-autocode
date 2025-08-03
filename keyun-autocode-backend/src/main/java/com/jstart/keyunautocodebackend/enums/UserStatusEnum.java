package com.jstart.keyunautocodebackend.enums;

import lombok.Getter;

@Getter
public enum UserStatusEnum {

    USABLE(0,"禁用"),
    DISABLE(1,"正常");

    private final Integer key;
    private final String value;

    UserStatusEnum(int key, String value) {
        this.key = key;
        this.value = value;
    }

    public static UserStatusEnum getByKey(int key) {
        for (UserStatusEnum statusEnum : UserStatusEnum.values()) {
            if (statusEnum.getKey() == key) {
                return statusEnum;
            }
        }
        return null;
    }



}
