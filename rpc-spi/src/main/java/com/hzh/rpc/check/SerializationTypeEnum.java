package com.hzh.rpc.check;

import lombok.Getter;

public enum SerializationTypeEnum {
    HESSIAN(0x10),
    JSON(0x20),
    PROTOBUF(0x30);

    @Getter
    private final int type;

    SerializationTypeEnum(int type) {
        this.type = type;
    }

    public static SerializationTypeEnum findByType(byte serializationType) {
        for (SerializationTypeEnum typeEnum : SerializationTypeEnum.values()) {
            if (typeEnum.getType() == serializationType) {
                return typeEnum;
            }
        }
        return HESSIAN;
    }
}
