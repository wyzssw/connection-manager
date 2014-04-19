package com.wyzssw.threadlocal.conmanager;



public  interface ConnectionFactory<C>{
	public abstract  Connection<C> getConnection();  		
}
