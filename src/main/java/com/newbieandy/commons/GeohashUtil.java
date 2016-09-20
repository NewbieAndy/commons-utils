package com.newbieandy.commons;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Geohash算法实现,以及距离计算算法实现
 * Created by machao on 2016/7/22.
 */
public class GeohashUtil {
    private final static int numbits = 6 * 5;
    private final static char[] digits = {'0', '1', '2', '3', '4', '5', '6', '7', '8',
            '9', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'j', 'k', 'm', 'n', 'p',
            'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
    private final static String BASE32 = "0123456789bcdefghjkmnpqrstuvwxyz";
    private final static double EARTH_RADIUS = 6371000;//赤道半径(单位m)
    private final static HashMap<Character, Integer> lookup = new HashMap<Character, Integer>();
    private final static Map<String, String> BORDERS = new HashMap<String, String>();
    private final static Map<String, String> NEIGHBORS = new HashMap<String, String>();

    static {
        int i = 0;
        for (char c : digits)
            lookup.put(c, i++);
        //初始化集合
        NEIGHBORS.put("right:even", "bc01fg45238967deuvhjyznpkmstqrwx");
        NEIGHBORS.put("left:even", "238967debc01fg45kmstqrwxuvhjyznp");
        NEIGHBORS.put("top:even", "p0r21436x8zb9dcf5h7kjnmqesgutwvy");
        NEIGHBORS.put("bottom:even", "14365h7k9dcfesgujnmqp0r2twvyx8zb");

        NEIGHBORS.put("right:odd", "p0r21436x8zb9dcf5h7kjnmqesgutwvy");
        NEIGHBORS.put("left:odd", "14365h7k9dcfesgujnmqp0r2twvyx8zb");
        NEIGHBORS.put("top:odd", "bc01fg45238967deuvhjyznpkmstqrwx");
        NEIGHBORS.put("bottom:odd", "238967debc01fg45kmstqrwxuvhjyznp");

        BORDERS.put("right:even", "bcfguvyz");
        BORDERS.put("left:even", "0145hjnp");
        BORDERS.put("top:even", "prxz");
        BORDERS.put("bottom:even", "028b");

        BORDERS.put("right:odd", "prxz");
        BORDERS.put("left:odd", "028b");
        BORDERS.put("top:odd", "bcfguvyz");
        BORDERS.put("bottom:odd", "0145hjnp");
    }

    /**
     * 对geohash码进行解码
     *
     * @param geohash GeoHash 编码
     * @return 经纬度 格式{纬度，精度}
     */
    public static double[] decode(String geohash) {
        StringBuilder buffer = new StringBuilder();
        for (char c : geohash.toCharArray()) {

            int i = lookup.get(c) + 32;
            buffer.append(Integer.toString(i, 2).substring(1));
        }

        BitSet lonset = new BitSet();
        BitSet latset = new BitSet();

        //even bits
        int j = 0;
        for (int i = 0; i < numbits * 2; i += 2) {
            boolean isSet = false;
            if (i < buffer.length())
                isSet = buffer.charAt(i) == '1';
            lonset.set(j++, isSet);
        }

        //odd bits
        j = 0;
        for (int i = 1; i < numbits * 2; i += 2) {
            boolean isSet = false;
            if (i < buffer.length())
                isSet = buffer.charAt(i) == '1';
            latset.set(j++, isSet);
        }

        double lon = decode(lonset, -180, 180);
        double lat = decode(latset, -90, 90);

        return new double[]{lat, lon};
    }

    /**
     * 获取中间位
     *
     * @param bs
     * @param floor
     * @param ceiling
     * @return
     */
    private static double decode(BitSet bs, double floor, double ceiling) {
        double mid = 0;
        for (int i = 0; i < bs.length(); i++) {
            mid = (floor + ceiling) / 2;
            if (bs.get(i))
                floor = mid;
            else
                ceiling = mid;
        }
        return mid;
    }

    /**
     * geohash编码
     *
     * @param lat 纬度
     * @param lon 精度
     * @return GeoHash编码
     */
    public static String encode(double lat, double lon) {
        BitSet latbits = getBits(lat, -90, 90);
        BitSet lonbits = getBits(lon, -180, 180);
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < numbits; i++) {
            buffer.append((lonbits.get(i)) ? '1' : '0');
            buffer.append((latbits.get(i)) ? '1' : '0');
        }
        return base32(Long.parseLong(buffer.toString(), 2));
    }

