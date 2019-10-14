package alloc;

public enum EError {
	INCONNU("Compte inconnu"),
	INCOMPLET("Compte incomplet");

	private String Texte;

	private EError(String texte) {
		Texte = texte;
	}
	
}
