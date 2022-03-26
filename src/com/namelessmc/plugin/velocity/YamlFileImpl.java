package com.namelessmc.plugin.velocity;

import com.namelessmc.plugin.common.AbstractYamlFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;

import java.util.List;

public class YamlFileImpl extends AbstractYamlFile {

	private final @NotNull MappingNode config;

	public YamlFileImpl(final @NotNull MappingNode config) {
		this.config = config;
	}

	private @Nullable Node findChild(String tag, List<NodeTuple> children) {
		for (NodeTuple child : children) {
			Node key = child.getKeyNode();
			if (!(key instanceof ScalarNode)) {
				throw new IllegalStateException("Key must be ScalarNode");
			}
			if (((ScalarNode) key).getValue().equals(tag)) {
				return child.getValueNode();
			}
		}
		return null;
	}

	@Override
	public String getString(final String path) {
		String[] tags = path.split("\\.");

		Node currentNode = config;
		for (String tag : tags) {
			if (currentNode instanceof MappingNode) {
				List<NodeTuple> children = ((MappingNode) currentNode).getValue();
				currentNode = findChild(tag, children);
			} else {
				throw new IllegalStateException("Value must be MappingNode");
			}
		}
		if (currentNode instanceof ScalarNode) {
			return ((ScalarNode) currentNode).getValue();
		} else {
			throw new IllegalStateException("Final value must be ScalarNode");
		}
	}

	@Override
	public boolean isString(String path) {
		// TODO better implementation
		try {
			this.getString(path);
			return true;
		} catch (IllegalStateException e) {
			return false;
		}
	}

}
