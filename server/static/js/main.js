(function() {
	$(document).ready(function() {
		$(".modalToggle").click(function() {
			$("#modalContainer").fadeToggle();
			event.preventDefault();
		});
	});
})()