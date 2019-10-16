package fr.recia.ws.bourse;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class CsvReader {
	private static final Logger log = LoggerFactory.getLogger(AllocController.class);
	
	private static final Pattern P_STR = Pattern.compile("\";\"");
	//private static final Pattern P_END = Pattern.compile("\\D");
	
	@Value("${csv.file.name}")
	String fileName;
	
	Map<String, Integer> ine2niveau = new HashMap<>();
	
	
	@PostConstruct
	public Integer loadFile ()  {
		Map<String, Integer> inLoadMap = new HashMap<>();
		
		int count = 0;
		try (	Scanner scannerFile = new Scanner(ResourceUtils.getFile(fileName)) ) {
			log.info(scannerFile.nextLine());
			while (scannerFile.hasNextLine()) {
				String line = "No init";
				try ( Scanner scannerLine = new Scanner(line = scannerFile.nextLine()) ){
					scannerLine.useDelimiter(P_STR);
					log.debug("line = {}", line);
					if (scannerLine.next().length() > 0) { // on absorbe le 1er champs
							// la colonne 2 donne l'ine
							// la 3 le niveau de bourse
						if (scannerLine.hasNext()) {
							String ine = scannerLine.next();
							int niveau = scannerLine.nextInt();
							log.debug("ine {} : niveau {}", ine , niveau);
							inLoadMap.put(ine, niveau);
							count++;
						} 
					}
				} catch (InputMismatchException e) {
					log.error("InputMismatchException : {}" , line);
				}
			}
		} catch (FileNotFoundException e) {
			log.error("loadFile  filename=" + fileName, e);
		}
		if (! inLoadMap.isEmpty()) {
			synchronized (this) {
				ine2niveau = inLoadMap;
			}
		}
		return count;
	}
	
	
	
	public ShibBean niveau(ShibBean rep) {
		if (rep != null && rep.ine != null) {
			synchronized (this) {
				rep.niveau = ine2niveau.get(rep.ine);
			}
			if (rep.niveau != null && rep.niveau > 0) {
				rep.boursier = true;
			}
		}
		return rep;
	}
}
