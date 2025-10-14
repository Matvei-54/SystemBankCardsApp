package com.example.bankcards.service;

import com.example.bankcards.exception.ErrorValueIdempotencyKeyException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class IdempotencyService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper mapper;
    private final long timeLifeRecordDB;

    public IdempotencyService(@Value("${cache.ttl}") long timeLifeRecordDB, ObjectMapper mapper, RedisTemplate<String, Object> redisTemplate){
        this.timeLifeRecordDB = timeLifeRecordDB;
        this.mapper = mapper;
        this.redisTemplate = redisTemplate;
    }

    public boolean idempotencyKeyCheck(String idempotencyKey) {

        if (idempotencyKey.isBlank()) {
            throw new ErrorValueIdempotencyKeyException();
        }
        return redisTemplate.hasKey(idempotencyKey);

    }

    @Transactional(readOnly = true)
    public <T> T getResultByIdempotencyKey(String idempotencyKey, Class<T> clazz) {

        var resultOperation = redisTemplate.opsForValue().get(idempotencyKey);

        return mapper.convertValue(resultOperation, clazz);
    }

    @Transactional
    public void saveIdempotencyKey(String idempotencyKey, Object resultMethod) {

        redisTemplate.opsForValue().set(idempotencyKey, resultMethod, timeLifeRecordDB, TimeUnit.SECONDS);
    }
}
