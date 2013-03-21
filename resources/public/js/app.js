$(function() {
  window.socketurl = window.location.href.replace("http://", "ws://");
                                         //.replace(':5000', ':5001');
  //console.log(window.socketurl);
  window.socket = new WebSocket(socketurl);

  socket.onmessage = function(msg) {
    $("#messages").append("<p>" + msg.data + "</p>");
  };

  window.username = 'user' + Math.floor((Math.random()*1000000)+1000);
  $("#username").text(window.username);

  $("#socket").on("submit", function(e) {
    e.preventDefault();
    //socket.send(window.username + ': ' + $("#message").val());
    $.post('', window.username + ': ' + $("#message").val(), function(){
      $("#message").val("");
    });
  });

});
