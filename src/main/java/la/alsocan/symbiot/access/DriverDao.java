/*
 * The MIT License
 *
 * Copyright 2014 Florian Poulin - https://github.com/fpoulin.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package la.alsocan.symbiot.access;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import la.alsocan.symbiot.api.to.drivers.DriverTo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Florian Poulin - https://github.com/fpoulin
 */
public class DriverDao {
	
	private Logger LOG = LoggerFactory.getLogger(DriverDao.class);
	private static final String DRIVER_FOLDER_NAME = "drivers";
	private final Map<String, DriverTo> drivers;

	public DriverDao(ObjectMapper om) {
		
		// open drivers folder
		String jarFolder;
		try {
			URL url = getClass().getProtectionDomain().getCodeSource().getLocation();
			File jarFile = new File(url.toURI());
			jarFolder = jarFile.getParentFile().getPath();
		} catch (URISyntaxException ex) {
			jarFolder = "<unable to resolve>";
		}
		String driverFolderPath = jarFolder + File.separatorChar + DRIVER_FOLDER_NAME;
		File folder = new File(driverFolderPath);
		if (!folder.exists() || !folder.isDirectory()) {
			throw new IllegalStateException("Could not find folder '"+driverFolderPath+"'");
		}
		
		// load drivers in memory
		drivers = new LinkedHashMap<>();
		for (File file : folder.listFiles((File dir, String name) -> { return name.endsWith(".json"); })) {
			DriverTo to;
			try {
				to = om.readValue(file, DriverTo.class);
				if (!drivers.containsKey(to.getId())) {
					drivers.put(to.getId(), to);
				} else {
					LOG.warn("The driver '"+file.getName()+"' uses a conflicting ID '"+to.getId()+"'");
					LOG.info("Driver '"+file.getName()+"' will not be loaded.");
				}
			} catch (IOException ex) {
				LOG.warn("Malformed driver '"+file.getName()+"': " + ex.getMessage());
				LOG.info("Driver '"+file.getName()+"' will not be loaded.");
			}
		}
	}
	  
	public List<DriverTo> findAll() {
		return new LinkedList<>(drivers.values());
	}
	
	public DriverTo findById(String id) {
		return drivers.get(id);
	}
}
