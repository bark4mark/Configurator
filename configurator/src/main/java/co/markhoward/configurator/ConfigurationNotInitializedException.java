package co.markhoward.configurator;

public class ConfigurationNotInitializedException extends RuntimeException{
	private static final long serialVersionUID = 1L;
	public ConfigurationNotInitializedException(String message){
		super(message);
	}
	
}
