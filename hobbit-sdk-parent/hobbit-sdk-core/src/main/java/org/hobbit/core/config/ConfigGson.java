package org.hobbit.core.config;

import org.springframework.context.annotation.Bean;

import com.google.gson.Gson;

public class ConfigGson {

	@Bean
	public Gson gson() {
		//return new GsonBuilder().setPrettyPrinting().create();
		return new Gson();
	}
}
