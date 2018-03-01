package py.com.cds.framework.redis;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@ApplicationScoped
public class JedisProvider {

	@Inject
	JedisPool jedisPool;

	@Produces
	public Jedis produceJedis() {
		return jedisPool.getResource();
	}

	public void close(@Disposes Jedis jedis) {
		jedis.close();
	}

}
