$(function() {
  window.socketurl = 'ws://' + window.location.hostname + ':81/';
  window.stateurl = '//' + window.location.hostname + ':82/';
  window.username = 'user' + Math.floor((Math.random()*1000000)+1000);
  window.socket = null;
  window.messageCount = 0;

  function intializeSocket($, socketurl){
    var s = new WebSocket(socketurl);
    s.onmessage = function(msg) {
      window.messageCount++;
      if(window.messageCount > 50){
        $('#messages:last-child').remove();
      }
      $("#messages").prepend("<p>" + msg.data + "</p>");
    };
    s.onopen = function(){
      // Setup complete, so re-enable input
      $("form button[type=submit], form input[type=submit]").prop("disabled", false);
    };
    return s;
  }

  // Disable input until setup is complete
  $("form button[type=submit], form input[type=submit]").prop("disabled", true);

  // Show username
  $("#username").text(window.username);

  // Handle message submission
  $("form").on("submit", function(e) {
    e.preventDefault();
    $.ajax({
      type: "POST",
      url: '',
      data: window.username + ': ' + $("#message").val(),
      contentType: "text/plain",
      success: function(){
        $("#message").val("");
      }
    });
  });

  // Get initial state
  $.getJSON(window.stateurl, function(data, textStatus, jqXHR){
    window.messageCount = data.data.length;
    $.each(data.data, function() {
      $("#messages").append("<p>" + this + "</p>");
    });

    // Initialize the update websocket
    window.socket = intializeSocket($, window.socketurl);
  });
});
