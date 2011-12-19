var year;
var month;
var day;
var weekday;
var hour;
var cal;
var date;
var timestamp;

function init() {
    scheduleChange();
}

function scheduleChange() {
    cal = java.util.Calendar.getInstance();

    weekday = cal.get(java.util.Calendar.DAY_OF_WEEK);
    hour = cal.get(java.util.Calendar.HOUR_OF_DAY);
    refreshDates(cal);

    if (weekday == 1 || weekday == 7) {
	    startEvent();
     }
    else {
	cal.set(java.util.Calendar.DATE, cal.get(java.util.Calendar.DATE) + (7 - weekday));
	refreshDates(cal);
	var date = year + "-" + month + "-" + day + " 00:00:00.0";
	var timeStamp = java.sql.Timestamp.valueOf(date).getTime();
	setupTask = em.scheduleAtTimestamp("startEvent", timeStamp);
    }
}

function finishEvent() {
    em.DoubleEXPRate(false);
    scheduleChange();
}

function startEvent() {
    refreshDates(cal);
    date = year + "-" + month + "-" + day + " 00:00:00.0";
    timeStamp = java.sql.Timestamp.valueOf(date).getTime();
    setupTask = em.scheduleAtTimestamp("finishEvent", timeStamp);
}

function changeRates() {
    em.DoubleEXPRate(true);
    em.broadcastServerMsg(5120014, "2x EXP rate event has started!", true);
}

function refreshDates(calendar) {
    year = calendar.get(java.util.Calendar.YEAR);
    month = calendar.get(java.util.Calendar.MONTH) + 1;
    if (Math.floor(month / 10) == 0) {
	month = "0" + month;
    }
    day = calendar.get(java.util.Calendar.DATE);
    if (Math.floor(day / 10) == 0) {
	day = "0" + day;
    }
}

function cancelSchedule() {
    setupTask.cancel(true);
}
