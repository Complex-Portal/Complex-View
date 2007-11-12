#!/usr/bin/perl -w
# SOAP::Lite version 0.67
# Please note: IntAct webservices uses document/literal binding

#use SOAP::Lite + trace => qw(debug);
use SOAP::Lite;


# Query
my $myQuery = 'brca2';

# Setup service
#my $WSDL = 'http://localhost:7877/intact/ws/binarysearch?wsdl';
my $WSDL = 'http://www.ebi.ac.uk/intact/binary-search-ws/binarysearch?wsdl';
my $nameSpace = 'http://ebi.ac.uk/intact/binarysearch/wsclient/generated';

my $soap = SOAP::Lite
-> uri($nameSpace)
-> proxy($WSDL);


# Setup method and parameters
    my $method = SOAP::Data->name('findBinaryInteractions') 
                           ->attr({xmlns => $nameSpace});

    my @params = ( SOAP::Data->name(query => $myQuery));

    # Call method
    my $result = $soap->call($method => @params);


# if no error
unless ($result->fault) {

    # Retrieve for example all ChEBI identifiers for the ontology parents
    @stuff = $result->valueof('//interactionLines');
    print @stuff;

} else {
   # some error handling
  print join ', ',
    $result->faultcode,
    $result->faultstring,
    $result->faultdetail;
  print "\n";
}
