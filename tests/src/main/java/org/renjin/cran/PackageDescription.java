package org.renjin.cran;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;

public class PackageDescription {

	private Map<String, String> properties;

	public static class PackageDependency {
		private String name;
		private String versionRange;

		public PackageDependency(String spec) {
			int versionSpecStart = spec.indexOf('(');
			if(versionSpecStart==-1) {
				name = spec;
				versionRange = "[0,)";
			} else {
				this.name = spec.substring(0, versionSpecStart).trim();

				int versionSpecEnd = spec.indexOf(')', versionSpecStart);
				if(versionSpecEnd == -1) {
					throw new IllegalArgumentException("Unterminated version specification: " + spec);
				}
				String versionSpec = spec.substring(versionSpecStart+1, versionSpecEnd).trim();
				if(versionSpec.startsWith(">=")) {
					versionRange = "[" + versionSpec.substring(">=".length()).trim() + ",)";
				} else if(versionSpec.startsWith(">")){
					versionRange = "(" + versionSpec.substring(">".length()).trim() + ",)";
				} else {
					versionRange = versionSpec;
				}
			}
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getVersionRange() {
			return versionRange;
		}

		public void setVersionRange(String versionRange) {
			this.versionRange = versionRange;
		}
	}

	private static class PackageDependencyParser implements Function<String, PackageDependency> {

		@Override
		public PackageDependency apply(String arg0) {
			return new PackageDependency(arg0);
		}
	}

	public static class Person {
		private String name;
		private String email;

		private Person(String spec) {
			int bracketStart = spec.indexOf('<');
			if(bracketStart == -1) {
				this.name = spec.trim();
			} else {
				this.name = spec.substring(0, bracketStart).trim();
				int bracketEnd = spec.indexOf('>', bracketStart);
				this.email = spec.substring(bracketStart+1, bracketEnd);
			}
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getEmail() {
			return email;
		}

		public void setEmail(String email) {
			this.email = email;
		}
	}

	private static class PersonParser implements Function<String, Person> {

		@Override
		public Person apply(String arg0) {
			return new Person(arg0);
		}
	}

	public static PackageDescription fromString(String contents) throws IOException {

		PackageDescription d = new PackageDescription();
		d.properties = Maps.newHashMap();

		List<String> lines = CharStreams.readLines(new StringReader(contents));
		String key = null;
		StringBuilder value = new StringBuilder();
		for(String line : lines) {
			if(line.length() > 0) {
				if(Character.isWhitespace(line.codePointAt(0))) {
					if(key == null) {
						throw new IllegalArgumentException("Expected key at line '" + line + "'");
					}
					value.append(" ").append(line.trim());
				} else {
					if(key != null) {
						d.properties.put(key, value.toString());
						value.setLength(0);
					}
					int colon = line.indexOf(':');
					if(colon == -1) {
						throw new IllegalArgumentException("Expected line in format key: value, found '" + line + "'");
					}
					key = line.substring(0, colon);
					value.append(line.substring(colon+1).trim());
				}
			}
		}
		if(key != null) {
			d.properties.put(key, value.toString());
		}
		return d;
	}
	
	public static PackageDescription fromInputStream(InputStream in) throws IOException {
		return fromString(CharStreams.toString(new InputStreamReader(in)));
	}

	public String getProperty(String key) {
		return properties.get(key);
	}

	public String getPackage() {
		return getProperty("Package");
	}

	public String getTitle() {
		return getProperty("Title");
	}

	public String getVersion() {
		return getProperty("Version");
	}

	public Iterable<Person> getAuthors() {
		return Iterables.transform(Arrays.asList(getProperty("Author").split("\\s*,\\s*")), new PersonParser());
	}

	public Person getMaintainer() {
		return new Person(getProperty("Maintainer"));
	}

	public String getDescription() {
		return getProperty("Description");
	}

	public Iterable<PackageDependency> getImports() {
		return getPackageDependencyList("Imports");
	}

	public Iterable<PackageDependency> getDepends() {
		return getPackageDependencyList("Depends");
	}

	private Iterable<PackageDependency> getPackageDependencyList(String property) {
		String list = getProperty(property);
		if(list == null) {
			return Collections.emptySet();
		} else {
			return Iterables.transform(Arrays.asList(list.split("\\s*,\\s*")), new PackageDependencyParser());
		}
	}

	public String getLicense() {
		return getProperty("License");
	}

	public String getUrl() {
		return getProperty("URL");
	}

}