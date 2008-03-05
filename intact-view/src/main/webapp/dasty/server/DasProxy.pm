#!/usr/bin/perl

package DasProxy;

# DAS proxy. Proxies requests to DAS Registry and DAS servers. Query parameters:
#   s   Server URL
#   m   Method (one of: sequence, features, types, stylesheet, registry)
#   q   DAS segment (only required for sequence and features methods)
#   t   HTTP timeout (default is 5 seconds)
# Returns HTTP and DAS errors in <exception> elements.
#
# Author    Antony Quinn <aquinn@ebi.ac.uk>
# Version   $Id: DasProxy.pm,v 1.3 2007/06/21 13:27:45 aquinn Exp $

use strict;
use CGI qw/:standard/;

# CGI params
my ($query) = new CGI;

# Default proxy server
# my $DEFAULT_PROXY = 'http://wwwcache.ebi.ac.uk:3128/';
my $DEFAULT_PROXY = '';
my $CACHE_PATH='../htdocs/dasty2/server/pdb/';
# my $CACHE_PATH='../htdocs/server/pdb/';

# DAS error codes
# See http://biodas.org/documents/spec.html#response
my %dasErrorCodes = (
		       200=>'OK, data follows',
		       400=>'Bad command',
		       401=>'Bad data source',
		       402=>'Bad command arguments',
		       403=>'Bad reference object',
		       404=>'Bad stylesheet',
		       405=>'Coordinate error',
		       500=>'Server error',
		       501=>'Unimplemented feature',
		      );

# Get URL of network proxy server, if required
# See http://cpan.uwinnipeg.ca/htdocs/libwww-perl/LWP/UserAgent.html#ua_gt_env_proxy
sub getProxyServer()    {
    my $proxy = '';
    if (defined $ENV{'CGI_HTTP_PROXY'}) {
        $proxy = $ENV{'CGI_HTTP_PROXY'};
    }
    else    {
        # Use default
        $proxy = $DEFAULT_PROXY;
    }
    return $proxy;
}

# Returns response from DAS server or registry, or exception if error.
sub getResponse        {
    # 'use' statements here because clashed at top level for some reason
    use LWP;
    use HTTP::Request::Common;
    my ($url, $timeout) = @_;
    my $ua = LWP::UserAgent->new;
    my $proxy = getProxyServer();
    $ua->timeout($timeout);
    if (not($proxy eq ""))	{
        $ua->proxy(['http','https'], $proxy);
        $ua->no_proxy('localhost');
    }
    my $method = $query->param('m');
    my $id     = $query->param('q');
    
    if ($method eq "pdb"){
    	if (open ("PDBFILE", $CACHE_PATH.$id)){
    		return "<response>ok</response>";
    	}
    }
    my $response = $ua->request(GET $url);

    if ($response->is_success) {
    	if ($method eq "pdb"){
    		open ("PDBFILE2",">> ".$CACHE_PATH.$id) or die "$CACHE_PATH$id cannot be opened.";
    		print PDBFILE2 $response->content;
    		return "<response>ok</response>";
    	}
    	
        my $headers = $response->headers;
        if (defined $headers->header("X-Das-Status"))   {
            my $status = $headers->header("X-Das-Status");
            if ($status =~ m/200/)   {
                # OK
                return $response->content;
            }
            else    {
                # DAS error
                DasProxy::throwException("$status $dasErrorCodes{$status}")
            }
        }
        else    {
            # No X-Das-Status defined, so assume DAS registry response
            return $response->content;
        }
    }
    else        {
        # HTTP error
        DasProxy::throwException($response->message." [$url]");
    }
}

# Get HTTP timeout
sub getTimeout()    {
    # Get query parameters
    my $timeout = 5;
    if (defined $query->param('t')) {
        $timeout = $query->param('t');
    }
    else  {
        # Command line (useful for testing)
        if (defined $ARGV[3]) {
            $timeout = $ARGV[3];
        }
    }
    return $timeout;
}

# Get URL of DAS server or DAS registry
sub getUrl()    {

    # Get query parameters
    my $server = "";
    my $method = "";
    my $id     = "";
    my $reg_authority	= "";
    my $reg_label		= "";
    my $reg_type		= "";
    
    if (defined $query->param('s')) {
        $server = $query->param('s');
        $method = $query->param('m');
        $id     = $query->param('q');
        $reg_authority	= $query->param('a');
        $reg_label		= $query->param('l');
        $reg_type		= $query->param('c');
    }
    # Command line (useful for testing)
    elsif (defined $ARGV[0])   {
        $server = $ARGV[0];
        $method = $ARGV[1];
        $id     = $ARGV[2];
        $reg_authority	=  $ARGV[3];
        $reg_label		= $ARGV[4];
        $reg_type		= $ARGV[5];
    }

    # Check
    DasProxy::checkParam("server", $server);
    DasProxy::checkParam("method", $method);

    # Check server has trailing slash
    my $last_char = substr($server,-1, 1);
    if (!($last_char eq "/"))   {
        $server .= "/";
    }

    # Build URL
    my $url = "";
    if ($method eq "sequence" or $method eq "features")	{
       #DasProxy::checkParam("id", $id);
       $url = "$server$method?segment=$id";
    }
    # PDB URL example: http://www.pdb.org/pdb/files/1aal.pdb
    elsif ($method eq "pdb")	{
       #DasProxy::checkParam("id", $id);
       $url = "$server$id";
    }
    elsif ($method eq "alignment")	{
       #DasProxy::checkParam("id", $id);
       $url = "$server$method?query=$id";
    }
    elsif ($method eq "ontology")	{
       $url = "$server$id";
    }       
    elsif ($method eq "types" or $method eq "stylesheet")	{
       $url = "$server$method";
    }
    elsif ($method eq "registry")	{
    	$url = $server;
    	if (not($reg_authority eq ""))	{
    		#DasProxy::checkParam("authority", $reg_authority);
    		$url .= "?authority=$reg_authority";
    		} 
    	if (not($reg_label eq ""))	{
    		#DasProxy::checkParam("label", $reg_label);
    		if (not($reg_authority eq ""))	{
    			$url .= "&label=$reg_label";
    			}
    		else	{
    			$url .= "?label=$reg_label";
    			}
    		}
    	if (not($reg_type eq ""))	{
    		#DasProxy::checkParam("type", $reg_type);
    		if (not($reg_authority eq "") or not($reg_label eq ""))	{
    			$url .= "&type=$reg_type";
    			}
    		else	{
    			$url .= "?type=$reg_type";
    			}
    		}    		
    		
    }
    else	{
        DasProxy::throwException("Method not allowed: $method");
    }

    return $url;

}

# Check parameter with given name (key) has a value other than ""
sub checkParam()    {
    my ($key, $value) = @_;
    if (!(defined $value) or ($value eq ""))	{
        DasProxy::throwException("'" .$key. "' parameter not specified.");
    }
}


# Print message in <exception> tags and exit
sub throwException() {
    my ($message) = @_;
    print "<exception>$message</exception>\n";
    exit();
}

# The '1' is required at the end of Perl modules
1;
