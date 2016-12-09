package com.lighthousesoftware.launchpad.common.exception

class AbstractEntityException extends RuntimeException {
	def Class clazz
	def id
	def displayName;

	AbstractEntityException(Class clazz, String s) {
		super(s)
		this.clazz = clazz
	}

	AbstractEntityException(Class clazz,  long id) {
		super(clazz.simpleName + " not found. id = " + id)
		this.clazz = clazz
		this.id = id
	}
	
	AbstractEntityException(Class clazz, long id, displayName) {
		this(clazz, id as String) ;
		this.displayName = displayName;
	}

	
	def getDisplayName() {
		if(displayName) {
			return displayName;
		}
		return this.clazz.simpleName;
	}
}
