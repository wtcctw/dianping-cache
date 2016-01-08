function loadmask(){
	//$("#BgDiv").css({
	//	display : "block",
	//	height : $(document).height()
	//});
	var yscroll = $(document).height();
	var screenx = $(window).width();
	var screeny = $(window).height();
	$(".DialogDiv").css("display", "block");
	var DialogDiv_width = $(".DialogDiv").width();
	var DialogDiv_height = $(".DialogDiv").height();
	$(".DialogDiv").css("left", (screenx / 2 - DialogDiv_width / 2) + "px");
	$(".DialogDiv").css("top", (screeny / 2) + "px");
}

function hidemask(){
	$("#BgDiv").css({
		display : "none",
	});
	$(".DialogDiv").css("display", "none");
}