    /**
     * 获取中间值
     *
     * @param lat
     * @param floor
     * @param ceiling
     * @return
     */
    private static BitSet getBits(double lat, double floor, double ceiling) {
        BitSet buffer = new BitSet(numbits);
        for (int i = 0; i < numbits; i++) {
            double mid = (floor + ceiling) / 2;
            if (lat >= mid) {
                buffer.set(i);
                floor = mid;
            } else {
                ceiling = mid;
            }
        }
        return buffer;
    }

    /**
     * base32 编码
     *
     * @param i
     * @return
     */
    private static String base32(long i) {
        char[] buf = new char[65];
        int charPos = 64;
        boolean negative = (i < 0);
        if (!negative)
            i = -i;
        while (i <= -32) {
            buf[charPos--] = digits[(int) (-(i % 32))];
            i /= 32;
        }
        buf[charPos] = digits[(int) (-i)];

        if (negative)
            buf[--charPos] = '-';
        return new String(buf, charPos, (65 - charPos));
    }


    /**
     * 获取九个点的矩形编码
     *
     * @param geohash
     * @return
     */
    public static String[] getGeoHashExpand(String geohash) {
        try {
            String geohashTop = calculateAdjacent(geohash, "top");
            String geohashBottom = calculateAdjacent(geohash, "bottom");
            String geohashRight = calculateAdjacent(geohash, "right");
            String geohashLeft = calculateAdjacent(geohash, "left");
            String geohashTopLeft = calculateAdjacent(geohashLeft, "top");
            String geohashTopRight = calculateAdjacent(geohashRight, "top");
            String geohashBottomRight = calculateAdjacent(geohashRight, "bottom");
            String geohashBottomLeft = calculateAdjacent(geohashLeft, "bottom");
            String[] expand = {geohash, geohashTop, geohashBottom, geohashRight, geohashLeft, geohashTopLeft, geohashTopRight, geohashBottomRight, geohashBottomLeft};
            return expand;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 分别计算每个点的矩形编码
     *
     * @param srcHash
     * @param dir
     * @return
     */
    private static String calculateAdjacent(String srcHash, String dir) {
        //转换为小写
        srcHash = srcHash.toLowerCase();
        //取最末尾的字符
        char lastChr = srcHash.charAt(srcHash.length() - 1);
        //判断奇偶
        int a = srcHash.length() % 2;
        String type = (a > 0) ? "odd" : "even";
        //取geohash
        String base = srcHash.substring(0, srcHash.length() - 1);
        //
        if (BORDERS.get(dir + ":" + type).indexOf(lastChr) != -1) {
            base = calculateAdjacent(base, dir);
        }
        base = base + BASE32.toCharArray()[(NEIGHBORS.get(dir + ":" + type).indexOf(lastChr))];
        return base;
    }

    //=======================================根据经纬度计算亮点距离===============

    /**
     * 转化为弧度(rad)
     */
    private static double rad(double d) {
        return d * Math.PI / 180.0;
    }

    /**
     * 基于googleMap中的算法得到两经纬度之间的距离,计算精度与谷歌地图的距离精度差不多，相差范围在0.2米以下
     *
     * @param lon1 第一点的经度
     * @param lat1 第一点的纬度
     * @param lon2 第二点的经度
     * @param lat2 第二点的纬度
     * @return 返回的距离，单位m
     */
    public static double GetDistance(double lon1, double lat1, double lon2, double lat2) {
        double radLat1 = rad(lat1);
        double radLat2 = rad(lat2);
        double a = radLat1 - radLat2;
        double b = rad(lon1) - rad(lon2);
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) + Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS;
        s = Math.round(s * 10000) / 10000;
        return s;
    }
}
