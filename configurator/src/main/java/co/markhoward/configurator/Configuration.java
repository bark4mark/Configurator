package co.markhoward.configurator;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Configuration {
	private final File configurationFile;
	private final Map<String, String> configurationMap = new ConcurrentHashMap<>();
	private final ObjectMapper objectMapper = new ObjectMapper();
	private final ExecutorService service;
	
	private WatchService watchService;
	private boolean initialized = false;
	public Configuration(File configurationFile){
		this.configurationFile = configurationFile;
		this.service = Executors.newFixedThreadPool(1);
	}
	
	public void init(boolean isNew) throws ConfigurationCannotBeLoadedException{
		try {
			this.watchService = FileSystems.getDefault().newWatchService();
			if(isNew){
				this.initialized = true;
				return;
			}
			loadConfiguration();
			addWatcher();
			this.initialized = true;
		} catch (IOException exception) {
			throw new ConfigurationCannotBeLoadedException("Cannot create watch service", exception);
		}
	}
	
	public Map<String, String> getConfiguraton() throws ConfigurationNotInitializedException{
		checkInitialization();
		return this.configurationMap;
	}
	
	public void saveConfiguration(Map<String, String> config) throws IOException{
		checkInitialization();
		this.objectMapper.writeValue(configurationFile, config);
	}
	
	public File getConfigurationFile(){
		return this.configurationFile;
	}
	
	private void checkInitialization(){
		if(this.initialized)
			return;
		
		throw new ConfigurationNotInitializedException("Please run Configuration#init first");
	}
	
	private void loadConfiguration(){
		try {
			this.configurationMap.clear();
			this.configurationMap.putAll(objectMapper.readValue(configurationFile, new TypeReference<Map<String,String>>() {}));
		} catch (IOException exception) {
			throw new ConfigurationCannotBeLoadedException(String.format("The configuration could not be parsed from provided file: %s", this.configurationFile), exception);
		}
	}
	
	private void addWatcher() throws IOException{
		String configFileName = this.configurationFile.getName();
		if(configFileName == null || configFileName.trim().isEmpty())
			throw new IOException("Filename cannot be empty");
		Path configParent = this.configurationFile.getParentFile().toPath();
		configParent.register(this.watchService,
				StandardWatchEventKinds.ENTRY_MODIFY,
				StandardWatchEventKinds.ENTRY_DELETE);
		
		service.execute(new Runnable() {
			@Override
			public void run() {
				try {
					while (true) {
						WatchKey watchKey = watchService.take();
						for (WatchEvent<?> event : watchKey.pollEvents()) {
							WatchEvent.Kind<?> kind = event.kind();
							if(!configFileName.equals(event.context()))
								continue;
							switch(kind.name()){
							case "ENTRY_MODIFY":
								loadConfiguration();
								break;
							case "ENTRY_DELETE":
								initialized = false;
								break;
							}
						}
						watchKey.reset();
					}
				} catch (InterruptedException exception) {
					exception.printStackTrace();
				}
			}
		});
	}
}
