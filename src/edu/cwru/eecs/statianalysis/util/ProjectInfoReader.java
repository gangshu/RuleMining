package edu.cwru.eecs.statianalysis.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import edu.cwru.eecs.statianalysis.data.Project;

/*
 * Read the xml configuration file and return a project
 * @author CXH
 */

public class ProjectInfoReader {

	private static Document doc;
	static {
		try {
			doc = new SAXReader().read(Project.class.getResource("").toString()
					+ "project.xml");
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public static ProjectInfo getProject(String user) {

		ProjectInfo info = new ProjectInfo();
		Element root = doc.getRootElement();
		String driver = root.attributeValue("database-driver");
		String url = root.attributeValue("url");
		info.setDriver(driver);
		info.setUrl(url);
		info.setUser(user);
		List<Element> elements = ((Element) doc
				.selectObject("//project[@name=\"" + user + "\"]")).elements();
		
		try {
			for (Element element : elements) {
				String name = element.getName();
				name = name.substring(0, 1).toUpperCase() + name.substring(1);

				Method method = info.getClass().getMethod("set" + name,
						String.class);
				method.invoke(info, element.getText());
				// System.out.println(element.getName());
			}

		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return info;
	}
}
