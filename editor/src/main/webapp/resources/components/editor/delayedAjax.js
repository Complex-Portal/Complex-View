var timer;

function ia_delay(func, timeout) {

    clearTimeout(timer);

    timer = setTimeout(func, timeout);
}