package alloc;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class CsvReader {
	private static final Logger log = LoggerFactory.getLogger(AllocController.class);
	
	private static final Pattern P_STR = Pattern.compile(",");
	private static final Pattern P_END = Pattern.compile("\\D");
	
	@Value("${csv.file.name}")
	String fileName;
	
	Map<String, Integer> ine2niveau = new HashMap<>();
	
	public Integer loadFile ()  {
		int count = 0;
		try (	Scanner scannerFile = new Scanner(ResourceUtils.getFile(fileName)) ) {
			log.warn(scannerFile.nextLine());
			while (scannerFile.hasNextLine()) {
				String line = "No init";
				try ( Scanner scannerLine = new Scanner(line = scannerFile.nextLine()) ){
					scannerLine.useDelimiter(P_STR);
					if (scannerLine.hasNext()) {
						String ine = scannerLine.next();
						scannerLine.useDelimiter(P_END);
						int niveau = scannerLine.nextInt();
						log.info("ine {} : niveau {}", ine , niveau);
						ine2niveau.put(ine, niveau);
						count++;
					} 
				} catch (InputMismatchException e) {
					log.error("InputMismatchException : {}" , line);
				}
			}
		} catch (FileNotFoundException e) {
			log.error("loadFile  filename=" + fileName, e);
		}
		return count;
	}
	
	
	
	public ShibBean niveau(ShibBean rep) {
		if (rep != null && rep.ine != null) {
			rep.niveau = ine2niveau.get(rep.ine);
			rep.boursier = rep.niveau != null && rep.niveau > 0;
		}
		return rep;
	}
}
