$(function() {
  window.socketurl = 'ws://' + window.location.hostname + ':81/';
  window.stateurl = '//' + window.location.hostname + ':82/';
  window.username = 'user' + Math.floor((Math.random()*1000000)+1000);

  // Show random username
  $("#username").text(window.username);

  // State update socket
  window.socket = new WebSocket(socketurl);
  socket.onmessage = function(msg) {
    $("#messages").append("<p>" + msg.data + "</p>");
  };
  $("#socket").on("submit", function(e) {
    e.preventDefault();

    $.ajax({
      type: "POST",
      url: '',
      data: window.username + ': ' + $("#message").val(),
      contentType: "text/plain",
      success: function(){ $("#message").val(""); }
    });

  });

  // Get initial state
  $.getJSON(window.stateurl, function(data, textStatus, jqXHR){
    $.each(data.data, function() {
      $("#messages").append("<p>" + this + "</p>");
    });
  });

});
