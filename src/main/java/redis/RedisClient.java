package redis;

import redis.clients.jedis.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @Author: buku.ch
 * @Date: 2018/10/23 7:47 PM
 */


public class RedisClient {

    private static RedisClient instance = new RedisClient();

    private Jedis jedis;

    private JedisPool jedisPool;


    public static RedisClient getInstance() {
        return instance;
    }

    private RedisClient() {
        initPool();
        jedis = jedisPool.getResource();
    }


    private void initPool() {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(10);
        config.setMaxIdle(5);
        config.setMaxWaitMillis(1000L);
        jedisPool = new JedisPool(config,"127.0.0.1",6379);
    }


    public Jedis getJedis() {
        return jedis;
    }

    public void close(){
        jedis.close();
        jedisPool.close();
    }




}
