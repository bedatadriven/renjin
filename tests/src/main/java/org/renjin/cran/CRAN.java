package org.renjin.cran;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;

import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;


public class CRAN {

	public static final String CRAN_MIRROR = "http://ftp.yalwa.org/cran/";

	public static List<CranPackage> fetchPackageList() throws IOException {
	  Document dom = fetchAsDom(
			  new URL(CRAN_MIRROR + "web/packages/available_packages_by_name.html"));

	  List<CranPackage> packages = Lists.newArrayList();
      
      NodeList rows = dom.getElementsByTagName("tr");
      for(int i=0;i!=rows.getLength();++i) {
    	  Element row = (Element)rows.item(i);
    	  NodeList cells = row.getElementsByTagName("td");
    	  if(cells.getLength() == 2) {
    		  packages.add(parsePackage(cells));
    	  }
      }
      return packages;
	}
	
	public static String textContent(Node node) {
		StringBuilder sb = new StringBuilder();
		appendTextContent(sb, node);
		return sb.toString();
	}
	
	private static void appendTextContent(StringBuilder sb, Node node) {
		if(node instanceof CharacterData) {
			sb.append(node.getNodeValue());
		} else {
			NodeList children = node.getChildNodes();
			for(int i=0;i!=children.getLength();++i) {
				appendTextContent(sb, children.item(i));
			}
		}
	}

	private static Document fetchAsDom(URL url) throws MalformedURLException, IOException {
		  Tidy tidy = new Tidy();
		  tidy.setXHTML(false);
		  tidy.setQuiet(true);
		  tidy.setShowWarnings(false);
		  
		  InputStream in = url.openStream();
		  Document dom = tidy.parseDOM(in, null);
		  in.close();
		return dom;
	}
	
	private static CranPackage parsePackage(NodeList cells) {
		CranPackage cp = new CranPackage();
		
		Element nameCell = (Element)cells.item(0);
		Element descCell = (Element)cells.item(1);
		
		try {
			cp.setName(nameCell.getFirstChild().getFirstChild().getNodeValue().trim());
			cp.setDescription(descCell.getFirstChild().getNodeValue().trim());
		} catch(Exception e) {
			// ignore
		}
		return cp;
	}
	

	public static void downloadSrc(CranPackage pkg, File destFolder) throws MalformedURLException, IOException {
		downloadSrc(pkg.getName(), fetchCurrentVersion(pkg.getName()), destFolder);
	}

	public static String fetchCurrentVersion(String pkgName)
			throws MalformedURLException, IOException {
		Document dom = fetchAsDom(
				new URL(CRAN_MIRROR + "web/packages/" + pkgName + "/index.html"));

		String version = null;

		NodeList rows = dom.getElementsByTagName("tr");
		for(int i=0;i!=rows.getLength();++i) {
			Element row = (Element) rows.item(0);
			NodeList cells = row.getElementsByTagName("td");
			String header = CRAN.textContent(cells.item(0)).trim();
			if(header.equals("Version:")) {
				version = CRAN.textContent(cells.item(1));
			}
		}
		return version;
	}


	public static void downloadSrc(String pkgName, String version, File destFolder) throws IOException {

		File sourceZip = new File( destFolder, pkgName + "_" + version + ".tar.gz" );
		if(sourceZip.exists()) {
			System.out.println(sourceZip + ": already exists.");
		
		} else {
			System.out.println(sourceZip + ": downloading...");
			URL url = new URL(CRAN.CRAN_MIRROR + "src/contrib/" + sourceZip.getName());
			try {
				InputStream in = url.openStream();
				FileOutputStream out = new FileOutputStream(sourceZip);
				ByteStreams.copy(in, out);
				in.close();
				out.close();
			} catch(Exception e) {
				sourceZip.delete();
				e.printStackTrace();
			}
		}
	}
}