package com.namelessmc.plugin.NamelessSponge;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

public class Chat {

	public static String convertColorsString(String message) {
		return TextSerializers.FORMATTING_CODE.replaceCodes(message, '&');
	}
	
	public static Text toText(String message) {
		return Text.builder(convertColorsString(message)).toText();
	}
	
	

}