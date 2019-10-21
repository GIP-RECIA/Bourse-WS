#!/usr/bin/perl

use IPC::Open2;


my $adr_ftp='rca_masterent@pinson.giprecia.net';

my $rep_ftp = '/srv/catelaaf/exports/BOURSES-LYCEE/';

my $curl_format = 'curl -k  https://%s:8443/niveau-bourse/loaddata';

my $prefixeFile = 'liste_boursiers';

my $fileName = "$prefixeFile.csv";

my $tmpFile = "/tmp/$fileName";

my @PORTAIL= ('ara.giprecia.net', 'cacatoes.giprecia.net');

my $rep_conf_portail = '/opt/uportal-properties/prod/niveau-bourse/';

my @listFile;

my $nbFtpFileToKeep = 2;

#la commande sftp
my $sftp = "/usr/bin/sftp -b- $adr_ftp";

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
	open COM, "$com 2> /tmp/curl.error |" or die $!;
	$line = <COM>;
	print $line, "\n";
	if ($line =~ /(\d+)/){
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
	die "ERREUR d'entête : $_" unless /^\"id\";\"ine\";\"echelon\"/;
	while (<FILE>) {
		chomp;
		if ($_) {
			my @col = split('";"', $_);
			$cpt ++;
			die "ERREUR mauvais format de ligne ($cpt) : $_\n " unless $col[2] =~ /^\d+$/;
		}
	}
	close FILE;
	
	return $cpt;
}


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

# on recupere le nombre de lignes du fichier
my $nbBoursierACharger = &verifFile();

&printLog("$tmpFile ok : $nbBoursierACharger boursiers");



# copie sur les portails

foreach my $portail ( @PORTAIL ) {
	&scp($portail);
	&testSize($portail, $lastSize);
}


my $nbOk= 0;

# demande  le reload des données
foreach my $portail ( @PORTAIL ) {
	my $nbBoursierCharge = &curl($portail);
	if ($nbBoursierCharge == $nbBoursierACharger) {
		&printLog("Chargement terminé sans erreur.\n");
		$nbOk++;
	}  else {
		&printLog("ERREUR de chargement il devrait avoir $nbBoursierACharger chargement et non pas $nbBoursierCharge");
	}
}

# nettoyage du sftp:
if ($nbOk == @PORTAIL) {
	if (@listFile > $nbFtpFileToKeep) {
		for (my $cpt = 0 ; $cpt < @listFile - $nbFtpFileToKeep; $cpt++){
			print WRITE "rm $listFile[$cpt]\n";
			&printLog(<READ>);
			while (<READ>) {
				last if /^$prompt$/;
				print ;
			}
		}
	}
} else {
	&printLog("ERROR : $nbOk portail chargé"); 
}

