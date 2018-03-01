package py.com.cds.framework.redis;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.apache.logging.log4j.Logger;

import py.com.cds.framework.util.Config;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@ApplicationScoped
public class JedisPoolProvider {

	@Inject
	Config config;

	@Produces
	JedisPool jedisPool;

	@Inject
	Logger logger;

	@PostConstruct
	public void produceJedisPool() {
		String host = config.getString("jedis.url", "127.0.0.1");
		int port = config.getInteger("jedis.port", 6379);

		JedisPoolConfig jedisConfig = new JedisPoolConfig();
		jedisConfig.setMaxIdle(10);
		jedisConfig.setMaxTotal(20);
		logger.info("Connectin to redis, with values {}", config.getString("jedis.url", "127.0.0.1"));
		jedisPool = new JedisPool(jedisConfig, host, port);

	}

}
