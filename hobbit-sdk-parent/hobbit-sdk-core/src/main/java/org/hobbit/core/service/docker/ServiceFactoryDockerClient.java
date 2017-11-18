package org.hobbit.core.service.docker;

import java.util.Map;

import com.google.common.util.concurrent.Service;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

//public class ServiceFactoryDockerClient
//	implements ServiceFactory<DockerService>
//{
//	class Config {
//		public String imageName;
//		public Map<String, String> env;
//	}
//	
//	
//	@Override
//	public Service create(JsonObject jsonConfig) {
//		//Objects.requireNonNull(config);
//		Gson gson = new Gson();
//		Config mapToJson = gson.fromJson(jsonConfig, Config.class);
//
//		
//        DockerServiceDockerClient result = new DockerServiceDockerClient(dockerClient, containerConfig);
//
//		
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//}
