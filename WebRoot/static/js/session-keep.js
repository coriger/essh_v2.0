window.setInterval(sessionInfo, 15 * 60 * 1000);
function sessionInfo() {
    $.ajax({
        type: "GET",
        url: "${ctx}/login/sessionInfo",
        cache: false,
        dataType: "json",
        success: function (data) {

        }});
}