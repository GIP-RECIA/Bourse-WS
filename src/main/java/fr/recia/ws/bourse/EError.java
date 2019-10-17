package fr.recia.ws.bourse;

import com.fasterxml.jackson.annotation.JsonValue;


public enum EError {
	INCONNU("Compte inconnu"),
	INCOMPLET("Compte incomplet"), 
	INVALIDE("Clé invalide");

	@JsonValue
	private String texte;

	private EError(String texte) {
		this.texte = texte;
	}
}
