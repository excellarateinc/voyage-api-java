package launchpad.common.exception

class AccessDeniedException extends AbstractEntityException {

	AccessDeniedException(Class clazz, String s) {
		super(clazz, s);
	}	

	AccessDeniedException(Class clazz, long id) {
		super(clazz, id);
	}
	
	AccessDeniedException(Class clazz, long id, displayName) {
		super(clazz, id, displayName);
	}
}