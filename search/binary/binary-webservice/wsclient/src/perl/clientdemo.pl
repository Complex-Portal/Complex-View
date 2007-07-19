#!/usr/bin/perl -w
# SOAP::Lite version 0.67
# Please note: IntAct webservices uses document/literal binding

use SOAP::Lite + trace => qw(debug);
#use SOAP::Lite;

# Setup service
my $WSDL = 'http://localhost:9090/intact/ws-1.6.0-beta-2/binarysearch?wsdl';
#my $WSDL = 'http://www.ebi.ac.uk/intact/binarysearch-ws/binarysearch?wsdl';
my $nameSpace = 'http://ebi.ac.uk/intact/binarysearch/wsclient/generated';
my $soap = SOAP::Lite
-> uri($nameSpace)
-> proxy($WSDL);

# Setup method and parameters
    my $method = SOAP::Data->name('findBinaryInteractions')
    ->attr({xmlns => $nameSpace});

    my @params = ( SOAP::Data->name(query => 'P12345'));

    # Call method
    my $result = $soap->call($method => @params);

# if no error
unless ($result->fault) {

    # Retrieve for example all ChEBI identifiers for the ontology parents
    #@stuff = $result->valueof('//OntologyParents//chebiId');
    #print @stuff;

} else {
   # some error handling
  print join ', ',
    $result->faultcode,
    $result->faultstring,
    $result->faultdetail;
  print "\n";
}