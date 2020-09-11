package fr.recia.ws.bourse;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.Map;
import java.util.NoSuchElementException;
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
	private static final Logger log = LoggerFactory.getLogger(CsvReader.class);
	
	private static final Pattern P_COT = Pattern.compile("\"");
	//private static final Pattern P_STR = Pattern.compile("\";\"");
	//private static final Pattern P_END = Pattern.compile("\\D");
	private static final Pattern P_PV = Pattern.compile(";");
	
	@Value("${csv.file.name}")
	String fileNameIne;
	
	@Value("${csv.file.uid}")
	String fileNameUid;
	
	Map<String, Integer> ine2niveau = new HashMap<>();
	Map<String, Integer> uid2niveau = new HashMap<>();
	
	@PostConstruct
	final public void loadFiles () {
		synchronized (this) {
			loadFileIne () ;
			loadFileUid ();
		}
	}

	
	public Integer loadFileIne ()  {
		Map<String, Integer> inLoadMap = new HashMap<>();
		
		int count = 0;
		int nbline = 1;
		log.debug("fileNameIne "+ fileNameIne);
		try (	Scanner scannerFile = new Scanner(ResourceUtils.getFile(fileNameIne)) ) {
			log.info(scannerFile.nextLine());
			while (scannerFile.hasNextLine()) {
				nbline++;
				String line = "No init";
				try ( Scanner scannerLine = new Scanner(line = scannerFile.nextLine()) ){
					scannerLine.useDelimiter(P_COT);
					log.debug("line = {}", line);
					
					String ine = scannerLine.next();
					if (ine.length() > 10) {
						scannerLine.next(); // on absorbe le ;
						int niveau = scannerLine.nextInt();
						log.debug("ine {} : niveau {}", ine , niveau);
						inLoadMap.put(ine, niveau);
						count++;
					}
				} catch (InputMismatchException e) {
					log.error("InputMismatchException :ligne {} : {}" ,nbline , line);
				} catch (NoSuchElementException e) {
					log.error("NoSuchElementException :ligne {} : {} ",nbline, line);
				}
			}
		} catch (FileNotFoundException e) {
			log.error("loadFile  filename=" + fileNameIne, e);
		}
		if (! inLoadMap.isEmpty()) {
			synchronized (this) {
				ine2niveau = inLoadMap;
			}
		}
		return count;
	}
	
	
	public Integer loadFileUid () {
		Map<String, Integer> inLoadMap = new HashMap<>();
		int count = 0;
		int nbline = 1;
		try (	Scanner scannerFile = new Scanner(ResourceUtils.getFile(fileNameUid)) ) {
			log.info(scannerFile.nextLine());
			while (scannerFile.hasNextLine()) {
				String line = "No init";
				try ( Scanner scannerLine = new Scanner(line = scannerFile.nextLine()) ){
					scannerLine.useDelimiter(P_PV);
					log.debug("line = {}", line);
					
					if (scannerLine.hasNext()) {
						String uid =  scannerLine.next();
						if (scannerLine.hasNext()) {
							int niveau = scannerLine.nextInt();
							log.debug("uid {} : niveau {}", uid , niveau);
							inLoadMap.put(uid, niveau);
							count++;
						}
					}
					
				} catch (InputMismatchException e) {
					log.error("InputMismatchException : ligne {} : {}" , nbline, line);
				} catch (NoSuchElementException e) {
					log.error("NoSuchElementException :ligne {} : {} ", nbline, line);
				}
			}
		}catch (FileNotFoundException e) {
			log.error("loadFile  filename=" + fileNameUid, e);
		}
		if (! inLoadMap.isEmpty()) {
			synchronized (this) {
				uid2niveau = inLoadMap;
			}
		}
		return count;
	}
	
	public ShibBean niveau(ShibBean rep, String ineUid,  Map<String, Integer> niveaux) {
		if (ineUid != null && ineUid != null) {
			rep.niveau = niveaux.get(ineUid);
			if (rep.niveau != null && rep.niveau > 0) {
				rep.boursier = true;
			}
		}
		return rep;
	}
	
	public ShibBean niveauByUId(ShibBean rep) {
		Map<String, Integer> niveaux;
		synchronized (this) {
			niveaux = uid2niveau;
		}
		return niveau(rep, rep.uid, niveaux);
	}
	
	public ShibBean niveauByIne(ShibBean rep) {
		Map<String, Integer> niveaux;
		synchronized (this) {
			niveaux = ine2niveau;
		}
		return niveau(rep, rep.ine, niveaux);
	}
}
