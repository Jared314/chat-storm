$(function() {
  window.socket = new WebSocket(window.location.href.replace("http://", "ws://"));

  socket.onmessage = function(msg) {
    $("#messages").append("<p>" + msg.data + "</p>");
  };

  window.username = 'user' + Math.floor((Math.random()*1000000)+1000);
  $("#username").text(window.username);

  $("#socket").on("submit", function(e) {
    e.preventDefault();
    socket.send(window.username + ': ' + $("#message").val());
    $("#message").val("");
  });

});
