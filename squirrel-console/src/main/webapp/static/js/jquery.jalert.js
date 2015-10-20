/**
* jAlert v.1.0.0
* Copyright (c) 2008 Julian Castaneda
* http://www.smooka.com/blog/
* Requires: jQuery 1.2+
*/

(function($) {
    $.fn.jAlert = function (msg, type, uid, alert_box_width, yoffset)
    {
        var tmpobj = this;
		var overlay = 0;
				
		if (uid == undefined)
		{	//generate an unique ID
			var d = new Date();
			var uid = d.getMonth()+""+d.getDate()+""+d.getHours()+""+d.getMinutes()+""+d.getSeconds();
		}

		if ($('#jalert_box_cont_'+uid).css('display') == 'block')
		{
			return;
		}
		
        if (!type) {
            // set type to a default warning
            type = 'warning';
        }

        if (!alert_box_width) {
            //set default width of alert box
            alert_box_width = 350;
        }

        if (!yoffset) {
            //set default y offset of alert box
            yoffset = 0;
        }
		
		if (overlay==1) {
			$('<div id="jalert_overlay_'+uid+'"></div>').prependTo('body');
				var overlayWidth = $(window).width();
				var overlayHeight = $(document).height();
				var winHeight =  $(window).height();
				$("#jalert_overlay_"+uid).css({
											  top: 0, 
											  left: 0, 
											  width: overlayWidth, 
											  height: winHeight, 
											  position: "fixed",
											  display: "block",
											  background: "#000",
											  zIndex: "1000"
										  });
				$("#jalert_overlay_"+uid).css("opacity", 0.7);
		}
		

        //create a prepend the alert box to the container
        $('<div class="msg-box-cont msg-'+type+'" id="jalert_box_cont_'+uid+'"><table width="100%" border="0" cellpadding="0" cellspacing="0"><tr><td><div class="msg-text"><div class="msg-icon msg-icon-'+type+'"></div>'+msg+'</div></td><td width="21" valign="top"><div class="msg-btn close-'+type+'"></div></td></tr></table></div>').appendTo('body');

        $("#jalert_box_cont_"+uid).width(alert_box_width);

		alignCenter();
		
        //get the y (top) position of the container
		var top=this.y() + yoffset;

        $("#jalert_box_cont_"+uid).css("top",top+"px");
		
		$("#jalert_box_cont_"+uid).fadeIn(500);
		
 
		$(document).click(function() {
			$("#jalert_overlay_"+uid).fadeOut(100);
			$("#jalert_overlay_"+uid).remove();
		});
	
	
		$('.msg-btn').click(function() {
			if (overlay==1) {
				$("#jalert_overlay_"+uid).fadeOut(100);
				$("#jalert_overlay_"+uid).remove();
			}
			$("#jalert_box_cont_"+uid).fadeOut(100);
			$("#jalert_box_cont_"+uid).empty();
			$("#jalert_box_cont_"+uid).remove();
			$(window).unbind("resize");
		});
		
		
        //always center
        $(window).resize(function() {alignCenter();});
		
				
		function alignCenter() {
			var alert_box_width = $("#jalert_box_cont_"+uid).width();
            //get the width of the container
            var container_width = tmpobj.innerWidth();
            // get the x position of the container
            var container_left = tmpobj.x();
            //get the center position of the alert box within the container
            var actual_left = ((container_width-alert_box_width)/2)+container_left;
            //get the y (top) position of the container
            $("#jalert_box_cont_"+uid).css("left",actual_left+"px");
			
		}

    };

    //vertical positioning
    $.fn.y = function(n) {
        var result = null;
        this.each(function() {
            var o = this;
            if (n === undefined) {
                var y = 0;
                if (o.offsetParent) {
                    while (o.offsetParent) {
                        y += o.offsetTop;
                        o = o.offsetParent;
                    }
                }
                if (result === null) {
                    result = y;
                } else {
                    result = Math.min(result, y);
                }
            } else {
                o.style.top = n + 'px';
            }
        });
        return result;
    };
    
    //horizontal positioning
    $.fn.x = function(n) {
        var result = null;
        this.each(function() {
            var o = this;
            if (n === undefined) {
                var x = 0;
                if (o.offsetParent) {
                    while (o.offsetParent) {
                        x += o.offsetLeft;
                        o = o.offsetParent;
                    }
                }
                if (result === null) {
                    result = x;
                } else {
                    result = Math.min(result, x);
                }
            } else {
                o.style.left = n + 'px';
            }
        });
        return result;
    };
})(jQuery);