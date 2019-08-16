//package com.sixi.gateway.marketservice.kits;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.redis.core.StringRedisTemplate;
//import org.springframework.data.redis.core.script.DefaultRedisScript;
//import org.springframework.data.redis.core.script.RedisScript;
//import org.springframework.stereotype.Component;
//import org.springframework.util.StringUtils;
//
//import java.util.Collections;
//import java.util.List;
//import java.util.UUID;
//
///**
// * @Author: ZY
// * @Date: 2019/8/15 15:27
// * @Version 1.0
// * @Description:
// */
//@Component
//@Slf4j
//public class RedisDistributedKit {
//    private static final String LOCK_SCRIPT = "return redis.call('SET', KEYS[1], ARGV[1], 'PX', tonumber(ARGV[2]), 'NX') and true or false";
//    private static final String LOCK_RELEASE_SCRIPT = "return redis.call('GET', KEYS[1]) == ARGV[1] and (redis.call('DEL', KEYS[1]) == 1) or false";
//    private static final String LOCK_REFRESH_SCRIPT = "if redis.call('GET', KEYS[1]) == ARGV[1] then\n" +
//            "    redis.call('PEXPIRE', KEYS[1], tonumber(ARGV[2]))\n" +
//            "    return true\n" +
//            "end\n" +
//            "return false";
//
//    private final RedisScript<Boolean> lockScript = new DefaultRedisScript<>(LOCK_SCRIPT, Boolean.class);
//    private final RedisScript<Boolean> lockReleaseScript = new DefaultRedisScript<>(LOCK_RELEASE_SCRIPT, Boolean.class);
//    private final RedisScript<Boolean> lockRefreshScript = new DefaultRedisScript<>(LOCK_REFRESH_SCRIPT, Boolean.class);
//
//    @Autowired
//    private StringRedisTemplate stringRedisTemplate;
//
//    private long getMilliseconds() {
//        return System.nanoTime() / 1000;
//    }
//
//    /**
//     * 获取分布式锁<br>
//     * 在Redis里的存储为 storeId:key
//     *
//     * @param key        Key
//     * @param storeId    存储区域
//     * @param expiration 过期时间
//     * @return 获得的Token 用于释放锁
//     */
//    public String acquire(String key, final String storeId, final long expiration) throws InterruptedException {
//        return this.acquire(key, storeId, expiration, 0);
//    }
//
//    /**
//     * 获取分布式锁<br>
//     * 在Redis里的存储为 storeId:key
//     *
//     * @param key        Key
//     * @param storeId    存储区域
//     * @param expiration 过期时间
//     * @param timeout    等待时间(超过等待时间则直接返回)
//     * @return 获得的Token 用于释放锁
//     */
//    public String acquire(String key, final String storeId, final long expiration, int timeout) throws InterruptedException {
//        long startTime = getMilliseconds();
//        final List<String> singletonKeyList = Collections.singletonList(storeId + ":" + key);
//        final String token = UUID.randomUUID().toString();
//        if (StringUtils.isEmpty(token)) {
//            throw new IllegalStateException("Cannot lock with empty token");
//        }
//        String expStr = String.valueOf(expiration);
//        boolean locked = stringRedisTemplate.execute(lockScript, singletonKeyList, token, expStr);
//        while (getMilliseconds() - startTime < timeout && !locked) {
//            Thread.sleep(10);
//            locked = stringRedisTemplate.execute(lockScript, singletonKeyList, token, expStr);
//        }
//        log.debug("Tried to acquire lock for key {} with token {} in store {}. Locked: {}", key, token, storeId, locked);
//        return locked ? token : null;
//    }
//
//    /**
//     * 释放锁
//     *
//     * @param key     Key
//     * @param storeId 存储区域
//     * @param token   获得锁的时候返回的Token
//     * @return 是否释放成功
//     */
//    public boolean release(String key, final String storeId, final String token) {
//        final List<String> singletonKeyList = Collections.singletonList(storeId + ":" + key);
//
//        final boolean released = stringRedisTemplate.execute(lockReleaseScript, singletonKeyList, token);
//        if (released) {
//            log.debug("Release script deleted the record for key {} with token {} in store {}", key, token, storeId);
//        } else {
//            log.error("Release script failed for key {} with token {} in store {}", key, token, storeId);
//        }
//        return released;
//    }
//
//    /**
//     * 刷新锁的过期时间
//     *
//     * @param key        Key
//     * @param storeId    存储区域
//     * @param token      获得锁的时候返回的Token
//     * @param expiration 新的过期时间
//     * @return 是否释放成功
//     */
//    public boolean refresh(String key, final String storeId, final String token, final long expiration) {
//        final List<String> singletonKeyList = Collections.singletonList(storeId + ":" + key);
//        final boolean refreshed = stringRedisTemplate.execute(lockRefreshScript, singletonKeyList, token, String.valueOf(expiration));
//        if (refreshed) {
//            log.debug("Refresh script updated the expiration for key {} with token {} in store {} to {}", key, token, storeId, expiration);
//        } else {
//            log.debug("Refresh script failed to update expiration for key {} with token {} in store {} with expiration: {}",
//                    key,
//                    token,
//                    storeId,
//                    expiration);
//        }
//        return refreshed;
//    }
//}
