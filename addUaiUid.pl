#!/usr/bin/perl 

#
# prend en entré un fichier csv des niveaux de bourse agricole au format:
#	Nom;Prenom;INA;Echelon_calculé;Etablissement_support;Site_d_inscription
# 
# renvoie sur la sortie standard  un fichier compatible csv.file.uid: liste_bourses_uid.csv au format:
#	uid;niveau bourse;comment
#
#	verifier qu'il n'y a pas d'erreur sur la sortie erreur (notamment pour cause de doublons.
#	le fichier d'entré doit être un utf8 
#	

use strict;
use warnings;

use utf8;
use open qw( :encoding(utf8) :std );
use Unicode::Normalize;


use Net::LDAP;
# use Net::LDAP::Util qw( escape_dn_value , unescape_dn_value );

my $ldapHost = 'pigeon.giprecia.net';

my $ldapUsr = 'cn=synchro,ou=administrateurs,dc=esco-centre,dc=fr';

my $ldapPass;

unless ($ldapPass) {
	system "stty -echo";
    print STDERR "Ldap $ldapHost Password: ";
    chomp($ldapPass = <STDIN>);
    print "\n";
    system "stty echo";
}

my $ldap = Net::LDAP->new($ldapHost,  async => 1,
						raw => '^UTF-8$' ) or die "$@";


$ldap->debug(0);

my $mesg = $ldap->bind( $ldapUsr,
						password => $ldapPass
						);

$mesg->code && die $mesg->error;

print STDERR "connexion OK\n";

my @UAI=('0370878D','0370794M','0180585N','0280706R','0360017Y','0370781Y','0410629L','0410626H','0410018X','0450027K','0450094H');

my %toUid;

my %homonyme;

sub lireLdapEtab(){
	my $uai = shift;
	my $mesg = $ldap->search( base => "ou=people,dc=esco-centre,dc=fr",
							filter => "(&(ObjectClass=ENTEleve)(ESCOUAI=$uai))",
							attrs => [ 'uid', 'ENTEleveBoursier', 'cn']);
                            
    $mesg->code && die $mesg->error;
    
    foreach my $tuple ($mesg->entries) {
        my $cn = $tuple->get_value('cn');
        my $uid = $tuple->get_value('uid');
        my $cle = "${cn}_${uai}";
        
        if (exists $toUid{$cle}) {
            if (exists $homonyme{$cle}) {
                $homonyme{$cle} .= " $uid";
            } else {
                $homonyme{$cle} = "$toUid{$cle} $uid";
            }
        } else {
            $toUid{"$cle"} = $uid;
        }
    }
}

foreach my $uai (@UAI){
    &lireLdapEtab($uai);
}


#ATTENTION le fichier doit etre en utf8
#%CORRESPONDANCE = (
#    "EPLEFPA d'Amboise-Chambray-Lès-Tours;LPA Amboise" ,'0370878D',
#    "EPLEFPA d'Amboise-Chambray-Lès-Tours;LPA Chambray Les Tours" , '0370794M',
#    'EPLEFPA de Bourges Le Subdray;LEGTA Bourges Le Subdray' , '0180585N',
#    'EPLEFPA de Chartres;LEGTA Chartres', '0280706R',
#    'EPLEFPA de Châteauroux;LEGTA de Châteauroux', '0360017Y',
#    'EPLEFPA de Tours Fondettes;LEGTA Tours Fondettes', '0370781Y',
#    'EPLEFPA de Vendôme;Site de Blois du LEGTA Vendôme Blois Montoire', '0410629L',
#    'EPLEFPA de Vendôme;Site de Montoire du LEGTA Vendôme Blois Montoire', '0410626H',
#    'EPLEFPA de Vendôme;Site de Vendôme du LEGTA Vendôme Blois Montoire', '0410018X',
#    'EPLEFPA du Loiret;LPA Beaune La Rolande', '0450027K',
#    'EPLEFPA du Loiret;Site du Chesnoy du LEGTA Le Chesnoy Les Barres', '0450094H' 
#);
my %CORRESPONDANCE = (
    "LPA Amboise" ,'0370878D',
    "LPA Chambra" , '0370794M',
    'LEGTA Bourg' , '0180585N',
    'LEGTA Chart', '0280706R',
    'LEGTA de Ch', '0360017Y',
    'LEGTA Tours', '0370781Y',
    'Site de Blo', '0410629L',
    'Site de Mon', '0410626H',
    'Site de Ven', '0410018X',
    'LPA Beaune ', '0450027K',
    'Site du Che', '0450094H' 
);


sub normalize(){
    my $mot = shift;  
    $mot =  NFKD($mot);
    $mot =~ s/\p{NonspacingMark}//g;
    $mot =~ tr/-'/ /s;
    $mot =~ s/\s+$//s;
    $mot =~ s/\s+/ /s;
    chomp($mot);
    return uc($mot)
}

sub findUid() {
    my $cle = shift;
    my $uid = $homonyme{$cle};
    if ($uid) {
        print STDERR "homonyme $cle : $uid\n";
        return ''; 
    } 
    return $toUid{$cle}
}

my $cpt = 0;
while (<>) {
    if (m/^([^\;]+)\;([^\;]+)\;([^\;]+)\;([^\;]+)\;([^\;]+)\;((.{11}).+)$/) {
        my $etab = "$7";
        #print  $nom,  "\n";
        my $nom=$1;
        my $prenom=$2;
        my $ina = $3;
        my $niveau = $4;
        
        my $uai = $CORRESPONDANCE{$etab};
        if ($uai) {
            my $nomN = &normalize( $nom );
            my $prenomN = &normalize( $prenom );
            my $cle = "$nomN ${prenomN}_$uai";
            my $uid = &findUid($cle);
            if ($uid) {
                print "$uid;$niveau;$uai\n";
            } else {
                print STDERR "$nom $prenom $etab => $cle introuvable\n";
            }
        } else {
            if ($cpt) {
                print STDERR "erreur ligne $cpt\n";
            } else {
				# on reecrit la 1er ligne 
                print "uid;niveau bourse;comment\n";
            }
        }
    }
}
