const express = require("express");
const app = express();
const WebSocket = require("ws");
const server = require("http").createServer(app);
const msgRouter = require("./src/router/messageRouter");
const followRouter = require("./src/router/followRouter");
const friendRouter = require("./src/router/friendRouter");
const groupRouter = require("./src/router/groupRouter");
const userRouter = require("./src/router/userRouter");
const contactRouter = require("./src/router/contactsRouter");
const locationRouter = require("./src/router/locationRouter");
const notificationRouter = require("./src/router/notificationRouter");
const initSocket = require("./src/websocket/socketInit")
const url = require('url');
const ip = require("ip");
// app.use(express.urlencoded({extended: true}));
// app.use(express.json());
app.use(express.json({limit: '50mb'}));
app.use(express.urlencoded({limit: '50mb'}));
const webSocketServerLogin = new WebSocket.Server({noServer: true});
const webSocketServerChat = new WebSocket.Server({noServer: true});
const webSocketServerCall = new WebSocket.Server({noServer: true});
console.log(ip.address())

initSocket.initSocketLogin(webSocketServerLogin)
initSocket.initSocketChat(webSocketServerChat)
initSocket.initSocketCall(webSocketServerCall)
server.on('upgrade', function upgrade(request, socket, head) {
    const pathname = url.parse(request.url).pathname;
    if (pathname === '/login') {
        webSocketServerLogin.handleUpgrade(request, socket, head, function done(ws) {
            webSocketServerLogin.emit('connection', ws, request);
        });
    } else if (pathname === '/call') {
        webSocketServerCall.handleUpgrade(request, socket, head, function done(ws) {
            webSocketServerCall.emit('connection', ws, request);
        });
    }else if (pathname === '/chat') {
        webSocketServerChat.handleUpgrade(request, socket, head, function done(ws) {
            webSocketServerChat.emit('connection', ws, request);
        });
    } else if (pathname === '/call') {
        webSocketServerCall.handleUpgrade(request, socket, head, function done(ws) {
            webSocketServerCall.emit('connection', ws, request);
        });
    } else {
        socket.destroy();
    }
});
server.listen(3000);
app.use(express.static('public'));
app.use('/image', express.static('image'));
app.use('/voice', express.static('voice'));
app.use("/user", msgRouter, followRouter, friendRouter,groupRouter,notificationRouter,contactRouter,locationRouter);
app.use("/account", userRouter);


