function initCalendar(lang, timeformatarg) {
    $('.calendar1').datepicker({
        dateFormat: 'dd-M-yy',
        locale: lang
        //dateonly: true
    });
}

function initTimePicker2(locale) {
    $('.timePicker1').datetimepicker({
        onSelect: function(dateText) {
        $(this).change(dateText);
        },
        dateFormat: '',
        timeFormat: 'HH:mm:ss',
        timeOnly: true,
        showButtonPanel: false
    });
}

function initTimePicker(locale) {
    $('.timePickerInput1').datetimepicker({
        dateFormat: '',
        timeFormat: 'HH:mm:ss',
        timeOnly: true,
        showButtonPanel: false
    });
}
function initTimePickerWithTime(locale,hours,mins,secs) {
    
    var curDateTime = new Date();
    curDateTime.setHours(hours);
    curDateTime.setMinutes(mins);
    curDateTime.setSeconds(secs);
    
    $('.timePickerInput1').datetimepicker({
        dateFormat: '',
        timeFormat: 'HH:mm:ss',
        timeOnly: true,
        showButtonPanel: false
    }).datetimepicker("setTime", curDateTime);
}

function onDelete1(creating, msg) {
    if (creating) {
        $(".table tr:eq(1)").remove();
        return true;
    }
    if (confirm(msg/*'#{bundle.ConfirmBeforeDelete}'*/))
        return true;

    return false;
};

function onDelete(usedBy, creating, msg1, msg2, msg3) {
    if (creating) {
        $(".table tr:eq(1)").remove();
        return true;
    }
    //if (confirm(msg/*'#{bundle.ConfirmBeforeDelete}'*/))
    //    return true;
    if (usedBy > 1) {
        if (confirm(msg1))
            return true;
    } else if (usedBy === 1) {
        if (confirm(msg2))
            return true;
    } else {
        if (confirm(msg3))
            return true;
    }

    return false;
}

function onChange(usedBy, msg, msg2) {
    if (usedBy > 0) {
        if (confirm(usedBy > 1 ? msg : msg2/*'#{bundle.ConfirmBeforeDelete}'*/))
            return true;
        else
            return false;
    } else
        return true;
}

function onDeleteAction(usedBy, creating, msg1, msg2, msg3) {
    if (creating)
        return true;
    else {
        if (usedBy > 1) {
            if (confirm(msg1/*'#{bundle.ConfirmBeforeDelete}'*/))
                return true;
        } else if (usedBy === 1) {
            if (confirm(msg2/*'#{bundle.ConfirmBeforeDelete}'*/))
                return true;
        } else {
            if (confirm(msg3/*'#{bundle.ConfirmBeforeDelete}'*/))
                return true;
        }
    }
    return false;
}

function handleCalendarAjax(data) {
    var status = data.status;

    switch (status) {
        case "begin":
            // This is invoked right before ajax request is sent.
            break;

        case "complete":
            // This is invoked right after ajax response is returned.
            break;

        case "success":
            // This is invoked right after successful processing of ajax response and update of HTML DOM.
            // someFunction();
            break;
    }

}



