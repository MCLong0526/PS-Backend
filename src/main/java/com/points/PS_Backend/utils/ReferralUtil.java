package com.points.PS_Backend.utils;

import java.util.UUID;

public class ReferralUtil {

    public static String generateCode() {
        return "REF-" + UUID.randomUUID().toString().substring(0,8);
    }

}