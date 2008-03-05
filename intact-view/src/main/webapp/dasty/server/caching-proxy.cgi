#!/ebi/extserv/bin/perl/bin/perl -w

# Caching DAS proxy. Proxies requests to DAS Registry and DAS servers, storing
# result in temporary local file cache.
# Query parameters:
#   s   Server URL
#   m   Method (one of: sequence, features, types, stylesheet, registry)
#   q   DAS segment (only required for sequence and features methods)
#   t   HTTP timeout (default is 5 seconds)
# Can also be tested from command line:
#   perl caching-proxy.cgi <server> <method> <id> <timeout>
# Returns HTTP and DAS errors in <exception> elements.
#
# Author    Antony Quinn <aquinn@ebi.ac.uk>
# Version   $Id$

use strict;
use CGI qw/:standard/;
use Cache::SizeAwareFileCache;

# See DasProxy.pm
use DasProxy;

# Cache settings
my $CACHE_NAMESPACE = "uk.ac.ebi.das.proxy"; # Store cache under /tmp/FileCache/uk.ac.ebi.das.proxy
my $CACHE_EXPIRES   = "60 minutes";          # Cache content for 1 hour
my $CACHE_PURGE_INTERVAL = "24 hours";	     # Purge cache every day
my $CACHE_MAX_SIZE  = 100 * 1000000; 	     # Do not cache more than 100MB of data

# All responses, including error messages, must be valid XML
print ("Content-type: text/xml\n\n");
my $url     = DasProxy::getUrl();
my $timeout = DasProxy::getTimeout();
my $cache = new Cache::SizeAwareFileCache({'namespace' => $CACHE_NAMESPACE,
                                           'auto_purge_interval' => $CACHE_PURGE_INTERVAL,
                                           'max_size' => $CACHE_MAX_SIZE});
my $response = $cache->get($url);
if (not defined $response)	{
    $response = DasProxy::getResponse($url, $timeout);
    $cache->set($url, $response, $CACHE_EXPIRES);
}
print $response;
