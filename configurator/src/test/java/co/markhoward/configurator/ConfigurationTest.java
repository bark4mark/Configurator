package co.markhoward.configurator;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ConfigurationTest {
	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();
	private Configuration configuration;
	
	@Before
	public void setup() throws IOException{
		File configurationFile = tempFolder.newFile();
		configuration = new Configuration(configurationFile);
	}
	
	@Test(expected=ConfigurationCannotBeLoadedException.class)
	public void shouldThrowAnExceptionIfTheConfigurationCannotBeLoaded() throws IOException{
		configuration.init(false);
	}
	
	@Test
	public void shouldReturnEmptyConfigurationMap(){
		configuration.init(true);
		Map<String, String> config = configuration.getConfiguraton();
		Assert.assertTrue(config.isEmpty());
	}
	
	@Test(expected=ConfigurationNotInitializedException.class)
	public void shouldThrowExceptionIfNotInitialized(){
		configuration.getConfiguraton();
	}
	
	@Test
	public void shouldWriteConfiguration() throws IOException{
		configuration.init(true);
		Map<String, String> config = configuration.getConfiguraton();
		config.put("Test", "Test");
		configuration.saveConfiguration(config);
		File configurationFile = configuration.getConfigurationFile();
		Configuration newConfiguration = new Configuration(configurationFile);
		newConfiguration.init(false);
		Map<String, String> newConfig = newConfiguration.getConfiguraton();
		Assert.assertTrue(newConfig.get("Test").equals("Test"));
	}
}
