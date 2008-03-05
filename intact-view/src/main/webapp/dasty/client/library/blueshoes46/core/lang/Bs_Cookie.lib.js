/********************************************************************************************
* BlueShoes Framework; This file is part of the php application framework.
* NOTE: This code is stripped (obfuscated). To get the clean documented code goto 
*       www.blueshoes.org and register for the free open source *DEVELOPER* version or 
*       buy the commercial version.
*       
*       In case you've already got the developer version, then this is one of the few 
*       packages/classes that is only available to *PAYING* customers.
*       To get it go to www.blueshoes.org and buy a commercial version.
* 
* @copyright www.blueshoes.org
* @author    Samuel Blume <sam at blueshoes dot org>
* @author    Andrej Arn <andrej at blueshoes dot org>
*/
function setCookie(name, value, expires, path, domain, secure) {
if (typeof(value) == 'object') {
if (typeof(Bs_Wddx) == 'undefined') {
alert('Webmaster: To set an object as cookie the wddx class is required: core/util/Bs_Wddx.class.js');return false;}
var wddx = new Bs_Wddx();var value = wddx.serialize(value);}
var curCookie = name + "=" + escape(value) +
((expires) ? "; expires=" + expires.toGMTString() : "") +
((path) ? "; path=" + path : "") +
((domain) ? "; domain=" + domain : "") +
((secure) ? "; secure" : "");document.cookie = curCookie;}
function getCookie(name) {
var dc = document.cookie;var prefix = name + "=";var begin = dc.indexOf("; " + prefix);if (begin == -1) {
begin = dc.indexOf(prefix);if (begin != 0) return null;} else
begin += 2;var end = document.cookie.indexOf(";", begin);if (end == -1)
end = dc.length;var value = dc.substring(begin + prefix.length, end);value = unescape(value);if (value.substr(0, 11) == '<wddxPacket') {
if ((typeof(Bs_Wddx) == 'undefined') || (typeof(Bs_XmlParser) == 'undefined')) {
alert('Webmaster: To read an object from cookie the wddx and xmlparser classes are required: core/util/Bs_Wddx.class.js and Bs_XmlParser.class.js');return false;}
var wddx = new Bs_Wddx();value = wddx.deserialize(value);}
return value;}
function deleteCookie(name, path, domain) {
if (getCookie(name)) {
document.cookie = name + "=" +
((path) ? "; path=" + path : "") +
((domain) ? "; domain=" + domain : "") +
"; expires=Thu, 01-Jan-70 00:00:01 GMT";}
}
function fixDate(date) {
var base = new Date(0);var skew = base.getTime();if (skew > 0)
date.setTime(date.getTime() - skew);}
