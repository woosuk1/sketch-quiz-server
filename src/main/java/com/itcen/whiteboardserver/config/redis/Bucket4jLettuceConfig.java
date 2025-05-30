package com.itcen.whiteboardserver.config.redis;

import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Bucket4jLettuceConfig {

    /**
     * LettuceClient 를 이용해 String keys, byte[] values 용 RedisCodec 을 만든 뒤
     * StatefulRedisConnection<String, byte[]> 으로 연결합니다.
     * 이 연결을 LettuceBasedProxyManager 에 넘겨 분산 토큰 버킷을 관리합니다.
     */
    @Bean
    public LettuceBasedProxyManager<String> bucket4jProxyManager(RedisClient redisClient) {
        // 1) String 키, byte[] 값용 Codec 생성
        RedisCodec<String, byte[]> codec = RedisCodec.of(
                StringCodec.UTF8,        // key  <String,String> → encode/decode 키
                ByteArrayCodec.INSTANCE  // value<byte[],byte[]> → encode/decode 값
        );

        // 2) Lettuce 연결 생성
        StatefulRedisConnection<String, byte[]> connection = redisClient.connect(codec);

        // 3) 분산 ProxyManager 빌드
        return LettuceBasedProxyManager.<String>builderFor(connection)
                .build();
    }
}
