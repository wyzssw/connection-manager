package com.wyzssw.threadlocal.conmanager;

import redis.clients.jedis.Jedis;


public class ThreadLocalJedisTest {
	
	public static void main(String[] args) {
		
		ThreadLocalConManager<Jedis> threadLocalConManager =	new ThreadLocalConManager<Jedis>(new ConnectionFactory<Jedis>() {

			public Connection<Jedis> getConnection() {
				   final Jedis jedis = new Jedis("127.0.0.1");
				   return new Connection<Jedis>(jedis) {

					@Override
					public boolean isBroken() {
						Jedis innerjedis = (Jedis)getConn();
						return !innerjedis.isConnected() || !innerjedis.ping().equals("PONG");
					}

					@Override
					public boolean close() {
						Jedis innerjedis = (Jedis)getConn();
						innerjedis.disconnect();
						return true;
					}
				};
			}
		});
		
		 Jedis jedis = (Jedis)threadLocalConManager.getConnection().getConn();
		 System.out.println(jedis.get("abc"));
	}

}
