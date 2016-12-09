package com.lighthousesoftware.launchpad.common.exception

class EntityNotFoundException extends AbstractEntityException {

	EntityNotFoundException(Class clazz, String s) {
		super(clazz, s);
	}	

	EntityNotFoundException(Class clazz, long id) {
		super(clazz, id);
	}
	
	EntityNotFoundException(Class clazz, long id, displayName) {
		super(clazz, id, displayName);
	}
}