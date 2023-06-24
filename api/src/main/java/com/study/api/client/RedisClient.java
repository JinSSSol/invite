package com.study.api.client;

import static com.study.api.exception.ErrorCode.FAILED_DELETE_REDIS;
import static com.study.api.exception.ErrorCode.INVALID_REDIS_KEY;
import static com.study.api.exception.ErrorCode.JSON_PARSING_ERROR;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.api.exception.CustomException;
import com.study.api.group.dto.redis.Invite;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

@Service
@RequiredArgsConstructor
public class RedisClient {

    private static final Long URL_EXPIRATION = 3600000L;
    private final RedisTemplate<String, Object> redisTemplate;
    private static final ObjectMapper mapper = new ObjectMapper();

    public <T> T get(String key, Class<T> classType) {
        String redisValue = (String) redisTemplate.opsForValue().get(key);

        if (ObjectUtils.isEmpty(redisValue)) {
            throw new CustomException(INVALID_REDIS_KEY);
        }

        try {
            return mapper.readValue(redisValue, classType);
        } catch (JsonProcessingException e) {
            throw new CustomException(JSON_PARSING_ERROR);
        }
    }

    public void putInvite(String key, Invite invite) {
        try {
            redisTemplate.opsForValue()
                .set(key, mapper.writeValueAsString(invite), URL_EXPIRATION, TimeUnit.MILLISECONDS);
        } catch (JsonProcessingException e) {
            throw new CustomException(JSON_PARSING_ERROR);
        }
    }

    public void delete(String key) {
        Boolean result = redisTemplate.delete(key);
        if (result == null || !result) {
            throw new CustomException(FAILED_DELETE_REDIS);
        }
    }
}
