/**
* Bs_XmlParser
* Parse XML
*
*
* @author     Sam Blum <sam at blueshoes dot org>
* @copyright  blueshoes.org
* @version    4.0.$Id: Bs_XmlParser.class.js,v 1.1.1.1 2003/12/31 23:19:56 andrej Exp $
* @package    javascript_core
* @subpackage util
* @access     public
*/
function Bs_XmlParser () {
  
  /**
  * @access private
  * @var    ? _index
  */
  this._index;
  
  /**
  * oh well. if this is private, and there is no setter, how can this be set? --andrej
  * @access private
  * @var    bool _debugOn
  * @see    var this._debug
  */
  this._debugOn = false;
  
  /**
  * array with debug strings.
  * @access private
  * @var    array _debug
  * @see    var this._debugOn
  */
  this._debug = new Array();
  
  /**
  * @access private
  * @var    string _stackStr
  * @see    _expandFromStack()
  * @since  bs-4.5
  */
  this._stackStr = '';
  
  /**
  * @access private
  * @var    int _stackPos
  * @see    _expandFromStack()
  * @since  bs-4.5
  */
  this._stackPos = 0;
  
  /**
  * pure optimization. because string handling in javascript is so slow.
  * @author andrej arn
  * @access private
  * @param  object xmlFragment (instance of _Bs_XmlParserStrFragment)
  * @return object (instance of _Bs_XmlParserStrFragment)
  * @see    vars _stackStr, _stackPos
  * @since  bs-4.5
  */
  this._expandFromStack = function(xmlFragment) {
    if (this._stackPos < this._stackStr.length) {
      var numChars = 1000;
      xmlFragment.str += this._stackStr.substr(this._stackPos, numChars);
      this._stackPos += numChars;
    }
    return xmlFragment;
  }
  
  /**
  * @access public
  * @param  string xmlInput
  * @return ?
  */
  this.parse = function(xmlInput) {
    // init
    this._index = new Array();
    
  	// Get rid of those MS and MAC carrige returns and replace them whith 'standard' \n
  	var xml = xmlInput.replace(/\r(\n)?/g,"\n");
    
    var xmlFragment = new _Bs_XmlParserStrFragment();
    this._stackStr  = this._stripXmlHeader(xml);
    this._stackPos  = 2000;
    xmlFragment.str = this._stackStr.substr(0, 2000);
  	
    // alert(xmlFragment.str);
  	// main recursive function to process the xml
  	xmlFragment = this._parseRecursive(xmlFragment);
  
    // create a root element to contain the document
  	this.root = new _Bs_XmlParserElement();
  	this.root.name = 'ROOT';
  
  	// all done, lets return the root element + index + document
  	this.root.children = xmlFragment.list;
    this.root.index = this._index;
  	return this.root;
  }
  
  
  /**
  * Strip the XML header <?xml ...?> and <!DOCTYPE  ... ]>
  * @access private
  * @param  ? xml
  * @return ?
  */
  this._stripXmlHeader = function(xml) {
  	// Get rid of starting <?xml ... ?> 
  	var start_p = -1; 
  	var end_p = -1; 
    start_p = xml.indexOf("<");
  	if('<?x' == xml.substring(start_p, start_p +3).toLowerCase()) {
  		end_p = xml.indexOf("?>");
  		xml = xml.substring(end_p +2, xml.length);
  	}
  	// Get rid of starting  <!DOCTYPE  ... ]>
  	start_p = xml.indexOf("<!DOCTYPE");
  	if(start_p != -1) {
  		end_p = xml.indexOf(">", start_p) +1;
  		var dp = xml.indexOf("[", start_p);
  		if(dp < end_p && dp != -1) {
  			end_p = xml.indexOf("]>", start_p) +2;
  		}
  		xml = xml.substring(end_p, xml.length);
  	}
    return this._trim(xml);
  }

  /**
  * does the main job. calls itself recursively.
  * @access private
  * @param  object xmlFragment (instance of _Bs_XmlParserStrFragment)
  * @return ?
  */
  this._parseRecursive = function(xmlFragment) {
    var regExStartTag = new RegExp("^\\s*<", "i"); 
    
  	do { // Keep looping reducing the sting to ''
  		if (this._debugOn) {
        this._debug[this._debug.length] = '<hr>' + this._entity(xmlFragment.str);
      }
      
      //we want at least 3 "<" tags, one ">" after the 3 "<" tags in the string, 
      //#and a string length of at least 100 chars.
      /*
      if (xmlFragment.str.length < 100) {
        xmlFragment = this._expandFromStack(xmlFragment);
      }*/
      var lastFoundPos = 0;
      var lastPos      = 0;
      for (var i=0; i<3; i++) {
        //if (lastPos < 0) lastPos = 100;
        lastPos = xmlFragment.str.indexOf("<", lastFoundPos);
        if (lastPos == -1) {
          xmlFragment = this._expandFromStack(xmlFragment);
          i--;
        } else {
          lastFoundPos = lastPos;
        }
      }
      for (var i=0; i>=0; i++) { //pseudo-endless
        lastPos = xmlFragment.str.indexOf(">", lastFoundPos);
        if (lastPos == -1) {
          //alert(xmlFragment.str);
          //alert('expand');
          var lastLength = xmlFragment.str.length;
          xmlFragment = this._expandFromStack(xmlFragment);
          //alert(xmlFragment.str);
          if (lastLength >= xmlFragment.str.length) {
            //end, give up.
            break;
          }
        } else {
          break;
        }
      }
      
  		// Look for next '<'
      //old code:
      //xmlFragment.str = this._trimL(xmlFragment.str);
      //var start_p = xmlFragment.str.indexOf("<");
      //new code:
      //i prefer the regexp because we can safe the trimL, which safes us 
      //copying the whole str on each loop. with large strings that really 
      //makes a difference. so it's only an optimization. 
      //2003-09-15 --andrej
      var start_p = xmlFragment.str.search(regExStartTag);
  	  //dump(start_p);
      
      //if (start_p == 0) { //old code
      if (start_p != -1) { //new code
  			// determine what the next section is, and process it
  			if (xmlFragment.str.substring(start_p+1,2) == "?") {
  				xmlFragment = this._tag_pi(xmlFragment);
  			} else if (xmlFragment.str.substring(start_p+1,4) == "!--") {
  			  xmlFragment = this._tag_comment(xmlFragment);
  			} else if (xmlFragment.str.substring(start_p+1,9) == "![CDATA[") {
  			  xmlFragment = this._tag_cdata(xmlFragment);
  		  }	else {
          var regEx = new RegExp("^\\s*</"+xmlFragment.end+"\\s*>", "ig"); 
          var result = xmlFragment.str.match(regEx);
      		
          if (this._debugOn) {
            var strResult = (null != result) ?  "Found:"+ this._entity(result[0]) : '[Not Found]';
    		    this._debug[this._debug.length] = "<hr><b>94: Looking for " + this._entity('^\\s*</'+xmlFragment.end+'\\s*>')  +" Result is " + strResult + '</b><br>';
          }
          if (null != result) {
            // found the end of the current tag, end the recursive process and return
            // alert(result[0]); alert(xmlFragment.str);
					  xmlFragment.str = xmlFragment.str.substring(result[0].length);
            // alert(xmlFragment.str);
					  xmlFragment.end = "";
            return xmlFragment;
				  }	else {
            // Next element found. Recurse into next element
					  xmlFragment = this._tag_element(xmlFragment);
				  }
  			}
      } else {
  			// Found char data before start of next xml-tag  -- or no data at all. (?)
        var start_p = xmlFragment.str.indexOf("<");
        
        var tmpObj = new _Bs_XmlParserElement();
        tmpObj.type = 'chardata';
  			
        if (start_p == -1) {
  				tmpObj.value = this._trimL(xmlFragment.str);
  				xmlFragment.str = "";
  			} else {
  				tmpObj.value = this._trimL(xmlFragment.str.substring(0,start_p));
  				xmlFragment.str = xmlFragment.str.substring(start_p);
  			}
        
        xmlFragment.list[xmlFragment.list.length] = tmpObj;
        this._index[this._index.length] = tmpObj;
      }
      
      if (xmlFragment.str.length == 0) {
        var lastLength = xmlFragment.str.length;
        xmlFragment = this._expandFromStack(xmlFragment);
        if (lastLength >= xmlFragment.str.length) {
          //end
          break;
        }
      }
  	} while (true);
    return xmlFragment;
  }
  
  /**
  * XML-Tag (Standard) 
  * @access private
  * @param  object xmlFragment (instance of _Bs_XmlParserStrFragment)
  * @return object (instance of _Bs_XmlParserStrFragment)
  */
  this._tag_element = function(xmlFragment) {
  	var endMatch = ">";
    var end_p = xmlFragment.str.indexOf(endMatch);
  	var isShortTag = (xmlFragment.str.substring(end_p-1,end_p) == "/");

    var xmlTag = '';
  	if (isShortTag) {
  		xmlTag = this._normalize(xmlFragment.str.substring(1, end_p-1));
  	} else {
  		xmlTag = this._normalize(xmlFragment.str.substring(1, end_p));
    }
    
    // Split up into name and attributes
    var parts = xmlTag.match(/(\w+)(.*)/);
    
    var tmpObj = new _Bs_XmlParserElement();
    tmpObj.type = 'element';
    tmpObj.name = parts[1].toLowerCase();
    tmpObj.attributes = this._extractAttributes(parts[2]);
    var currentPos = xmlFragment.list.length;
    xmlFragment.list[currentPos] = tmpObj;
    this._index[this._index.length] = tmpObj;
  	
    // Catch some html short-tags that are not xhtml standard like <img ....>
    if (!isShortTag) {
      // There must be more
      //yes, for example the <script> tag CAN be one of these, but does not have to be.
      switch (tmpObj.name.toLowerCase()) {
        case 'br':
        case 'img':  
        case 'hr':
        case 'link':
        case 'meta':
         isShortTag = true;
         break;
      }
    }
    
    if (isShortTag) {
  		xmlFragment.str = xmlFragment.str.substring(end_p+1);
  	} else {
  		// Parse the content too
  		var nextFragment = new _Bs_XmlParserStrFragment();
  		if (this._debugOn) {
        this._debug[this._debug.length] = "<hr><b>160:Processing:"+ tmpObj.name + '</b>';
      }
      nextFragment.str = xmlFragment.str.substring(end_p+1);
  		nextFragment.end = tmpObj.name;
  		nextFragment = this._parseRecursive(nextFragment);
  		xmlFragment.list[currentPos].children = nextFragment.list;
  		xmlFragment.str = nextFragment.str;
  	}
  	return xmlFragment;
  }
  
  /**
  * XML-Tag Comment 
  * @access private
  * @param  object xmlFragment (instance of _Bs_XmlParserStrFragment)
  * @return object (instance of _Bs_XmlParserStrFragment)
  */
  this._tag_comment = function(xmlFragment) {
  	var endMatch = "-->";
    var end_p = xmlFragment.str.indexOf(endMatch);
    var tmpObj = new _Bs_XmlParserElement();
    tmpObj.type = 'comment';
    tmpObj.value = xmlFragment.str.substring(4, end_p); // <!--
    xmlFragment.list[xmlFragment.list.length] = tmpObj;
    this._index[this._index.length] = tmpObj;

  	xmlFragment.str = xmlFragment.str.substring(end_p + endMatch.length);
  	return xmlFragment;
  }
  
  /**
  * XML-Tag PI 
  * @access private
  * @param  object xmlFragment (instance of _Bs_XmlParserStrFragment)
  * @return object (instance of _Bs_XmlParserStrFragment)
  */
  this._tag_pi = function(xmlFragment) {
  	var endMatch = "?>";
    var end_p = xmlFragment.str.indexOf(endMatch);
    var tmpObj = new _Bs_XmlParserElement();
    tmpObj.type = 'pi';
    tmpObj.value = xmlFragment.str.substring(2, end_p); // <? 
    this._index[this._index.length] = tmpObj;
    
    xmlFragment.list[xmlFragment.list.length] = tmpObj;
  	xmlFragment.str = xmlFragment.str.substring(end_p + endMatch.length);
  	return xmlFragment;
  }

  /**
  * XML-Tag CDATA 
  * @access private
  * @param  object xmlFragment (instance of _Bs_XmlParserStrFragment)
  * @return object (instance of _Bs_XmlParserStrFragment)
  */
  this._tag_cdata = function(xmlFragment) {
  	var endMatch = "]]>";
    var end_p = xmlFragment.str.indexOf(endMatch);
    var tmpObj = new _Bs_XmlParserElement();
    tmpObj.type = 'chardata';
    tmpObj.value = xmlFragment.str.substring(9, end_p); // <!CDATA[[
    xmlFragment.list[xmlFragment.list.length] = tmpObj;
    this._index[this._index.length] = tmpObj;
  	
    xmlFragment.str = xmlFragment.str.substring(end_p + endMatch.length);
  	return xmlFragment;
  }
  
  /**
  * Attribute parsing
  * Sample:
  *   input:  name = "peter"  works = "Jos\'s Pizza Land"   active 
  *   out: object['name']   = peter;            // string
  *        object['works']  = Jos's Pizza Land; // string
  *        object['active'] = true;             // boolean
  *
  * @access private
  * @param sting The XML-tag content WITHOUT tag and ending '>'
  * @return object as attr-name = value OR empty Object if nothing found.
  */
  this._extractAttributes = function(str) {
    var tmp = '';
    var retObj = new Object();
    var attrStr = this._trim(str);
    if (0 == attrStr.length) return retObj;
    
    attrStr = attrStr.replace(/\s*=\s*/g, '=');
    attrStr = attrStr.replace(/\=(')[^']*\1/g, this._spaceReplacer); // second param is a function call !
    attrStr = attrStr.replace(/\=(")[^"]*\1/g, this._spaceReplacer);
  
    var parts = attrStr.split(/\s+/);
    if (0 == parts.length) return null;
    for (var i=0; i<parts.length; i++) {
      if (-1 == parts[i].indexOf('=')) {
        retObj[parts[i]] = true;
      } else {
        var p = parts[i].split('=');
        p[1] = p[1].match(/^(['"]?)(.*)\1/)[2];
        retObj[p[0].toLowerCase()] = this._trim(this._unspaceReplacer(p[1]));
      }
    }
    return retObj;
  }
  
  //--------------------------------------------------------------------------------------------------------------
  //---  HELPERS
  //--------------------------------------------------------------------------------------------------------------
  /**
  * Trim a string 
  * @access private
  * @param  string input
  * @return string
  */
  this._trim = function(input) {
    var ret = input.replace(/^\s*/, '');
    return ret.replace(/\s*$/, '');
  }
  /**
  * Trim a string on the left side
  * @access private
  * @param  string input
  * @return string
  */
  this._trimL = function(input) {
    return input.replace(/^\s*/, '');
  }
  
  /**
  * Replace ALL \n \t with a single space
  */
  this._normalize = function(input) {
    return input.replace(/[\n\t]/g, ' ');
  }
  
  /**
  * Delete ALL white chars
  * @access private
  * @param  string input
  * @return string
  */
  this._strip = function(input) {
    return input.replace(/\s*/g, '');
  }
  
  /**
  *
  * @access private
  * @param  string input
  * @return string
  */
  this._entity  = function (input) {
    // NOTE: & first to replace !
    return input.replace(/&/g,'&amp;').replace(/'/g,'&#039;').replace(/"/g,'&quot;').replace(/</g,'&lt;').replace(/>/g,'&gt;'); 
  }
  
  /**
  * Used by this._extractAttributes
  * replace spaces by a 'unique'-random code and do the opposit in this._unspaceReplacer()
  * 
  * @access private
  * @param  string input
  * @return string
  */
  this._spaceReplacer = function(input) {
    return input.replace(/\t/g, 'xzAzx').replace(/\n/g, 'xzBzx').replace(/ /g, 'xzCzx');
  }
  /**
  * @access private
  * @param  string input
  * @return string
  */
  this._unspaceReplacer = function(input) {
    return input.replace(/xzAzx/g, "\t").replace(/xzBzx/g, "\n").replace(/xzCzx/g, ' ');
  }


  //--------------------------------------------------------------------------------------------------------------
  //---  DEBUG HELPERS
  //--------------------------------------------------------------------------------------------------------------
  /**
  * @access public
  * @return string (html)
  */
  this.toHtml = function() {
    return this._recursivViewStruct(this.root);
  }
  
  /**
  * @access private
  * @param  ? item
  * @return ?
  */
  this._recursivViewStruct = function(item) {
    var color = '';
    switch (item.type) {
      case 'element' : color = 'red'; break;
      case 'comment' : color = 'green'; break;
      case 'cdata'   : color = 'lime'; break;
      case 'chardata': color = 'mangenta'; break;
      default: color = 'blue';
    }
    var out = new Array();
    var i = 0;
    var ii = 0; 
    out[ii++] = '<fieldset style="border:solid thin '+ color +'; padding:5"><legend><b>' + item.type + ': ' + item.name +'</b></legend>'; 
    out[ii++] = 'Value: [' + item.value + "]<br \>\n";
    for (x in item.attributes) {
      out[ii++] = x + '=' + item.attributes[x] +"<br \>\n"
    }
    
    for (i=0; i<item.children.length; i++) {
      out[ii++] = this._recursivViewStruct(item.children[i]);
    }
    out[ii++] =  "</fieldset>\n";

    if (this._debugOn) {
      return this._debug.join('');
    }
    return out.join('');
  }
  
} // END CLASS


//**************************************************************************************************************
//***  HELPERS Objects
//**************************************************************************************************************
function _Bs_XmlParserElement() {
  //i think this.index exists too but is not documented here. --andrej
	this.type = "";
	this.name = "";
  this.value = "";
	this.attributes = new Object();
	this.children = new Array();
}

/*
* an internal fragment that is passed between functions.
*/
function _Bs_XmlParserStrFragment() {
  
  /*
  * the whole string we're working with, gets smaller and smaller.
  */
	this.str  = '';
  
  /*
  * vector of _Bs_XmlParserElement instances.
  */
	this.list = new Array();
  
  /*
  * the elements name, for example 'table', as far as i understand. 
  * i think "end" is a strange name for that. it must be named this way because 
  * it is used to find the 'end' tag, like </TABLE>.
  */
	this.end  = '';
}

