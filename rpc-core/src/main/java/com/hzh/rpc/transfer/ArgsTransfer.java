package com.hzh.rpc.transfer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * @ClassName ArgsTransfer
 * @Description TODO
 * @Author DaHuangGo
 * @Date 2023/8/19 0:58
 * @Version 0.0.1
 **/
public class ArgsTransfer {
    public static Class<?>[] transferToTypes(String parameterTypeNames) throws ClassNotFoundException {
        return Arrays.stream(parameterTypeNames.split(","))
                .map(String::trim)
                .map(typeName -> {
                    try {
                        return Class.forName(typeName);
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                })
                .toArray(Class<?>[]::new);
    }


    public static Object[] transferToObjects(String reqBody, Class<?>[] parameterTypes) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode rootNode = objectMapper.readTree(reqBody);
            return IntStream.range(0, parameterTypes.length)
                    .mapToObj(i -> {
                        try {
                            return objectMapper.treeToValue(rootNode.get(i), parameterTypes[i]);
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .toArray();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error parsing request body", e);
        }
    }
}
