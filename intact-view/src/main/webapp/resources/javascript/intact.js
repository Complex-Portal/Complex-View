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

function ia_submitToReactome(selectedIds) {
    var ids = selectedIds.replace(/,/g, '\r');

    var reactomeForm = document.createElement('form');
    reactomeForm.method='post';
    reactomeForm.action='http://www.reactome.org/cgi-bin/skypainter2';
    reactomeForm.enctype='multipart/form-data';
    reactomeForm.name='skypainter';
    reactomeForm.target='_blank';

    var inputQuery = ia_createHiddenInput('QUERY', ids);
    var inputDb = ia_createHiddenInput('DB', 'gk_current');
    var inputSubmit = ia_createHiddenInput('SUBMIT', '1');
    reactomeForm.appendChild(inputQuery);
    reactomeForm.appendChild(inputDb);
    reactomeForm.appendChild(inputSubmit);

    document.getElementById('intactForm').parentNode.appendChild(reactomeForm);
    reactomeForm.submit();
}

function ia_createHiddenInput(name,value) {
    var input = document.createElement('input');
    input.type = 'hidden';
    input.name = name;
    input.value = value;
    return input;
}