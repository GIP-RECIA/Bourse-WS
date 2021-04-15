#!/usr/bin/perl

use IPC::Open2;


my @PORTAIL= qw(
				ara.giprecia.net 
				portail2.giprecia.net
				portail3.giprecia.net
				autour.giprecia.net
				portail6.giprecia.net
				butor.giprecia.net
				portail9.giprecia.net
				portail10.giprecia.net
				portail11.giprecia.net
				portail12.giprecia.net
			);

my $adr_ftp='rca_masterent@pinson.giprecia.net';

my $rep_ftp = '/srv/catelaaf/exports/BOURSES-LYCEE/';

my $curl_format = 'curl -k  https://%s:8443/niveau-bourse/loaddata';

my $prefixeFile = 'liste_boursiers';

my $fileName = "$prefixeFile.csv";

my $tmpFile = "/tmp/$fileName";


my $rep_conf_portail = '/opt/uportal-properties/prod/niveau-bourse/';

my @listFile;

my $nbFtpFileToKeep = 2;

#la commande sftp
my $sftp = "/usr/bin/sftp -b- $adr_ftp";

my $arg = shift;
my $reloadOnly;
if ($arg =~ /^(RE)?LOAD$/) {
	$reloadOnly = $1;
} else {
	die "manque ou mauvais d'argument : LOAD | RELOAD";
}

print $reloadOnly, "load\n";



#la commande scp
sub scp(){
	my $addr = shift;
	
	my $com = "scp $tmpFile ${addr}:$rep_conf_portail";
	&printLog("$com");
	system "$com" || die "scp error: $!";
}

#la commande curl
sub curl(){
	my $addr = shift;
	
	my $result = -1;
	
	my $com = sprintf ($curl_format, $addr);
	&printLog("$com");
	open COM, "$com 2> /tmp/curl.error && echo  |" or die $!;
	$line = <COM>;
	print $line, "\n";
	if ($line =~ /(\d+),(\d+)/){
		$result = $1;
	}
	close COM;
	open ERROR, "/tmp/curl.error";
	while (<ERROR>) {
		print ;
	}
	close ERROR;
	&printLog( "$result boursiers ont été chargés");
	return $result;
}


# print daté 
sub printLog(){
 
	my @localTime = localtime time;
	my      $horodatage = sprintf(
										'%d/%.2d/%.2d %.2d:%.2d:%.2d',
										(1900 + $localTime[5])  ,
										$localTime[4]+1,
										$localTime[3],
										$localTime[2],
										$localTime[1],
										$localTime[0]
								);

	print $horodatage, "\t" , @_, "\n";
}

#Le controle de la taille du fichier copier:
sub testSize(){
	my $addr = shift;
	my $sizeOk = shift;
	
	my $com = "ssh $addr 'ls -l ${rep_conf_portail}${fileName}'";
	open COM, "$com |" or die $!;
	if ( &size(<COM>) != $sizeOk ){
		die "Erreur de copie sur $addr!\n";
	}
	&printLog("copie Ok");
	close COM;
}

# recuperation de la taille d'un fichier a partir d'une ligne donnée par ls -l
sub size(){
	my $line = shift;
	my @col = split ('\s+', $line);
	
	return $col[4];
}

# verification du fichier dans tmp renvoie le nombre de ligne de boursier; 
sub verifFile(){
	open FILE, "$tmpFile" or die $!;
	
	my $cpt = 0;
	$_ = <FILE>;
	die "ERREUR d'entête : $_" unless /^\"ine\";\"echelon\"/;
	while (<FILE>) {
		chomp;
		if ($_) {
			my @col = split('";"', $_);
			$cpt ++;
			#	print "$_>" . $col[0]. "<>" . $col[1] . "<\n";
			die "ERREUR mauvais format de ligne ($cpt) : $_; \n " unless $col[1] =~ /^\d+\"/;
		}
	}
	close FILE;
	
	return $cpt;
}
my $nbBoursierACharger ;

unless ($reloadOnly) {
	#on ouvre la connexion sftp
	&printLog("$sftp");
	open2 (READ, WRITE, $sftp ) or die "erreur connexion sftp: $!\n";

	&printLog("\t connexion ok");

	#on recupere une ligne vide pour le prompt
	print WRITE "\n";
	my $prompt=<READ>;
	chop $prompt;


	#on change de repertoire distant
	print WRITE "cd $rep_ftp \n";

	$_=<READ>;

	# on recupere la liste des fichiers
	print WRITE "ls -l ${prefixeFile}_*.csv\n\n";

	$_=<READ>;
	&printLog($_);

	while (<READ>) {
		last if /^$prompt$/;
		print($_);
		if (/(${prefixeFile}_\d{8}\.csv)$/) {
			push @listFile, $1;
			$sizeFile{$1} = &size($_);
		}
	}

	# on determine le dernier fichier 
	@listFile = sort @listFile;
	 
	my $lastFile  = $listFile[-1];
	my $lastSize = $sizeFile{$lastFile};
	&printLog("last : $lastSize $lastFile");

	# on le recupère dans /tmp
	if ($lastFile){
		print WRITE "get $lastFile $tmpFile\n\n";
		while (<READ>) {
			last if /^$prompt$/;
			&printLog($_);
		}
	}

	# on ajoute une ligne avec un compte de test;
	#open TMP, ">> $tmpFile" or die $!;
	#print TMP '"11995";"760495646YZ";"3";"2019-11-13 15:09:08";NULL',"\n";
	#$lastSize += 53;
	#close TMP;

	# on recupere le nombre de lignes du fichier
	$nbBoursierACharger = &verifFile();

	&printLog("$tmpFile ok : $nbBoursierACharger boursiers");



	# copie sur les portails

	foreach my $portail ( @PORTAIL ) {
		&scp($portail);
		&testSize($portail, $lastSize);
	}
}

my $nbOk= 0;

# demande  le reload des données
foreach my $portail ( @PORTAIL ) {
	my $nbBoursierCharge = &curl($portail);
	unless ($reloadOnly ) {
		if ($nbBoursierCharge == $nbBoursierACharger) {
			&printLog("Chargement terminé sans erreur.\n");
			$nbOk++;
		}  else {
			&printLog("ERREUR de chargement il devrait avoir $nbBoursierACharger chargement et non pas $nbBoursierCharge");
		}
	}
}

unless ($reloadOnly) {
	if ($nbOk == @PORTAIL) {
		# nettoyage du sftp:
			if (@listFile > $nbFtpFileToKeep) {
				for (my $cpt = 0 ; $cpt < @listFile - $nbFtpFileToKeep; $cpt++){
					print WRITE "rm $listFile[$cpt]\n\n";
					while (<READ>) {
						last if /^$prompt$/;
						&printLog($_);
					}
				}
			}
		
	} else {
		&printLog("ERROR : $nbOk portail chargé sur " . @PORTAIL ); 
	}
}

