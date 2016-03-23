package co.markhoward.configurator;

public class ConfigurationCannotBeLoadedException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	public ConfigurationCannotBeLoadedException(String message, Exception cause){
		super(message, cause);
	}
}
