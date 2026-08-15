package com.zbkj.common.utils;

public class DistanceUtil {

    private static final double EARTH_RADIUS = 6371; // 地球半径 km

    public static double getDistance(double lat1, double lon1, double lat2, double lon2) {

        double radLat1 = Math.toRadians(lat1);
        double radLat2 = Math.toRadians(lat2);

        double a = radLat1 - radLat2;
        double b = Math.toRadians(lon1) - Math.toRadians(lon2);

        double s = 2 * Math.asin(Math.sqrt(
                Math.pow(Math.sin(a / 2), 2)
                        + Math.cos(radLat1) * Math.cos(radLat2)
                        * Math.pow(Math.sin(b / 2), 2)));

        s = s * EARTH_RADIUS;

        return s; // km
    }
}