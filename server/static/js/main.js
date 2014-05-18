(function() {
	$(document).ready(function() {
		$(".modalToggle").click(function() {
			event.preventDefault();
			$("#modalContainer").fadeToggle();
		});
		
		$("#addISBN").click(function() {
			event.preventDefault();
			var isbn = $("#isbn").val();
			$.ajax({
				url: "/volumes/" + isbn,
				type: "GET",
				dataType: "json"
			}).done(function(data, status) {
				if (status == "success") {
					window.location.href = "/book/" + isbn + ".html";
				} 
				$("#modalContainer").fadeOut();
			});
		});
	});
})()