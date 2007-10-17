function submitEnter(commandId, e)
{
    var keycode;
    if (window.event)
        keycode = window.event.keyCode;
    else if (e)
        keycode = e.which;
    else
        return true;

    if (keycode == 13) {
        document.getElementById(commandId).click();
        return false;
    } else
        return true;
}

function startsWith(string, prefix) {
    return (string.substr(0, prefix.length) == prefix);
}

function endsWith(string, suffix) {
    return (string.substr(string.length - suffix.length, string.length) == suffix);
}

/**
 * Changes the color for the tags in the document with have an id that starts with the idPrefix
 */
function hilightElements(idPrefix, idSuffix, tag, color)
{
    var objs = document.getElementsByTagName(tag);

    for (var i = 0; i < objs.length; i++) {

        if (startsWith(objs[i].id, idPrefix))
        {
            if (endsWith(objs[i].id, idSuffix)) {
                document.getElementById(objs[i].id).style.color = color;
            //} else {
            //    document.getElementById(objs[i].id).style.color = 'inherit';
            }
        }
    }
}

function stopHilighting(idPrefix, tag)
{
    var objs = document.getElementsByTagName(tag);

    for (var i = 0; i < objs.length; i++) {

        if (startsWith(objs[i].id, idPrefix))
        {
           document.getElementById(objs[i].id).style.color = 'inherit';
        }
    }
}