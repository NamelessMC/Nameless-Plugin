package com.namelessmc.plugin.common;

import com.namelessmc.java_api.NamelessAPI;
import com.namelessmc.java_api.NamelessException;

public interface NamelessApiProvider {

	NamelessAPI getNamelessApi() throws NamelessException;

}
