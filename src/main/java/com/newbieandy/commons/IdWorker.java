package com.newbieandy.commons;

import java.security.SecureRandom;
import java.util.regex.Pattern;

/**
 * @Description: 获取全局唯一自增ID
 * 一.使用默认无参构造函数需要做如下配置
 * 需要配置环境变量 IDWORKER_CONFIG
 * 值的格式为  a_b   其中a,b为正整数
 * a为处理中心ID，取值为 0-7，
 * b为工作机器ID，取值为 0-63
 * 注意：每个运行环境的环境变量要保证彼此唯一，
 * 即 相同的处理中心ID后不能配置重复的工作机器ID
 * 例如. 1_2
 * @Author: machao
 * @Date:2016/5/17
 * @Version:1.0.0
 * @Company: 诸葛修车网
 */
public class IdWorker {

    private long workerId;
    private long datacenterId;
    //毫秒级内序列号
    private long sequence = 0L;
    //起始纪元时间(2016-05-17 13:53:22)，时间标识 = 当前时间-此时间
    private long twepoch = 1463464402094L;
    //工作机器占用bit数
    private long workerIdBits = 6L;
    //处理中心占用bit数
    private long datacenterIdBits = 3L;
    //最大工作机器ID 111111 -> 64
    private long maxWorkerId = -1L ^ (-1L << workerIdBits);
    //最大处理中心ID 111 -> 8
    private long maxDatacenterId = -1L ^ (-1L << datacenterIdBits);
    //毫秒级内序列号占用bit数
    private long sequenceBits = 12L;
    //工作机器ID左偏移量 12
    private long workerIdShift = sequenceBits;
    //处理中心ID左偏移量 12+6=18
    private long datacenterIdShift = sequenceBits + workerIdBits;
    //时间标识左偏移量 12+6+3=21
    private long timestampLeftShift = sequenceBits + workerIdBits + datacenterIdBits;
    //毫秒级内序列号掩码（最大值）
    private long sequenceMask = -1L ^ (-1L << sequenceBits);

    //上次生成ID的时间标记
    private long lastTimestamp = -1L;

    /**
     * 创建ID生成器
     *
     * @param workerId     工作机器ID(0-63,同处理中心下机器ID不可重复)
     * @param datacenterId 处理中心ID(0-7，此值不可重复)
     */
    public IdWorker(long workerId, long datacenterId) {
        // 验证输入的工作机器ID是否合法
        if (workerId > maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException(
                    String.format("工作机器ID不能大于%d或者小于0!", maxWorkerId));
        }
        //验证输入的处理中心ID是否合法
        if (datacenterId > maxDatacenterId || datacenterId < 0) {
            throw new IllegalArgumentException(
                    String.format("处理中心ID不能大于%d或者小于0!", maxDatacenterId));
        }
        this.workerId = workerId;
        this.datacenterId = datacenterId;
    }

    /**
     * 创建ID生成器
     */
    public IdWorker() {
        //获取环境变量
        String idworkerConfig = System.getenv("IDWORKER_CONFIG");
        //判断环境变量是否合法
        if (null == idworkerConfig || !configVerify(idworkerConfig)) {
            throw new RuntimeException("IDWORKER_CONFIG环境变量配置错误！");
        }
        String[] configArgs = idworkerConfig.split("_");
        long sysDatacenterId = String2long(configArgs[0]);
        long sysWorkerId = String2long(configArgs[1]);
        // 验证输入的工作机器ID是否合法
        if (sysWorkerId > maxWorkerId || sysWorkerId < 0) {
            throw new IllegalArgumentException(
                    String.format("工作机器ID不能大于%d或者小于0!", maxWorkerId));
        }
        //验证输入的处理中心ID是否合法
        if (sysDatacenterId > maxDatacenterId || sysDatacenterId < 0) {
            throw new IllegalArgumentException(
                    String.format("处理中心ID不能大于%d或者小于0!", maxDatacenterId));
        }
        this.workerId = sysWorkerId;
        this.datacenterId = sysDatacenterId;
    }


    /**
     * 获取全局自增唯一ID
     *
     * @return
     */
    public synchronized long nextId() {
        long timestamp = timeGen();

        if (timestamp < lastTimestamp) {
            throw new RuntimeException(String
                    .format("时间被调回，导致上次生成ID时间大于本次时间，不能生成ID，两次相差%d毫秒！",
                            lastTimestamp - timestamp));
        }

        //同一毫秒级内操作
        if (lastTimestamp == timestamp) {
            //毫秒级内序列号进行+1操作,并验证
            sequence = (sequence + 1) & sequenceMask;
            //当前毫秒内，序列号已用完（1111 1111 1111 -> 4096）
            if (sequence == 0) {
                //等待下一毫秒
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            //非同一毫秒级内，理论上计数器归零
            //sequence = 0;
            //但为保证尾数随机性大一些，毫秒级计数器归为0-9的随机数
            sequence = new SecureRandom().nextInt(10);
        }

        //当前生成ID时间设置为最后生成时间
        lastTimestamp = timestamp;
        //进行bit拼接
        return ((timestamp - twepoch) << timestampLeftShift) | (datacenterId << datacenterIdShift) | (
                workerId << workerIdShift) | sequence;
    }

    /**
     * 等待下一毫秒
     *
     * @param lastTimestamp
     * @return
     */
    protected long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    /**
     * 获取系统当前时间
     *
     * @return
     */
    protected long timeGen() {
        return System.currentTimeMillis();
    }

    /**
     * 验证环境变量的值是否符合要求
     *
     * @param environmentVar
     * @return
     */
    protected boolean configVerify(String environmentVar) {
        String reg = "^[0-7]_[0-9]{1,2}$";
        Pattern pattern = Pattern.compile(reg);
        return pattern.matcher(environmentVar).matches();
    }

    /**
     * 字符串转long类型
     *
     * @param value
     * @return
     */
    protected long String2long(String value) {
        try {
            return Long.parseLong(value);
        } catch (Exception e) {
            throw new RuntimeException("配置的信息转换为long类型失败！");
        }
    }
}
