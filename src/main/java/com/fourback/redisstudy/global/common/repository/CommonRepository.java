package com.fourback.redisstudy.global.common.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fourback.redisstudy.global.common.enums.PrefixEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class CommonRepository {
    private final ObjectMapper objectMapper;
    private final StringRedisTemplate redisTemplate;

    // hash
    public Map<String, String> hGetAll(String key) {
        HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();

        return hashOperations.entries(key);
    }

    public List<Map<String, String>> hGetAllFromKeys(List<String> keys) {
        List<byte[]> byteKeys = keys.stream().map(String::getBytes).toList();

        List<Object> objects = redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            byteKeys.forEach(byteKey -> connection.hashCommands().hGetAll(byteKey));
            return null;
        });

        return objectMapper.convertValue(
                objects, objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class));
    }

    public void hIncrBy(String key, String field, long amount) {
        HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
        hashOperations.increment(key, field, amount);
    }

    // set
    public Boolean sAdd(String key, String member) {
        Long addCount = redisTemplate.opsForSet().add(key, member);

        return addCount != null && addCount > 0;
    }

    public Boolean sIsMember(String key, String member) {
        return redisTemplate.opsForSet().isMember(key, member);
    }

    public Boolean sRem(String key, String member) {
        Long remCount = redisTemplate.opsForSet().remove(key, member);

        return remCount != null && remCount > 0;
    }

    public Set<String> sMembers(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    public Set<String> sInter(String key, String anotherKey) {
        return redisTemplate.opsForSet().intersect(key, anotherKey);
    }

    // sorted set
    public Double zScore(String key, String member) {
        return redisTemplate.opsForZSet().score(key, member);
    }

    public Set<String> zRange(Long lastEndAt) {
        Long millisTime = lastEndAt;

        if (millisTime == null)
            millisTime = LocalDate.now().atStartOfDay(ZoneId.of("Asia/Seoul")).toInstant().toEpochMilli();

        return redisTemplate.opsForZSet().rangeByScore(
                PrefixEnum.ITEM_ENDING_AT.getPrefix(), millisTime, Long.MAX_VALUE, 0, 3L);
    }

    // list
    public List<String> lRange(String key, long start, long end) {
        return redisTemplate.opsForList().range(key, start, end);
    }
}
