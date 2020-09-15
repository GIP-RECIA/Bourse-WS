#!/usr/bin/perl

my $curl_format = 'curl -k  https://%s:8443/niveau-bourse/loaddata';


my @PORTAIL= ('ara.giprecia.net', 'cacatoes.giprecia.net');




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


my $nbOk= 0;

# demande  le reload des données
foreach my $portail ( @PORTAIL ) {
        my $nbBoursierCharge = &curl($portail);
}



