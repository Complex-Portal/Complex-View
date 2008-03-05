#!/usr/bin/perl

# DAS proxy. Proxies requests to DAS Registry and DAS servers. Query parameters:
#   s   Server URL
#   m   Method (one of: sequence, features, types, stylesheet, registry)
#   q   DAS segment (only required for sequence and features methods)
#   t   HTTP timeout (default is 5 seconds)
# Can also be tested from command line:
#   perl proxy.cgi <server> <method> <id> <timeout>
# Returns HTTP and DAS errors in <exception> elements.
#
# Author    Antony Quinn <aquinn@ebi.ac.uk>
# Version   $Id$

use strict;
use CGI qw/:standard/;

# See DasProxy.pm
use DasProxy;

# All responses, including error messages, must be valid XML
print ("Content-type: text/xml\n\n");
my $url     = DasProxy::getUrl();
my $timeout = DasProxy::getTimeout();
print DasProxy::getResponse($url, $timeout);
