package com.wyzssw.threadlocal.conmanager;

public abstract class Connection<C> {
    	final C  c ;
    	public Connection(final C c) {
			this.c = c;
		}
    	public abstract boolean isBroken();
    	public abstract boolean close();
    	public C       getConn(){
    		return c;
    	}
}
